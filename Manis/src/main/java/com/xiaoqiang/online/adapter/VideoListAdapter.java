package com.xiaoqiang.online.adapter;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cisco.core.entity.Participant;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.fragment.VideoFragment2;

import java.util.List;

/**
 * @author linpeng
 */
public class VideoListAdapter extends BaseAdapter {
    private VideoFragment2 context;
    private List<Participant> videoList;

    public VideoListAdapter(VideoFragment2 context, List<Participant> videoList) {
        super();
        this.context = context;
        this.videoList = videoList;

    }


    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return videoList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public View getView(final int arg0, View convertView, ViewGroup arg2) {
        final ViewHolder viewHodler;
//        if (convertView == null) {
            convertView = View.inflate(context.getActivity(), R.layout.list_videoview, null);
            viewHodler = new ViewHolder();
            viewHodler.video_relativielayout = (RelativeLayout) convertView.findViewById(R.id.video_relativielayout);

//            convertView.setTag(viewHodler);
//        } else {
//            viewHodler = (ViewHolder) convertView.getTag();
//        }
        Log.d("linpeng","linpeng,---listadapter,="+arg0+",videoList.size="+videoList.size());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 5, 0);
        RelativeLayout surfaceLayout = new RelativeLayout(context.getActivity());
        surfaceLayout.addView(videoList.get(arg0).getSurfaceView(context.getActivity()));
        surfaceLayout.setLayoutParams(layoutParams);
        videoList.get(arg0).getSurfaceView(context.getActivity()).setZOrderMediaOverlay(true);//必须layout.addView之后使用，必须动态调用。

        viewHodler.video_relativielayout.addView(surfaceLayout);

        return convertView;
    }


    private class ViewHolder {
        public RelativeLayout video_relativielayout;

    }
}
