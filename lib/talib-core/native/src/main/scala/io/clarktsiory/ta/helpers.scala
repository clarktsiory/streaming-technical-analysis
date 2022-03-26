package io.clarktsiory.ta

import scala.scalanative.*
import scala.scalanative.runtime.{Array as _, *}
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

object helpers:
  inline def arrayToCArray(inline array: Array[Double])(using Zone): Ptr[CDouble] =
    // array.at(0) in newer versions of scala-native
    val ptr = alloc[CDouble](array.length.toULong)
    array.indices.foreach(index => ptr(index) = array(index))
    ptr
  end arrayToCArray

  inline def cArrayToArray(inline cArray: Ptr[CDouble], length: Int): Array[Double] =
    val array = new Array[Double](length)
    array.indices.foreach(index => array(index) = cArray(index))
    array
  end cArrayToArray

  inline def emptyDoubleCArray(length: Long)(using Zone): Ptr[CDouble] =
    alloc[CDouble](length.toULong)

  inline def newCIntPtr()(using Zone): Ptr[CInt] = alloc[CInt]()

  extension (array: Array[Double])
    inline def toCArray(using Zone): Ptr[Double] = arrayToCArray(array)

  extension (cArray: Ptr[Double])
    inline def toArray(length: Int): Array[Double] = cArrayToArray(cArray, length)

  extension (cInt: Ptr[CInt]) inline def toInt: Int = !cInt

  extension (zone: Zone)
    def toAutoCloseable: AutoCloseable & Zone =
      new Zone:
        export zone.*

  given Conversion[Zone, AutoCloseable & Zone] = _.toAutoCloseable

end helpers
