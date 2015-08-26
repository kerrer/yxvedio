package com.yixiang.wlyx.service;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yixiang.wlyx.model.Inquiry;

//f@ContextConfiguration("file:src/main/resources/spring-bean.xml")
@ContextConfiguration("classpath:/spring-bean.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class InquiryTest {

  @Autowired
  ApplicationContext context;

  @Test
  public void testMybastic() {
    SqlSessionTemplate sqlsession = (SqlSessionTemplate) context.getBean("sqlSession");
    assertNotNull("failure - parents is null", sqlsession);
  }

  @Test
  public void countAll() {
    InquiryService inquiryService = (InquiryService) context.getBean("inquiryService");
    List<Inquiry> inquiries = inquiryService.findAll();
    for (Inquiry inquiry : inquiries) {
      System.out.println("===============================dotor id " + inquiry.getPatient_id());

    }
  }
}
