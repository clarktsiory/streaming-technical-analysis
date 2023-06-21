package io.clarktsiory.ta.streams.zio

import java.time.LocalDateTime
import java.util.concurrent.Semaphore

import org.junit.Assert.*
import org.junit.{AfterClass, Test}
import zio.*
import zio.stream.{ZSink, ZStream => Stream}

import io.clarktsiory.signals.*
import io.clarktsiory.ta.given
import io.clarktsiory.ta.streams.BufferedIndicator
import io.clarktsiory.ta.{ComputedIndicator, Indicator}

class IndicatorStreamingZIOTest {
  import IndicatorStreamingZIOTest.*

  val startDate = LocalDateTime.of(1970, 1, 1, 8, 0, 0)
  val dateStream = Stream.iterate(startDate)(_.plusHours(1)).buffer(16)
  given runtime: Runtime[Any] = Runtime.default

  @Test def RSI2_pipe_should_have_the_right_time_1_chunk(): Unit = {
    val inputStream =
      dateStream.zipWith(Stream(3, 2, 3.4, 4.8, 5.9, 6.1, 4.5, 4.9, 4.6))(ScalarSignal(_, _))
        .rechunk(16)

    val outputStream = inputStream.via(Indicator.RSI(2).asPipeline)

    val expectedStream =
      dateStream
        .drop(2)
        .zipWith(
          Stream(58.333333333333336, 80.76923076923077, 89.58333333333334, 91.07142857142857,
            27.71739130434782, 46.3709677419355, 33.43023255813953)
        )(RSISignal(_, _))

    val output = outputStream.collectToList.unsafeRun()
    val expected = expectedStream.collectToList.unsafeRun()

    expected.zip(output).zipWithIndex.foreach { case ((e, o), i) =>
      assertEquals("Index: " + i, e, o)
    }
    assertEquals(1, inputStream.countChunks.unsafeRun())
  }

  @Test def RSI2_pipe_should_have_the_right_time_2_chunks(): Unit = {
    val inputStream =
      dateStream.zipWith(Stream(3, 2, 3.4, 4.8, 5.9, 6.1, 4.5, 4.9, 4.6))(ScalarSignal(_, _))
        .rechunk(5)

    val outputStream = inputStream.via(Indicator.RSI(2).asPipeline)

    val expectedStream =
      dateStream
        .drop(2)
        .zipWith(
          Stream(58.333333333333336, 80.76923076923077, 89.58333333333334, 91.07142857142857,
            27.71739130434782, 46.3709677419355, 33.43023255813953)
        )(RSISignal(_, _))

    val output = outputStream.collectToList.unsafeRun()
    val expected = expectedStream.collectToList.unsafeRun()

    expected.zip(output).zipWithIndex.foreach { case ((e, o), i) =>
      assertEquals("Index: " + i, e, o)
    }
    assertEquals(2, inputStream.countChunks.unsafeRun())
  }

  @Test def RSI2_pipe_should_have_the_right_time_3_chunks(): Unit = {
    val inputStream =
      dateStream.zipWith(Stream(3, 2, 3.4, 4.8, 5.9, 6.1, 4.5, 4.9, 4.6))(ScalarSignal(_, _))
        .rechunk(3)

    val outputStream = inputStream.via(Indicator.RSI(2).asPipeline)

    val expectedStream =
      dateStream
        .drop(2)
        .zipWith(
          Stream(58.333333333333336, 80.76923076923077, 89.58333333333334, 91.07142857142857,
            27.71739130434782, 46.3709677419355, 33.43023255813953)
        )(RSISignal(_, _))

    val output = outputStream.collectToList.unsafeRun()
    val expected = expectedStream.collectToList.unsafeRun()

    expected.zip(output).zipWithIndex.foreach { case ((e, o), i) =>
      assertEquals("Index: " + i, e, o)
    }
    assertEquals(3, inputStream.countChunks.unsafeRun())
  }

  @Test def RSI16_pipe_should_have_the_right_output_long_sequence_100_input(): Unit = {
    val inputStream =
      dateStream.zipWith(
        Stream(
          2.7502931836911926, 2.2321073814882277, 7.364712141640124, 6.766994874229113,
          8.921795677048454, 0.8693883262941615, 4.2192181968527045, 0.29797219438070344,
          2.1863797480360336, 5.053552881033624, 0.26535969683863625, 1.988376506866485,
          6.498844377795232, 5.449414806032166, 2.204406220406967, 5.892656838759088,
          8.094304566778266, 0.06498759678061017, 8.05819251832808, 6.981393949882269,
          3.4025051651799187, 1.5547949981178155, 9.572130722067811, 3.365945451126268,
          0.9274584338014791, 0.9671637683346401, 8.474943663474598, 6.037260313668911,
          8.071282732743802, 7.297317866938179, 5.362280914547007, 9.731157639793706,
          3.785343772083535, 5.52040631273227, 8.294046642529949, 6.185197523642461,
          8.617069003107773, 5.77352145256762, 7.045718362149235, 0.45824383655662215,
          2.2789827565154686, 2.8938796360210715, 0.797919769236275, 2.327908863610302,
          1.0100142940972912, 2.779736031100921, 6.356844442644002, 3.6483217897008426,
          3.701809671168826, 2.095070307714877, 2.669778220491134, 9.36654587712494,
          6.480353852465935, 6.091310056669882, 1.7113864819809699, 7.291267979503492,
          1.634024937619284, 3.794554417576478, 9.895233506365953, 6.399997598540929,
          5.569497437746462, 6.846142509898746, 8.428519201898096, 7.759999115462448,
          2.2904807196410437, 0.3210024390403776, 3.154530480590819, 2.6774087597570273,
          2.109828435863265, 9.429097143350544, 8.763676264726689, 3.146778807984779,
          6.5543866529488, 3.9563190106066424, 9.145475897405435, 4.5885185258739885,
          2.6488016649805246, 2.4662750769398345, 5.613681341631508, 2.6274160852293527,
          5.845859902235405, 8.978228836024769, 3.9940050514039727, 2.1932075915728335,
          9.975376064951103, 5.095262936764645, 0.9090941217379389, 0.4711637542473457,
          1.0964913035065915, 6.2744604170309, 7.920793643629641, 4.22159966799684,
          0.6352770615195713, 3.816192865065368, 9.961213802400968, 5.29114345099137,
          9.710783776136182, 8.60779702234498, 0.11481021942819636, 7.207218193601946,
        )
      )(ScalarSignal(_, _)).rechunk(5)

    val outputStream = inputStream.via(Indicator.RSI(16).asPipeline)

    // Hard-coded values extracted from a TA-Lib interactive session
    val expectedStream =
      dateStream
        .drop(16)
        .zipWith(
          Stream(
            55.37752552981706, 47.235684132236656, 54.36088872230973, 53.32612942817504,
            49.955008863615035, 48.27440438719467, 55.243376629581206, 49.71297836652215,
            47.711177992915054, 47.74772176292896, 54.20323386073401, 51.97915998944729,
            53.671093257444625, 52.91439703944968, 50.996955586118766, 54.9301350960661,
            49.1977401707959, 50.79603924109405, 53.30124855796197, 51.187599844766105,
            53.45781007210814, 50.526870196277294, 51.788321705442556, 45.39531003236508,
            47.31281333251787, 47.970978912368246, 45.886835212952214, 47.6575050724361,
            46.26651974282493, 48.422784951494776, 52.52986800774869, 49.35561693308004,
            49.4199990788293, 47.48573363009605, 48.258385366594254, 56.25773264360755,
            52.52465921839852, 52.028266209701755, 46.72538319036517, 53.20644155485331,
            47.02030346432745, 49.41610978043325, 55.4799111799364, 51.69294802635154,
            50.81386577934811, 52.1481732174367, 53.8049981925639, 52.97837038112472,
            46.71501270564008, 44.685883065669714, 48.14263945549616, 47.60826517912825,
            46.947037978846346, 55.45682362926829, 54.607412322896835, 47.98927156454856,
            51.771679601370835, 48.88071634818379, 54.31563423960832, 49.39626698759322,
            47.44521471341763, 47.257859689552454, 50.829233410922, 47.569300837662986,
            51.16954614318151, 54.418829713152725, 48.89643552435346, 47.05612778870291,
            54.8834562034788, 49.944413750628655, 46.14479841733579, 45.756360841396656,
            46.44302713282247, 51.82898915542741, 53.417736592700614, 49.50453539667932,
            46.01836319886886, 49.39022521745795, 55.16153514764529, 50.49379387957872,
            54.38984136879972, 53.27377464785984, 45.59020870893725, 51.78458073216571,
          )
        )(RSISignal(_, _))

    val output = outputStream.collectToList.unsafeRun()
    val expected = expectedStream.collectToList.unsafeRun()

    expected.zip(output).zipWithIndex.foreach { case ((e, o), i) =>
      assertEquals("Index: " + i, e, o)
    }
    assertEquals(20, inputStream.countChunks.unsafeRun())
  }

  @Test def RSI16_pipe_should_have_the_right_output_long_sequence_random_input(): Unit = {
    val rsi: Indicator.RSI = Indicator.RSI(16)
    // test that time is still linear even if buffer is full
    val N = BufferedIndicator[Indicator.RSI].bufferSize(rsi) + rsi.timeperiod * 10
    val bufferN = 10
    // beware this is an infinite stream, you should #take(N) it
    val inputStream: Stream[Any, Nothing, ScalarSignal] =
      for
        r <- Stream.fromZIO(Random.setSeed(42))
        ret <- dateStream
          .zipWith(Stream.repeatZIO(Random.nextGaussian))(ScalarSignal(_, _))
          .rechunk(bufferN)
      yield ret

    val outputStream = inputStream.via(rsi.asPipeline).take(N)

    val expectedStreamDates = dateStream.take(N).drop(rsi.timeperiod)

    // Time testing
    val outputTime = outputStream.collectToList.unsafeRun().map(_.time)
    val expectedTime = expectedStreamDates.collectToList.unsafeRun()

    expectedTime.zip(outputTime).zipWithIndex.foreach { case ((e, o), i) =>
      assertEquals("Index: " + i, e, o)
    }

    // Value testing: compare to when input is a single chunk, thus RSI can be computed without buffering
    val inputSingleChunk = inputStream.take(N).rechunk(N)
    val output =
      inputSingleChunk.via(rsi.asPipeline).collect { case s: RSISignal => s.value.toDouble }
    val expected = ComputedIndicator[Indicator.RSI, Array[Double], Array[Double]]
      .compute(
        rsi,
        inputSingleChunk.map(_.value.doubleValue).collectToVector.unsafeRun().toArray,
      )
      .toList

    // TODO: compute a statistical variance and standard deviation to use as epsilon. epsilon on individual value may fail
    val epsilon = 0.01 // this value may be to high if RSI buffer size gets smaller
    assertEquals(1, inputSingleChunk.countChunks.unsafeRun())
    expected.zip(output.collectToList.unsafeRun()).zipWithIndex.foreach { case ((e, o), i) =>
      assertEquals(e, o, epsilon)
    }
  }

  @Test def MACD_pipe_should_have_the_right_time_1_chunk(): Unit = {
    val inputStream =
      dateStream.zipWith(Stream(3, 2, 3.4, 4.8, 5.9, 6.1, 4.5, 4.9, 4.6))(ScalarSignal(_, _))
        .rechunk(16)

    val outputStream = inputStream.via(Indicator.MACD(2, 4, 3).asPipeline)

    val expectedStream =
      dateStream
        .drop(5)
        .zipWith(
          Stream(
            (0.7893333333333334, 0.8497777777777777, -0.0604444444444443),
            (0.11804444444444506, 0.4839111111111114, -0.36586666666666634),
            (0.0589748148148157, 0.27144296296296355, -0.21246814814814785),
            (-0.04856572839506157, 0.11143861728395099, -0.16000434567901256),
          )
        ) { case (d, (v, s, h)) => MACDSignal(d, value = v, signal = s, histogram = h) }

    val output = outputStream.collectToList.unsafeRun()
    val expected = expectedStream.collectToList.unsafeRun()

    expected.zip(output).zipWithIndex.collect {
      case ((e, MACDSignal(time, signal, value, histogram)), i) =>
        def message(key: Option[String] = None) =
          s"Index: $i, time: ${e.time}${key.map(", " + _).getOrElse("")}"
        assertEquals(message(), e.time, time)
        assertEquals(message(Some("value")), e.value, value)
        assertEquals(message(Some("signal")), e.signal, signal)
        assertEquals(
          message(Some("histogram")),
          e.histogram.toDouble,
          histogram.toDouble,
          if i == 3 then 0.2 else 0,
        ) // weird runtime precision error
    }
    assertEquals(1, inputStream.countChunks.unsafeRun())
  }

  @Test def MACD_pipe_should_have_the_right_time_long_sequence_random_input(): Unit = {
    val macd: Indicator.MACD = Indicator.MACD(8, 10, 8)
    // test that time is still linear even if buffer is full
    val N = BufferedIndicator[Indicator.MACD].bufferSize(macd) + macd.signalPeriod * 10
    val bufferN = 10
    // beware this is an infinite stream, you should #take(N) it
    val inputStream: Stream[Any, Throwable, ScalarSignal] =
      for
        r <- Stream.fromZIO(Random.setSeed(42))
        ret <- dateStream
          .zipWith(Stream.repeatZIO(Random.nextGaussian))(ScalarSignal(_, _))
          .rechunk(bufferN)
      yield ret

    val expectedStreamDates =
      dateStream.take(N).drop(BufferedIndicator[Indicator.MACD].minComputationSize(macd) - 1)

    val inputSingleChunk = inputStream.take(N).rechunk(N)
    val output = inputSingleChunk.via(macd.asPipeline).collect { case s: MACDSignal => s }
    val expected = ComputedIndicator[Indicator.MACD, Array[
      Double
    ], (Array[Double], Array[Double], Array[Double])]
      .compute(
        macd,
        inputSingleChunk.map(_.value.doubleValue).collectToVector.unsafeRun().toArray,
      )

    assertEquals(1, inputSingleChunk.countChunks.unsafeRun())

    val RMSE_deviation = 0.01 // means that standard deviation of the error is 0.01
    val result = output.collectToList.unsafeRun()

    val valueDeviation = Math.sqrt(
      (expected._1
        .zip(result.map(_.value))
        .map(_ - _)
        .map(_.pow(2))
        .sum / expected._1.length).doubleValue
    )
    assertTrue(s"MACD value deviation: $valueDeviation", valueDeviation <= RMSE_deviation)

    val signalDeviation = Math.sqrt(
      (expected._2
        .zip(result.map(_.signal))
        .map(_ - _)
        .map(_.pow(2))
        .sum / expected._2.length).doubleValue
    )
    assertTrue(s"MACD signal deviation: $signalDeviation", signalDeviation <= RMSE_deviation)

    val histogramDeviation = Math.sqrt(
      (expected._3
        .zip(result.map(_.histogram))
        .map(_ - _)
        .map(_.pow(2))
        .sum / expected._3.length).doubleValue
    )
    assertTrue(
      s"MACD histogram deviation: $histogramDeviation",
      histogramDeviation <= RMSE_deviation,
    )

  }

}

object IndicatorStreamingZIOTest {
  extension [A](z: Task[A])
    def unsafeRun()(using runtime: Runtime[Any]): A =
      Unsafe.unsafe(implicit unsafe =>
        runtime.unsafe.run(z).getOrThrow()
      )
    end unsafeRun
  end extension

  extension [E, A](stream: Stream[Any, E, A])
    def collectToList: ZIO[Any, E, List[A]] =
      stream.run(ZSink.collectAll[A]).map(_.toList)

    def collectToVector: ZIO[Any, E, Vector[A]] =
      stream.run(ZSink.collectAll[A]).map(_.toVector)

    def countChunks: ZIO[Any, E, Long] =
      stream.chunks.run(ZSink.count)
  end extension

}
