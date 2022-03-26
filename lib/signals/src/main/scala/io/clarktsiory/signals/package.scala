package io.clarktsiory.signals

import scala.concurrent.duration.*
import java.time.temporal.TemporalUnit
import java.time.{LocalDateTime, ZoneId}

import cats.kernel.{Monoid, Semigroup}
import io.circe.derivation.{Configuration, ConfiguredDecoder, ConfiguredEncoder}

import math.Ordering.Implicits.*

given signalsConfiguration: Configuration = Configuration.default.withDiscriminator("_type")

sealed trait Signal(val localDateTime: LocalDateTime) derives ConfiguredEncoder

sealed abstract class IndicatorSignal(val time: LocalDateTime) extends Signal(time)
    derives ConfiguredDecoder,
      ConfiguredEncoder

final case class MACDSignal(
  override val time: LocalDateTime,
  signal: BigDecimal,
  value: BigDecimal,
  histogram: BigDecimal,
) extends IndicatorSignal(time)

final case class RSISignal(override val time: LocalDateTime, value: BigDecimal)
    extends IndicatorSignal(time)

final case class IdentitySignal(override val time: LocalDateTime, value: BigDecimal)
    extends IndicatorSignal(time)

sealed abstract class TradingSignal(val time: LocalDateTime) extends Signal(time)
    derives ConfiguredDecoder,
      ConfiguredEncoder

final case class ScalarSignal(override val time: LocalDateTime, value: BigDecimal)
    extends TradingSignal(time)

final case class StrategySignal(override val time: LocalDateTime, value: Boolean)
    extends TradingSignal(time)

final case class OHLCSignal(
  override val time: LocalDateTime,
  open: BigDecimal,
  high: BigDecimal,
  low: BigDecimal,
  close: BigDecimal,
) extends TradingSignal(time):
  def closeSignal: ScalarSignal = ScalarSignal(time, close)
end OHLCSignal
