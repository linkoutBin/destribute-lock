package com.example.demo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import com.example.demo.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: xingshulin Date: 2019/4/16 下午6:40
 *
 *
 * Description: redisson配置类 Version: 1.0
 **/
@Configuration
@Slf4j
public class RedissonConfigure {

  @Value("${redisson.config.filePath}")
  private String configFile;

  @Bean
  public Config config() {
    URL fileUrl = this.getClass().getClassLoader().getResource(configFile);
    File file;
    try {
      if (null != fileUrl && (file = new File(fileUrl.toURI())).exists()) {
        return Config.fromJSON(file);
      } else {
        throw new BusinessException("redisson配置文件不存在，无法实例化配置类");
      }
    } catch (URISyntaxException | IOException e) {
      throw new BusinessException("实例化redisson配置类失败", e);
    }
  }

  @Bean
  public RedissonClient redissonClient() {
    return Redisson.create(config());
  }

}
