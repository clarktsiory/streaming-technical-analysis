package io.clarktsiory.trading

import scala.util.{Failure, Success, Try}

import cats.arrow.Arrow
import cats.conversions.all.*
import cats.data.Kleisli
import cats.effect.kernel.Ref
import cats.implicits.*
import cats.{Applicative, Monad, MonadError}
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.{DecodingFailure, Json}

import io.clarktsiory.signals.*
import io.clarktsiory.ta.*

class SignalInMemStreamingService[F[_]: [F[_]] =>> MonadError[F, Throwable]](
  topic: Topic[F, (Symbol, Signal)]
) extends SignalStreamingService[[X] =>> Stream[F, X]]:

  def getSymbols: Stream[F, Symbol] =
    topic
      .subscribe(1024)
      .collect { case (symbol, _) =>
        symbol
      }
  end getSymbols

  def getOHLCBySymbol(symbol: Symbol): Stream[F, OHLCSignal] =
    topic
      .subscribe(1024)
      .collect { case (`symbol`, ohlc: OHLCSignal) =>
        ohlc
      }
  end getOHLCBySymbol

  def saveSignal(symbol: Symbol, stream: Stream[F, Signal]): Stream[F, Unit] =
    stream.map(s => (symbol, s)).evalMap(topic.publish1).drain
  end saveSignal

end SignalInMemStreamingService
