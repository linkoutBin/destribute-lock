package com.example.demo.lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Author: xingshulin Date: 2019/4/16 下午11:29
 *
 *
 * Description: zookeeper锁 Version: 1.0
 **/
public class ZkLock implements Lock {

  private static Logger log = LoggerFactory.getLogger(ZkLock.class);

  private static final String PATH_SEPARATOR = "/";

  private String lockPath;

  private ZkClient client;

  private String currentPath;

  private String prevPath;

  public ZkLock(String serverString, String lockPath) {
    this.lockPath = lockPath;
    this.client = new ZkClient(serverString);
    this.client.setZkSerializer(new SerializableSerializer());
    if (!this.client.exists(lockPath)) {
      try {
        this.client.createPersistent(lockPath);
      } catch (ZkNodeExistsException e) {
      }
    }
  }


  @Override
  public void lock() {
    if (!tryLock()) {
      waitForLock();
      lock();
    }
  }

  @Override
  public boolean tryLock() {
    if (null == currentPath) {
      currentPath = this.client.createEphemeralSequential(lockPath + PATH_SEPARATOR, "data");
    }
    //获取子节点
    List<String> children = this.client.getChildren(lockPath);
    Collections.sort(children);
    if (currentPath.equals(lockPath + PATH_SEPARATOR + children.get(0))) {
      return true;
    } else {
      int curIndex = children.indexOf(currentPath.substring(lockPath.length() + 1));
      prevPath = lockPath + PATH_SEPARATOR + children.get(curIndex - 1);
    }

    return false;
  }

  private void waitForLock() {
    CountDownLatch cdl = new CountDownLatch(1);
    IZkDataListener listener = new IZkDataListener() {
      @Override
      public void handleDataChange(String s, Object o) throws Exception {
      }

      @Override
      public void handleDataDeleted(String s) throws Exception {
        log.info("--------节点删除---------");
        cdl.countDown();
      }
    };
    client.subscribeDataChanges(prevPath, listener);
    if (client.exists(prevPath)) {
      try {
        cdl.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    client.unsubscribeDataChanges(prevPath, listener);
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    return false;
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
  }

  @Override
  public void unlock() {
    client.delete(currentPath);
  }

  @Override
  public Condition newCondition() {
    return null;
  }
}
