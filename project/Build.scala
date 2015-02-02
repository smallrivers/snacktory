import sbt._ 
import Keys._

object Dependencies {
  val Junit = "junit" % "junit" % "4.8.1"
  val Jsoup = "org.jsoup" % "jsoup" % "1.7.2"
  val Slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.6"
  val Slf4jLog4j12 = "org.slf4j" % "slf4j-log4j12" % "1.6.6" 
  val CommonsLang = "commons-lang" % "commons-lang" % "2.6"
  val Log4j = "log4j" % "log4j" % "1.2.14"
}

object SnacktoryBuild extends Build {
  import Dependencies._

  lazy val root = Project("snacktory", file("."), 
                    settings = Defaults.defaultSettings ++ 
                    Seq(libraryDependencies ++= Seq(Junit, Jsoup, Slf4jApi, Slf4jLog4j12, CommonsLang, Log4j)))
}