package io.clarktsiory.trading

import io.clarktsiory.signals.{IndicatorSignal, ScalarSignal}
import io.clarktsiory.ta.*

trait IndicatorStreamingService[F[_]]:
  def getIndicatorStream(symbol: Symbol, indicator: Indicator): F[IndicatorSignal]
end IndicatorStreamingService
