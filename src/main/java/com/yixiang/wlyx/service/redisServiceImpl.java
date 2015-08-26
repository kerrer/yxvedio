package com.yixiang.wlyx.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.github.ooxi.phparser.SerializedPhpParser;
import com.github.ooxi.phparser.SerializedPhpParserException;
import com.yixiang.wlyx.model.YxUser;

@Service("redisService")
public class redisServiceImpl implements redisService {

  // inject the actual template
  @Autowired
  private RedisTemplate<String, String> template; // inject the template as ListOperations

  @Resource(name = "redisTemplate")
  private ListOperations<String, String> listOps;

  @Autowired
  private StringRedisTemplate redisTemplate;

  /**
   * Using SpEL for conditional caching - only cache method executions when the name is equal to
   * "Joshua"
   */
  @Cacheable(value = "messageCache", condition = "'Joshua'.equals(#name)")
  // @CachePut(value = "user", key = "#name")
  public String getMessage(String name) {
    System.out.println("Executing HelloServiceImpl" + ".getHelloMessage(\"" + name + "\")");

    return "Hello " + name + "!";
  }

  public void addLink(String userId, String url) {
    System.out.println("here is");
    System.out.println(template.getClientList().toArray().toString());
    listOps.leftPush(userId, url);
  }

  public void useCallback() {

    redisTemplate.execute(new RedisCallback<Object>() {
      public Object doInRedis(RedisConnection connection) throws DataAccessException {
        Long size = connection.dbSize();
        // Can cast to StringRedisConnection if using a StringRedisTemplate
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("this is 1", "this is 1");
        hashMap.put("this is 2", "this is 2");
        hashMap.put("this is 3", "this is 3");
        ((StringRedisConnection) connection).set("key", "value");
        return true;
      }
    });
  }

  @Override
  public YxUser readSession(final String session_id) {
    return redisTemplate.execute(new RedisCallback<YxUser>() {
      @Override
      public YxUser doInRedis(RedisConnection connection) throws DataAccessException {
        byte[] key = redisTemplate.getStringSerializer().serialize("sessions/" + session_id);
        if (connection.exists(key)) {
          byte[] value = connection.get(key);
          String session_str = redisTemplate.getStringSerializer().deserialize(value);
          String sss = session_str.substring(10, session_str.length());
          SerializedPhpParser serializedPhpParser = new SerializedPhpParser(sss);
          Map result;
          YxUser user = new YxUser();
          try {
            result = (Map) serializedPhpParser.parse();
            user.setId((String) ((Map) result.get("user_auth")).get("user_id"));
            user.setUserName((String) ((Map) result.get("user_auth")).get("user_name"));
            user.setUserType((String) ((Map) result.get("user_auth")).get("user_type"));
          } catch (SerializedPhpParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          // JsonObject jsonObject = new JsonObject();
          // Gson gson = new Gson();
          //
          // JsonObject json = gson.fromJson(sess, JsonObject.class);

          return user;
        }
        return null;
      }
    });
  }

  @Override
  public void set(final String key, final String value) {
    redisTemplate.opsForValue().set(key, value);

  }

  @Override
  public String get(final String key) {
    return redisTemplate.opsForValue().get(key, 0, -1);
  }

  @Override
  public void delete(final String key) {
    redisTemplate.delete(key);
  }
}
