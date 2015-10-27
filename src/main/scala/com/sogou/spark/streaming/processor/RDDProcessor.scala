package com.sogou.spark.streaming.processor

import org.apache.spark.rdd.RDD

/**
 * Created by Tao Li on 2015/10/27.
 */
trait RDDProcessor extends Serializable {
  def process(rdd: RDD[String])
}
