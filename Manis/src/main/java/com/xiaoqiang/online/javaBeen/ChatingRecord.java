package com.xiaoqiang.online.javaBeen;
/**
 *  聊天信息的实体类
 * @author Administrator
 *   
 */

public class ChatingRecord {
	
	private String userName; //用户名
	private String chatingText;  //发送的文本信息
	private int type;   //发送的类型 代表是好友发送还是自己发送
	private String date;   //发送的时间
	private String friendName;   //好友的名字
	
	public String getFriendName() {
		return friendName;
	}
	public void setFriendName(String friendName) {
		this.friendName = friendName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getChatingText() {
		return chatingText;
	}
	public void setChatingText(String chatingText) {
		this.chatingText = chatingText;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	@Override
	public String toString() {
		return "ChatingRecord [userName=" + userName + ", chatingText=" + chatingText + ", type=" + type + ", date="
				+ date + ", friendName=" + friendName + "]";
	}
	
	

}
