package io.clarktsiory.ta.fs2

import fs2.{Pipe, Scan}

import io.clarktsiory.signals.*
import io.clarktsiory.ta.Indicator
import io.clarktsiory.ta.given

given BufferedIndicator[Indicator.RSI] = new RSIBufferedIndicator with MutableSignalBuffer
given BufferedIndicator[Indicator.MACD] = new MACDBufferedIndicator with MutableSignalBuffer

extension (rsi: Indicator.RSI)
  def asPipe[F[_]]: Pipe[F, ScalarSignal, RSISignal] = _.through(RSIChunksState.scan(rsi).toPipe[F])
  def asScan: Scan[RSIChunksState, ScalarSignal, RSISignal] = RSIChunksState.scan(rsi)
end extension

extension (macd: Indicator.MACD)
  def asPipe[F[_]]: Pipe[F, ScalarSignal, MACDSignal] =
    _.through(MACDChunksState.scan(macd).toPipe[F])
  def asScan: Scan[MACDChunksState, ScalarSignal, MACDSignal] = MACDChunksState.scan(macd)
end extension
