package com.sogou

import com.typesafe.config.{Config, ConfigFactory, ConfigValue}

/**
 * Created by Tao Li on 9/30/15.
 */
class Settings(config: Config) extends Serializable {
  config.checkValid(ConfigFactory.defaultReference(), "root")

  val ALERT_SERVICE_ROOT_URL = config.getString("root.alertService.rootUrl")

  val METADATA_UPDATE_INTERVAL_SECONDS = config.getLong("root.realtimeAlert.metadataUpdateIntervalSeconds")
  val QUEUE_BUFFERING_MAX_MESSAGE = config.getInt("app.realtimeAlert.queueBufferingMaxMessages")

  val KAFKA_ZOOKEEPER_QUORUM = config.getString("root.kafka.zookeeperQuorum")
  val KAFKA_TOPICS = config.getString("root.kafka.topics")
  val KAFKA_CONSUMER_GROUP = config.getString("root.kafka.consumerGroup")
  val KAFKA_CONSUMER_THREAD_NUM = config.getInt("root.kafka.consumerThreadNum")

  val SPARK_APP_NAME = config.getString("root.spark.appName")
  val BATCH_DURATION_SECONDS = config.getLong("root.spark.streaming.batchDurationSeconds")

  val SPARK_STREAMING_PROCESSOR_CLASS = config.getString("root.sparkStreaming.processor.class")
  val KAFKA_CONSUMER_PROCESSOR_CLASS = config.getString("root.kafkaConsumer.processor.class")

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