package io.clarktsiory.trading

import cats.effect.Async
import fs2.{Pipe, Stream}
import io.circe.Json
import io.circe.parser.*
import io.circe.syntax.*
import skunk.*
import skunk.codec.all.*
import skunk.data.*
import skunk.implicits.*

import io.clarktsiory.signals.*
import io.clarktsiory.ta.*
import io.clarktsiory.ta.streams.fs2.*

class IndicatorSkunkStreamingService[F[_]: Async](
  signalService: SignalStreamingService[[X] =>> Stream[F, X]],
  session: Session[F],
) extends IndicatorStreamingService[[X] =>> Stream[F, X]]:

  val channel: Channel[F, String, String] = session.channel(id"indicators")

  override def getIndicatorStream(
    symbol: Symbol,
    indicator: Indicator,
  ): Stream[F, IndicatorSignal] =
    channel
      .listen(1024)
      .map(n => decode[Json](n.value))
      .flatMap(Stream.fromEither[F](_))
      .map(_.as[IndicatorSignal])
      .flatMap(Stream.fromEither[F](_))
      .concurrently(saveTransformationToChannel(symbol, indicator))
  end getIndicatorStream

  def saveTransformationToChannel(symbol: Symbol, indicator: Indicator): Stream[F, Unit] =
    signalService
      .getOHLCBySymbol(symbol)
      .close
      .through(indicator match
        case rsi: Indicator.RSI => rsi.asPipe
        case macd: Indicator.MACD => macd.asPipe
        case _ => Function.const(Stream.empty)
      )
      .map(_.asJson.noSpaces)
      .through(channel)
  end saveTransformationToChannel
end IndicatorSkunkStreamingService
