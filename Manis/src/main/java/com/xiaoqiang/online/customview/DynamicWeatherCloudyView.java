package com.xiaoqiang.online.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.xiaoqiang.online.commonUtils.MLog;

import java.io.InputStream;

@SuppressLint("AppCompatCustomView")
public class DynamicWeatherCloudyView extends ImageView implements Runnable {
    /**
     * 要处理的图
     */
    private Bitmap bitmap;
    private int left;
    private int left1;
    /**
     * 图片移动频率
     */
    private int dx = 1;
    private int dy = 1;
    private int sleepTime;
    /**
     * 图片是否在移动
     */
    private static boolean IsRunning = true;
    private Handler handler;

    private Thread thread;

    public DynamicWeatherCloudyView(Context context, int resource, int screenWidth, int screenHeiht, int sleepTime) {
        super(context);
        this.left = screenWidth;
        this.left1 = screenWidth;
        this.sleepTime = sleepTime;

        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        InputStream is = this.getResources().openRawResource(resource);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // decode的bitmap为null,只是把图片的宽高放在Options里
        Bitmap bitmap1 = BitmapFactory.decodeStream(is, null, options);// 这里返回的bmp是null
        float realWidth = options.outWidth;
        float realHeight = options.outHeight;
        MLog.d("原始数据 图片高度：" + realHeight + "宽度:" + realWidth);
        // 计算缩放比
        int h = (int) realHeight / screenHeiht;
        MLog.d("缩放比例计算值:" + h);
        if (h < 1) {
            h = 1;// 如果短边比屏幕小就不缩放
        }
        options.inJustDecodeBounds = false;// 这次要设为 false,这次图片要读取出来
        options.inScaled = true; //是否支持缩放，当设置了这个，Bitmap将会以inTargetDensity的值进行缩放
        options.inSampleSize = h;
        options.inDither = false;    /*不进行图片抖动处理*/
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true; // 当存储Pixel的内存空间在系统内存不足时是否可以被回收
        options.inInputShareable = true; // 为true情况下才生效，是否可以共享一个InputStream
        InputStream iss = this.getResources().openRawResource(resource);//此处须重新获取，如重复使用InputStream则bitmap为空
        bitmap = BitmapFactory.decodeStream(iss, null, options);
        if (bitmap == null) {
            MLog.e("bitmap为空");
        }
        MLog.d("适配屏幕缩放 图片高度：" + bitmap.getHeight() + "宽度:" + bitmap.getWidth());
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                DynamicWeatherCloudyView.this.invalidate();//会触发View的重绘
            }
        };
        thread=new Thread(this);
    }

    public void move() {
//        new Thread(this).start();
        thread.start();
    }
    public void stop() {
        if(DynamicWeatherCloudyView.IsRunning) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
                thread = null;
            }
            IsRunning=false;
        }
    }

    /**
     * 注：Bitmap对象是存储在Java堆中的，所以在回收Bitmap时，需要回收两个部分的空间：native和java堆。
     * 即先调用recycle()释放native中Bitmap的像素数据，再对Bitmap对象置null，保证GC对Bitmap对象的回收
     * <3.0以上的系统中，无需主动调用recycle()，只需将对象置null，由GC自动管理/>
     */
    //    public void recycle() {
    //        if (!bitmap.isRecycled()) {
    //            bitmap=null;
    //        }
    //    } // 系统回收时机很难把握，放弃此方法
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, left, 0, null);
    }

    @Override
    public void run() {
        while (DynamicWeatherCloudyView.IsRunning) {
            if ((bitmap != null) && (left > (getWidth()))) {
                left = -bitmap.getWidth();
            }
            left = left + dx;
            if (left < -200) {
                handler.sendMessage(handler.obtainMessage());
            } else {
                left = left1;
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
