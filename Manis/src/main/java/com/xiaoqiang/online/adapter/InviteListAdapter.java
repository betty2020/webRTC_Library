package com.xiaoqiang.online.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.cisco.core.entity.Friend;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.ToastMessage;
import com.xiaoqiang.online.R;

import java.util.List;

public class InviteListAdapter extends BaseAdapter {
	private Context context;
	private List<Friend> list;

	public InviteListAdapter(Context context, List<Friend> friendsList) {
		super();
		this.context = context;
		this.list = friendsList;
	}

	public void refresh(List<Friend> friendsList) {
		this.list = friendsList;
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int arg0, View convertView, ViewGroup arg2) {
		final ViewHolder viewHodler;
		if (convertView == null) {
			convertView = View.inflate(context,
					R.layout.list_invite_text, null);
			viewHodler = new ViewHolder();
			viewHodler.btn_invite = (Button) convertView.findViewById(R.id.btn_invite);
			viewHodler.item_name = (TextView) convertView.findViewById(R.id.item_name);
			convertView.setTag(viewHodler);
		} else {
			viewHodler = (ViewHolder) convertView.getTag();
		}
		viewHodler.item_name.setText(list.get(arg0).getUserName());
		viewHodler.btn_invite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomNumber = Key.roomnumber;
				String friendid=list.get(arg0).getmUserId();
				String type = "invite";
				CiscoApiInterface.app.CallFriend(friendid, type, roomNumber);
				ToastMessage.logAndToast("invitelistadapter",context,"邀请成功");
			}
		});

		return convertView;
	}
	private class ViewHolder {
		public TextView item_name;
		public Button btn_invite;

	}
}
