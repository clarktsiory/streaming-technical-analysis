package io.clarktsiory.ta.streams.zio

import zio.stream.ZPipeline

import io.clarktsiory.signals.*
import io.clarktsiory.ta.Indicator
import io.clarktsiory.ta.given
import io.clarktsiory.ta.streams.*

given BufferedIndicator[Indicator.RSI] = new RSIBufferedIndicator with MutableSignalBuffer
given BufferedIndicator[Indicator.MACD] = new MACDBufferedIndicator with MutableSignalBuffer

extension (rsi: Indicator.RSI)
  def asPipeline[R, E]: ZPipeline[R, E, ScalarSignal, RSISignal] = RSIChunksState.pipeline(rsi)
end extension

extension (macd: Indicator.MACD)
  def asPipeline[R, E]: ZPipeline[R, E, ScalarSignal, MACDSignal] = MACDChunksState.pipeline(macd)
end extension
