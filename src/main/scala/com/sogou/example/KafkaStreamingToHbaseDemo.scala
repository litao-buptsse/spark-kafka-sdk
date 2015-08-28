package com.sogou.spark.streaming.demo

import com.sogou.spark.streaming.processor.RDDProcessor
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.rdd.RDD

/**
 * Created by Tao Li on 8/20/15.
 */
class KafkaStreamingToHbaseDemo extends RDDProcessor {

  override def process(rdd: RDD[String]) = {
    val table = "kafka-streaming-to-hbase-demo"

    val wordCount = rdd.map((_, 1L)).reduceByKey(_ + _)
    val toHbase: RDD[(String, Map[String, Map[String, (Long, Long)]])] = wordCount.map(x =>
      (x._1, Map("cf" -> Map("pv" ->(x._2, System.currentTimeMillis)))))

    import unicredit.spark.hbase._

    implicit val config = HBaseConfig()
    implicit val longWriter = new Writes[Long] {
      def write(data: Long) = Bytes.toBytes(data)
    }
    toHbase.toHBase(table)
  }
}
