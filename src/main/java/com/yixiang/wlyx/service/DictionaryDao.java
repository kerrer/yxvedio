package com.yixiang.wlyx.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DictionaryDao {

  private StringRedisTemplate redisTemplate;

  @Autowired
  public DictionaryDao(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public Long addWordWithItsMeaningToDictionary(String word, String meaning) {
    Long index = redisTemplate.opsForList().rightPush(word, meaning);
    return index;
  }

  public List<String> getAllTheMeaningsForAWord(String word) {
    List<String> meanings = redisTemplate.opsForList().range(word, 0, -1);
    return meanings;
  }

  public void removeWord(String word) {
    redisTemplate.delete(Arrays.asList(word));
  }

  public void removeWords(String... words) {
    redisTemplate.delete(Arrays.asList(words));
  }
}