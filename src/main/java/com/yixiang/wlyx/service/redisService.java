package com.yixiang.wlyx.service;

import com.yixiang.wlyx.model.YxUser;

public interface redisService {
  String getMessage(String name);

  void addLink(String userId, String url);

  void useCallback();

  void set(String key, String value);

  String get(String key);

  void delete(String key);

  YxUser readSession(String uid);
}
