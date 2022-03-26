package io.clarktsiory.ta

import org.junit.Assert.*
import org.junit.{After, Test}

class ComputedIndicatorTest {
  import Indicator.*

  val rsiExecutor = ComputedIndicator[RSI, Array[Double], Array[Double]]
  val macdExecutor =
    ComputedIndicator[MACD, Array[Double], (Array[Double], Array[Double], Array[Double])]

  // "RSI should return an empty array when parameter is lower than 1"
  @Test def RSI_should_return_an_empty_array_when_parameter_is_lower_than_1(): Unit = {
    val input = Array[Double](3, 2, 3.4, 4.8, 5.9, 6.1, 4.5, 4.9, 4.6)
    assertEquals(
      (-1 to 1).map(l => rsiExecutor.compute(RSI(l), input).isEmpty),
      (-1 to 1).map(_ => true),
    )
  }

  // "RSI should return an empty array when input array is empty"
  @Test def RSI_should_return_an_empty_array_when_input_array_is_empty(): Unit =
    assertEquals(rsiExecutor.compute(RSI(4), Array.emptyDoubleArray).length, 0)

  // "RSI 2 should return the correct result"
  @Test def RSI_2_should_return_the_correct_result(): Unit = {
    val input = Array[Double](3, 2, 3.4, 4.8, 5.9, 6.1, 4.5, 4.9, 4.6)
    val output = rsiExecutor.compute(RSI(2), input)
    assertEquals(
      Array[Double](58.333333333333336, 80.76923076923077, 89.58333333333334, 91.07142857142857,
        27.71739130434782, 46.3709677419355, 33.43023255813953).toList,
      output.toList,
    )
  }

  // "RSI 3 should return the correct result"
  @Test def RSI_3_should_return_the_correct_result(): Unit = {
    val input = Array[Double](3, 2, 3.4, 4.8, 5.9, 6.1, 4.5, 4.9, 4.6)
    val output = rsiExecutor.compute(RSI(3), input)
    assertEquals(
      Array[Double](73.68421052631578, 81.65137614678899, 83.05084745762711, 43.36283185840708,
        51.969981238273924, 44.38213498898456).toList,
      output.toList,
    )
  }

  // "MACD should return an empty array when input array is empty"
  @Test def MACD_should_return_an_empty_array_when_input_array_is_empty(): Unit =
    assertEquals(macdExecutor.compute(MACD(4, 8, 2), Array.emptyDoubleArray)._1.length, 0)

  // "MACD should return an empty array when parameter is lower than 1"
  @Test def MACD_should_return_an_empty_array_when_parameter_is_lower_than_1(): Unit = {
    val input = Array[Double](3, 2, 3.4, 4.8, 5.9, 6.1, 4.5, 4.9, 4.6)
    assertEquals(
      (-1 to 1).map(l => macdExecutor.compute(MACD(l, 8, 2), input)._1.isEmpty),
      (-1 to 1).map(_ => true),
    )
    assertEquals(
      (-1 to 1).map(l => macdExecutor.compute(MACD(4, l, 2), input)._1.isEmpty),
      (-1 to 1).map(_ => true),
    )
    assertEquals(
      (-1 to 1).map(l => macdExecutor.compute(MACD(4, 8, l), input)._1.isEmpty),
      (-1 to 1).map(_ => true),
    )
  }

  // "MACD(12, 26, 9) should return the correct result"
  @Test def MACD_12_26_9_should_return_the_correct_result(): Unit = {
    val input = Array[Double](
      722.4215489116929, 39.68473875219536, 132.37412416056227, 820.8327034830797,
      160.52519805272757, 789.7390509079572, 94.5694303321477, 645.6342096568309, 914.6502590754036,
      912.8984513784447, 639.7067549015048, 484.48495021493954, 291.46766761297704,
      578.6720735740239, 559.2805461832797, 677.9012908170417, 499.81947678684327,
      925.4259023992695, 736.4095546519093, 347.57371130610204, 292.0219893836907,
      24.59585818302312, 287.1621293051014, 461.14889311059403, 754.9110591244289,
      6.974940608554747, 748.7240932297304, 84.2499117116069, 614.3008814816645, 318.24636219356915,
      773.2549573686797, 15.837667695226433, 644.8766490939585, 655.2349005650432,
      580.7181095372339, 292.3597752009682, 183.49928729918142, 717.5753642108148,
      807.8438790247626, 994.8620237299043, 353.8097165208717, 145.13413717770018,
      696.8745778500743, 489.77940110552566, 782.1748169120131, 184.52395987308412,
      873.5201418595698, 338.15208445061637, 993.6637592463436, 63.76733641232668,
      972.0705026421191, 104.39443285511007,
    )
    val value = Array[Double](
      -2.592568520484008, 5.056750680800974, -12.010763802917722, -33.92990080878957,
      -8.111988899541302, 19.409071343854635, 55.66879594015779, 32.30493405517154,
      -3.0147182986765415, 13.361027612186604, 9.518352587090419, 29.724228941312163,
      -2.459545746563265, 27.31593356808662, 7.625584479213899, 44.40330116560574,
      -1.4680405872636584, 35.06683643479937, -5.924918889155265,
    )
    val signal = Array[Double](
      -20.525104806040133, -15.40873370867191, -14.729139727521073, -18.569291943774772,
      -16.477831334928077, -9.300450799171534, 3.693398548694331, 9.415705649989773,
      6.9296208602565095, 8.215902210642529, 8.476392285932107, 12.725959617008119,
      9.688858544293842, 13.214273549052397, 12.096535735084696, 18.557888821188904,
      14.552702939498392, 18.655529638558587, 13.739439933015817,
    )
    val histogram = Array[Double](
      17.932536285556125, 20.465484389472884, 2.7183759246033503, -15.360608865014797,
      8.365842435386774, 28.70952214302617, 51.97539739146346, 22.88922840518177,
      -9.944339158933051, 5.145125401544075, 1.041960301158312, 16.998269324304044,
      -12.148404290857107, 14.101660019034224, -4.470951255870798, 25.845412344416836,
      -16.02074352676205, 16.411306796240783, -19.66435882217108,
    )
    val (outValue, outSignal, outHistogram) = macdExecutor.compute(MACD(12, 26, 9), input)

    val precision = 10e-14
    outValue
      .lazyZip(outSignal)
      .lazyZip(outHistogram)
      .toList
      .zip(value.lazyZip(signal).lazyZip(histogram).toList)
      .zipWithIndex
      .foreach { case (((v, s, h), (ve, se, he)), i) =>
        assertEquals(s"Index $i value", ve, v, precision)
        assertEquals(s"Index $i signal", se, s, precision)
        assertEquals(s"Index $i histogram", he, h, precision)
      }
  }

}
