package com.sogou.spark.streaming

import com.typesafe.config.{Config, ConfigFactory, ConfigValue}

/**
 * Created by Tao Li on 9/30/15.
 */
class SparkStreamingSettings(config: Config) extends Serializable {
  config.checkValid(ConfigFactory.defaultReference(), "root")

  val KAFKA_ZOOKEEPER_QUORUM = config.getString("root.kafka.zookeeperQuorum")
  val KAFKA_BROKER_LIST = config.getString("root.kafka.brokerList")
  val KAFKA_TOPICS = config.getString("root.kafka.topics")
  val KAFKA_CONSUMER_GROUP = config.getString("root.kafka.consumerGroup")
  val KAFKA_CONSUMER_THREAD_NUM = config.getInt("root.kafka.consumerThreadNum")

  val KAFKA_SESSION_TIMEOUT = config.getInt("root.kafka.zookeeperSessionTimeout")
  val KAFKA_CONNECTION_TIMEOUT = config.getInt("root.kafka.zookeeperConnectionTimeout")
  val KAFKA_OFFSETS_COMMIT_BATCH_INTERVAL = config.getInt("root.kafka.offsetsCommitBatchInterval")

  val SPARK_APP_NAME = config.getString("root.spark.appName")
  val BATCH_DURATION_SECONDS = config.getLong("root.spark.streaming.batchDurationSeconds")

  val PROCESSOR_CLASS = config.getString("root.app.processor.class")

  import scala.collection.JavaConversions._

  val kafkaConfigMap = if (config.hasPath("root.kafka.passthrough")) {
    config.getConfig("root.kafka.passthrough").root map { case (key: String, cv: ConfigValue) =>
      (key, cv.atPath(key).getString(key))
    }
  } else {
    Map.empty[String, String]
  }

  val sparkConfigMap = if (config.hasPath("root.spark.passthrough")) {
    config.getConfig("root.spark.passthrough").root map { case (key: String, cv: ConfigValue) =>
      (key, cv.atPath(key).getString(key))
    }
  } else {
    Map.empty[String, String]
  }
}
