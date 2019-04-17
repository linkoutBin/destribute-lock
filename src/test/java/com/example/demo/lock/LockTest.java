package com.example.demo.lock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.example.demo.service.BusinessService;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: xingshulin Date: 2019/4/16 下午10:52
 *
 *
 * Description: 锁测试 Version: 1.0
 **/
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class LockTest {

  @Autowired
  @Qualifier("myBusiness")
  private BusinessService businessService;

  @Test
  public void testRedisLock() {
    int threadNum = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
    CyclicBarrier cyclicBarrier = new CyclicBarrier(threadNum);
    while (threadNum > 0) {
      executorService.submit(() -> {
        try {
          cyclicBarrier.await();
          businessService.doBusinessLockByRedis("lock_test", 3000);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      threadNum--;
    }
    try {
      log.info("等待子线程处理完成");
      executorService.shutdown();
      executorService.awaitTermination(30000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testZkLock() {
    int threadNum = 50;
    ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
    CountDownLatch countDownLatch = new CountDownLatch(threadNum);
    for (int i = 0; i < 50; i++) {
      executorService.submit(() -> {
        try {
          countDownLatch.await();
          businessService.doBusinessLockByZk("localhost:2181", "/temp");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
      countDownLatch.countDown();
      threadNum--;
    }
    try {
      executorService.shutdown();
      executorService.awaitTermination(20000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
