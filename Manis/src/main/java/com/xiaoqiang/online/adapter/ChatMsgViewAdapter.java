package com.xiaoqiang.online.adapter;

import android.app.ActivityManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiaoqiang.online.R;
import com.xiaoqiang.online.javaBeen.ChatingRecord;

import java.util.List;

public class ChatMsgViewAdapter extends BaseAdapter {

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}


	private List<ChatingRecord> coll;
	
	

	public List<ChatingRecord> getColl() {
		return coll;
	}

	public void setColl(List<ChatingRecord> coll) {
		this.coll = coll;
	}

	private Context ctx;

	private LayoutInflater mInflater;

	public ChatMsgViewAdapter(Context context, List<ChatingRecord> coll) {
		ctx = context;
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		final int memClass = ((ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		ChatingRecord entity = coll.get(position);

		if (entity.getType()==1) {
			return IMsgViewType.IMVT_COM_MSG;
		} else {
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	public int getViewTypeCount() {
		return 2;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		final ChatingRecord entity = coll.get(position);
		int isComMsg = entity.getType();

		final ViewHolder viewHolder ;;
	  
		if (convertView == null) {
			viewHolder = new ViewHolder();
			if (isComMsg==1) {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_left, null);
//				viewHolder.tvUserName = (TextView) convertView
//						.findViewById(R.id.tv_username);
			} else {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_right, null);
			}
			viewHolder.tvUserName = (TextView) convertView
					.findViewById(R.id.tv_username);
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.tvTime = (TextView) convertView
					.findViewById(R.id.tv_time);
			viewHolder.isComMsg = isComMsg;
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
//		if (isComMsg==1){
//		if(key.map.size()>0){
//			viewHolder.tvUserName.setText(Key.map.get(entity.getFriendName()));
		viewHolder.tvUserName.setText(entity.getUserName());
//		}
			
//		}
		viewHolder.tvSendTime.setText(entity.getDate());
			viewHolder.tvContent.setText(entity.getChatingText());			
			viewHolder.tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			viewHolder.tvTime.setText("");
		
		
		return convertView;
	}

	static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public TextView tvTime;
		public int isComMsg = 1;
	}
	 
	 

}
