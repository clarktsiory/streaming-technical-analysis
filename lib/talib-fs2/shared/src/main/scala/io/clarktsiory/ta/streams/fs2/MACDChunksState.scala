package io.clarktsiory.ta.streams.fs2

import cats.data.State
import cats.syntax.traverse.*
import fs2.{Chunk, Scan}

import io.clarktsiory.signals.{MACDSignal, ScalarSignal}
import io.clarktsiory.ta.streams.BufferedIndicator
import io.clarktsiory.ta.streams.fs2.MACDChunksState.copyBuffer
import io.clarktsiory.ta.{ComputedIndicator, Indicator}

/** Stateful computation of MACD using a buffer that keeps previous signal values.
  * The more signal data points there is, the more precision in latest MACD values.
  * @param macd MACD indicator
  * @param b BufferedIndicator implementation that defines the buffer size and min number of periods that yield a
  * computation result.
  * @param buffer buffer of signal values mostly used for right-append, left-drop and array conversion
  * @param bufferTimeIdx index of the first element that contains the upcoming signal date
  */
final private[fs2] case class MACDChunksState(
  macd: Indicator.MACD,
  b: BufferedIndicator[Indicator.MACD],
)(buffer: b.type#SignalBuffer, bufferTimeIdx: Int):

  lazy val buffSize = b.bufferSize(macd)

  def next(inputValue: ScalarSignal)(using
    ComputedIndicator[Indicator.MACD, Array[
      Double
    ], (Array[Double], Array[Double], Array[Double])]
  ): (MACDChunksState, Chunk[MACDSignal]) =
    val joinedSignals = buffer.added(inputValue)
    val dropPeriods = b.minComputationSize(macd) - 1

    if joinedSignals.size < b.minComputationSize(macd) then
      (this.copyBuffer(joinedSignals, bufferTimeIdx), Chunk.empty)
    else
      val joinedSignalsArray = joinedSignals.toArray
      val (value, signal, histogram) =
        ComputedIndicator[Indicator.MACD, Array[
          Double
        ], (Array[Double], Array[Double], Array[Double])]
          .compute(macd, joinedSignalsArray.map(_.value.toDouble))
      val transformed =
        Chunk.iterable[(Double, Double, Double)](
          value.iterator
            .to(scala.collection.immutable.Iterable)
            .lazyZip(signal.iterator.to(scala.collection.immutable.Iterable))
            .lazyZip(histogram.iterator.to(scala.collection.immutable.Iterable))
        )
      val macdChunk =
        transformed
          .drop(bufferTimeIdx)
          .zipWith(Chunk.array(joinedSignalsArray.drop(bufferTimeIdx + dropPeriods))) {
            case ((v, s, h), sig) => MACDSignal(sig.time, signal = s, value = v, histogram = h)
          }

      val nextBuffer = joinedSignals.takeRight(buffSize)
      (this.copyBuffer(nextBuffer, nextBuffer.size - dropPeriods), macdChunk)
    end if

  end next

end MACDChunksState

object MACDChunksState:

  extension (state: MACDChunksState)
    private def copyBuffer(buffer: state.b.SignalBuffer, bufferTimeIdx: Int): MACDChunksState =
      MACDChunksState(state.macd, state.b)(buffer, bufferTimeIdx)
  end extension

  def empty(macd: Indicator.MACD)(using b: BufferedIndicator[Indicator.MACD]): MACDChunksState =
    MACDChunksState(macd, b)(b.emptyBuffer(), 0)

  private[fs2] def scan(macd: Indicator.MACD)(using
    BufferedIndicator[Indicator.MACD],
    ComputedIndicator[Indicator.MACD, Array[
      Double
    ], (Array[Double], Array[Double], Array[Double])],
  ): Scan[MACDChunksState, ScalarSignal, MACDSignal] =
    Scan.stateful(empty(macd))(_.next(_))

end MACDChunksState
