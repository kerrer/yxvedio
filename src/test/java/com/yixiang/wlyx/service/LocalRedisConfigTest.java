package com.yixiang.wlyx.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.JedisShardInfo;

@ContextConfiguration("file:src/main/resources/spring-bean.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class LocalRedisConfigTest {

  @Autowired
  private JedisConnectionFactory jedisConnectionFactory;

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Autowired
  private JedisShardInfo jedisShardInfo;

  @Test
  public void testProperties() {
    assertEquals("192.168.56.16", jedisShardInfo.getHost());
  }

  @Test
  public void testJedisConnectionFactory() {
    assertNotNull(jedisConnectionFactory.getConnection());
  }

  @Test
  public void testRedisTemplate() {
    assertNotNull(redisTemplate);
  }

}