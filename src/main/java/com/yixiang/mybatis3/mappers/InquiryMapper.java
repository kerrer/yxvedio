package com.yixiang.mybatis3.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.yixiang.wlyx.model.Inquiry;

public interface InquiryMapper {
  @Select("select * from wlyy_inquiry where inquiry_status=1")
  public List<Inquiry> findAllWaitting();

  @Update("update wlyy_inquiry set inquiry_status=4 where inquiry_id=#{inquireId}")
  public int abInquiry(int inquireId);
}
