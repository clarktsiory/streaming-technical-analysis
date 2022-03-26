package io.clarktsiory.ta.fs2

import fs2.{Chunk, Scan}

import io.clarktsiory.signals.{RSISignal, ScalarSignal}
import io.clarktsiory.ta.fs2.RSIChunksState.copyBuffer
import io.clarktsiory.ta.{ComputedIndicator, Indicator}

/** Stateful computation of RSI using a buffer that keeps previous signal values.
  * The more signal data points there is, the more precision in latest RSI values.
  * @param rsi RSI indicator
  * @param b BufferedIndicator implementation that defines the buffer size and min number of periods that yield a
  * computation result.
  * @param buffer buffer of signal values mostly used for right-append, left-drop and array conversion
  * @param bufferTimeIdx index of the first element that contains the upcoming signal date
  */
final private[fs2] case class RSIChunksState private (
  rsi: Indicator.RSI,
  b: BufferedIndicator[Indicator.RSI],
)(buffer: b.type#SignalBuffer, bufferTimeIdx: Int):

  lazy val buffSize = b.bufferSize(rsi)

  def next(inputValue: ScalarSignal)(using
    ComputedIndicator[Indicator.RSI, Array[Double], Array[Double]]
  ): (RSIChunksState, Chunk[RSISignal]) =
    val joinedSignals = buffer.added(inputValue)
    val dropPeriods = b.minComputationSize(rsi) - 1

    if joinedSignals.size < b.minComputationSize(rsi) then
      (this.copyBuffer(joinedSignals, bufferTimeIdx), Chunk.empty)
    else
      val joinedSignalsArray = joinedSignals.toArray
      val transformed = Chunk.array(
        ComputedIndicator[Indicator.RSI, Array[Double], Array[Double]]
          .compute(rsi, joinedSignalsArray.map(_.value.toDouble))
      )

      val rsiChunk =
        transformed
          .drop(bufferTimeIdx)
          .zipWith(Chunk.array(joinedSignalsArray.drop(bufferTimeIdx + dropPeriods))) {
            case (s, sig) => RSISignal(sig.time, s)
          }

      val nextBuffer =
        if joinedSignals.size > buffSize
        then joinedSignals.dropped(joinedSignals.size - buffSize)
        else joinedSignals

      (this.copyBuffer(nextBuffer, nextBuffer.size - dropPeriods), rsiChunk)
    end if

  end next
end RSIChunksState

object RSIChunksState:

  extension (state: RSIChunksState)
    def copyBuffer(buffer: state.b.SignalBuffer, bufferTimeIdx: Int): RSIChunksState =
      RSIChunksState(state.rsi, state.b)(buffer, bufferTimeIdx)

  def empty(rsi: Indicator.RSI)(using b: BufferedIndicator[Indicator.RSI]): RSIChunksState =
    RSIChunksState(rsi, b)(b.emptyBuffer(), 0)

  def scan(rsi: Indicator.RSI)(using
    BufferedIndicator[Indicator.RSI],
    ComputedIndicator[Indicator.RSI, Array[Double], Array[Double]],
  ): Scan[RSIChunksState, ScalarSignal, RSISignal] =
    Scan.stateful(empty(rsi))(_.next(_))

end RSIChunksState
