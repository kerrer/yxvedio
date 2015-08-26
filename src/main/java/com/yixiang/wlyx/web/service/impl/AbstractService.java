/**
 * File		: AbstractService.java
 * Date		: 06-Apr-2012 
 * Owner	: arul
 * Project	: red5Demo
 * Contact	: http://arulraj.net
 * Description : 
 * History	:
 */
package com.yixiang.wlyx.web.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.yixiang.wlyx.utils.PropertiesUtil;

/**
 * @author arul
 *
 */
public class AbstractService {

  @Autowired
  ApplicationContext context;

  @Autowired
  PropertiesUtil properties;

}
