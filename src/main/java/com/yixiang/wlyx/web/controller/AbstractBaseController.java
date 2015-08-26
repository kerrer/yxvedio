/**
 * File		: AbstractBaseController.java
 * Date		: 06-Dec-2011 
 * Owner	: arul
 * Project	: red5Demo
 * Contact	: http://arulraj.net
 * Description : 
 * History	:
 */
package com.yixiang.wlyx.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.yixiang.wlyx.utils.ChatConstants;

/**
 * @author arul
 *
 */
public class AbstractBaseController implements HandlerExceptionResolver {

  private final static Logger LOG = Red5LoggerFactory.getLogger(AbstractBaseController.class,
      ChatConstants.APP_NAME);

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.web.servlet.HandlerExceptionResolvlivecontenter#resolveException(javax.
   * servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object,
   * java.lang.Exception)
   */
  @Override
  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception exception) {
    LOG.debug("#### in resolveException");

    ModelAndView viewData = new ModelAndView();

    String errorRefNumber = Long.toString(System.currentTimeMillis());
    request.setAttribute("errorRefNumber", errorRefNumber);
    viewData.addObject("exception", exception);
    LOG.error(exception.toString());
    LOG.error("### Error-Reference-Number " + errorRefNumber);

    String viewName = "errors.error";
    int status = HttpStatus.INTERNAL_SERVER_ERROR.value();

    if (exception instanceof IllegalArgumentException
    // || exception instanceof NoSuchDefinitionException
    ) {
      status = HttpStatus.NOT_FOUND.value();
      viewName = "errors.notfound";
    }
    response.setStatus(status);
    viewData.setViewName(viewName);

    return viewData;
  }
}
