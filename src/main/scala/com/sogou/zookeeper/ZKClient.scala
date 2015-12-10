package com.sogou.zookeeper

import java.util.concurrent.{TimeUnit, CountDownLatch}

import org.apache.zookeeper.Watcher.Event
import org.apache.zookeeper._
import org.slf4j.LoggerFactory

/**
 * Created by Tao Li on 2015/12/10.
 */
class ZKClient(connectString: String, sessionTimeout: Int) {
  private val LOG = LoggerFactory.getLogger(getClass)

  // a CountDownLatch initialized with 1 and used to wait zk connection
  private val connectedSignal = new CountDownLatch(1)

  private class ZKWatcher extends Watcher {
    override def process(event: WatchedEvent): Unit = {
      LOG.debug(s"Receive event: ${event.getType()}, ${event.getState()}")
      if (event.getState() == Event.KeeperState.SyncConnected) {
        connectedSignal.countDown()
      }
    }
  }

  private val watcher = new ZKWatcher()
  private val zk = new ZooKeeper(connectString, sessionTimeout, watcher)

  if (connectedSignal.await(10000, TimeUnit.MILLISECONDS)) {
    LOG.info("Zookeeper connection create succeed.")
  } else {
    LOG.error("Zookeeper connection create failed.")
    throw new ZKClientException("Fail to create zookeeper connection.")
  }

  def createPath(path: String, createMode: CreateMode) {
    if (zk.exists(path, false) == null) {
      zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode)
    }
  }

  def createPersistentPath(path: String) = createPath(path, CreateMode.PERSISTENT)

  def createEphemeralPath(path: String) = createPath(path, CreateMode.EPHEMERAL)
}
