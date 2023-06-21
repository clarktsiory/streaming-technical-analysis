package io.clarktsiory.ta.streams

import scala.collection.mutable.ListBuffer

import io.clarktsiory.signals.ScalarSignal

//TODO: move to jvm/ once a NativeSignalBuffer is implemented
trait MutableSignalBuffer { self: BufferedIndicator[?] =>
  opaque type SignalBuffer = ListBuffer[ScalarSignal]

  def emptyBuffer(): SignalBuffer = ListBuffer.empty[ScalarSignal]

  extension (buffer: SignalBuffer)
    def added(value: ScalarSignal): SignalBuffer = buffer.append(value)
    def dropped(n: Int): SignalBuffer = buffer.drop(n)
    def takeRight(n: Int): SignalBuffer = buffer.takeRight(n)
    def size: Int = buffer.size
    def toArray: Array[ScalarSignal] = buffer.toArray
  end extension
}
