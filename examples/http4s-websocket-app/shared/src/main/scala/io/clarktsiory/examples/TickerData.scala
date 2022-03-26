package io.clarktsiory.examples

import java.time.{Instant, LocalDateTime, ZoneOffset}

import io.circe.Decoder

case class TickerData(
  symbol: String,
  time: LocalDateTime,
  open: Double,
  high: Double,
  low: Double,
  close: Double,
)

given Decoder[TickerData] = Decoder.instance { cursor =>
  for {
    time <- cursor
      .get[Long]("E")
      .map(timestamp => LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC))
    symbol <- cursor.get[String]("s")
    open <- cursor.get[Double]("o")
    high <- cursor.get[Double]("h")
    low <- cursor.get[Double]("l")
    close <- cursor.get[Double]("c")
  } yield TickerData(symbol, time, open, high, low, close)
}
