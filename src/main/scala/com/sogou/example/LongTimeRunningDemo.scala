package com.sogou.example

import com.sogou.spark.streaming.processor.LineProcessor

/**
 * Created by Tao Li on 2015/12/10.
 */
class LongTimeRunningDemo extends LineProcessor {
  override def init(): Unit = {
    println("init...")
  }

  override def process(message: String): Unit = {
    println(s"process: $message")
    Thread.sleep(10 * 1000)
  }

  override def close(): Unit = {
    println("close...")
  }
}