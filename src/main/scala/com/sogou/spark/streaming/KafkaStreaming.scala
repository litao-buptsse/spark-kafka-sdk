package com.sogou.spark.streaming

import com.sogou.common.driver.Driver
import com.sogou.common.util.Utils._
import com.sogou.kafka.serializer.AvroFlumeEventBodyDecoder
import com.sogou.spark.streaming.processor.RDDProcessor
import com.typesafe.config.{Config, ConfigFactory}
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.slf4j.LoggerFactory

/**
 * Created by Tao Li on 2015/8/19.
 */
class KafkaStreamingSettings(config: Config) extends Serializable {
  config.checkValid(ConfigFactory.defaultReference(), "kafka", "flume", "spark", "app")

  val SPARK_APP_NAME = config.getString("spark.app.name")

  val STREAMING_BATCH_DURATION_SECONDS = config.getLong("spark.streaming.batchDurationSeconds")

  val KAFKA_ZOOKEEPER_QUORUM = config.getString("kafka.zookeeperQuorum")
  val KAFKA_TOPICS = config.getString("kafka.topics")
  val KAFKA_CONSUMER_GROUP = config.getString("kafka.consumerGroup")
  val KAFKA_CONSUMER_THREAD_NUM = config.getInt("kafka.consumerThreadNum")
  // TODO should support auto load all kafka properties which start with "kafka."
  val KAFKA_FETCH_MESSAGE_MAX_BYTES = config.getString("kafka.fetch.message.max.bytes")

  val FLUME_PARSE_AS_FLUME_EVENT = config.getBoolean("flume.parseAsFlumeEvent")
  val FLUME_INPUT_CHARSET = config.getString("flume.inputCharset")

  val PROCESSOR_CLASS = config.getString("app.kafka-streaming.processor.class")
}

class KafkaStreamingDriver(settings: KafkaStreamingSettings)
  extends Driver with Serializable {
  private val LOG = LoggerFactory.getLogger(getClass)

  private val batchDuration = Seconds(settings.STREAMING_BATCH_DURATION_SECONDS)
  private val kafkaParams = Map[String, String](
    "zookeeper.connect" -> settings.KAFKA_ZOOKEEPER_QUORUM,
    "group.id" -> settings.KAFKA_CONSUMER_GROUP,
    "fetch.message.max.bytes" -> settings.KAFKA_FETCH_MESSAGE_MAX_BYTES,
    "flume.event.body.serializer.encoding" -> settings.FLUME_INPUT_CHARSET
  )
  private val topicMap = settings.KAFKA_TOPICS.split(",").
    map((_, settings.KAFKA_CONSUMER_THREAD_NUM)).toMap
  private val processor = Class.forName(settings.PROCESSOR_CLASS).
    newInstance.asInstanceOf[RDDProcessor]

  private var sscOpt: Option[StreamingContext] = None

  override def start = {
    val conf = new SparkConf().
      setAppName(settings.SPARK_APP_NAME).set("spark.scheduler.mode", "FAIR")
    val sscOpt = Some(new StreamingContext(conf, batchDuration))

    val inputStream = KafkaUtils.createStream[
      String, String, StringDecoder, AvroFlumeEventBodyDecoder](
        sscOpt.get, kafkaParams, topicMap, StorageLevel.MEMORY_ONLY)
    inputStream.map(_._2).foreachRDD(rdd => processor.process(rdd))

    sscOpt.get.start
    sscOpt.get.awaitTermination
  }

  override def stop = {
    try {
      if (sscOpt.isDefined) sscOpt.get.stop(true, true)
    } catch {
      case e: Exception => LOG.error(getStackTraceStr(e))
    }
  }
}

object KafkaStreaming {

  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val driver = new KafkaStreamingDriver(new KafkaStreamingSettings(config))

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run = driver.stop
    }))

    driver.start
  }
}