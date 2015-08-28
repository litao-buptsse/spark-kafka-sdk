package com.sogou.kafka.consumer.processor

import kafka.message.MessageAndMetadata

/**
 * Created by Tao Li on 2015/8/28.
 */
trait KafkaMessageProcessor {
  def process(messageAndMetaData: MessageAndMetadata[Array[Byte], Array[Byte]])
}
