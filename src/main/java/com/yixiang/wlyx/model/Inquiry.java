package com.yixiang.wlyx.model;

public class Inquiry {
  private int inquiry_id;
  private int patient_id;
  private int pharmacy_id;
  private int doctor_id;
  private int hospital_id;
  private int uid;
  private int inquiry_start_time;
  private int inquiry_add_time;
  private int sex;
  private int inquiry_status;
  private String inquiry_evaluation;

  public int getInquiry_id() {
    return inquiry_id;
  }

  public void setInquiry_id(int inquiry_id) {
    this.inquiry_id = inquiry_id;
  }

  public int getPatient_id() {
    return patient_id;
  }

  public void setPatient_id(int patient_id) {
    this.patient_id = patient_id;
  }

  public int getPharmacy_id() {
    return pharmacy_id;
  }

  public void setPharmacy_id(int pharmacy_id) {
    this.pharmacy_id = pharmacy_id;
  }

  public int getDoctor_id() {
    return doctor_id;
  }

  public void setDoctor_id(int doctor_id) {
    this.doctor_id = doctor_id;
  }

  public int getHospital_id() {
    return hospital_id;
  }

  public void setHospital_id(int hospital_id) {
    this.hospital_id = hospital_id;
  }

  public int getInquiry_status() {
    return inquiry_status;
  }

  public void setInquiry_status(int inquiry_status) {
    this.inquiry_status = inquiry_status;
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public int getInquiry_start_time() {
    return inquiry_start_time;
  }

  public void setInquiry_start_time(int inquiry_start_time) {
    this.inquiry_start_time = inquiry_start_time;
  }

  public int getInquiry_add_time() {
    return inquiry_add_time;
  }

  public void setInquiry_add_time(int inquiry_add_time) {
    this.inquiry_add_time = inquiry_add_time;
  }

  public int getSex() {
    return sex;
  }

  public void setSex(int sex) {
    this.sex = sex;
  }

  public String getInquiry_evaluation() {
    return inquiry_evaluation;
  }

  public void setInquiry_evaluation(String inquiry_evaluation) {
    this.inquiry_evaluation = inquiry_evaluation;
  }

}
