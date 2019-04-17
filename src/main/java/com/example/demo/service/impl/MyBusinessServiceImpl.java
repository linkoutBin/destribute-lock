package com.example.demo.service.impl;

import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.springframework.stereotype.Service;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: xingshulin Date: 2019/4/16 下午10:34
 *
 *
 * Description: 业务类 Version: 1.0
 **/
@Service("myBusiness")
@Slf4j
public class MyBusinessServiceImpl extends BusinessServiceImpl {

  private static int count = 100;

  @Override
  protected void doBusiness() {
    //reduceWarehouse();
    if (count >= 0) {
      count--;
      log.info("{}:库存：{}", Thread.currentThread().getName(), count);
    } else {
      log.info("库存不足");
    }
    log.info("业务处理完成");

  }

  private void reduceWarehouse() {
    log.info("业务处理开始");
    String warehouse = "warehouse";
    String command = "local value = redis.call('get',KEYS[1]); if (tonumber(value) > 0) then local result = redis.call('decr',KEYS[1]); return result;end;return 0";
    Long result = getRedissonClient().getScript()
        .eval(Mode.READ_WRITE, command, ReturnType.INTEGER, Collections.singletonList(warehouse));
    if (result >= 0) {
      log.info("{}:库存：{}", Thread.currentThread().getName(), result);
    } else {
      log.info("库存不足");
    }
    log.info("业务处理完成");
  }
}
