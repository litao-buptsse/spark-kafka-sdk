package com.sogou.example

import com.sogou.kafka.consumer.processor.FlumeEventProcessor
import org.apache.flume.Event
import org.slf4j.LoggerFactory

/**
 * Created by Tao Li on 2015/8/28.
 */
class KafkaConsumerToConsoleDemo extends FlumeEventProcessor {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def processEvent(event: Event) = {
    val body = new String(event.getBody)
    LOG.info(body)
  }
}
