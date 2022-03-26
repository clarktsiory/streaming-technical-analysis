package io.clarktsiory.ta

// Enforce types (input args and output array shape) for each indicator computed with talib
sealed trait IndicatorSig[A <: Indicator, I, O]

object IndicatorSig:

  given IndicatorSig[Indicator.RSI, Array[Double], Array[Double]] with {}
  given IndicatorSig[Indicator.MACD, Array[Double], (Array[Double], Array[Double], Array[Double])]
    with {}

end IndicatorSig
