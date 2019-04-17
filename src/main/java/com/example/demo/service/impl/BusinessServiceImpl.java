package com.example.demo.service.impl;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.util.concurrent.TimeUnit;
import com.example.demo.exception.BusinessException;
import com.example.demo.lock.ZkLock;
import com.example.demo.service.BusinessService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: xingshulin Date: 2019/4/16 下午6:49
 *
 *
 * Description: 业务处理抽象类 Version: 1.0
 **/
@Service
@Primary
@Slf4j
@Getter
public abstract class BusinessServiceImpl implements BusinessService {

  @Autowired
  private RedissonClient redissonClient;

  @Override
  public void doBusinessLockByRedis(String lockName, long waitTime) throws InterruptedException {
    Assert.notNull(lockName, "锁名不能为空");
    Assert.isTrue(waitTime >= 0, "锁等待时间不能小于零");
    RLock lock = redissonClient.getLock(lockName);
    boolean isLock = lock.tryLock(waitTime, TimeUnit.MILLISECONDS);
    if (isLock) {
      log.info("线程：" + Thread.currentThread().getName() + "获取锁[{}]成功", lockName);
      try {
        doBusiness();
      } catch (Exception e) {
        throw new BusinessException("业务处理异常", e);
      } finally {
        lock.unlock();
      }
    } else {
      log.info("线程：" + Thread.currentThread().getName() + "未获取到锁[{}]", lockName);
    }
  }

  protected abstract void doBusiness();

  @Override
  public void doBusinessLockByZk(String serverString, String lockPath) {
    log.info(Thread.currentThread().getName() + ":start to reduce:" + System.currentTimeMillis());
    ZkLock zkLock = new ZkLock(serverString, lockPath);
    try {
      zkLock.lock();
      doBusiness();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      zkLock.unlock();
    }
    log.info(Thread.currentThread().getName() + ":reduce over");
  }
}
