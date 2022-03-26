package io.clarktsiory.examples

import java.time.LocalDateTime

import cats.effect.syntax.resource.*
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.applicative.*
import org.http4s.client.websocket.WSClient
import org.http4s.netty.client.NettyWSClientBuilder

import io.clarktsiory.ta.*
import io.clarktsiory.ta.fs2.*
import io.clarktsiory.trading.Symbol

object App extends IOApp.Simple:

  def run: IO[Unit] =
    (for
      wsClient <- websocketClient()
      module <- TradingModule.make(wsClient)
      _ <- IO.println("Starting WS app").toResource
      symbol = Symbol.fromString("BTCUSDT")
      _ <- wsApp(module)(symbol, Indicator.RSI(8)).toResource
    yield ()).useForever.as(ExitCode.Success)
  end run

  def wsApp(
    tradingModule: TradingModule
  )(
    symbol: Symbol,
    indicator: Indicator,
  ): IO[Unit] =
    tradingModule.tickerIndicatorService
      .computeTickerIndicator(symbol, indicator)
      .evalTap(signal => IO.delay(println(s"computation : $signal")))
      .concurrently(
        tradingModule.signalService
          .getOHLCBySymbol(symbol)
          .through(_.evalTap(signal =>
            val delayMillis = (LocalDateTime.now().getNano() - signal.time.getNano()) / 1000000
            IO.delay(println(s"[delay $delayMillis] signal : $signal"))
          ))
      )
      .compile
      .drain
  end wsApp

  private def websocketClient(): Resource[IO, WSClient[IO]] =
    NettyWSClientBuilder[IO].withWebSocketCompression
      .withFrameMaxPayloadLength(2621440)
      .resource
  end websocketClient

end App
