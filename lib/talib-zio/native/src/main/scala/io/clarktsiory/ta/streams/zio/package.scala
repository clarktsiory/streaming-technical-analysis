package io.clarktsiory.ta.streams.zio

import zio.ZIO
import zio.stream.ZPipeline

import io.clarktsiory.signals.*
import io.clarktsiory.ta.given
import io.clarktsiory.ta.streams.*
import io.clarktsiory.ta.{ComputedIndicator, Indicator, IndicatorSig}
import scalanative.unsafe.Zone
import impl.*

given BufferedIndicator[Indicator.RSI] = new RSIBufferedIndicator with MutableSignalBuffer
given BufferedIndicator[Indicator.MACD] = new MACDBufferedIndicator with MutableSignalBuffer

extension (rsi: Indicator.RSI)
  def asPipeline[R, E]: ZPipeline[R, E, ScalarSignal, RSISignal] = rsiPipeline(rsi)
end extension

extension (macd: Indicator.MACD)
  def asPipeline[R, E]: ZPipeline[R, E, ScalarSignal, MACDSignal] = macdPipeline(macd)
end extension

private object impl:
  import io.clarktsiory.ta.given

  def macdPipeline[R, E](macd: Indicator.MACD)(using
    buf: BufferedIndicator[Indicator.MACD],
    s: IndicatorSig[Indicator.MACD, Array[Double], (Array[Double], Array[Double], Array[Double])],
  ): ZPipeline[R, E, ScalarSignal, MACDSignal] =
    zonedPipeline(MACDChunksState.pipeline(macd))

  def rsiPipeline[R, E](rsi: Indicator.RSI)(using
    buf: BufferedIndicator[Indicator.RSI],
    s: IndicatorSig[Indicator.RSI, Array[Double], Array[Double]],
  ): ZPipeline[R, E, ScalarSignal, RSISignal] =
    zonedPipeline(RSIChunksState.pipeline(rsi))

  private[this] def zonedPipeline[S, R, E, I, O](
    f: Zone ?=> ZPipeline[R, E, I, O]
  ): ZPipeline[R, E, I, O] =
    ZPipeline.unwrapScoped(
      ZIO
        .acquireRelease(ZIO.succeed(Zone.open()))(z => ZIO.succeed(z.close()))
        .map(implicit z => f)
    )
  end zonedPipeline

end impl
