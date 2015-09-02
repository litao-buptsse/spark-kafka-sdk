package com.sogou.kafka.consumer

import java.util.Properties

import com.sogou.common.driver.Driver
import com.sogou.common.thread.DaemonThreadFactory
import com.sogou.common.util.Utils._
import com.sogou.kafka.consumer.processor.KafkaMessageProcessor
import com.typesafe.config.ConfigFactory
import kafka.consumer.{Consumer, ConsumerConfig, KafkaStream}
import org.slf4j.{Logger, LoggerFactory}

/**
 * Created by Tao Li on 2015/8/28.
 */
class KafkaConsumerConfiguration extends Serializable {
  private val config = ConfigFactory.load()

  val KAFKA_ZOOKEEPER_QUORUM = config.getString("kafka.zookeeperQuorum")
  val KAFKA_TOPICS = config.getString("kafka.topics")
  val KAFKA_CONSUMER_GROUP = config.getString("kafka.consumerGroup")
  val KAFKA_CONSUMER_THREAD_NUM = config.getInt("kafka.consumerThreadNum")

  val FLUME_PARSE_AS_FLUME_EVENT = config.getBoolean("flume.parseAsFlumeEvent")
  val FLUME_INPUT_CHARSET = config.getString("flume.inputCharset")

  val PROCESSOR_CLASS = config.getString("app.kafka-consumer.processor.class")
}

class KafkaConsumerDriver(config: KafkaConsumerConfiguration) extends Driver {
  private val LOG: Logger = LoggerFactory.getLogger(getClass)

  private val props = new Properties()
  props.put("zookeeper.connect", config.KAFKA_ZOOKEEPER_QUORUM)
  props.put("group.id", config.KAFKA_CONSUMER_GROUP)

  private val topicMap = config.KAFKA_TOPICS.split(",").
    map((_, config.KAFKA_CONSUMER_THREAD_NUM)).toMap

  private val processor = Class.forName(config.PROCESSOR_CLASS).
    newInstance.asInstanceOf[KafkaMessageProcessor]

  private val consumer = Consumer.create(new ConsumerConfig(props))

  override def start = {
    val messageStreams = consumer.createMessageStreams(topicMap)
    for ((topic, kafkaStreams) <- messageStreams) {
      for ((stream, index) <- kafkaStreams.zipWithIndex) {
        val consumerThread = new ConsumerThread(stream)
        new DaemonThreadFactory(s"ConsumerThread-$index").newThread(consumerThread).start()
      }
    }
  }

  override def stop = {
    consumer.commitOffsets
    consumer.shutdown()
  }

  private class ConsumerThread(stream: KafkaStream[Array[Byte], Array[Byte]]) extends Runnable {
    override def run(): Unit = {
      try {
        val it = stream.iterator
        while (it.hasNext()) {
          try {
            processor.process(it.next)
          } catch {
            case e: Exception => LOG.error(getStackTraceStr(e))
          }
        }
      } catch {
        case e: Exception => LOG.error(getStackTraceStr(e))
      }
    }
  }

}

object KafkaConsumer {
  def main(args: Array[String]) {
    val driver = new KafkaConsumerDriver(new KafkaConsumerConfiguration)

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run = driver.stop
    }))

    driver.start
  }
}
