package io.clarktsiory.trading

import cats.effect.Async
import cats.syntax.eq.*
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}

import io.clarktsiory.signals.*
import io.clarktsiory.ta.*
import io.clarktsiory.ta.streams.fs2.*

class IndicatorInMemStreamingService[F[_]: Async](
  topic: Topic[F, (Symbol, Signal)],
  signalService: SignalStreamingService[[X] =>> Stream[F, X]],
) extends IndicatorStreamingService[[X] =>> Stream[F, X]]:

  override def getIndicatorStream(
    symbol: Symbol,
    indicator: Indicator,
  ): Stream[F, IndicatorSignal] =
    signalService
      .getOHLCBySymbol(symbol)
      .close
      .through(indicator match
        case rsi: Indicator.RSI => rsi.asPipe
        case macd: Indicator.MACD => macd.asPipe
        case _ => Function.const(Stream.empty)
      )
      .evalTap(topic.publish1(symbol, _))
  end getIndicatorStream

end IndicatorInMemStreamingService
