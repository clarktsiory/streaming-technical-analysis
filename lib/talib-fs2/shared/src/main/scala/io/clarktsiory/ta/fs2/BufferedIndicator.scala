package io.clarktsiory.ta.fs2
import scala.collection.mutable.ListBuffer

import io.clarktsiory.signals.ScalarSignal
import io.clarktsiory.ta.Indicator
import io.clarktsiory.ta.Indicator.*

private[fs2] trait BufferedIndicator[A <: Indicator]:
  def bufferSize(indicator: A): Int

  /** Defines the min number of periods that yield a computation result.
    */
  def minComputationSize(indicator: A): Int

  type SignalBuffer
  def emptyBuffer(): SignalBuffer

  extension (buffer: SignalBuffer)
    def added(value: ScalarSignal): SignalBuffer
    def dropped(n: Int): SignalBuffer
    def takeRight(n: Int): SignalBuffer
    def size: Int
    def toArray: Array[ScalarSignal]
  end extension
end BufferedIndicator

abstract private[fs2] class MACDBufferedIndicator extends BufferedIndicator[MACD]:
  override def bufferSize(macd: MACD): Int = 1_000
  override def minComputationSize(macd: MACD): Int = macd.slow + macd.signalPeriod - 1
end MACDBufferedIndicator

abstract private[fs2] class RSIBufferedIndicator extends BufferedIndicator[RSI]:
  override def bufferSize(rsi: Indicator.RSI): Int = 500
  override def minComputationSize(rsi: RSI): Int = rsi.timeperiod + 1
end RSIBufferedIndicator

private[fs2] object BufferedIndicator:
  def apply[A <: Indicator](using BufferedIndicator[A]): BufferedIndicator[A] = summon
end BufferedIndicator
