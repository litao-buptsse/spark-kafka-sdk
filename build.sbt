name := "spark-kafka-sdk"

version := "1.0"

scalaVersion := "2.10.4"

autoScalaLibrary := false

resolvers ++= Seq(
  "Maven Repository" at "http://repo1.maven.org/maven/",
  "Apache Repository" at "https://repository.apache.org/content/repositories/releases",
  "JBoss Repository" at "https://repository.jboss.org/nexus/content/repositories/releases",
  "MQTT Repository" at "https://repo.eclipse.org/content/repositories/paho-releases",
  "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos",
  "MapR Repository" at "http://repository.mapr.com/maven",
  "spring-releases" at "https://repo.spring.io/libs-release",
  "Job Server Bintray" at "http://dl.bintray.com/spark-jobserver/maven",
  "Sogou Maven Repository" at "http://cloud.sogou-inc.com/nexus/content/groups/public"
)

// TODO ugly solution to package the conf file to the jar
unmanagedResourceDirectories in Compile += baseDirectory.value / "conf"

unmanagedBase := baseDirectory.value / "lib"

libraryDependencies ++= {
  val sparkVersion = "1.4.0"
  val hbaseVersion = "0.98.13-hadoop2"
  val excludeSpark = ExclusionRule(organization = "org.apache.spark")
  val excludeHbase = ExclusionRule(organization = "org.apache.hbase")
  val excludeScalaLang = ExclusionRule(organization = "org.scala-lang")
  val excludeScalaTest = ExclusionRule(organization = "org.scalatest")
  val excludeNetty = ExclusionRule(organization = "io.netty")
  val excludeThrift = ExclusionRule(organization = "org.apache.thrift")
  val excludeAvro = ExclusionRule(organization = "org.apache.avro")
  val excludeJson4s = ExclusionRule(organization = "org.json4s")
  val excludeSlf4j = ExclusionRule(organization = "org.slf4j")
  val excludeLog4j = ExclusionRule(organization = "log4j")
  val excludeSparkProject = ExclusionRule(organization = "org.spark-project.spark")
  Seq(
    "log4j" % "log4j" % "1.2.17" % "provided",
    "org.slf4j" % "slf4j-api" % "1.7.5" % "provided",
    "org.slf4j" % "slf4j-log4j12" % "1.7.5" % "provided",
    "com.typesafe" % "config" % "1.2.1" % "provided",
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-streaming-kafka" % sparkVersion
      excludeAll(excludeSparkProject, excludeScalaLang, excludeSlf4j, excludeLog4j),
    "org.apache.flume" % "flume-ng-sdk" % "1.6.0"
      excludeAll(excludeSlf4j, excludeThrift, excludeAvro, excludeNetty),
    "org.apache.hbase" % "hbase-common" % hbaseVersion % "provided",
    "org.apache.hbase" % "hbase-client" % hbaseVersion % "provided",
    "org.apache.hbase" % "hbase-server" % hbaseVersion % "provided",
    "eu.unicredit" %% "hbase-rdd" % "0.5.2"
      excludeAll(excludeSpark, excludeHbase, excludeScalaLang, excludeJson4s)
  )
}

mainClass in assembly := Some("com.sogou.spark.streaming.KafkaStreaming")
