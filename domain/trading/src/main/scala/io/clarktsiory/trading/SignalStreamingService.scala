package io.clarktsiory.trading

import io.clarktsiory.signals.{OHLCSignal, Signal, TradingSignal}

trait SignalStreamingService[F[_]]:
  def getSymbols: F[Symbol]
  def getOHLCBySymbol(symbol: Symbol): F[OHLCSignal]
  def saveSignal(symbol: Symbol, stream: F[Signal]): F[Unit]
end SignalStreamingService
