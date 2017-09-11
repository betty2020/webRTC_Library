package com.cisco.core.util;

import android.content.Context;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

/**
 * @author linpeng
 */
public class SdkPublicKey {

    /**
     * 全局变量，非退出操作时，禁止clear
     */
    public static EglBase rootEglBase = null;

    //创建完整功能的对象
    public static SurfaceViewRenderer createSurfaceView(Context context) {
        SurfaceViewRenderer surfaceView = new SurfaceViewRenderer(context);
        surfaceView.init(SdkPublicKey.rootEglBase.getEglBaseContext(), null);//初始化SurfaceView功能
        return surfaceView;
    }

    // 初始化SurfaceView配置参数
    public static void initSurfaceView(SurfaceViewRenderer surfaceView) {
        surfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);//设置视频的方式将填补允许布局区域。
        surfaceView.setMirror(false);//设置是否应该反映视频流。
    }

}
