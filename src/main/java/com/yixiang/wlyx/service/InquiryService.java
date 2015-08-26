package com.yixiang.wlyx.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yixiang.mybatis3.mappers.InquiryMapper;
import com.yixiang.wlyx.model.Inquiry;

@Service("inquiryService")
public class InquiryService {

  @Autowired
  private InquiryMapper inquiryMapper;

  public void setUserMapper(InquiryMapper inquiryMapper) {
    this.inquiryMapper = inquiryMapper;
  }

  public List<Inquiry> findAll() {
    return this.inquiryMapper.findAllWaitting();
  }

  public int abInquiry(int inquireId) {
    return this.inquiryMapper.abInquiry(inquireId);
  }
}