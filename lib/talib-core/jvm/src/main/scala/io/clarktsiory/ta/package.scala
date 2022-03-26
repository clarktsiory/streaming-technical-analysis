package io.clarktsiory.ta

import com.tictactec.ta.lib.{Core, MInteger}

import io.clarktsiory.ta.Indicator.*

private val talib = new Core()

private def mint() = new MInteger()

given ComputedIndicator[RSI, Array[Double], Array[
  Double
]] = // (t: RSI, inArray: Array[Double]) => {
  new:
    def compute(indicator: RSI, input: Array[Double]): Array[Double] =
      if indicator.timeperiod <= 1 || input.length <= 0
      then Array.emptyDoubleArray
      else
        val outArray: Array[Double] = new Array[Double](input.length)
        val (begin, nbElements) = (mint(), mint())
        talib.rsi(0, input.length - 1, input, indicator.timeperiod, begin, nbElements, outArray)
        outArray.slice(0, nbElements.value)
end given

given ComputedIndicator[MACD, Array[Double], (Array[Double], Array[Double], Array[Double])] =
  new:
    def compute(
      indicator: MACD,
      input: Array[Double],
    ): (Array[Double], Array[Double], Array[Double]) =
      if indicator.fast <= 1 || indicator.slow <= 1 || indicator.signalPeriod <= 1 || input.length <= 0
      then (Array.emptyDoubleArray, Array.emptyDoubleArray, Array.emptyDoubleArray)
      else
        val optimisticLength =
          (input.length - indicator.fast).max(indicator.slow).max(indicator.signalPeriod)
        def makeArray() = new Array[Double](optimisticLength)
        val (value, signal, histogram) = (makeArray(), makeArray(), makeArray())
        val (begin, nbElements) = (mint(), mint())
        talib.macd(
          0,
          input.length - 1,
          input,
          indicator.fast,
          indicator.slow,
          indicator.signalPeriod,
          begin,
          nbElements,
          value,
          signal,
          histogram,
        )
        def slice(a: Array[Double]) = a.slice(0, nbElements.value)
        (slice(value), slice(signal), slice(histogram))
  end new
end given
