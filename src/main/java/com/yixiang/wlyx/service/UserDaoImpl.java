package com.yixiang.wlyx.service;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yixiang.mybatis3.mappers.UserMapper;
import com.yixiang.wlyx.model.YxUser;

@Service("userDaoService")
public class UserDaoImpl implements UserDao {

  @Autowired
  private SqlSession sqlSession;

  public void setSqlSession(SqlSession session) {
    this.sqlSession = session;
  }

  public int createUser(YxUser user) {
    UserMapper mapper = this.sqlSession.getMapper(UserMapper.class);
    return mapper.insert(user);
  }

  public int countAll() {
    UserMapper mapper = this.sqlSession.getMapper(UserMapper.class);
    return mapper.countAll();
  }

  public int delete(String userName) {
    UserMapper mapper = this.sqlSession.getMapper(UserMapper.class);
    return mapper.delete(userName);
  }

  public YxUser findByUserName(String userName) {
    UserMapper mapper = this.sqlSession.getMapper(UserMapper.class);
    return mapper.findByUserName(userName);
  }

  public List<YxUser> selectAll() {
    UserMapper mapper = this.sqlSession.getMapper(UserMapper.class);
    return mapper.selectAll();
  }

  public int update(YxUser user) {
    UserMapper mapper = this.sqlSession.getMapper(UserMapper.class);
    return mapper.update(user);
  }

}