package com.cisco.core.interfaces;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.HttpTask.AdminHttp;
import com.cisco.core.HttpTask.GetModeratorHttp;
import com.cisco.core.PeerConnectionClient;
import com.cisco.core.PeerConnectionClient.PeerConnectionParameters;
import com.cisco.core.VideoImplement;
import com.cisco.core.util.Constants;
import com.cisco.core.util.Lg;
import com.cisco.core.util.Tools;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.XmppConnection;
import com.cisco.library.R;
import com.cisco.nohttp.CallServer;
import com.cisco.nohttp.HttpListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.jivesoftware.smack.XMPPException;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.List;
import java.util.Random;

/**
 * 
 * @author jlp
 * 
 */

public class VideoInterface implements CiscoApiInterface.OnCallEvents {
	public String tag = "VideoInterface";
	public Context context;
	public String roomnumber;// 房间号 guest房间和 登录成功后的都用这个变量
	public String username;// 登录的用户名
	public String password;// 登录的用户密码
	public String addressUrl;
	public String conferencePass;
	public String hostPass;// 主持人密码
	// public String conferenceNumber;//会议号
	public String type;// 用于区分验证会议是否存在接口，0代表guest匿名参会，1代表登录成功后用户参会

	public boolean issuccess;
	private Toast logToast;
	XmppConnection xmppConnection = XmppConnection.getInstance();
	private CiscoApiInterface.OnGuestLoginEvents OnGuestLoginlistener;
	private CiscoApiInterface.OnLoginEvents OnLoginlistener;
	private CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents;

	public String xmpp_userid_toupper;// 大写userid
	public String xmpp_userid_toLower;// 小写userid
	public String xmpp_password;// 密码
	public String xmpp_username;// 用户名， 昵称是android+随机数
	public String xmpp_nick;// 昵称是android+随机数
	public String xmpp_url;// xmpp 地址 60.206.107.181

	// ///////////////////////////////////对音视频操作////////////////////////////////////////////////
	private PeerConnectionClient peerConnectionClient = PeerConnectionClient
			.getInstance();
	private boolean micEnabled = true;
	private boolean viewEnabled = true;
	private boolean videoEnabled = true;
	private boolean audioEnabled = true;
	private PeerConnectionParameters peerConnectionParameters;

	private static VideoInterface videoInterface;

	public static VideoInterface getInstance() {
		if (videoInterface == null) {
			synchronized (VideoInterface.class) {
				videoInterface = new VideoInterface();
			}
		}
		return videoInterface;
	}

	// 程序配置信息
	public void InitConfiginfo() {

	}

	/***
	 * (2)login vip登录
	 * 
	 * @param username
	 * @param password
	 * @param addressUrl
	 *            地址不写 ，代表默认地址
	 * @return
	 */
	public void Login(String username, String password, String addressUrl,
			Context context, CiscoApiInterface.OnLoginEvents OnLoginlistener) {
		this.OnLoginlistener = OnLoginlistener;
		this.context = context;
		this.username = username;
		this.password = password;
		LoginHTTP lh = new LoginHTTP();
		lh.requestLogin();
	}

	/***
	 * （3）guestlogin 匿名登录
	 * 
	 * @return
	 */
	public void GuestLogin(String roomnumber, String username,
			String conferencePass, String addressUrl, String type,
			Context context,
			CiscoApiInterface.OnGuestLoginEvents OnGuestLoginlistener) {
		this.OnGuestLoginlistener = OnGuestLoginlistener;
		this.roomnumber = roomnumber;
		this.type = type;
		// key.xmpp_room=roomnumber;
		this.context = context;
		this.username = username;
		this.conferencePass = conferencePass;
		Constants.SERVER=addressUrl;
		if (addressUrl.trim().equals("")) {
			addressUrl = Constants.SERVER;
		}
		GuestLoginHttp glh = new GuestLoginHttp();
		glh.requestGuestGetNick(username);
	}

	/***
	 * （4）登录成功后， 如果房间没有存在 创建自己的房间 （即时会议），如果存在直接进入会议。
	 * 
	 */
	private LoginAfterCreateRoomHttp lacr;
	private String  ujid;
	public void LoginAfterCreateRoom(
			final String conferencePass,
			final String hostPass,
			String type,
			final Context context,
			CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents) {
		this.conferencePass = conferencePass;
		this.hostPass = hostPass;
		this.type = type;
		this.context = context;
		this.onLoginLafterJoinRoomEvents = onLoginLafterJoinRoomEvents;
		 lacr = new LoginAfterCreateRoomHttp();
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
				ujid =getUserJid();
//				Message msg= Message.obtain();
//				createRoomHander.sendMessage(msg);
//			}
//		}).start();
		if(ujid!=null){
			lacr.requestXmppCreateRoom(conferencePass, hostPass,
					xmpp_userid_toupper, ujid);
		}else{
			logAndToast("创建即时会议失败！xmppConnection.connection="+xmppConnection.connection);
		}

	}
	private Handler createRoomHander = new Handler() {
		public void handleMessage(Message msg) {
				lacr.requestXmppCreateRoom(conferencePass, hostPass,
						xmpp_userid_toupper, ujid);
		};
	};


	/***
	 * （5）登录成功后 输入会议号加入会议
	 * 
	 * @return
	 */
	public void LoginAfterJoinRoom(
			String conferenceNumber,
			String conferencePass,
			String type,
			Context context,
			CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents) {
		this.roomnumber = conferenceNumber;
		this.conferencePass = conferencePass;
		this.type = type;
		this.context = context;
		this.onLoginLafterJoinRoomEvents = onLoginLafterJoinRoomEvents;
		GuestLoginHttp glh = new GuestLoginHttp();
		glh.joinConferenceClient(roomnumber, conferencePass,
				xmpp_userid_toupper);
	}

	/***
	 * （6）验证判断等操作完成后，连接会议
	 * 
	 * @return
	 */
	public void connectToRoom(Activity activity,
			EglBase.Context renderEGLContext,
			VideoRenderer.Callbacks localRender,
			List<SurfaceViewRenderer> remoteRenders,
			VideoRenderer.Callbacks viewRender , CiscoApiInterface.UpdateUIEvents updateEvents ) {

	}

	/***
	 * （7）及时会议，服务器上创建会议室
	 * 
	 * @return
	 */
	public void CreateRoom(String hostpass, String roomid, String videobridge) {
		xmppConnection.createRoom(roomnumber, videobridge, context);
	}

	/***
	 * 断开连接
	 */
	public void DisConnect() {
		xmppConnection.disconnect();
	}

	/***
	 * 结束会议
	 */
	@Override
	public void onCallHangUp() {
		VideoImplement.videoImplement.disconnect();
	}

	/***
	 * ice连接断开后，会议重连。
	 */
	public void onReconnection() {
		VideoImplement.videoImplement.onMeetReconnection();
	}

	/***
	 * 切换摄像头
	 */
	@Override
	public void onCameraSwitch() {
		if (peerConnectionClient != null) {
			peerConnectionClient.switchCamera();
		}
	}

	@Override
	public void onVideoScalingSwitch(ScalingType scalingType) {
		// 刷新view
		// this.scalingType = scalingType;
		// updateVideoView();
	}

	@Override
	public void onCaptureFormatChange(int width, int height, int framerate) {
		if (peerConnectionClient != null) {
			peerConnectionClient.changeCaptureFormat(width, height, framerate);
		}
	}

	@Override
	public void onChatChange() {

	}

	/***
	 * 好友列表
	 */
	@Override
	public void onFriendsList() {
		List<String> list = XmppConnection.getInstance().findMulitUser();
	}

	/***
	 * 切换麦克风
	 */
	@Override
	public boolean onToggleMic() {
		if (peerConnectionClient != null) {
			micEnabled = !micEnabled;
			peerConnectionClient.setAudioEnabled(micEnabled);
		}
		return micEnabled;
	}

	//设置麦克风
	public void setMic(boolean ismute){
		peerConnectionClient.setAudioEnabled(ismute);
	}
	//打开或关闭视频
	public void setVideo(boolean videoEnabled){
		if (videoEnabled) {
			peerConnectionClient.stopVideoSource();
		} else {
			peerConnectionClient.startVideoSource();
		}
	}
	//获取自己的jid
	public String getUserJid(){
		String	getujid=null;
		if(xmppConnection.connection!=null){
			getujid = xmppConnection.connection.getUser();
		}

		return getujid;
	}

	public void sendPresenceMessageUpdateState(){
		XmppConnection.getInstance().SendPresenceMessage();
	}

	/***
	 * 不显示自己画面
	 */
	@Override
	public boolean onHiddenView() {
		if (peerConnectionClient != null) {
			viewEnabled = !viewEnabled;
			peerConnectionClient.setHiddenView(viewEnabled);
		}
		return viewEnabled;
	}

	/***
	 * 打开/关闭音量
	 */
	@Override
	public boolean notSendAudioStream() {
		if (peerConnectionClient != null) {
			audioEnabled = !audioEnabled;
			if(audioEnabled){
				VideoImplement.videoImplement.audioManager.setSpeakerPhoneOn(true);
			}else{
				VideoImplement.videoImplement.audioManager.setSpeakerPhoneOn(false);
			}
		}
		return audioEnabled;
	}

	/***
	 * 打开/关闭画面，对方看不到自己,不发流
	 */
	@Override
	public boolean notSendVideoStream() {
		if (peerConnectionClient != null) {
			videoEnabled = !videoEnabled;
			if (videoEnabled) {
				peerConnectionClient.startVideoSource();
			} else {
				peerConnectionClient.stopVideoSource();
			}
		}
		return videoEnabled;
	}

	/***
	 * 发送聊天消息
	 */
	public void sendImMessage(String contString) {
		if (peerConnectionClient != null) {
			try {
				xmppConnection.muc.sendMessage(contString);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	}
	/***
	 * 授权主持人，前提是 自己是主持人
	 */
	public void grantAdmin(String coverJid,String coverUserid ) {
		String ujid = coverJid;
//		String ujid=getUserJid();
		AdminHttp adminHttp=new AdminHttp(context,ujid,roomnumber,coverUserid);
		adminHttp.GrantAdmin();
	}
	/***
	 * 获取主持人，前提是会议中没有主持人。
	 */
	public void getModerator(String hostPass) {
		Log.d("videointerface","linpeng,hostpass="+hostPass);
		String ujid = getUserJid();
		Log.d("videointerface","linpeng,ujid="+ujid);
		GetModeratorHttp adminHttp=new GetModeratorHttp(context,ujid,roomnumber,hostPass);
		adminHttp.GetModerator();
	}
	/***
	 * 我是主持人--踢人。
	 */
	public void hostTiren(String coverTirenid) {
		if(xmppConnection!=null) {
			xmppConnection.getInstance().SendTirenByMessage(coverTirenid);
		}
	}
	/***
	 * 我是主持人--静音。
	 */
	public void hostMute(String coverMuteid,boolean isMute) {
		if(xmppConnection!=null) {
			xmppConnection.getInstance().SendMuteMessage(coverMuteid,isMute);
		}
	}
	/***
	 * 呼叫sip
	 */
	public void CallSip(String sipNumber) {
		if(xmppConnection!=null)
			xmppConnection.getInstance().SendCallSipByIq(sipNumber);
	}

	/***
	 * 接收 sip
	 */
	public void inviteSip() {
		if(xmppConnection!=null)
			xmppConnection.getInstance().SendInviteSipByIq();
	}
	/***
	 * 拒绝sip
	 */
	public void rejectSip() {
		if(xmppConnection!=null)
			xmppConnection.getInstance().SendRejectSipByIq();
	}
	/***
	 * 呼叫好友
	 */
	public void CallFriend(String friendid,String type,String roomNumber) {
		if(xmppConnection!=null)
			xmppConnection.getInstance().SendCallFriend(friendid,type,roomNumber);
	}
	/***
	 * 传入事件
	 */
	public void OnCallEvent(CiscoApiInterface.OnIMCallEvents onIMCallEvents) {
		if(xmppConnection!=null)
			xmppConnection.getInstance().OnIMCallEvents(onIMCallEvents);
	}


	/***
	 *
	 * @author 切换分辨率
	 *
	 */
	public void onSwitchResolution( int width,  int height,int framerate ) {
		if (peerConnectionClient != null) {
			peerConnectionClient.changeCaptureFormat(width,height,framerate);
		}

	}
	public void enableStatsEvents() {
		int STAT_CALLBACK_PERIOD = 5000;
		if (peerConnectionClient != null) {
			peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
		}
	}

	public void setAudioEnabled(boolean audioEnabled) {
		if (peerConnectionClient != null) {
			peerConnectionClient.setAudioEnabled(audioEnabled);
		}
	}

	public PeerConnectionParameters InitParameters(boolean loopback,String videoQuality) {
		boolean useCamera2,hwCodec,captureToTexture;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// Added in API level 21.
			 useCamera2 = true;// 使用camera2选项 默认false;
			 hwCodec = true;// 检查硬件编解码
			 captureToTexture = true;// 检查捕获到纹理。
		}else{
			 useCamera2 = false;// 使用camera2选项 默认false;
			 hwCodec = false;// 检查硬件编解码
			 captureToTexture = false;// 检查捕获到纹理。
		}

		boolean videoCallEnabled = true;// 视频呼叫默认true
		String videoCodec = "VP9";// 默认视频编码
		String audioCodec = "OPUS";// 默认音频编码
		boolean noAudioProcessing = false;// 检查禁用音频处理标志
		boolean aecDump = false;// AEC转储
		boolean useOpenSLES = false;// 检查OpenSL ES的启用标志。
		boolean disableBuiltInAEC = false;// 检查禁用内置的AEC
		int videoWidth = 0;
		int videoHeight = 0;
		// 获取视频分辨率。
//		String resolution = "320 x 240";// "1280 x 720","640 x 480"
		Log.d("","videoQuality="+videoQuality);
		String resolution="";
		if (!"".equals(videoQuality.trim())) {

			if(videoQuality.equals("CIF")){
				resolution="320 x 240";
			}
			if(videoQuality.equals("VGA")){
				resolution="640 x 480";
			}
			if(videoQuality.equals("720p")){
				resolution="1280 x 720";
			}
		}else{
			resolution="320 x 240";
		}
		String[] dimensions = resolution.split("[ x]+");
		if (dimensions.length == 2) {
			try {
				videoWidth = Integer.parseInt(dimensions[0]);
				videoHeight = Integer.parseInt(dimensions[1]);
			} catch (NumberFormatException e) {
				videoWidth = 0;
				videoHeight = 0;
			}
		}
		// 设置相机的FPS
		int cameraFps = 15;// cameraFps=videoFps
		boolean captureQualitySlider = false;// 检查采集质量
		// 获取音频和视频的比特率值
		int videoStartBitrate = 1000;
		int audioStartBitrate = 32;

		boolean displayHud = false;// //检查统计显示选项
		boolean tracing = false;// //检查统计显示选项
		peerConnectionParameters = new PeerConnectionParameters(
				videoCallEnabled, loopback, tracing, useCamera2, videoWidth,
				videoHeight, cameraFps, videoStartBitrate, videoCodec, hwCodec,
				captureToTexture, audioStartBitrate, audioCodec,
				noAudioProcessing, aecDump, useOpenSLES, disableBuiltInAEC);
		return peerConnectionParameters;
		//
	}
	/**
	 *更新view
	 */
	public void updataViewAll() {
		VideoImplement.videoImplement.updataViewAll();
	}
	public void updataView(int i) {
		VideoImplement.videoImplement.updataView(i);
	}

	private void logAndToast(String msg) {
		Log.d(tag, "manis:"+msg);
		if (logToast != null) {
			logToast.cancel();
		}
		logToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		logToast.show();
	}




	public class LoginHTTP {
		// ------------------------------------------------------------login部分----------------------------------------
		/**
		 * login发起请求
		 */
		public void requestLogin() {
			Request<String> request = NoHttp.createStringRequest(
					Constants.SERVER + Constants.URL_NOHTTP_LOGIN,
					RequestMethod.POST);
			request.add("username", username);
			request.add("password", password);
			CallServer.getRequestInstance().add(context, 0, request,
					httpListener, true, true);
		}

		/**
		 * login接受响应
		 */
		public HttpListener<String> httpListener = new HttpListener<String>() {
			@Override
			public void onSucceed(int what, Response<String> response) {
				String result = response.get();
				Lg.i(tag, "result=" + result);
				JSONObject rootNode=JSON.parseObject(result);
				String codeNode =rootNode.getString("code");
				if (codeNode.equals("200")) {
					JSONObject jsonNode= rootNode.getJSONObject("obj");
					String guest_username = jsonNode.getString("mUserName");
					String guest_mUserId = jsonNode.getString("mUserId");
					String guest_password = jsonNode.getString("token");
					String guest_xmpp_url = jsonNode.getString("xmpp");

					JSONObject turnNode= jsonNode.getJSONObject("turn");
					JSONArray iceNode=turnNode.getJSONArray("iceServers");
					String ice_stun=iceNode.getJSONObject(0).getString("url");
					String ice_credential=iceNode.getJSONObject(1).getString("credential");
					String ice_credential_turn=iceNode.getJSONObject(1).getString("url");
					String ice_credential_username=iceNode.getJSONObject(1).getString("username");
					Key.ice_stun=ice_stun;
					Key.ice_credential=ice_credential;
					Key.ice_credential_turn=ice_credential_turn;
					Key.ice_credential_username=ice_credential_username;

					Lg.i(tag, "result=guest_username=" + guest_username);
					Lg.i(tag, "result=guest_mUserId=" + guest_mUserId);
					Lg.i(tag, "result=guest_password=" + guest_password);
					Lg.i(tag, "result=guest_xmpp_url=" + guest_xmpp_url);
					
					xmpp_userid_toupper = guest_mUserId.toUpperCase();
					xmpp_userid_toLower = guest_mUserId.toLowerCase();
					xmpp_password = guest_password;
					xmpp_username = guest_username;
					xmpp_url = guest_xmpp_url;
//					// 登录xmpp服务器
					requestLoginXmpp();
				}
				if (codeNode.equals("403")) {
					logAndToast("用户名或密码不正确");
				}
			}

			@Override
			public void onFailed(int what, String url, Object tag,
					Exception message, int responseCode, long networkMillis) {
				logAndToast("请求失败");
			}
		};

		/**
		 * 会员登录 xmpp服务器
		 */
		public void requestLoginXmpp() {
			new Thread() {
				public void run() {
					xmpp_nick = "android"
							+ Integer.toString((new Random()).nextInt(1000));
					issuccess = xmppConnection.login(context, xmpp_username,
							xmpp_password, xmpp_nick, xmpp_url,
							xmpp_userid_toLower);
					Lg.i(tag, "issuccess=" + issuccess);
					Lg.i(tag, "issuccess=" + issuccess);
					Message msg = Message.obtain();
					Bundle bundle = new Bundle();
					bundle.putBoolean("xmpp_login_result", issuccess);
					msg.obj = bundle;
					handler.sendMessage(msg);

				};
			}.start();
		}

		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				Bundle data = (Bundle) msg.obj;
				boolean issuccess = data.getBoolean("xmpp_login_result");
//				if (issuccess) {
					if (OnLoginlistener != null) {
						OnLoginlistener.LoginResult(issuccess);
					}
				}
//			else {
//					Toast.makeText(context, "登录失败！", Toast.LENGTH_SHORT).show();
//				}
//			};
		};

	}

	/***
	 * 
	 * @author WXS Guest登录部分请求
	 * 
	 */

	public class GuestLoginHttp {
		// -----------------------------------------------------------guestLogin部分---------------------------------------

		/**
		 * guest 从服务器获取nick
		 */
		private void requestGuestGetNick(String guest_nickname) {
			System.out.println("----------Constants.SERVER2---------"
					+ Constants.SERVER);
			Request<String> request = NoHttp.createStringRequest(
					Constants.SERVER + Constants.URL_NOHTTP_GUEST_LOGIN,
					RequestMethod.POST);
			request.add("nickname", guest_nickname);
			CallServer.getRequestInstance().add(context, 0, request,
					guesthttpListener, true, true);
		}

		private HttpListener<String> guesthttpListener = new HttpListener<String>() {
			@Override
			public void onSucceed(int what, Response<String> response) {
				// {"code":200,"msg":"","obj":{"mUserId":"71FB08A415371A3A","mUserName":"linpeng","token":"D6F24AABCC6BFDCF33047FBB9998A2B2","xmpp":"60.206.107.181",
				// "turn":{"iceServers":[{"url":"stun:124.202.164.3"},{"credential":"5kaSZ4J5PJQEefQWA4U0fC3b+zk=","url":"turn:124.202.164.3","username":"1472111789:manis"}]}}}
				String result = response.get();
				Lg.i(tag, "result=" + result);
				JSONObject rootNode=JSON.parseObject(result);
				String codeNode =rootNode.getString("code");
				JSONObject jsonNode= rootNode.getJSONObject("obj");
				String guest_username = jsonNode.getString("mUserName");
				String guest_mUserId = jsonNode.getString("mUserId");
				String guest_password = jsonNode.getString("token");
				String guest_xmpp_url = jsonNode.getString("xmpp");
				JSONObject turnNode= jsonNode.getJSONObject("turn");
				JSONArray iceNode=turnNode.getJSONArray("iceServers");
				String ice_stun=iceNode.getJSONObject(0).getString("url");
				String ice_credential=iceNode.getJSONObject(1).getString("credential");
				String ice_credential_turn=iceNode.getJSONObject(1).getString("url");
				String ice_credential_username=iceNode.getJSONObject(1).getString("username");
//			     turnNode={"iceServers":[{"url":"stun:124.202.164.3"},{"credential":"Yb+QEx663PPPUARxgUSLFTKYjh8=","url":"turn:124.202.164.3","username":"1481351247:manis"}]}
//				 ice_stun=stun:124.202.164.3
//				 ice_credential=Yb+QEx663PPPUARxgUSLFTKYjh8=
//				 ice_credential_url=turn:124.202.164.3
//				 ice_credential_username=1481351247:manis
//				String ice_stun="";
//				String ice_credential="";
//				String ice_credential_turn="";
//				String ice_credential_username="";

				Key.ice_stun=ice_stun;
				Key.ice_credential=ice_credential;
				Key.ice_credential_turn=ice_credential_turn;
				Key.ice_credential_username=ice_credential_username;

				xmpp_userid_toupper = guest_mUserId.toUpperCase();
				xmpp_userid_toLower = guest_mUserId.toLowerCase();
				xmpp_password = guest_password;
				xmpp_username = guest_username;
				xmpp_url = guest_xmpp_url;
//				// guest登录会控系统

				requestGuestLoginXmpp();


//				joinConferenceClient(roomnumber, conferencePass,
//						xmpp_userid_toupper);
			}

			@Override
			public void onFailed(int what, String url, Object tag,
					Exception message, int responseCode, long networkMillis) {
				logAndToast("请求失败");
			}
		};
		private void joinConferenceClient(String roomnumber,
				String conferencePass, String useridtoupper) {
			String ujid = getUserJid();
			Request<String> request = NoHttp.createStringRequest(
					Constants.SERVER
							+ Constants.URL_NOHTTP_JoinConferenceClient + "r="
							+ roomnumber + "&p="
							+ Tools.get32MD5Str(conferencePass) + "&u="
							+ useridtoupper+"&j="+ujid, RequestMethod.POST);
			CallServer.getRequestInstance().add(context, 0, request,
					httpJoinConferenceListener, true, true);
		}

		/**
		 * 登录响应
		 */
		private HttpListener<String> httpJoinConferenceListener = new HttpListener<String>() {
			@Override
			public void onSucceed(int what, Response<String> response) {
				String result = response.get();
				Lg.i(tag, "result=" + result);
				JSONObject rootNode=JSON.parseObject(result);
				String codeNode =rootNode.getString("code");
				if (codeNode.equals("200")) {
					JSONObject jsonNode= rootNode.getJSONObject("obj");
					Key.vb = jsonNode.getString("vb");
					String moderator = jsonNode.getString("moderator");
					Key.gw = jsonNode.getString("gw");
					String  ujid=xmppConnection.getConnection(context).getUser();
					if("true".equals(moderator)||ujid.equals(moderator)){
						Key.Moderator=true;
					}else{
						Key.Moderator=false;
					}

					if (type.equals("0")) {
						// guest登录，获取昵称 xmpp服务器等信息
//						requestGuestLoginXmpp();
						outOfXmpp(ujid);
					} else if (type.equals("1")) {
						// 判断会议是否超出资源 登录成功后的用户 验证会议是否存在，
						outOfXmpp(ujid);
					}
				} else if (codeNode.equals("401")) {
					logAndToast(context.getResources().getString(
							R.string.conference_emptyNameTip));
				} else if (codeNode.equals("403")) {
					logAndToast(context.getResources().getString(
							R.string.conference_lock));
				}

			}

			@Override
			public void onFailed(int what, String url, Object tag,
					Exception message, int responseCode, long networkMillis) {
				Toast.makeText(context, "请求失败！", Toast.LENGTH_SHORT).show();
			}
		};

		/**
		 * 登录xmpp服务器
		 */
		public void requestGuestLoginXmpp() {
			new Thread() {
				public void run() {
					xmpp_nick = "android"
							+ Integer.toString((new Random()).nextInt(1000));
//					xmpp_url="192.168.2.235";
					issuccess = xmppConnection.login(context, xmpp_username,
							xmpp_password, xmpp_nick, xmpp_url,
							xmpp_userid_toLower);
					Lg.i(tag, "issuccess=" + issuccess);
					Message msg = Message.obtain();
					Bundle bundle = new Bundle();
					bundle.putBoolean("guest_xmpp_login_result", issuccess);
					msg.obj = bundle;
					handler.sendMessage(msg);
				};
			}.start();
		}

		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				Bundle data = (Bundle) msg.obj;
				boolean issuccess = data.getBoolean("guest_xmpp_login_result");
				if (issuccess) {
					// 判断资源是否超出限制https://manis.fdclouds.com//user/join?u=39df48694f9af987@60.206.107.181/android806&r=109920016$c=71FB08A415371A3A
					joinConferenceClient(roomnumber, conferencePass,xmpp_userid_toupper);

				} else {
					Toast.makeText(context, "加入房间失败！", Toast.LENGTH_SHORT)
							.show();
				}
			};
		};

		/**
		 * 验证超出会议资源使用限制
		 */
		public void outOfXmpp(String ujid) {
			// https://manis.fdclouds.com/user/join?j=39df48694f9af987@60.206.107.181/ios424&r=109920008&u=39DF48694F9AF987
			Request<String> request = NoHttp.createStringRequest(
					Constants.SERVER + Constants.URL_NOHTTP_OutOfResources
							+ "j=" + ujid + "&r=" + roomnumber + "&u="
							+ xmpp_userid_toupper, RequestMethod.POST);
			CallServer.getRequestInstance().add(context, 0, request,
					httpOutOfListener, true, true);
		}

		/**
		 * 验证超出会议资源使用限制,禁止使用会议 响应
		 */
		private HttpListener<String> httpOutOfListener = new HttpListener<String>() {
			@Override
			public void onSucceed(int what, Response<String> response) {
				String result = response.get();
				Lg.i(tag, "result=" + result);
				if (result.contains("code")) {
					JSONObject rootNode=JSON.parseObject(result);
					String codeNode =rootNode.getString("code");
					if (codeNode.equals("200")) {
						if (type.equals("0")) {
							if (OnGuestLoginlistener != null) {
								OnGuestLoginlistener.GuestLoginResult(true);
							}
						} else if (type.equals("1")) {
							if (onLoginLafterJoinRoomEvents != null) {
								onLoginLafterJoinRoomEvents
										.LoginLafterJoinRoom(true);
							}
						}
					} else {
						logAndToast("超出会议资源使用限制,禁止使用会议");
					}
				} else {
					logAndToast("请求失败");
				}
			}

			@Override
			public void onFailed(int what, String url, Object tag,
					Exception message, int responseCode, long networkMillis) {
				logAndToast("请求失败");
			}
		};
	}

	public class LoginAfterCreateRoomHttp {

		public void requestXmppCreateRoom(String conferencePass,
				String hostPass, String id, String jid) {
			// Request<String> request = NoHttp.createStringRequest(
			// Constants.SERVER+Constants.URL_NOHTTP_CreateConferenceClient +
			// "p="+ conferencePass + "&op="+ hostPass + "&u="+
			// key.xmpp_userid_toupper, RequestMethod.POST);
			// CallServer.getRequestInstance().add(context, 0,
			// request,httpXmppCreateRoomListener, false, true);

			Request<String> request = NoHttp.createStringRequest(
					Constants.SERVER
							+ Constants.URL_NOHTTP_CreateConferenceClient
							+ "p=" + conferencePass + "&op=" + hostPass + "&u="
							+ xmpp_userid_toupper + "&j=" + jid,
					RequestMethod.POST);
			CallServer.getRequestInstance().add(context, 0, request,
					httpXmppCreateRoomListener, true, true);
		}

		/**
		 * 创建会议接受响应
		 */
		private HttpListener<String> httpXmppCreateRoomListener = new HttpListener<String>() {
			@Override
			public void onSucceed(int what, Response<String> response) {
				// result={"code":200,"msg":"","obj":{"op":"96490423","r":"100020007","Moderator":"true","vb":"jitsi-videobridge.60.206.107.182.60.206.107.181"}}
				String result = response.get();
				Lg.i(tag, "result=" + result);
				
				JSONObject rootNode=JSON.parseObject(result);
				String codeNode =rootNode.getString("code");

				if (codeNode.equals("200")) {
					// 成功
					String ujid=getUserJid();
					JSONObject jsonNode= rootNode.getJSONObject("obj");
					String op = jsonNode.getString("op");//主持人密码
					String roomName = jsonNode.getString("r");//会议号
					String moderator = jsonNode.getString("moderator");//是否是主持人
					String vb = jsonNode.getString("vb");//videobridge
					roomnumber = roomName;
					Key.login_iscreateroom = true;
					Key.op = op;
					if("true".equals(moderator)||ujid.equals(moderator)){
						Key.Moderator = true;
					}else{
						Key.Moderator = false;
					}
					// joinRoom();
					if (onLoginLafterJoinRoomEvents != null) {
						onLoginLafterJoinRoomEvents.LoginLafterCreateRoom(true,
								op, roomName, vb);
					}
				}
				if (codeNode.equals("500")) {
					// 会议已存在,验证会议是否存在等判断result={"code":500,"msg":"Conference exist!","obj":{"r":"100020007"}}
					JSONObject jsonNode= rootNode.getJSONObject("obj");
					String r = jsonNode.getString("r");
					roomnumber = r;
					GuestLoginHttp gl = new GuestLoginHttp();
					String ujid =getUserJid();
					gl.outOfXmpp(ujid);
				}
			}

			@Override
			public void onFailed(int what, String url, Object tag,
					Exception message, int responseCode, long networkMillis) {
				Toast.makeText(context, "请求失败！", Toast.LENGTH_SHORT).show();
			}
		};

	}

}
