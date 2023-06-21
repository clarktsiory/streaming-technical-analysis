# Streaming technical analysis library for JVM and Scala Native

Technical analysis consists of computing indicators on financial data. This library provides a streaming implementation of technical analysis indicators, using the [TALIB](https://ta-lib.org/) library. It is implemented both for JVM and native platform (respectively using the [Java library](https://ta-lib.org/hdr_dw.html), and the native [C library](https://ta-lib.org/d_api/d_api.html)).


It allows to compute indicators on a stream of data, which is a very relevant use case for financial data which by nature is a timely series of data. The library provides a basis for real-time trading systems, which need to compute indicators reactively as the flow of data is received.


## Library modules 
- [talib-core](/lib/talib-core/) provides the shared TALIB friendlier interfaces for indicator inputs and outputs. This level enforces memory-safe operations and accurate computations for each [indicator](https://ta-lib.org/function.html). It makes use of standard data-structures (e.g. `Array[Double]`) that satisfy the needs of higher-level modules.
The [talib-core-tests](/lib/talib-core-tests/) module provides tests for the core module, to guarantee the accuracy of the computations for each indicator, on any platform.

- [talib-streams](/lib/talib-streams/) provides streaming definitions. It contain the main definitions of the streaming operations for computating indicators in a streaming fashion. This is where the inputs is buffered according to the indicator computation needs : most indicators will need to buffer past values in order to output more accurate results on current values. It consists of buffering know-how and heuristics for streaming each of the indicators. This module serves as a basis for the streaming adapters modules that are detailed below.

- [talib-streams-fs2](/lib/talib-fs2/) provides streaming adapters for the [fs2](https://fs2.io/) library using the _fs2_ API.
The [talib-streams-fs2-tests](/lib/talib-fs2-tests/) module provides tests for the fs2 module, to test the accuracy of the computations for each indicator, on any platform. The measured accuracy is compared to the accuracy of the batch computation of the indicator, which is the reference for the accuracy of the indicator.

- [talib-streams-zio](/lib/talib-zio/) provides streaming adapters for the [zio](https://zio.dev/) library using the _zio_ API.
The [talib-streams-zio-tests](/lib/talib-zio-tests/) module provides tests for the zio module, in an analogous way to the fs2 module.

- [signals](/lib/signals/) provides data types for type-safety when manipulating indicator inputs and outputs. Each indicator has its associated signal type, which simply contains as many output values as the indicator has outputs. It introduces concepts from trading signals, such as 'ohlc' (open-high-low-close), for which the streaming modules may provide helpers to compute indicators on.


## Application modules

Some other modules in the project are applications that make use of the library modules. They are designed to be used as a basis for building streaming applications, and to represent real-world use cases with persistence and presentation needs.

 - [trading domain](/domain/trading/) provides the services to work with in the domain of trading, such as getting a stream of data by a trading symbol (e.g. `BTCUSD` for crypto, `EUR` for forex, ...), and returning the streamed indicators.
 - [trading persistence in-memory](/infrastructure/trading-inmem/) provides the implementation of the persistence layer for the trading domain, using a fs2 [Topic](https://fs2.io/#/concurrency-primitives?id=topic) to do in-memory pub/sub
 - [trading persistence with skunk](/infrastructure/trading-skunk/) provides the implementation of the persistence layer for the trading domain, using the [Skunk](https://tpolecat.github.io/skunk/) library to persist data with PostgreSQL. It currently makes use of the PostgreSQL pub/sub system to save and retrieve data but it the pub/sub system has no message delivery guarantee, so DO NOT USE IT IN PRODUCTION for your own good ! It would appear to be the same with Redis pub/sub, so the only way to have a reliable pub/sub system is to use Kafka or other messaging systems with strong message delivery guarantees. An implementation using [fs2-kafka](https://fd4s.github.io/fs2-kafka/) (JVM only) should be the most fitted integration with the streaming capabilities of the application.
  

## Examples

Examples in both JVM and Scala Native are included for :

- [random data](/examples/random-skunk-app/) that is passed with the PostgreSQL pub/sub system
- [real data](/examples/http4s-websocket-app/) that is fetched with websockets from [Binance](https://binance-docs.github.io/apidocs/spot/en/#individual-symbol-ticker-streams).
skunk, pub/sub , multiple apps can receive signals and compute indicators, thread runtime using epollcat that can limit the capabilities of async web frameworks (http4s ember is supported, you can even process external data in streaming fashion with http4s-curl and its websocket capabilities).

In order to run the examples, you need to either have [nix](https://nixos.org/download.html) installed, or have [docker](https://docs.docker.com/get-docker/) (have a look at the [Dockerfile](/Dockerfile) that uses _nix_ itself), then to run the latter example :
- with nix : `nix develop` or `nix --enable-experimental-features 'nix-command flakes' develop` will open a shell with `sbt` installed, then run `sbt exampleWebsocketAppNative/run`
- with docker : `docker build -t streaming-ta . && docker run --rm -it streaming-ta` will open a shell `sbt` installed, then run `sbt exampleWebsocketAppNative/run`

## JVM or Scala Native ?

Real benchmarks have not been made yet. Running the examples on both platforms and trying to extend the usage further will give you a good idea of the best use cases for each platform.

The following considerations can be made :

* The JVM bindings were designed to use the Java API as a standard way to stream indicators on a stream of data. The ecosystem of streaming and reactive libraries is much more mature and battle-tested on the JVM, for instance with the [fs2](https://fs2.io/) library and others libraries build on top that can be used to stream data from/to a database/messaging/caching system.

* The Scala Native bindings were designed to use the C API for performance reasons, specifically for the computation of indicators on a stream of data. When the ecosystem that cross-builds for Scala Native is limited or harder to integrate with (C networking APIs), the native output proves to have a better throughput. However, the community around Scala Native and contributors are very active, and the ecosystem is growing fast, hopefully thanks to all the effort, we can hope to have a mature ecosystem in the near future.


## Contributing

Contributions are welcome ! Please open an issue or a PR if you want to contribute to the project.

Currently the roadmap is very open, and the project is in its early stages. The following features are planned :

- use [sn-bindgen](https://github.com/indoorvivants/sn-bindgen) to generate the Scala Native bindings for each one of the indicators. This will avoid the need to manually write the bindings, and will allow to generate bindings for the whole TALIB library.
- use code generation to generate the streaming interfaces for each one of the indicator, and corresponding tests. Currently the streaming interfaces are manually written (e.g. for [RSI](/lib/talib-fs2/shared/src/main/scala/io/clarktsiory/ta/fs2/RSIChunksState.scala) and [MACD](/lib/talib-fs2/shared/src/main/scala/io/clarktsiory/ta/fs2/MACDChunksState.scala) which are quite similar), and the code generation will allow to provide the implementation, knowing the indicator inputs and outputs. Ideally, only prior knowledge of what the [buffering](/lib/talib-fs2/shared/src/main/scala/io/clarktsiory/ta/fs2/BufferedIndicator.scala) needs are for the indicator should be needed to generate the streaming interfaces.
- use natively implemented linked-list as buffers for the Scala Native platform (currently the scala `ListBuffer` does the job but performance could be improved).

- cross-build for Scala.js ! This will allow to use the library in the browser, which may be useful for visualizations of indicators, or for trading UIs and analysis tools in the browser.

- use [fs2-kafka](https://fd4s.github.io/fs2-kafka/) to persist data with Kafka, and to retrieve data from Kafka topics. This will allow to have a reliable pub/sub system for the JVM platform.

## Special thanks to community

Thanks to maintainers of the [cats-effect](https://typelevel.org/cats-effect/) library, the [fs2](https://fs2.io/), and the [http4s](https://http4s.org/) libraries that made the [cross-platform capabilities](https://typelevel.org/blog/2022/09/19/typelevel-native.html) possible, also for the [zio](https://zio.dev/) library that cross-builds to Scala Native and JVM.


## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details
