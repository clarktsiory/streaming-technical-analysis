package io.clarktsiory.ta

enum Indicator:
  case MACD(fast: Int, slow: Int, signalPeriod: Int)
  case RSI(timeperiod: Int)
  case Identity
end Indicator
