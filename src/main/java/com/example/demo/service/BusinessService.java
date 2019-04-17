package com.example.demo.service;

public interface BusinessService {

  void doBusinessLockByRedis(String lockName, long waitTime) throws InterruptedException;

  void doBusinessLockByZk(String serverString, String lockPath);

}
