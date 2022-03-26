package io.clarktsiory.ta

import scala.scalanative.unsafe.{Ptr, Zone}

import helpers.*
import Indicator.*

given (using Zone): ComputedIndicator[RSI, Array[Double], Array[Double]] =
  new:
    def compute(indicator: RSI, input: Array[Double]): Array[Double] =
      if indicator.timeperiod <= 1 || input.length <= 0
      then Array.emptyDoubleArray
      else
        val (begin, nbElements) = (newCIntPtr(), newCIntPtr())
        val outPtr = emptyDoubleCArray(input.length)
        talib.RSI(
          0,
          input.length - 1,
          input.toCArray,
          indicator.timeperiod,
          begin,
          nbElements,
          outPtr,
        )
        outPtr.toArray(!nbElements)
  end new
end given

given (using
  Zone
): ComputedIndicator[MACD, Array[Double], (Array[Double], Array[Double], Array[Double])] =
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
        def makeOutPtr() = emptyDoubleCArray(optimisticLength)
        def convertOutPtr(outPtr: Ptr[Double], length: Int): Array[Double] = outPtr.toArray(length)
        val (value, signal, histogram) = (makeOutPtr(), makeOutPtr(), makeOutPtr())
        val (begin, nbElements) = (newCIntPtr(), newCIntPtr())
        talib.MACD(
          0,
          input.length - 1,
          input.toCArray,
          indicator.fast,
          indicator.slow,
          indicator.signalPeriod,
          begin,
          nbElements,
          value,
          signal,
          histogram,
        )
        val actualLength = !nbElements
        (
          convertOutPtr(value, actualLength),
          convertOutPtr(signal, actualLength),
          convertOutPtr(histogram, actualLength),
        )
  end new
end given
