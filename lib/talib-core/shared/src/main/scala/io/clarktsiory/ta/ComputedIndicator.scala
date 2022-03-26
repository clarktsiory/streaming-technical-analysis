package io.clarktsiory.ta

trait ComputedIndicator[A <: Indicator, I, O](using IndicatorSig[A, I, O]):
  def compute(indicator: A, input: I): O
end ComputedIndicator

object ComputedIndicator:
  def apply[A <: Indicator, I, O](using ComputedIndicator[A, I, O]): ComputedIndicator[A, I, O] =
    summon
end ComputedIndicator
