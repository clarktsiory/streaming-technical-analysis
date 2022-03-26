package io.clarktsiory.examples

import scala.concurrent.duration.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import cats.effect.*
import cats.effect.std.{Env, Random}
import cats.effect.syntax.resource.*
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import natchez.Trace.Implicits.noop
import fs2.Stream
import skunk.Session

import io.clarktsiory.signals.*
import io.clarktsiory.ta.*
import io.clarktsiory.trading.*

object App extends IOApp.Simple:

  def session(env: Env[IO]): Resource[IO, Session[IO]] =
    (
      env.get("POSTGRES_HOST").map(_.getOrElse("localhost")).toResource,
      env.get("POSTGRES_PORT").map(_.map(_.toInt).getOrElse(5432)).toResource,
      env.get("POSTGRES_USER").map(_.getOrElse("docker")).toResource,
      env.get("POSTGRES_DB").map(_.getOrElse("postgres")).toResource,
      env.get("POSTGRES_PASSWORD").map(_.orElse(Some("docker"))).toResource,
    ).mapN((host, port, user, db, password) =>
      Session.single[IO](
        host = host,
        port = port,
        user = user,
        database = db,
        password = password,
      )
    ).flatten

  val stubSignal =
    for
      r <- Stream.eval(Random.scalaUtilRandomSeedLong[IO](42))
      ref <- Stream.eval(Ref.of[IO, Int](0))
      time <- Stream.eval(Ref.of[IO, Long](0))
      _ <- Stream.bracket(
        IO.delay(println("Starting stream")) >> time.set(System.currentTimeMillis())
      )(_ =>
        time.get.flatMap(t =>
          IO.delay {
            val s = System.currentTimeMillis() - t
            println(s"Time taken: $s")
          }
        )
      )
      s <- Stream
        .repeatEval(
          (
            r.nextLongBounded(100),
            r.nextLongBounded(100),
            r.nextLongBounded(100),
            r.nextLongBounded(100),
          ).tupled <* ref.update(_ + 1)
        )
        .evalMap(value =>
          ref.get.map(d =>
            OHLCSignal(
              LocalDateTime.of(1970, 1, 1, 8, 0, 0).plus(d, ChronoUnit.HOURS),
              value._1,
              value._2,
              value._3,
              value._4,
            )
          )
        )
    yield s
  end stubSignal

  override val run: IO[Unit] = session(Env.make[IO]).use { s =>
    val symbol = Symbol.fromString("BTCUSD")
    for
      ref <- Ref.of[IO, List[Symbol]](List(symbol))
      signalService = new SignalSkunkStreamingService(s, ref)
      indicatorService = new IndicatorSkunkStreamingService(signalService, s)
      symbols <- signalService.getSymbols.compile.toList

      indicator = indicatorService
        .getIndicatorStream(symbol, Indicator.RSI(4))
        .evalMap(s => IO.delay(println(s"[${Thread.currentThread().getName}]($s))")))
      ohlc = signalService
        .getOHLCBySymbol(symbol)
        .evalMap(s => IO.delay(println(s"[${Thread.currentThread().getName}]($s))")))
      producer = signalService.saveSignal(symbol, stubSignal)

      _ <- Stream(indicator, ohlc, producer).parJoin(3).compile.drain.timeoutAndForget(2.seconds)
    yield ()
    end for
  }
  end run
end App
