package io.clarktsiory.ta.streams
import scala.collection.mutable.ListBuffer

import io.clarktsiory.signals.ScalarSignal
import io.clarktsiory.ta.Indicator
import io.clarktsiory.ta.Indicator.*

/**
  * Type-class that encodes buffering needs according to the indicator computation properties.
  * Most indicators will need to buffer past values in order to output more accurate results on current values.
  * @tparam A the indicator type
  */
trait BufferedIndicator[A <: Indicator]:
  /**
   * Defines the max number of periods that need to be buffered. 
   * If the value is static, it may influence the precision of computations.
   * If the value is 0 or purely depends on the indicator parameters, it means that the indicator computation should 
   * always be deterministic and computation is always fundamentally accurate (e.g. the mean of _n_ values).
   */
  def bufferSize(indicator: A): Int

  /** 
    * Defines the min number of periods that yield a computation result (e.g. _n_ for the indicator that is mean of the 
    * _n_ latest values).
    */
  def minComputationSize(indicator: A): Int

  /**
    * The type of the buffer used to store past values. It allows buffer manipulation with a pre-defined number of 
    * operations through the extension methods.
    * It should always be an opaque type, to avoid accidental buffer manipulation apart from the extension methods.
    */
  type SignalBuffer

  /**
    * @return An empty buffer than can be further manipulated with the extension methods.
    */
  def emptyBuffer(): SignalBuffer

  extension (buffer: SignalBuffer)
    def added(value: ScalarSignal): SignalBuffer
    def dropped(n: Int): SignalBuffer
    def takeRight(n: Int): SignalBuffer
    def size: Int
    def toArray: Array[ScalarSignal]
  end extension
end BufferedIndicator

/**
  * Partial implementation of the BufferedIndicator type-class for MACD, using known properties of the indicator, and
  * a heuristic for the buffer size.
  */
abstract private[streams] class MACDBufferedIndicator extends BufferedIndicator[MACD]:
  override def bufferSize(macd: MACD): Int = 1_000
  override def minComputationSize(macd: MACD): Int = macd.slow + macd.signalPeriod - 1
end MACDBufferedIndicator

/**
  * Partial implementation of the BufferedIndicator type-class for RSI, using known properties of the indicator, and
  * a heuristic for the buffer size.
  */
abstract private[streams] class RSIBufferedIndicator extends BufferedIndicator[RSI]:
  override def bufferSize(rsi: Indicator.RSI): Int = 500
  override def minComputationSize(rsi: RSI): Int = rsi.timeperiod + 1
end RSIBufferedIndicator

object BufferedIndicator:
  def apply[A <: Indicator](using BufferedIndicator[A]): BufferedIndicator[A] = summon
end BufferedIndicator
