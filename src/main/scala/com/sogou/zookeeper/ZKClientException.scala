package com.sogou.zookeeper

/**
 * Created by Tao Li on 2015/12/10.
 */
class ZKClientException private(e: Exception) extends Exception(e) {
  def this(message: String) = this(ZKClientException(message))

  def this(message: String, cause: Throwable) = this(ZKClientException(message, cause))
}

object ZKClientException {
  def apply(message: String) = new ZKClientException(message)

  def apply(message: String, cause: Throwable) = new ZKClientException(message, cause)
}