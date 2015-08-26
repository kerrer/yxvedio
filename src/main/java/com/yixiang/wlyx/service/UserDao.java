package com.yixiang.wlyx.service;

import java.util.List;

import com.yixiang.wlyx.model.YxUser;

public interface UserDao {

  public int createUser(YxUser user);

  public int update(YxUser user);

  public int delete(String userName);

  public List selectAll();

  public int countAll();

  public YxUser findByUserName(String userName);

}
