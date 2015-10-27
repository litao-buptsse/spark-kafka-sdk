package com.sogou.spark.streaming.processor

import org.apache.spark.rdd.RDD

/**
 * Created by Tao Li on 2015/8/28.
 */
trait LineProcessor extends RDDProcessor {
  def init()

  def process(message: String)

  def close()

  override def process(rdd: RDD[String]): Unit = {
    rdd.foreachPartition { iter =>
      this.init()
      iter.foreach(message => this.process(message))
      this.close()
    }
  }
}
