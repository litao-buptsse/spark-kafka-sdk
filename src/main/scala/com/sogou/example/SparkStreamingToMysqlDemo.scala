package com.sogou.example

import java.sql.{Connection, DriverManager, Statement}

import com.sogou.spark.streaming.processor.LineProcessor

/**
 * Created by Tao Li on 8/20/15.
 */
class SparkStreamingToMysqlDemo extends LineProcessor {
  private val IP = "localhost"
  private val PORT = 3306
  private val DATABASE = "test"
  private val USERNAME = "root"
  private val PASSWORD = "123456"
  private val CONNECT_URL = s"jdbc:mysql://$IP:$PORT/$DATABASE?user=$USERNAME&password=$PASSWORD"
  private val DRIVER = "com.mysql.jdbc.Driver"

  private var connOpt: Option[Connection] = None
  private var stmtOpt: Option[Statement] = None

  override def init(): Unit = {
    Class.forName(DRIVER)
    connOpt = Some(DriverManager.getConnection(CONNECT_URL))
    stmtOpt = Some(connOpt.get.createStatement)
  }

  override def process(message: String): Unit = {
    val arr = message.split("\t")
    val name = arr(0)
    val age = arr(1)
    val sql = s"INSERT INTO student(name, age) VALUES('$name', '$age')"
    stmtOpt.get.execute(sql)
  }

  override def close(): Unit = {
    if (stmtOpt.isDefined) {
      stmtOpt.get.close()
    }
    if (connOpt.isDefined) {
      connOpt.get.close()
    }
  }
}
