package com.xiaoqiang.online.javaBeen;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cisco.core.entity.Participant;

/**
 * Created by qihao on 2017/7/10 20:26
 * 2017 to: 邮箱：sin2t@sina.com
 * androidApp
 */

public class ParticipantInfo {
    private Participant participant;
    private RelativeLayout surfaceLayout;
    private TextView nameText;
    //    private VideoRenderer renderer;
    //    private MediaStream stream;
    //    private VideoTrack track;

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public RelativeLayout getSurfaceLayout() {
        return surfaceLayout;
    }

    public void setSurfaceLayout(RelativeLayout surfaceLayout) {
        this.surfaceLayout = surfaceLayout;
    }

    public TextView getNameText() {
        return nameText;
    }

    public void setNameText(TextView nameText) {
        this.nameText = nameText;
    }

    public ParticipantInfo(Participant participant, RelativeLayout surfaceLayout, TextView nameText) {
        this.participant = participant;
        this.surfaceLayout = surfaceLayout;
        this.nameText = nameText;
    }

    public ParticipantInfo() {
    }
}
