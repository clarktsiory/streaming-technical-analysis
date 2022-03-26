package io.clarktsiory.ta.fs2

import scala.collection.immutable.Queue
import scala.collection.mutable.ListBuffer
import scala.scalanative.unsafe.*
import java.time.LocalDateTime

import cats.effect.Sync
import cats.syntax.functor.*
import cats.syntax.monadError.*
import cats.syntax.option.*
import fs2.{Chunk, Pipe, Scan, Stream}

import io.clarktsiory.signals.*
import io.clarktsiory.ta.fs2.given
import io.clarktsiory.ta.given
import io.clarktsiory.ta.{ComputedIndicator, Indicator, IndicatorSig}
import impl.*

given BufferedIndicator[Indicator.RSI] = new RSIBufferedIndicator with MutableSignalBuffer
given BufferedIndicator[Indicator.MACD] = new MACDBufferedIndicator with MutableSignalBuffer

extension (rsi: Indicator.RSI)
  def asPipe[F[_]]: Pipe[F, ScalarSignal, RSISignal] = _.through(rsiPipe(rsi))
  def asScan: Scan[RSIChunksState, ScalarSignal, RSISignal] = rsiScan(rsi)
end extension

extension (macd: Indicator.MACD)
  def asPipe[F[_]]: Pipe[F, ScalarSignal, MACDSignal] = _.through(macdPipe(macd))
  def asScan: Scan[MACDChunksState, ScalarSignal, MACDSignal] = macdScan(macd)
end extension

private object impl:
  import io.clarktsiory.ta.given

  def macdScan(macd: Indicator.MACD)(using
    buf: BufferedIndicator[Indicator.MACD],
    s: IndicatorSig[Indicator.MACD, Array[Double], (Array[Double], Array[Double], Array[Double])],
  ): Scan[MACDChunksState, ScalarSignal, MACDSignal] =
    zonedScan(MACDChunksState.scan(macd))

  def macdPipe[F[_]](macd: Indicator.MACD)(using
    buf: BufferedIndicator[Indicator.MACD],
    s: IndicatorSig[Indicator.MACD, Array[Double], (Array[Double], Array[Double], Array[Double])],
  ): Pipe[F, ScalarSignal, MACDSignal] =
    macdScan(macd).toPipe[F]

  def rsiScan(rsi: Indicator.RSI)(using
    buf: BufferedIndicator[Indicator.RSI],
    s: IndicatorSig[Indicator.RSI, Array[Double], Array[Double]],
  ): Scan[RSIChunksState, ScalarSignal, RSISignal] =
    zonedScan(RSIChunksState.scan(rsi))

  def rsiPipe[F[_]](
    rsi: Indicator.RSI
  )(using
    buf: BufferedIndicator[Indicator.RSI],
    s: IndicatorSig[Indicator.RSI, Array[Double], Array[Double]],
  ): Pipe[F, ScalarSignal, RSISignal] =
    rsiScan(rsi).toPipe[F]

  // eagerly create the zone, and close it using the onComplete hook of the Scan
  private[this] def zonedScan[S, I, O](f: Zone ?=> Scan[S, I, O]): Scan[S, I, O] =
    val zoneScan =
      Scan.apply[Zone, I, I](Zone.open())(
        (state, input) => (state, Chunk.singleton(input)),
        zone =>
          zone.close()
          Chunk.empty,
      )
    zoneScan.andThen(f(using zoneScan.initial)).imapState(_._2)((zoneScan.initial, _))
  end zonedScan

end impl
