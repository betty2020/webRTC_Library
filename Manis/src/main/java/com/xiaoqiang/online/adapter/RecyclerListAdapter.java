package com.xiaoqiang.online.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cisco.core.entity.Participant;
import com.cisco.core.util.SdkPublicKey;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;

import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.List;
import java.util.Map;

/**
 * Created by xiaoqiang on 2017/7/19 9:56
 * 2017 to: 邮箱：sin2t@sina.com
 * androidApp2
 */

public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.mHolder> {
    private Map<String, Participant> personList;
    private Context context;
    private List<String> jidList;

    public RecyclerListAdapter(Map<String, Participant> personList, Context context, List<String> jidList) {
        this.personList = personList;
        this.context = context;
        this.jidList = jidList;
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
        //void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public RecyclerListAdapter.mHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.video_item, null);
        return new RecyclerListAdapter.mHolder(context, view);
    }

    @Override
    public void onBindViewHolder(RecyclerListAdapter.mHolder holder, int position) {
        MLog.e("数据总量：" + personList.size() + "当前ID：" + position);
        if (personList != null && personList.size() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Participant p = personList.get(jidList.get(position));
                    MLog.e("name=" + p.getNickname());
                    MediaStream stream = p.getStream();
                    VideoTrack track = stream.videoTracks.get(0);
                    VideoRenderer vr = new VideoRenderer(holder.surface);
                    track.addRenderer(vr);

                    holder.nick_name.setText(p.getNickname());
                    holder.loading.setVisibility(View.GONE);
                }
            }, 500);

            if (mOnItemClickLitener != null) {
                holder.surface.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getLayoutPosition();
                        mOnItemClickLitener.onItemClick(holder.surface, pos);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if (personList != null && personList.size() > 0) {
            return personList.size();
        }
        return 0;
    }

    public static class mHolder extends RecyclerView.ViewHolder {
        private SurfaceViewRenderer surface;
        private TextView nick_name;
        private ImageView loading;

        public mHolder(Context context, View itemView) {
            super(itemView);
            surface = (SurfaceViewRenderer) itemView.findViewById(R.id.surface);
            surface.setZOrderMediaOverlay(true);
            surface.init(SdkPublicKey.rootEglBase.getEglBaseContext(), null);
            SdkPublicKey.initSurfaceView(surface);

            nick_name = (TextView) itemView.findViewById(R.id.nick_name);
            loading = (ImageView) itemView.findViewById(R.id.loading);
            loading.setVisibility(View.VISIBLE);
            nick_name.bringToFront();

            RelativeLayout layout = (RelativeLayout) itemView.findViewById(R.id.layout_group);
            LinearLayout.LayoutParams layoutParams = InitComm.init().initLayout(4);
            layoutParams.setMargins(0, 0, 0, 5);
            layout.setLayoutParams(layoutParams);

        }


    }
}
