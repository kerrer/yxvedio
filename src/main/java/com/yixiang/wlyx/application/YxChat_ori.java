package com.yixiang.wlyx.application;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006-2008 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.service.IServiceCapableConnection;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import com.google.gson.Gson;
import com.yixiang.wlyx.model.Inquiry;
import com.yixiang.wlyx.model.YxUser;
import com.yixiang.wlyx.service.InquiryService;
import com.yixiang.wlyx.service.redisService;

public class YxChat_ori extends ApplicationAdapter {

  @SuppressWarnings("hiding")
  private static Logger log;
  public IScope globalScope = null;
  private static HashMap<String, IConnection> clients = new HashMap<String, IConnection>();
  private static HashMap<String, YxUser> onlines = new HashMap<String, YxUser>();
  private static HashMap<String, YxUser> users = new HashMap<String, YxUser>();
  private static HashMap<String, Integer> rooms = new HashMap<String, Integer>();

  public Object getBean(String beanId) {
    ApplicationContext appContext = globalScope.getContext().getApplicationContext();
    return appContext.getBean(beanId);
  }

  /**
  * 
  * 
  */
  public boolean appStart(IScope app) {
    Red5LoggerFactory.setUseLogback(true);
    log = Red5LoggerFactory.getLogger(YxChat_ori.class, "yxvedio");
    this.globalScope = scope;
    /*
     * TestBean testBean = (TestBean) getBean("testbean"); testBean.sayHell();
     * 
     * HelloService helloService = (HelloService) getBean("helloService");
     * 
     * helloService.useCallback();
     */
    log.info("********************myapp started!***********************************");
    return true;
  }

  /**
   * 每个新的客户端来连接时调用 这是我们覆盖了父类的实现
   */
  public boolean appConnect(IConnection con, Object[] params) {
    log.info("new client connectting..............");

    @SuppressWarnings("hiding")
    IScope scope = con.getScope();
    if (!scope.getPath().startsWith("/default/yxvedio/room")
        && !scope.getPath().startsWith("/default/yxvedio/doctor")) {
      rejectClient("ROOM IS NOT EXIST");
    }

    if (scope.getPath().startsWith("/default/yxvedio/room") && params.length < 3) {
      rejectClient("PARMAS NUMBER MUST BE 2");
    }

    String roomName = params[0].toString();
    String session_id = params[1].toString();
    String act = params[2].toString();

    String scopeRoomName = scope.getName();

    if (!scopeRoomName.equals(roomName)) {
      rejectClient(String.format("The room %1s is not this room %2s", roomName, scopeRoomName));
    }

    if ("new room".equals(act) && rooms.containsKey(roomName)) {
      rejectClient("ROOM HADE BEEN THERE");
    }

    String path = scope.getPath();
    if (path.startsWith("/default/yxvedio/room")) {
      inChatRoom(scopeRoomName, act);
      con.setAttribute("in.room", "chat");
      con.setAttribute("room.name", roomName);
    } else if (path.startsWith("/default/yxvedio/doctor")) {
      inDoctorRoom(scopeRoomName, act);
      con.setAttribute("in.room", "doctor");
    }

    YxUser user = getLogininfo(session_id);
    if (user == null) {
      rejectClient("NOT LOGIN");
    }

    String clientId = con.getClient().getId();
    user.setRoomName(roomName);
    user.setClientId(clientId);
    clients.put(clientId, con);
    users.put(user.getId(), user);
    onlines.put(clientId, user);

    con.setAttribute("user_id", user.getId());
    redisService redisService = (redisService) getBean("redisService");
    redisService.set("USER/" + user.getId(), "1");
    // List<String> ll = new ArrayList<String>();
    // ll.add(userId);

    // ISharedObject listSO;
    // listSO = getSharedObject(scope, roomName);
    // if (listSO == null) {
    // createSharedObject(scope, roomName, false);
    // listSO = getSharedObject(scope, roomName); }
    //
    // listSO.setAttribute(onlines.get(clientId).getId(), onlines.get(clientId).getUserName());
    // log.debug(listSO.getAttributes().toString());

    String loginName = user.getUserName();
    if (con instanceof IServiceCapableConnection) {
      IServiceCapableConnection sc = (IServiceCapableConnection) con;
      sc.invoke("loginInfo", new Object[] { loginName });
    }

    log.info(" new client have finish connectiong- client id: {} name:{} scope: {} .",
        user.getId(), user.getUserName(), scope);
    return super.appConnect(con, params);
  }

  private void inChatRoom(String roomName, String act) {
    if ("join room".equals(act)) {
      if (!rooms.containsKey(roomName))
        rejectClient(roomName + " ROOM IS NOT EXIST");
      else if (rooms.get(roomName) >= 2)
        rejectClient(roomName + "ROOM IS FULL");
    }
    inRoom(roomName);
  }

  private void inDoctorRoom(String roomName, String act) {
    if ("join room".equals(act)) {
      rejectClient("YOU ARE IN DOCTOR ROOM ");
    }
  }

  /**
   * 当客户端断开连接的时候调用！ 这里我们覆盖了父类的实现。
   */
  public void appDisconnect(IConnection conn) {
    String clientId = conn.getClient().getId();
    IScope chatScope = conn.getScope();
    String roomName = chatScope.getName();
    String user_id = (String) conn.getAttribute("user_id");
    log.info(user_id + ": client app disconnnecting...........");

    // 根据ID删除对应在线记录
    // onlineList.remove(user);
    // ISharedObject listSO = getSharedObject(chatScope, roomName);
    // if (listSO != null && listSO.hasAttribute(onlines.get(clientId).getId())) {
    // listSO.removeAttribute(onlines.get(clientId).getId());
    // }

    YxUser user = users.get(user_id);
    user.setRoomName("none");

    if (clients.containsKey(clientId))
      clients.remove(clientId);
    if (onlines.containsKey(clientId))
      onlines.remove(clientId);
    if (conn.getAttribute("user_id") != null && users.containsKey(conn.getAttribute("user_id")))
      users.remove(user_id);

    redisService redisService = (redisService) getBean("redisService");
    redisService.delete("USER/" + user_id);

    log.info("disconnnected - client id: {} name:{} scope: {} ", user_id, user.getUserName(), scope);
    log.info("in room: {}", conn.getAttribute("in.room"));

    if (conn.getAttribute("in.room") != null && "chat".equals(conn.getAttribute("in.room"))) {
      leaveChatRoom(conn, roomName);
    }

    super.appDisconnect(conn);
  }

  private void leaveChatRoom(IConnection conn, String roomName) {
    if (rooms.containsKey(roomName)) {
      log.info("ab disconnnected");
      leaveRoom(roomName);

      int inqueryId = Integer.valueOf(roomName);
      // InquiryService inquiryService = (InquiryService) getBean("inquiryService");
      // inquiryService.abInquiry(inqueryId);

      IScope scope = conn.getScope();
      Set<IClient> roomClients = scope.getClients();
      Iterator<IClient> it = roomClients.iterator();
      for (; it.hasNext();) {
        IClient client = (IClient) it.next();
        IConnection tempConn = (IConnection) client.getConnections().iterator().next();
        if (!tempConn.equals(conn) && tempConn instanceof IServiceCapableConnection) {
          tempConn.close();
        }
      }
    }
  }

  private void changeDoctorRoom() {

  }

  /**
   * 加入聊天室，必须带上用户名，假如用户名为空，则不能发送消息，也不能收到消息。
   * 
   * @param params
   *          客户端调用服务器端的参数。
   */
  public void jionChatRoom(Object[] params) {

    IConnection conn = Red5.getConnectionLocal();

    // 发通知给聊天室的所有的人，有新人加入了。
    IScope chatScope = conn.getScope();
    String displayName = onlines.get(conn.getClient().getId()).getUserName();
    Set<IClient> roomClients = chatScope.getClients();
    Iterator<IClient> it = roomClients.iterator();
    for (; it.hasNext();) {
      IClient client = (IClient) it.next();
      IConnection tempConn = (IConnection) client.getConnections().iterator().next();
      if (!tempConn.equals(conn) && tempConn instanceof IServiceCapableConnection) {
        IServiceCapableConnection sc = (IServiceCapableConnection) tempConn;
        sc.invoke("showJionInInfo", new Object[] { displayName });
      }
    }
  }

  /**
   * 给聊天室的所有人发送消息
   * 
   * @param params
   */
  public void sayToAll(Object[] params) {
    IConnection conn = Red5.getConnectionLocal();
    String user_id = conn.getClient().getId();
    String nickName = onlines.get(user_id).getUserName();
    String sayWhat = params[0].toString();

    // 发消息给聊天室的所有人
    IScope chatScope = conn.getScope();

    Set<IClient> roomClients = chatScope.getClients();
    Iterator<IClient> it = roomClients.iterator();
    for (; it.hasNext();) {
      IClient client = (IClient) it.next();
      IConnection tempConn = (IConnection) client.getConnections().iterator().next();
      if (!tempConn.equals(conn) && tempConn instanceof IServiceCapableConnection) {
        IServiceCapableConnection sc = (IServiceCapableConnection) tempConn;
        sc.invoke("showChatMessage", new Object[] { nickName + ":" + sayWhat });
      }
    }
  }

  /**
   * 视频邀请
   */
  public Boolean videoInvite(Object[] params) {
    IConnection conn = Red5.getConnectionLocal();
    String clientId = conn.getClient().getId();
    IScope chatScope = conn.getScope();
    String roomName = chatScope.getName();

    String receivedId = params[0].toString();
    String senderId = onlines.get(clientId).getId();
    if (!onlines.containsKey(receivedId)) {
      if (conn instanceof IServiceCapableConnection) {
        IServiceCapableConnection sc = (IServiceCapableConnection) conn;
        sc.invoke("showMessage", new Object[] { "对方不在线" });
      }
      return false;
    }

    if (!roomName.equals(users.get(receivedId).getRoomName())) {
      if (conn instanceof IServiceCapableConnection) {
        IServiceCapableConnection sc = (IServiceCapableConnection) conn;
        sc.invoke("showMessage", new Object[] { "系统错误" });
      }
      return false;
    }

    String senderName = onlines.get(clientId).getUserName();
    String receiverName = users.get(receivedId).getUserName();

    log.info("********视频邀请者是：" + senderName);

    IConnection tempConn = clients.get(users.get(receivedId).getClientId());
    if (tempConn instanceof IServiceCapableConnection) {
      IServiceCapableConnection sc = (IServiceCapableConnection) tempConn;
      sc.invoke("showPatientInviteMessage", new Object[] { senderName + ";" + senderId });
    }

    return true;
  }

  /**
   * 同意邀请后调用的邀请方法
   */
  public Boolean agreeVideoInvite(Object[] params) {
    // IConnection conn = Red5.getConnectionLocal();
    // 邀请者

    String reveiverName = params[0] == null ? "" : params[0].toString();

    String senderId = params[1].toString();

    IConnection tempconn = clients.get(users.get(senderId).getClientId());
    if (tempconn instanceof IServiceCapableConnection) {
      IServiceCapableConnection sc = (IServiceCapableConnection) tempconn;
      sc.invoke("startDoctorVideo", new Object[] { reveiverName });
    }
    return true;
  }

  public Boolean prepareVedio(Object[] params) {
    // IConnection conn = Red5.getConnectionLocal();
    // 邀请者
    log.info("prepareVedio");
    String reveiverName = params[0] == null ? "" : params[0].toString();

    String senderId = params[1].toString();
    // Gson gson = new Gson();
    // log.info(gson.toJson(users));
    String senderName = users.get(senderId).getUserName();

    IConnection conn = Red5.getConnectionLocal();

    IScope chatScope = conn.getScope();

    Set<IClient> roomClients = chatScope.getClients();
    Iterator<IClient> it = roomClients.iterator();
    for (; it.hasNext();) {
      IClient client = (IClient) it.next();
      IConnection tempConn = (IConnection) client.getConnections().iterator().next();
      if (tempConn instanceof IServiceCapableConnection) {
        IServiceCapableConnection sc = (IServiceCapableConnection) tempConn;
        sc.invoke("startVideo", new Object[] { senderName + ":" + reveiverName });
        log.info("startVideo:{}", tempConn.getAttribute("user_id"));
      }
    }

    // IConnection tempconn = clients.get(users.get(senderId).getClientId());
    // if (tempconn instanceof IServiceCapableConnection) {
    // IServiceCapableConnection sc = (IServiceCapableConnection) tempconn;
    // sc.invoke("startVideo", new Object[] { reveiverName });
    // }
    return true;
  }

  public Boolean flushHouZhen(Object[] params) {

    String doctor_id = params[0].toString();
    log.info("push quene to " + doctor_id);
    IConnection tempconn = clients.get(users.get(doctor_id).getClientId());
    if (tempconn instanceof IServiceCapableConnection) {
      IServiceCapableConnection sc = (IServiceCapableConnection) tempconn;
      sc.invoke("flushDoctorHouzhen", new Object[] { 1 });
    }
    return true;
  }

  public Boolean pushMessage(Object[] params) {
    String act = params[0].toString();
    String targetId = params[1].toString();
    String data = params[2].toString();
    log.info("push  " + act + ",target: " + targetId + ", data: " + data);
    HashMap<String, String> mess = new HashMap<String, String>();
    mess.put("dataType", act);
    mess.put("fromId", targetId);
    mess.put("data", data);
    Gson gson = new Gson();
    String json = gson.toJson(mess);
    IConnection conn = Red5.getConnectionLocal();
    IConnection otherConn = null;

    if (targetId.equals("0")) {

      IScope chatScope = conn.getScope();

      Set<IClient> roomClients = chatScope.getClients();
      Iterator<IClient> it = roomClients.iterator();
      for (; it.hasNext();) {
        IClient client = (IClient) it.next();
        otherConn = (IConnection) client.getConnections().iterator().next();
        if (!otherConn.equals(conn)) {
          break;
        }

      }
    } else if (users.get(targetId) != null) {
      otherConn = clients.get(users.get(targetId).getClientId());
    }

    if (otherConn != null && otherConn instanceof IServiceCapableConnection) {
      IServiceCapableConnection sc = (IServiceCapableConnection) otherConn;
      sc.invoke("getMessage", new Object[] { json });
    }
    return true;
  }

  /**
   * 关闭视频提示
   */
  public void discuzVideo(Object[] params) {
    String inviteUserName = params[0] == null ? "" : params[0].toString().trim();
    // 被邀请者
    String otherUserName = params[0] == null ? "" : params[0].toString();
    IConnection tempconn = clients.get(otherUserName);
    if (tempconn instanceof IServiceCapableConnection) {
      IServiceCapableConnection sc = (IServiceCapableConnection) tempconn;
      sc.invoke("discuzVideo", new Object[] { inviteUserName });
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.red5.server.adapter.MultiThreadedApplicationAdapter#roomConnect(org.red5.server.api.IConnection
   * , java.lang.Object[])
   */
  // @Override
  // public boolean roomConnect(IConnection conn, Object[] params) {
  // log.debug("roomJoin - client id: {} scope: {}", conn.getClient().getId(), conn.getScope());
  // if (conn.getBoolAttribute("in.room") != null && conn.getBoolAttribute("in.room") == true) {
  // return super.roomConnect(conn, params);
  // }
  //
  // conn.setAttribute("in.room", true);
  // //
  // // if (conn.getScope().getPath().startsWith("/default/yxvedio/doctor")) {
  // // System.out.println("docter in..................");
  // // HashMap<Integer, String> quene = getPatientQuene(1);
  // // Gson gson = new Gson();
  // // String json = gson.toJson(quene);
  // // System.out.println("json = " + json);
  // // if (conn instanceof IServiceCapableConnection) {
  // // IServiceCapableConnection sc = (IServiceCapableConnection) conn;
  // // sc.invoke("showPatientQuene", new Object[] { json });
  // // }
  // // }
  //
  // if (conn.getScope().getPath().startsWith("/default/yxvedio/room")) {
  //
  // IScope chatScope = conn.getScope();
  // String roomName = chatScope.getName();
  // String clientId = conn.getClient().getId();
  //
  // if (!roomName.equals(params[0])) {
  // rejectClient(String.format("The room %1s is not this room %2s", params[0], roomName));
  // }
  //
  // User user = onlines.get(clientId);
  // user.setInRoom(true);
  // user.setRoomName(roomName);
  //
  // ISharedObject listSO;
  // listSO = getSharedObject(chatScope, roomName);
  // if (listSO == null) {
  // createSharedObject(chatScope, roomName, false);
  // listSO = getSharedObject(chatScope, roomName);
  // }
  //
  // listSO.setAttribute(onlines.get(clientId).getId(), onlines.get(clientId).getUserName());
  // log.debug(listSO.getAttributes().toString());
  // if (rooms.containsKey(roomName)) {
  // int nums = rooms.get(roomName) + 1;
  // rooms.put(roomName, nums);
  // if (nums > 2) {
  // rejectClient();
  // }
  // } else {
  // rooms.put(roomName, 1);
  // }
  //
  // }
  //
  // return super.roomConnect(conn, params);
  // }

  // @Override
  // public void roomDisconnect(IConnection conn) {
  // String clientId = conn.getClient().getId();
  // IScope chatScope = conn.getScope();
  // String roomName = chatScope.getName();
  //
  // log.info("client leave room: " + roomName);
  // if (conn.getBoolAttribute("in.room")) {
  // log.info("client leaving room...............");
  // conn.setAttribute("in.room", false);
  //
  // // 根据ID删除对应在线记录
  // // onlineList.remove(user);
  // ISharedObject listSO = getSharedObject(chatScope, roomName);
  // if (listSO != null && listSO.hasAttribute(onlines.get(clientId).getId())) {
  // listSO.removeAttribute(onlines.get(clientId).getId());
  // }
  //
  // User user = onlines.get(clientId);
  // user.setInRoom(false);
  // user.setRoomName("none");
  //
  // if (rooms.containsKey(roomName)) {
  // int nums = rooms.get(roomName) - 1;
  // rooms.put(roomName, nums);
  // if (nums <= 0) {
  // rooms.remove(roomName);
  // }
  // }
  //
  // if (conn.getAttribute("normal.shut") == null) {
  // Set<IClient> roomClients = chatScope.getClients();
  // Iterator<IClient> it = roomClients.iterator();
  // for (; it.hasNext();) {
  // IClient client = (IClient) it.next();
  // IConnection tempConn = (IConnection) client.getConnections().iterator().next();
  // if (!tempConn.equals(conn) && tempConn instanceof IServiceCapableConnection) {
  // IServiceCapableConnection sc = (IServiceCapableConnection) tempConn;
  // sc.invoke("abNormalDisconnect", new Object[] { onlines.get(clientId).getUserName() });
  // }
  // }
  // }
  // log.info("client had leave room");
  // }
  //
  // super.roomDisconnect(conn);
  // }

  public Boolean prepareLeave(Object[] params) {
    String fromName = params[0].toString();
    log.info("prepare leave room:{} ", fromName);
    IConnection conn = Red5.getConnectionLocal();

    // 发消息给聊天室的所有人
    IScope chatScope = conn.getScope();
    String roomName = chatScope.getName();

    leaveRoom(roomName);

    Set<IClient> roomClients = chatScope.getClients();
    Iterator<IClient> it = roomClients.iterator();
    for (; it.hasNext();) {
      IClient client = (IClient) it.next();
      IConnection tempConn = (IConnection) client.getConnections().iterator().next();
      if (!tempConn.equals(conn) && tempConn instanceof IServiceCapableConnection) {
        IServiceCapableConnection sc = (IServiceCapableConnection) tempConn;
        sc.invoke("prepareLeave", new Object[] { fromName });
      }
    }
    return true;
  }

  public void sendToRoom(String msg, String roomName, String userName, String toUserName) {
    /*
     * // IScope scope = scopeMap.get(roomName); Iterator<Set<IConnection>> iterator =
     * scope.getConnections().iterator(); while (iterator.hasNext()) { IConnection conn =
     * (IConnection) iterator.next(); String connUserName =
     * conn.getClient().getAttribute("userName").toString(); if (conn instanceof
     * IServiceCapableConnection) { IServiceCapableConnection sc = (IServiceCapableConnection) conn;
     * if ("all".equalsIgnoreCase(toUserName)) { // 转发消息
     * 
     * String str = userName + "说:" + msg; sc.invoke("sendToRoom", new Object[] { str });
     * 
     * } else if (connUserName.equalsIgnoreCase(userName)) { String str = "你对" + toUserName + "说:" +
     * msg; sc.invoke("sendToRoom", new Object[] { str }); } else if
     * (connUserName.equalsIgnoreCase(toUserName)) { String str = userName + "对你说:" + msg;
     * sc.invoke("sendToRoom", new Object[] { str }); } } }
     */
  }

  public void updateUserList(String roomName) {
    /*
     * System.out.println("send userlist==" + roomName); List list = rooms.get(roomName);
     * System.out.println("有" + list.size() + "个子"); //IScope scope = scopeMap.get(roomName);
     * System.out.println(scope); Iterator<Set<IConnection>> iterator =
     * scope.getConnections().iterator(); while (iterator.hasNext()) {
     * System.out.println("connection"); IConnection conn = (IConnection) iterator.next(); if (conn
     * instanceof IServiceCapableConnection) { // 转发消息 System.out.println("conn====" + conn);
     * IServiceCapableConnection sc = (IServiceCapableConnection) conn; sc.invoke("updateList", new
     * Object[] { list }); } }
     */

  }

  private void leaveRoom(String roomName) {
    if (rooms.containsKey(roomName)) {
      int nums = rooms.get(roomName) - 1;
      rooms.put(roomName, nums);
      if (nums <= 0) {
        rooms.remove(roomName);
      }
    }
  }

  private void inRoom(String roomName) {
    if (rooms.containsKey(roomName)) {
      int nums = rooms.get(roomName) + 1;
      rooms.put(roomName, nums);
      if (nums > 2) {
        rejectClient();
      }
    } else {
      rooms.put(roomName, 1);
    }
  }

  private YxUser getLogininfo(String session_id) {
    YxUser user = new YxUser();
    // redisService redisService = (redisService) getBean("redisService");
    // user = redisService.readSession(session_id);
    Random rand = new Random();
    int id = (int) rand.nextInt(100);
    user.setId(String.valueOf(id));
    user.setUserName("max");
    user.setUserType("doctor");
    return user;
  }

  private HashMap<Integer, String> getPatientQuene(int doctorId) {
    HashMap<Integer, String> quene = new HashMap<Integer, String>();
    InquiryService inquiryService = (InquiryService) getBean("inquiryService");
    List<Inquiry> inquiries = inquiryService.findAll();
    for (Inquiry inquiry : inquiries) {
      quene.put(inquiry.getPatient_id(), "patient" + inquiry.getPatient_id());

    }
    return quene;
  }

}