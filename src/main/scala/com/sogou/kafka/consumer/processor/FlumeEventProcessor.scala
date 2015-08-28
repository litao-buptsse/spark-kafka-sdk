package com.sogou.kafka.consumer.processor

import com.sogou.kafka.serializer.AvroFlumeEventDecoder
import kafka.message.MessageAndMetadata
import org.apache.flume.Event

/**
 * Created by Tao Li on 8/28/15.
 */
trait FlumeEventProcessor extends KafkaMessageProcessor {
  private val decoder = new AvroFlumeEventDecoder()

  override def process(messageAndMetaData: MessageAndMetadata[Array[Byte], Array[Byte]]) = {
    val event = decoder.fromBytes(messageAndMetaData.message)
    processEvent(event)
  }

  def processEvent(event: Event)
}
