package com.yixiang.mybatis3.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yixiang.wlyx.model.YxUser;

public interface UserMapper {
  @Select("select * from tb_user where id=#{id}")
  YxUser findUserById(Integer id);

  @Select(" select * from tb_user where username=#{username}")
  YxUser findByUserName(String username);

  @Select("select count(*) c from tb_user")
  int countAll();

  @Insert("insert into tb_user(username,password,email,sex,age) values(#{username},#{password},#{email},#{sex},#{age})")
  public int insert(YxUser user);

  @Update("update tb_user set username=#{username},password=#{password},email=#{email},sex=#{sex},age=#{age} where username=#{username}")
  public int update(YxUser user);

  @Delete("delete from tb_user where username=#{username}")
  public int delete(String userName);

  @Select("select * from tb_user ")
  public List<YxUser> selectAll();

}
