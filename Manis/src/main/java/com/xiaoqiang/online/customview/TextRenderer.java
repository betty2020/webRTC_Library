package com.xiaoqiang.online.customview;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.TextureView;

import java.io.IOException;

/**
 * Created by xiaoqiang on 2017/7/20 10:17
 * 2017 to: 邮箱：sin2t@sina.com
 * androidApp2
 */

public class TextRenderer extends TextureView implements
        TextureView.SurfaceTextureListener {

    public TextRenderer(Context context) {
        super(context);
    }

    public TextRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextRenderer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        initCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if(null != mCamera){
            if(isPreview){
                mCamera.stopPreview();
            }
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private SurfaceTexture mSurfaceTexture;
    private boolean isPreview = false;
    private Camera mCamera;
    public void initCamera(){
        if(!isPreview && null != mSurfaceTexture){
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
        }
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size mSize = parameters.getSupportedPreviewSizes().get(0);
        parameters.setPreviewSize(mSize.width, mSize.height);
        parameters.setPreviewFpsRange(4, 10);
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setJpegQuality(80);
        parameters.setPictureSize(mSize.width, mSize.height);
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //mCamera.setParameters(parameters);
        mCamera.startPreview();
        isPreview = true;
    }
}
