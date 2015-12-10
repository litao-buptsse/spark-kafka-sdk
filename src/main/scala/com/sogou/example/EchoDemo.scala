package com.sogou.example

import com.sogou.spark.streaming.processor.LineProcessor

/**
 * Created by Tao Li on 2015/12/10.
 */
class EchoDemo extends LineProcessor {
  override def init(): Unit = {
    println("init...")
  }

  override def process(message: String): Unit = {
    println(s"process: $message")
  }

  override def close(): Unit = {
    println("close...")
  }
}