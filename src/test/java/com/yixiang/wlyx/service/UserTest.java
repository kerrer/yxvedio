package com.yixiang.wlyx.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yixiang.wlyx.model.YxUser;

@ContextConfiguration("file:src/main/resources/spring-bean.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class UserTest {

  @Autowired
  ApplicationContext context;

  @Test
  public void countAll() {
    UserService userService = (UserService) context.getBean("userService");
    System.out.println("数据库中的记录条数:" + userService.countAll());
  }

  @Test
  public void insert() {
    YxUser user = new YxUser();
    // user.setUsername("3333333");
    // user.setPassword("passtest");
    // user.setEmail("dennisit@163.com");
    // user.setSex("f");
    // user.setAge(23);
    UserService userService = (UserService) context.getBean("userService");
    userService.createUser(user);
  }

  @Test
  public void selectAll() {
    UserService userService = (UserService) context.getBean("userService");
    List<YxUser> list = userService.selectAll();
    for (int i = 0; i < list.size(); i++) {
      YxUser user = list.get(i);
      // System.out.println("用户名:" + user.getUsername() + "\t密码:" + user.getPassword() + "\t邮箱：" +
      // user.getEmail());
    }
  }

  @Test
  public void update() {
    YxUser user = new YxUser();
    // user.setUsername("苏若年");
    // user.setPassword("xxxxxxxx");
    // user.setEmail("xxxxxx@163xxx");
    // user.setSex("男");
    // user.setAge(23);
    UserService userService = (UserService) context.getBean("userService");
    userService.update(user);
  }

  @Test
  public void delete() {
    UserService userService = (UserService) context.getBean("userService");
    userService.delete("苏若年");
  }

  @Test
  public void findByName() {
    UserService userService = (UserService) context.getBean("userService");
    YxUser user = userService.findByUserName("苏若年");
    // System.out.println("用户名:" + user.getUsername() + "\t密码:" + user.getPassword() + "\t邮箱：" +
    // user.getEmail());

  }
}
