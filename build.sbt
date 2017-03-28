organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.+"
libraryDependencies += "tw.edu.ntu.csie" % "libsvm" % "3.17"
libraryDependencies ++= Seq(
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.2",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.2" classifier "models"
)
libraryDependencies += "com.sendgrid" % "sendgrid-java" % "2.2.2"
libraryDependencies += "io.netty" % "netty-all" % "4.0.33.Final"
libraryDependencies += "com.googlecode.json-simple" % "json-simple" % "1.1.1"


libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "io.spray"            %%  "spray-json"    % "1.3.2"
  )
}
libraryDependencies += "io.spray" % "spray-client_2.11" % "1.3.2"
libraryDependencies += "org.squeryl" % "squeryl_2.11" % "0.9.5-7"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "0.9.28"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
libraryDependencies += "org.scala-lang.modules" %% "scala-pickling" % "0.10.1"
libraryDependencies += "org.mongodb" %% "casbah" % "3.1.0"
Revolver.settings
