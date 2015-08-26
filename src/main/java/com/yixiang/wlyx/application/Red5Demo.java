/**
 * File		: Red5Demo.java
 * Date		: 09-Mar-2012 
 * Owner	: arul
 * Project	: red5Demo
 * Contact	: http://arulraj.net
 * Description : 
 * History	:
 */
package com.yixiang.wlyx.application;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.red5.io.utils.ObjectMap;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.so.ISharedObject;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.ISubscriberStream;
import org.red5.server.stream.ClientBroadcastStream;
import org.red5.server.util.ScopeUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import com.yixiang.wlyx.model.Channel;
import com.yixiang.wlyx.model.Connection;
import com.yixiang.wlyx.model.User;
import com.yixiang.wlyx.so.ChatSOSecurity;
import com.yixiang.wlyx.utils.DemoConstants;

/**
 * @author arul
 *
 */
public class Red5Demo extends Application {

  @SuppressWarnings("hiding")
  static final Logger LOG = Red5LoggerFactory.getLogger(Red5Demo.class, DemoConstants.APP_NAME);

  static IScope staticScope;

  boolean channelRecord;

  /**
   * Get red5 bean class using bean id
   * 
   * @param beanId
   * @return
   */
  public Object getBean(String beanId) {
    ApplicationContext appContext = globalScope.getContext().getApplicationContext();
    return appContext.getBean(beanId);
  }

  /**
   * @return the channelRecord
   */
  public boolean isChannelRecord() {
    return channelRecord;
  }

  /**
   * @param channelRecord
   *          the channelRecord to set
   */
  public void setChannelRecord(boolean channelRecord) {
    this.channelRecord = channelRecord;
  }

  /**
   * Private functions
   */
  private ISharedObject getChatSO(String channelName) {
    return getSharedObject(scopeMap.get(channelName), DemoConstants.CHATSO_PREFIX + channelName);
  }

  /**
   * START: override functions
   */

  /*
   * (non-Javadoc)
   * 
   * @see com.live.application.Application#appStart(org.red5.server.api.IScope)
   */
  @Override
  public boolean appStart(IScope scope) {
    boolean retVal = super.appStart(scope);
    if (retVal) {
      staticScope = scope;
      registerSharedObjectSecurity(new ChatSOSecurity());
    }
    return retVal;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.red5.server.adapter.MultiThreadedApplicationAdapter#appStop(org.red5
   * .server.api.IScope)
   */
  @Override
  public void appStop(IScope scope) {
    super.appStop(scope);
  }

  @Override
  public void streamPublishStart(IBroadcastStream stream) {
    String publishedName = stream.getPublishedName();
    LOG.info("streamPublishStart scope {}", stream.getScope().toString());

    if (channelRecord) {
      /**
       * Record user streams
       */
      ClientBroadcastStream broadcastStream = (ClientBroadcastStream) stream;
      if (!broadcastStream.isRecording()) {
        try {
          SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
          String folderName = format.format(new Date()).toString();
          broadcastStream.saveAs(folderName + File.separator
              + usersMap.get(publishedName).getLoginName(), true);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    super.streamPublishStart(stream);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.demo.application.Application#connect(org.red5.server.api.IConnection,
   * org.red5.server.api.IScope, java.lang.Object[])
   */
  @Override
  public boolean connect(IConnection connection, IScope scope, Object[] params) {
    String connectionName;
    String channelName;
    Channel demoChannel;
    if (ScopeUtils.isRoom(scope)) {
      if (params.length > 1) {
        connectionName = (String) params[0];
        LOG.info("this scope {}", scope.toString());
        if (connectionName.equals(DemoConstants.CONNECTIONS.get(0))) {
          /* For public connection */
          channelName = scope.getName();
          if (!channelsMap.containsKey(channelName)) {
            demoChannel = new Channel(channelName);
            demoChannel.setChannelName(channelName);
            channelsMap.put(channelName, demoChannel);
            scopeMap.put(channelName, scope);
          }
        }
      }
    }
    return super.connect(connection, scope, params);
  }

  @Override
  public void disconnect(IConnection connection, IScope scope) {
    LOG.debug("Application dis-connect called from connection ID ["
        + connection.getClient().getId() + "]");
    try {
      String connId = connection.getClient().getId();
      User demoUser = getUserByPublicId(connId);
      Connection connections = getConnectionByPublicId(connId);

      if (connections == null) {
        connections = getConnectionByVideoId(connId);
        demoUser = getUserByVideoId(connId);
        if (connections != null) {
          if (connections.getVideoId() != null) {
            connections.setVideoId(null);
            LOG.debug("Closing Video connection for user " + connections.getId() + ", Id ["
                + connId + "]");
          }
        } else {
          connections = getConnectionByAudioId(connId);
          demoUser = getUserByAudioId(connId);
          if (connections != null) {
            if (connections.getAudioId() != null) {
              connections.setAudioId(null);
              LOG.debug("Closing Audio connection for user " + connections.getId() + ", Id ["
                  + connId + "]");
            }
          } else {
            LOG.warn("Could not find user for the requested Id [" + connId + "]");
          }
        }
      } else {
        String id = demoUser.getId();
        LOG.debug("The user " + id + " is removed from Map, Id [" + connId + "]");
        connections.setPublicId(null);
        usersMap.remove(id);
        connectionsMap.remove(id);
        channelsMap.remove(scope.getName());
        scopeMap.remove(scope.getName());
      }

    } catch (Exception e) {
      LOG.debug("Exception in appDisconnect....", e);
    }
    super.disconnect(connection, scope);
  }

  @Override
  public void streamBroadcastClose(IBroadcastStream stream) {
    ClientBroadcastStream broadcastStream = (ClientBroadcastStream) stream;
    if (broadcastStream.isRecording()) {
      broadcastStream.stopRecording();
    }
    super.streamBroadcastClose(stream);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.red5.server.adapter.MultiThreadedApplicationAdapter#streamSubscriberStart(org.red5.server
   * .api.stream.ISubscriberStream)
   */
  @Override
  public void streamSubscriberStart(ISubscriberStream stream) {
    LOG.debug("Subscriber start for the stream : " + stream.getName());
    super.streamSubscriberStart(stream);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.red5.server.adapter.MultiThreadedApplicationAdapter#streamSubscriberClose(org.red5.server
   * .api.stream.ISubscriberStream)
   */
  @Override
  public void streamSubscriberClose(ISubscriberStream stream) {
    LOG.debug("Subscriber close for the stream : " + stream.getName());
    super.streamSubscriberClose(stream);
  }

  /**
   * END: override function
   */

  /**
   * @param id
   * @return
   */
  private User getUserByPublicId(String id) {
    try {
      Collection<Connection> cl = connectionsMap.values();
      Iterator<Connection> itr = cl.iterator();
      while (itr.hasNext()) {
        Connection connection = (Connection) itr.next();
        String chatId = connection.getPublicId();
        if (connection != null && chatId != null && chatId.equals(id)) {
          return usersMap.get(connection.getId());
        }
      }
    } catch (Exception e) {
      LOG.debug("Exception in getUserByChatId() ", e);
    } finally {
    }
    return null;
  }

  /**
   * @param id
   * @return
   */
  private User getUserByVideoId(String id) {
    try {
      Collection<Connection> cl = connectionsMap.values();
      Iterator<Connection> itr = cl.iterator();
      while (itr.hasNext()) {
        Connection connection = (Connection) itr.next();
        String videoId = connection.getVideoId();
        if (connection != null && videoId != null && videoId.equals(id)) {
          return usersMap.get(connection.getId());
        }
      }
    } catch (Exception e) {
      LOG.debug("Exception in getUserByVideoId() ", e);
    } finally {
    }
    return null;
  }

  /**
   * @param id
   * @return
   */
  private User getUserByAudioId(String id) {
    try {
      Collection<Connection> cl = connectionsMap.values();
      Iterator<Connection> itr = cl.iterator();
      while (itr.hasNext()) {
        Connection connection = (Connection) itr.next();
        String audioId = connection.getAudioId();
        if (connection != null && audioId != null && audioId.equals(id)) {
          return usersMap.get(connection.getId());
        }
      }
    } catch (Exception e) {
      LOG.debug("Exception in getUserByAudioId() ", e);
    } finally {
    }
    return null;
  }

  /**
   * @param id
   * @return
   */
  private Connection getConnectionByPublicId(String id) {
    try {
      Collection<Connection> cl = connectionsMap.values();
      Iterator<Connection> itr = cl.iterator();
      while (itr.hasNext()) {
        Connection connection = (Connection) itr.next();
        String chatId = connection.getPublicId();
        if (connection != null && chatId != null && chatId.equals(id)) {
          return connection;
        }
      }
    } catch (Exception e) {
      LOG.debug("Exception in getUserByChatId() ", e);
    } finally {
    }
    return null;
  }

  /**
   * @param id
   * @return
   */
  private Connection getConnectionByVideoId(String id) {
    try {
      Collection<Connection> cl = connectionsMap.values();
      Iterator<Connection> itr = cl.iterator();
      while (itr.hasNext()) {
        Connection connection = (Connection) itr.next();
        String videoId = connection.getVideoId();
        if (connection != null && videoId != null && videoId.equals(id)) {
          return connection;
        }
      }
    } catch (Exception e) {
      LOG.debug("Exception in getUserByVideoId() ", e);
    } finally {
    }
    return null;
  }

  /**
   * @param id
   * @return
   */
  private Connection getConnectionByAudioId(String id) {
    try {
      Collection<Connection> cl = connectionsMap.values();
      Iterator<Connection> itr = cl.iterator();
      while (itr.hasNext()) {
        Connection connection = (Connection) itr.next();
        String audioId = connection.getAudioId();
        if (connection != null && audioId != null && audioId.equals(id)) {
          return connection;
        }
      }
    } catch (Exception e) {
      LOG.debug("Exception in getConnectionByChatId() ", e);
    } finally {
    }
    return null;
  }

  /**
   * START: RPC functions
   */

  /**
   * Update the user info from flex
   * 
   * @param flexDemoUser
   */
  public void updateUserInfo(ObjectMap<String, Object> flexDemoUser) {
    String userid = (String) flexDemoUser.get("id");
    User user = usersMap.get(userid);
    utility.updateUserInfo(flexDemoUser, user);
  }

  /**
   * Update the channel info from flex
   * 
   * @param flexFmUser
   */
  public void updateChannelInfo(ObjectMap<String, Object> flexDemoChannel) {
    String channelid = (String) flexDemoChannel.get("id");
    User user = usersMap.get(channelid);
    utility.updateUserInfo(flexDemoChannel, user);
  }

  /**
   * @param userId
   * @param channelName
   * @param message
   */
  public void sendGroupMessage(String userId, String channelName, Object message) {
    LOG.debug(" sendGroupMessage : userId " + userId + " channelName " + channelName);
    ISharedObject chatSO = getChatSO(channelName);
    if (chatSO != null) {
      List<Object> params = new ArrayList<Object>();
      params.add(message);
      chatSO.sendMessage("receiveGroupMessage", params);
    }
  }

  /**
   * END: RPC functions
   */
}
