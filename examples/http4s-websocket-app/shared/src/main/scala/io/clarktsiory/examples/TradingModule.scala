package io.clarktsiory.examples

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.syntax.resource.*
import fs2.Stream
import fs2.concurrent.Topic
import org.http4s.Uri
import org.http4s.client.websocket.{WSClient, WSRequest}
import org.http4s.syntax.literals.*

import io.clarktsiory.signals.Signal
import io.clarktsiory.trading.*

final case class TradingModule private (
  indicatorService: IndicatorStreamingService[[X] =>> Stream[IO, X]],
  signalService: SignalStreamingService[[X] =>> Stream[IO, X]],
  tickerIndicatorService: TickerIndicatorService[IO],
)

object TradingModule:
  final private val largeData: Uri = uri"wss://stream.binance.com/ws/!ticker@arr"

  // Provide in-memory implementation
  def make(wsClient: WSClient[IO]): Resource[IO, TradingModule] =
    for
      topic <- Topic[IO, (Symbol, Signal)].toResource
      signalService = SignalInMemStreamingService(topic)
      indicatorService = IndicatorInMemStreamingService(topic, signalService)
      tickerIndicatorService = TickerIndicatorService(
        wsClient,
        WSRequest(largeData),
        indicatorService,
        signalService,
      )
      _ <- Resource.onFinalize(topic.close.void)
    yield TradingModule(indicatorService, signalService, tickerIndicatorService)
  end make

end TradingModule
