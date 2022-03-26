package io.clarktsiory.trading

import scala.util.{Failure, Success, Try}

import cats.arrow.Arrow
import cats.conversions.all.*
import cats.data.Kleisli
import cats.effect.kernel.Ref
import cats.implicits.*
import cats.{Applicative, Monad, MonadError}
import fs2.Stream
import io.circe
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.{DecodingFailure, Json}
import skunk.*
import skunk.codec.all.*
import skunk.data.*
import skunk.implicits.*

import io.clarktsiory.signals.*
import io.clarktsiory.ta.*

class SignalSkunkStreamingService[F[_]: [F[_]] =>> MonadError[F, Throwable]](
  session: Session[F],
  symbolsRef: Ref[F, List[Symbol]],
) extends SignalStreamingService[[X] =>> Stream[F, X]]:
  import SignalSkunkStreamingService.*

  val channel: Channel[F, String, String] = session.channel(id"signals")

  def getSymbols: Stream[F, Symbol] =
    Stream.eval(symbolsRef.get).flatMap(Stream.emits(_))
  end getSymbols

  def getOHLCBySymbol(symbol: Symbol): Stream[F, OHLCSignal] =
    channel
      .listen(1024)
      .flatMap(n =>
        Stream.fromEither[F](
          (
            (notificationToSymbol &&& notificationToOHLC) >>>
              Arrow[[A, B] =>> Kleisli[[T] =>> Either[Throwable, T], A, B]].lift(addSymbol(_).as(_))
          ).run(n)
        )
      )
      .flatMap(Stream.eval(_))
  end getOHLCBySymbol

  def saveSignal(symbol: Symbol, stream: Stream[F, Signal]): Stream[F, Unit] =
    stream.map(_.asJson.deepMerge(Json.obj("symbol" -> symbol.asJson)).noSpaces).through(channel)
  end saveSignal

  private def addSymbol(symbol: Symbol): F[Unit] =
    symbolsRef.get.flatMap(symbols =>
      Applicative[F].unlessA(symbols.contains(symbol))(
        symbolsRef.update(_ :+ symbol)
      )
    )
  end addSymbol

end SignalSkunkStreamingService

object SignalSkunkStreamingService:
  given circe.Decoder[Symbol] = circe.Decoder[String].map(Symbol.fromString)

  def notificationToSymbol(using
    circe.Decoder[Symbol]
  ): Kleisli[[A] =>> Either[Throwable, A], Notification[String], Symbol] =
    Kleisli(n => parse(n.value).flatMap(_.hcursor.get[Symbol]("symbol")))
  end notificationToSymbol

  def notificationToOHLC(using
    D: circe.Decoder[TradingSignal]
  ): Kleisli[[A] =>> Either[Throwable, A], Notification[String], OHLCSignal] =
    Kleisli(n =>
      parse(n.value).flatMap(_.hcursor.as[OHLCSignal](D.emapTry {
        case o: OHLCSignal => Success(o)
        case _ => Failure(DecodingFailure("Not an OHLCSignal", Nil))
      }))
    )
  end notificationToOHLC
end SignalSkunkStreamingService
