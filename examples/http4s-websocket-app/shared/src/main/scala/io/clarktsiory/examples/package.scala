package io.clarktsiory.examples

import io.clarktsiory.signals.*
import io.clarktsiory.trading.Symbol

extension (ticker: TickerData)
  def toOHLCSignal: OHLCSignal =
    OHLCSignal(ticker.time, ticker.open, ticker.high, ticker.low, ticker.close)
  def getSymbol: Symbol = Symbol.fromString(ticker.symbol)
end extension

extension (ohlc: OHLCSignal)
  def toTickerData(symbol: Symbol): TickerData =
    TickerData(
      symbol.value,
      ohlc.time,
      ohlc.open.doubleValue,
      ohlc.high.doubleValue,
      ohlc.low.doubleValue,
      ohlc.close.doubleValue,
    )
