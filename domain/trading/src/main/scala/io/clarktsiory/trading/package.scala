package io.clarktsiory.trading

import java.time.LocalDateTime

import cats.kernel.Eq
import io.circe.{Decoder, Encoder}

opaque type Symbol = String

object Symbol:
  def fromString(s: String): Symbol = s

  extension (symbol: Symbol) def value: String = symbol
  end extension

  given Decoder[Symbol] = Decoder.decodeString
  given Encoder[Symbol] = Encoder.encodeString

  given Eq[Symbol] = Eq.catsKernelInstancesForString
end Symbol
