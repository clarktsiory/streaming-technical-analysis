package io.clarktsiory.ta.streams.fs2

import fs2.Stream

import io.clarktsiory.signals.{OHLCSignal, ScalarSignal}

extension [F[_]](ohlcStream: Stream[F, OHLCSignal])
  def close: Stream[F, ScalarSignal] =
    ohlcStream.map(ohlc => ScalarSignal(ohlc.time, ohlc.`close`))