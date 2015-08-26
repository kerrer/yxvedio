package com.yixiang.wlyx.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yixiang.mybatis3.mappers.UserMapper;
import com.yixiang.wlyx.model.YxUser;

@Service("userService")
public class UserService {

  @Autowired
  private UserMapper userMapper;

  public void setUserMapper(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  public int createUser(YxUser user) {

    return this.userMapper.insert(user);
  }

  public int countAll() {
    return this.userMapper.countAll();
  }

  public int delete(String userName) {
    return this.userMapper.delete(userName);
  }

  public YxUser findByUserName(String userName) {
    return this.userMapper.findByUserName(userName);
  }

  public List<YxUser> selectAll() {
    return this.userMapper.selectAll();
  }

  public int update(YxUser user) {
    return this.userMapper.update(user);
  }

}