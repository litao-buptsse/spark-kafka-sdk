package com.sogou.kafka.serializer

import java.io.ByteArrayInputStream
import java.util.Collections

import com.sogou.common.util.Utils._
import kafka.utils.VerifiableProperties
import org.apache.avro.io.{BinaryDecoder, DecoderFactory}
import org.apache.avro.specific.SpecificDatumReader
import org.apache.flume.Event
import org.apache.flume.event.EventBuilder
import org.apache.flume.source.avro.AvroFlumeEvent

/**
 * Created by Tao Li on 2015/8/20.
 */
class AvroFlumeEventDecoder(props: VerifiableProperties = null)
  extends kafka.serializer.Decoder[Event] {
  private var reader: Option[SpecificDatumReader[AvroFlumeEvent]] = None
  private var decoder: ThreadLocal[BinaryDecoder] = new ThreadLocal[BinaryDecoder] {
    override def initialValue(): BinaryDecoder = null
  }

  override def fromBytes(bytes: Array[Byte]): Event = {
    val in = new ByteArrayInputStream(bytes)
    decoder.set(DecoderFactory.get.directBinaryDecoder(in, decoder.get))
    if (!reader.isDefined) {
      reader = Some(new SpecificDatumReader[AvroFlumeEvent](classOf[AvroFlumeEvent]))
    }
    val event: AvroFlumeEvent = reader.get.read(null, decoder.get)
    EventBuilder.withBody(event.getBody.array, toStringJavaMap(event.getHeaders))
  }
}

class SimpleFlumeEventDecoder(props: VerifiableProperties = null)
  extends kafka.serializer.Decoder[Event] {
  override def fromBytes(bytes: Array[Byte]): Event = {
    EventBuilder.withBody(bytes, Collections.emptyMap[String, String])
  }
}