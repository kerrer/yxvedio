package com.yixiang.wlyx.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

@ContextConfiguration("file:src/main/resources/spring-bean.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisTest {

  @Autowired
  protected ApplicationContext ac;

  public void testRedis() {
    Jedis jedis = new Jedis("192.168.56.16", 6379);
    jedis.set("max", "password");
    System.out.println("Connected to Redis");
  }

  @Test
  public void jedisTest() {

    Set<String> sentinels = new HashSet<String>();
    sentinels.add("192.168.56.41:26379");
    sentinels.add("192.168.56.42:26379");
    sentinels.add("192.168.56.43:26379");

    JedisSentinelPool sentinelPool = new JedisSentinelPool("mymaster", sentinels);

    Jedis jedis = sentinelPool.getResource();

    System.out.println("current Host:" + sentinelPool.getCurrentHostMaster());

    String key = "a";

    String cacheData = jedis.get(key);

    if (cacheData == null) {
      jedis.del(key);
    }

    jedis.set(key, "aaa");// 写入

    System.out.println(jedis.get(key));// 读取

    System.out.println("current Host:" + sentinelPool.getCurrentHostMaster());// down掉master，观察slave是否被提升为master

    jedis.set(key, "bbb");// 测试新master的写入

    System.out.println(jedis.get(key));// 观察读取是否正常

    sentinelPool.close();
    jedis.close();

  }

  @Test
  public void jedisTest2() {
    JedisSentinelPool sentinelPool = (JedisSentinelPool) ac.getBean("jedisSentinelPool");
    Jedis jedis = sentinelPool.getResource();
    System.out.println("current Host:" + sentinelPool.getCurrentHostMaster());

    String key = "a";

    String cacheData = jedis.get(key);

    if (cacheData == null) {
      jedis.del(key);
    }

    jedis.set(key, "aaa");// 写入

    System.out.println(jedis.get(key));// 读取

    System.out.println("current Host:" + sentinelPool.getCurrentHostMaster());// down掉master，观察slave是否被提升为master

    jedis.set(key, "bbb");// 测试新master的写入

    System.out.println(jedis.get(key));// 观察读取是否正常

    sentinelPool.close();
    jedis.close();
  }

  @Test
  public void testRedisConnect() {
    redisService redisService = (redisService) ac.getBean("redisService");
    redisService.useCallback();
  }

  @Test
  public void testRedisConnect3() {
    RedisTemplate redisTemplate = (RedisTemplate) ac.getBean("redisTemplate");
    System.out.print(redisTemplate.getClientList());
    assertNotNull("jedis not connect ", redisTemplate);
  }

  @Test
  public void testRedisConnect2() {

    redisService redisService = (redisService) ac.getBean("redisService");
    // First method execution using key="Josh", not cached
    System.out.println("message: " + redisService.getMessage("Josh"));

    // Second method execution using key="Josh", still not cached
    System.out.println("message: " + redisService.getMessage("Josh"));

    // First method execution using key="Joshua", not cached
    System.out.println("message: " + redisService.getMessage("Joshua"));

    // Second method execution using key="Joshua", cached
    System.out.println("message: " + redisService.getMessage("Joshua"));
  }

  @Test
  public void testRead() {
    redisService redisService = (redisService) ac.getBean("redisService");
    redisService.readSession("74f61esurhr47973sgf63kv4j7");
  }

  @Test
  public void testSet() {
    redisService redisService = (redisService) ac.getBean("redisService");
    redisService.set("USER/3333", "1");
    // System.out.print("===========================" + index.toString());
    // assertThat(index, is(notNullValue()));
    // assertThat(index, is(equalTo(1L)));
    String value = redisService.get("USER/3333");
    System.out.print("===========================" + value);
    assertThat(value, is(notNullValue()));
    assertThat(value, is(equalTo("1")));
  }

  @Test
  public void testDel() {
    redisService redisService = (redisService) ac.getBean("redisService");
    redisService.delete("USER/3333");
    String value = redisService.get("USER/3333");
    System.out.print("===========================" + value);
    // assertThat(value, is(nullValue()));
    assertThat(value, is(equalTo("")));
  }
}
