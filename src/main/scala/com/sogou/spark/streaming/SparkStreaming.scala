package com.sogou.spark.streaming

import com.sogou.common.driver.Driver
import com.sogou.common.util.Utils._
import com.sogou.kafka.serializer.AvroFlumeEventBodyDecoder
import com.sogou.spark.streaming.processor.RDDProcessor
import com.typesafe.config.ConfigFactory
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.slf4j.LoggerFactory

/**
 * Created by Tao Li on 2015/8/19.
 */
class SparkStreamingDriver(settings: SparkStreamingSettings)
  extends Driver with Serializable {
  private val LOG = LoggerFactory.getLogger(getClass)

  private val batchDuration = Seconds(settings.BATCH_DURATION_SECONDS)
  private val kafkaParams = Map[String, String](
    "zookeeper.connect" -> settings.KAFKA_ZOOKEEPER_QUORUM,
    "group.id" -> settings.KAFKA_CONSUMER_GROUP
  ) ++ settings.kafkaConfigMap
  private val topicMap = settings.KAFKA_TOPICS.split(",").
    map((_, settings.KAFKA_CONSUMER_THREAD_NUM)).toMap
  private val processor = Class.forName(settings.PROCESSOR_CLASS).
    newInstance.asInstanceOf[RDDProcessor]

  private var sscOpt: Option[StreamingContext] = None

  override def start = {
    val conf = new SparkConf().
      setAppName(settings.SPARK_APP_NAME).set("spark.scheduler.mode", "FAIR")
    for ((k, v) <- settings.sparkConfigMap) conf.set(k, v)
    sscOpt = Some(new StreamingContext(conf, batchDuration))

    val inputStream = KafkaUtils.createStream[
      String, String, StringDecoder, AvroFlumeEventBodyDecoder](
        sscOpt.get, kafkaParams, topicMap, StorageLevel.MEMORY_AND_DISK_SER)
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

object SparkStreaming {

  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val driver = new SparkStreamingDriver(new SparkStreamingSettings(config))

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run = driver.stop
    }))

    driver.start
  }
}