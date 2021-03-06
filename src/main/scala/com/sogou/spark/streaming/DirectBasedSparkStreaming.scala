package com.sogou.spark.streaming

import com.sogou.common.driver.Driver
import com.sogou.common.util.Utils._
import com.sogou.kafka.serializer.AvroFlumeEventBodyDecoder
import com.sogou.spark.streaming.processor.RDDProcessor
import com.typesafe.config.ConfigFactory
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.{ZKStringSerializer, ZkUtils}
import org.I0Itec.zkclient.ZkClient
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.slf4j.LoggerFactory

/**
 * Created by Tao Li on 2015/8/19.
 */
class DirectBasedSparkStreamingDriver(settings: SparkStreamingSettings)
  extends Driver with Serializable {
  private val LOG = LoggerFactory.getLogger(getClass)

  private val batchDuration = Seconds(settings.BATCH_DURATION_SECONDS)
  private val kafkaParams = Map[String, String](
    "zookeeper.connect" -> settings.KAFKA_ZOOKEEPER_QUORUM,
    "metadata.broker.list" -> settings.KAFKA_BROKER_LIST,
    "group.id" -> settings.KAFKA_CONSUMER_GROUP
  ) ++ settings.kafkaConfigMap
  private val topicSeq = settings.KAFKA_TOPICS.split(",").toSeq
  private val processor = Class.forName(settings.PROCESSOR_CLASS).
    newInstance.asInstanceOf[RDDProcessor]

  private var zkClientOpt: Option[ZkClient] = None
  private var sscOpt: Option[StreamingContext] = None

  override def start = {
    zkClientOpt = Some(new ZkClient(settings.KAFKA_ZOOKEEPER_QUORUM,
      settings.KAFKA_SESSION_TIMEOUT, settings.KAFKA_CONNECTION_TIMEOUT, ZKStringSerializer))

    val topicPartitionIds = ZkUtils.getPartitionsForTopics(zkClientOpt.get, topicSeq)

    def getConsumerOffsetPath(topic: String) = {
      s"/consumers/${settings.KAFKA_CONSUMER_GROUP}/offsets/$topic"
    }
    def getConsumerPartitionOffsetPath(topic: String, partitionId: Int) = {
      s"${getConsumerOffsetPath(topic)}/$partitionId"
    }

    // if consumer offsets not exist, set it with the latest
    topicSeq.foreach { topic =>
      val consumerOffsetPath = getConsumerOffsetPath(topic)
      if (!ZkUtils.pathExists(zkClientOpt.get, consumerOffsetPath)) {
        LOG.info(s"$consumerOffsetPath not exist, create it!")
        val latestOffsets = com.sogou.kafka.KafkaUtils.getLatestOffsets(
          settings.KAFKA_BROKER_LIST, topic)
        topicPartitionIds(topic).foreach { partitionId =>
          val latestOffset = latestOffsets(partitionId)
          val consumerPartitionOffsetPath = getConsumerPartitionOffsetPath(topic, partitionId)
          ZkUtils.updatePersistentPath(zkClientOpt.get,
            consumerPartitionOffsetPath, latestOffset.toString)
          LOG.info(s"init $consumerPartitionOffsetPath with offset $latestOffset")
        }
      }
    }

    // load the consumer offsets
    val fromOffsets = topicPartitionIds.flatMap { case (topic, partitionIds) =>
      partitionIds.map { partitionId => s"${topic}&${partitionId}"}
    }.map { topicPartitionId =>
      val arr = topicPartitionId.split("&")
      val topic = arr(0)
      val partitionId = arr(1).toInt
      (TopicAndPartition(topic, partitionId), ZkUtils.readData(zkClientOpt.get,
        getConsumerPartitionOffsetPath(topic, partitionId))._1.toLong)
    }.toMap
    LOG.info(s"fromOffsets: $fromOffsets")

    val conf = new SparkConf().
      setAppName(settings.SPARK_APP_NAME).set("spark.scheduler.mode", "FAIR")
    for ((k, v) <- settings.sparkConfigMap) conf.set(k, v)
    sscOpt = Some(new StreamingContext(conf, batchDuration))

    // create the inputStream from consumer offsets with direct api
    val inputStream = KafkaUtils.createDirectStream[
      String, String, StringDecoder, AvroFlumeEventBodyDecoder, String](
        sscOpt.get, kafkaParams, fromOffsets,
        (m: MessageAndMetadata[String, String]) => m.message()
      )

    var offsetRanges = Array[OffsetRange]()
    var unCommitBatchNum = 0

    inputStream.transform { rdd =>
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    }.foreachRDD { rdd =>
      processor.process(rdd)
      unCommitBatchNum += 1

      if (unCommitBatchNum >= settings.KAFKA_OFFSETS_COMMIT_BATCH_INTERVAL) {
        // update consumer offsets when batch complete
        for (o <- offsetRanges) {
            LOG.info(s"commit offset: ${o.topic}, ${o.partition}, ${o.untilOffset}")
          ZkUtils.updatePersistentPath(zkClientOpt.get,
            getConsumerPartitionOffsetPath(o.topic, o.partition), o.untilOffset.toString)
        }
        unCommitBatchNum = 0
      }
    }

    sscOpt.get.start
    sscOpt.get.awaitTermination
  }

  override def stop = {
    try {
      if (zkClientOpt.isDefined) zkClientOpt.get.close()
    } catch {
      case e: Exception => LOG.error(getStackTraceStr(e))
    }
    try {
      if (sscOpt.isDefined) sscOpt.get.stop(true, true)
    } catch {
      case e: Exception => LOG.error(getStackTraceStr(e))
    }
  }
}

object DirectBasedSparkStreaming {
  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val driver = new DirectBasedSparkStreamingDriver(new SparkStreamingSettings(config))

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run = driver.stop
    }))

    driver.start
  }
}
