package com.yixiang.wlyx.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration("file:src/main/resources/spring-bean.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DictionaryDaoTest {

  @Autowired
  private DictionaryDao dictionaryDao;

  @Autowired
  private StringRedisTemplate redisTemplate;

  @After
  public void tearDown() {
    redisTemplate.getConnectionFactory().getConnection().flushDb();
  }

  @Test
  public void testAddWordWithItsMeaningToDictionary() {
    String meaning = "To move forward with a bounding, drooping motion.";
    Long index = dictionaryDao.addWordWithItsMeaningToDictionary("lollop", meaning);
    assertThat(index, is(notNullValue()));
    assertThat(index, is(equalTo(1L)));
  }

  @Test
  public void shouldAddMeaningToAWordIfItExists() {
    Long index = dictionaryDao.addWordWithItsMeaningToDictionary("lollop",
        "To move forward with a bounding, drooping motion.");
    assertThat(index, is(notNullValue()));
    assertThat(index, is(equalTo(1L)));
    index = dictionaryDao.addWordWithItsMeaningToDictionary("lollop",
        "To hang loosely; droop; dangle.");
    assertThat(index, is(equalTo(2L)));
  }

  @Test
  public void shouldGetAllTheMeaningForAWord() {
    setupOneWord();
    List<String> allMeanings = dictionaryDao.getAllTheMeaningsForAWord("lollop");
    assertThat(allMeanings.size(), is(equalTo(2)));
    assertThat(
        allMeanings,
        hasItems("To move forward with a bounding, drooping motion.",
            "To hang loosely; droop; dangle."));
  }

  private Matcher<? super List<String>> hasItems(String string, String string2) {
    // TODO Auto-generated method stub
    return null;
  }

  private void setupOneWord() {
    // TODO Auto-generated method stub

  }

  @Test
  public void shouldDeleteAWordFromDictionary() throws Exception {
    setupOneWord();
    dictionaryDao.removeWord("lollop");
    List<String> allMeanings = dictionaryDao.getAllTheMeaningsForAWord("lollop");
    assertThat(allMeanings.size(), is(equalTo(0)));
  }

  @Test
  public void shouldDeleteMultipleWordsFromDictionary() {
    setupTwoWords();
    dictionaryDao.removeWords("fain", "lollop");
    List<String> allMeaningsForLollop = dictionaryDao.getAllTheMeaningsForAWord("lollop");
    List<String> allMeaningsForFain = dictionaryDao.getAllTheMeaningsForAWord("fain");
    assertThat(allMeaningsForLollop.size(), is(equalTo(0)));
    assertThat(allMeaningsForFain.size(), is(equalTo(0)));
  }

  private void setupTwoWords() {
    // TODO Auto-generated method stub

  }
}