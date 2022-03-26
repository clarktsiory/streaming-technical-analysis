package io.clarktsiory.examples

import cats.effect.kernel.Async
import cats.syntax.applicativeError.*
import cats.syntax.eq.*
import fs2.{Pipe, Stream}
import io.circe.Json
import io.circe.parser.decode
import org.http4s.client.websocket.*

import io.clarktsiory.signals.{IndicatorSignal, OHLCSignal}
import io.clarktsiory.ta.Indicator
import io.clarktsiory.trading.*

class TickerIndicatorService[F[_]: Async](
  wsClient: WSClient[F],
  tickerWSRequest: WSRequest,
  indicatorService: IndicatorStreamingService[[X] =>> Stream[F, X]],
  signalService: SignalStreamingService[[X] =>> Stream[F, X]],
):

  private def getTickers(symbol: Symbol): Stream[F, TickerData] =
    val save =
      for
        wsConnection <- Stream.resource(wsClient.connectHighLevel(tickerWSRequest))
        str <- wsConnection.receiveStream.collect { case WSFrame.Text(msg, _) => msg }
        ej <- Stream.fromOption[F](decode[Json](str).toOption.flatMap(_.hcursor.values))
        j <- Stream.fromIterator[F](ej.iterator, chunkSize = 32)
        _ <- Stream
          .fromEither[F](j.as[TickerData])
          .onError(err => Stream.eval(Async[F].delay(println(s"$err\n$str"))))
          .filter(_.getSymbol === symbol)
          .through(filterDistinctTickersOHLC)
          .through(signalService.saveSignal(symbol, _))
        _ = println(j)
      yield ()

    signalService
      .getOHLCBySymbol(symbol)
      .map(_.toTickerData(symbol))
      .concurrently(save)
  end getTickers

  private val filterDistinctTickersOHLC: Pipe[F, TickerData, OHLCSignal] =
    _.map(_.toOHLCSignal)
      .changesBy(_.close)
  end filterDistinctTickersOHLC

  def computeTickerIndicator(symbol: Symbol, indicator: Indicator): Stream[F, IndicatorSignal] =
    indicatorService
      .getIndicatorStream(symbol, indicator)
      .concurrently(getTickers(symbol))

  // other e.g. resampling to 1 minute : groupAdjacentBy(_.time.getMinute), etc.

end TickerIndicatorService
