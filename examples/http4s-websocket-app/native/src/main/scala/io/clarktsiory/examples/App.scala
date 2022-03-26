package io.clarktsiory.examples

import java.time.LocalDateTime

import cats.effect.syntax.resource.*
import cats.effect.{ExitCode, IO, Resource}
import cats.syntax.applicative.*
import org.http4s.curl.CurlApp

import io.clarktsiory.ta.*
import io.clarktsiory.ta.fs2.*
import io.clarktsiory.trading.Symbol

object App extends CurlApp.Simple:

  def run: IO[Unit] =
    (for
      wsClient <- websocketOrError().pure[[X] =>> Resource[IO, X]]
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

end App
