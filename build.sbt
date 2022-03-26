import scala.scalanative.build._

ThisBuild / tlBaseVersion := "0.1"
ThisBuild / organization := "io.clarktsiory"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  tlGitHubDev("clarktsiory", "Clark Andrianasolo")
)
ThisBuild / organizationName := "Typelevel"

ThisBuild / scalaVersion := "3.2.2"

val catsVersion = "2.9.0"
val catsEffectVersion = "3.3.14"
val circeVersion = "0.14.5"
val fs2Version = "3.6.1"
val kittensVersion = "3.0.0"
val skunkVersion = "0.6.0-RC2"

lazy val sharedSettings = Seq(
  scalacOptions ++= Seq(
    "UTF-8",
    "-explain",
    "-new-syntax",
    "-indent",
    "-Werror",
    "-language:implicitConversions",
    "-Xmax-inlines:100"
  )
)

lazy val testingJUnitSettings = Seq(
  libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test
)

lazy val testingNativeSettings = Seq(
  libraryDependencies += "org.scala-native" %% "junit-runtime_native0.4" % "0.4.12",
  addCompilerPlugin("org.scala-native" % "junit-plugin" % "0.4.12" cross CrossVersion.full),
  // nativeCheck := true,
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")
)

lazy val catsDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core" % catsVersion
  )
)

lazy val catsExtendedDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "kittens" % kittensVersion
  )
)

lazy val catsEffectOverrideDependencies = Seq(
  dependencyOverrides ++= Seq(
    "org.typelevel" %%% "cats-effect" % catsEffectVersion
  )
)
lazy val catsEffect3_4_0OverrideDependencies = Seq(
  dependencyOverrides ++= Seq(
    "org.typelevel" %%% "cats-effect" % "3.4.0"
  )
)

lazy val catsEffectNativeDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.armanbilge" %%% "epollcat" % "0.1.4",
    "org.http4s" %%% "http4s-curl" % "0.2.0"
  )
)

lazy val circeDependencies = Seq(
  libraryDependencies ++= Seq(
    "io.circe" %%% "circe-core" % circeVersion,
    "io.circe" %%% "circe-parser" % circeVersion
  )
)

lazy val fs2Dependencies = Seq(
  libraryDependencies ++= Seq(
    "co.fs2" %%% "fs2-core" % fs2Version
  )
)

lazy val http4sDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.http4s" %%% "http4s-ember-server" % "0.23.19",
    "org.http4s" %%% "http4s-ember-client" % "0.23.19",
    "org.http4s" %%% "http4s-circe" % "0.23.19",
    "org.http4s" %%% "http4s-dsl" % "0.23.19"
  )
)

lazy val http4sNettyDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-netty-server" % "0.5.7",
    "org.http4s" %% "http4s-netty-client" % "0.5.7"
  )
)

lazy val loggingDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "log4cats-slf4j" % "2.2.0",
    "org.typelevel" %%% "log4cats-natchez" % "0.2.0",
    "org.slf4j" % "slf4j-simple" % "1.7.36",
    "ch.qos.logback" % "logback-classic" % "1.4.5",
    "ch.qos.logback" % "logback-core" % "1.4.5"
  )
)

lazy val skunkDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.tpolecat" %%% "skunk-core" % skunkVersion
  )
)

lazy val talibDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.tictactec" % "ta-lib" % "0.4.0"
  )
)

lazy val root = tlCrossRootProject
  .aggregate(
    talibCore.native,
    talibCoreTests.native,
    signalsLib.native,
    talibStreamFs2.native,
    tradingDomain.native,
    tradingPersistenceSkunk.native,
    exampleSkunkApp.native
  )
  .enablePlugins(NoPublishPlugin)

lazy val talibCore = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("lib/talib-core"))
  .settings(moduleName := "talib-core", name := "Talib core")
  .settings(sharedSettings)
  .jvmSettings(talibDependencies)

lazy val commonNativeSettings = Seq(
  nativeLinkStubs := true,
  nativeConfig ~= { c =>
    c.withLTO(LTO.thin)
      .withMode(Mode.debug)
      .withGC(GC.boehm)
  },
  nativeLinkingOptions ++= Seq(
    sys.env.getOrElse("LD_LIBRARY_PATH", "")
  ).filter(_.nonEmpty).map("-L" + _) ++ Seq(
    sys.env.getOrElse("LIBRARY_PATH", "")
  ).filter(_.nonEmpty).map("-L" + _)
)

lazy val talibCoreTests = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("lib/talib-core-tests"))
  .settings(moduleName := "talib-core-tests", name := "Talib core tests")
  .settings(sharedSettings)
  .dependsOn(talibCore)
  .jvmSettings(talibDependencies, testingJUnitSettings)
  .nativeSettings(commonNativeSettings, testingNativeSettings)

lazy val signalsLib = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("lib/signals"))
  .settings(moduleName := "signals-lib", name := "Signals library")
  .settings(sharedSettings)
  .settings(circeDependencies)
  .nativeSettings(commonNativeSettings)

lazy val talibStreamFs2 = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("lib/talib-fs2"))
  .settings(moduleName := "talib-streams-fs2", name := "Talib streams fs2")
  .settings(sharedSettings)
  .settings(fs2Dependencies)
  .dependsOn(talibCore, signalsLib)
  .nativeSettings(commonNativeSettings, testingNativeSettings)

lazy val talibStreamFs2Tests = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("lib/talib-fs2-tests"))
  .settings(moduleName := "talib-streams-fs2-tests", name := "Talib streams fs2 tests")
  .settings(sharedSettings)
  .settings(fs2Dependencies, testingJUnitSettings)
  .dependsOn(talibStreamFs2)
  .nativeSettings(commonNativeSettings, testingNativeSettings)


lazy val tradingDomain = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("domain/trading"))
  .settings(moduleName := "trading-domain", name := "Trading domain")
  .settings(sharedSettings)
  .dependsOn(talibCore, signalsLib)
  .nativeSettings(commonNativeSettings)
  .enablePlugins(NoPublishPlugin)

lazy val tradingPersistenceInMemory = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("infrastructure/trading-inmem"))
  .settings(moduleName := "trading-persistence-in-memory", name := "Trading persistence in memory")
  .settings(sharedSettings)
  .settings(loggingDependencies, catsEffectOverrideDependencies)
  .dependsOn(talibCore, talibStreamFs2, tradingDomain)
  .nativeSettings(commonNativeSettings)

lazy val tradingPersistenceSkunk = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("infrastructure/trading-skunk"))
  .settings(moduleName := "trading-persistence-skunk", name := "Trading persistence skunk")
  .settings(sharedSettings)
  .settings(skunkDependencies, loggingDependencies, catsEffectOverrideDependencies)
  .dependsOn(talibCore, talibStreamFs2, tradingDomain)
  .nativeSettings(commonNativeSettings)
  .enablePlugins(NoPublishPlugin)

lazy val exampleSkunkApp = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("examples/random-skunk-app"))
  .settings(moduleName := "random-skunk-app", name := "Random skunk app")
  .settings(sharedSettings)
  .settings(loggingDependencies, http4sDependencies, catsEffectOverrideDependencies)
  .dependsOn(tradingDomain, tradingPersistenceSkunk)
  .nativeSettings(catsEffectNativeDependencies, commonNativeSettings)
  .enablePlugins(NoPublishPlugin)

lazy val exampleWebsocketApp = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("examples/http4s-websocket-app"))
  .settings(moduleName := "http4s-websocket-app", name := "Http4s websocket app")
  .settings(sharedSettings)
  .settings(loggingDependencies, http4sDependencies, catsEffect3_4_0OverrideDependencies)
  .dependsOn(tradingDomain, tradingPersistenceInMemory)
  .jvmSettings(http4sNettyDependencies)
  .nativeSettings(catsEffectNativeDependencies, commonNativeSettings)
  .enablePlugins(NoPublishPlugin)
