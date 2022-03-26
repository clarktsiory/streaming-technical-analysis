package io.clarktsiory.ta
import scala.scalanative.unsafe.*

@link("ta_lib")
@extern
object talib:
  @name("TA_RSI")
  def RSI(
    startIndex: CInt,
    endIndex: CInt,
    inReal: Ptr[Double],
    optInTimePeriod: CInt,
    outBegIdx: Ptr[CInt],
    outNBElement: Ptr[CInt],
    outReal: Ptr[Double],
  ): Ptr[Double] = extern

  @name("TA_MACD")
  def MACD(
    startIndex: CInt,
    endIndex: CInt,
    inReal: Ptr[Double],
    optInFastPeriod: CInt,
    optInSlowPeriod: CInt,
    optInSignalPeriod: CInt,
    outBegIdx: Ptr[CInt],
    outNBElement: Ptr[CInt],
    outMACD: Ptr[Double],
    outMACDSignal: Ptr[Double],
    outMACDHist: Ptr[Double],
  ): Ptr[Double] = extern
end talib
