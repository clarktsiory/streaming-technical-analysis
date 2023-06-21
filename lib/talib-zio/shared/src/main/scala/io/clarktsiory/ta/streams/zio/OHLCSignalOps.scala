package io.clarktsiory.ta.streams.zio

import zio.stream.ZStream

import io.clarktsiory.signals.{OHLCSignal, ScalarSignal}

extension [R, E](ohlcStream: ZStream[R, E, OHLCSignal])
  def close: ZStream[R, E, ScalarSignal] =
    ohlcStream.map(ohlc => ScalarSignal(ohlc.time, ohlc.`close`))