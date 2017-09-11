package com.xiaoqiang.online.customview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.xiaoqiang.online.commonUtils.MLog;

/**
 * Created by xiaoqiang on 2017/6/23 16:09
 * 2017 to: 邮箱：sin2t@sina.com
 * 该模块主要实现了放大和原大两个级别的缩放。
 * TODO:功能有
 * 1.以触摸点为中心放大（这个是网上其他的代码没有的）
 * 2.取消边界控制（这个是网上其他的代码没有的）也可以添加边界控制
 * 3.双击放大或缩小（主要考虑到电阻屏）
 * 4.多点触摸放大和缩小
 * 这个模块已经通过了测试，并且用户也使用有一段时间了，是属于比较稳定的了。
 * 5.新加了旋转和无极放大缩小及拖动（这是网上很难找到的）
 */
@SuppressLint("AppCompatCustomView")
public class TouchImageView extends ImageView {
    float x_down = 0;
    float y_down = 0;
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    float oldRotation = 0;
    float nowRotation = 0;
    Matrix matrix;
    Matrix matrix1 = new Matrix();
    Matrix savedMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    int mode = NONE;

    boolean matrixCheck = false;

    float widthScreen;
    float heightScreen;
    float widthImg;
    float heightImg;

    Bitmap mBitmap;
    //    Drawable drawable;
    //    private Handler handler;

    public TouchImageView(Activity activity, Bitmap bitmap) {
        super(activity);
        if (bitmap == null) {
            MLog.e("bitmap is null");
            return;
        }
        this.mBitmap = bitmap;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthScreen = dm.widthPixels;
        heightScreen = dm.heightPixels;

        widthImg = bitmap.getWidth();
        heightImg = bitmap.getHeight();

        float scaleX = widthScreen / widthImg;
        float scaleY = heightScreen / heightImg;

        scale = scaleX < scaleY ? scaleX : scaleY;
        if (scale < 1 && 1 / scale < bigScale) {
            bigScale = (float) (1 / scale + 0.5);
        }
        matrix = new Matrix();
        subX = (widthScreen - widthImg * scale) / 2;
        subY = (heightScreen - heightImg * scale) / 2;
        matrix.postScale(scale, scale);
        matrix.postTranslate(subX, subY);// 平移
        matrix.postRotate(360, widthScreen / 2, heightScreen / 2);// 旋轉
    }

    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        // 去除锯齿毛边
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        canvas.save();
        canvas.drawBitmap(mBitmap, matrix, null);
        canvas.restore();
    }

    long lastClickTime = 0; // 单击时间

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                x_down = event.getX();
                y_down = event.getY();
                if (event.getPointerCount() == 1) {
                    // 如果两次点击时间间隔小于一定值，则默认为双击事件
                    if (event.getEventTime() - lastClickTime < 300) {
                        changeSize(x_down, y_down);
                    } else if (isBig) {
                        mode = DRAG;
                    }
                }
                savedMatrix.set(matrix);
                lastClickTime = event.getEventTime();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                oldDist = spacing(event);
                oldRotation = rotation(event);
                savedMatrix.set(matrix);
                midPoint(mid, event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    matrix1.set(savedMatrix);

                    nowRotation = rotation(event);
                    float rotation = nowRotation - oldRotation;
                    float newDist = spacing(event);
                    float scale = newDist / oldDist;
                    matrix1.postScale(scale, scale, mid.x, mid.y);// 縮放
                    matrix1.postRotate(rotation, mid.x, mid.y);// 旋轉
                    matrixCheck = matrixCheck();
                    if (matrixCheck == false) {
                        matrix.set(matrix1);
                        invalidate();
                    }
                } else if ((mode == DRAG) && (isMoveX || isMoveY)) {
                    matrix1.set(savedMatrix);
                    matrix1.postTranslate(event.getX() - x_down, event.getY()
                            - y_down);// 平移
                    matrixCheck = matrixCheck();
                    matrixCheck = matrixCheck();
                    if (matrixCheck == false) {
                        matrix.set(matrix1);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            /*float rotation = Math.abs(nowRotation);
            if(rotation>=0&&rotation<=45){
                //rotation =90;
            }
            matrix.postRotate(rotation, mid.x, mid.y);// 旋轉
            invalidate();*/
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }
        return true;
    }

    Boolean isBig = false; // 是否是放大状态
    float scale; // 适合屏幕缩放倍数
    float subX;
    float subY;
    float bigScale = 3f; // 默认放大倍数
    float topHeight = 0f; // 状态栏高度和标题栏高度

    float limitX1;
    float limitX2;
    float limitY1;
    float limitY2;

    Boolean isMoveX = true; // 是否允许在X轴拖动
    Boolean isMoveY = true; // 是否允许在Y轴拖动

    private void changeSize(float x, float y) {
        if (isBig) {
            // 如果处于最大状态，则还原
            matrix.reset();
            matrix.postScale(scale, scale);
            matrix.postTranslate(subX, subY);
            isBig = false;
        } else {
            matrix.postScale(bigScale, bigScale); // 在原有矩阵后乘放大倍数
            float transX = -((bigScale - 1) * x);
            float transY = -((bigScale - 1) * (y - topHeight)); // (bigScale-1)(y-statusBarHeight-subY)+2*subY;
            float currentWidth = widthImg * scale * bigScale; // 放大后图片大小
            float currentHeight = heightImg * scale * bigScale;
            // 如果图片放大后超出屏幕范围处理
            if (currentHeight > heightScreen) {
                limitY1 = -(currentHeight - heightScreen); // 平移限制
                limitY2 = 0;
                isMoveY = true; // 允许在Y轴上拖动
                float currentSubY = bigScale * subY; // 当前平移距离
                // 平移后，内容区域上部有空白处理办法
                if (-transY < currentSubY) {
                    transY = -currentSubY;
                }
                // 平移后，内容区域下部有空白处理办法
                if (currentSubY + transY < limitY1) {
                    transY = -(currentHeight + currentSubY - heightScreen);
                }
            } else {
                // 如果图片放大后没有超出屏幕范围处理，则不允许拖动
                isMoveY = false;
            }

            if (currentWidth > widthScreen) {
                limitX1 = -(currentWidth - widthScreen);
                limitX2 = 0;
                isMoveX = true;
                float currentSubX = bigScale * subX;
                if (-transX < currentSubX) {
                    transX = -currentSubX;
                }
                if (currentSubX + transX < limitX1) {
                    transX = -(currentWidth + currentSubX - widthScreen);
                }
            } else {
                isMoveX = false;
            }

            matrix.postTranslate(transX, transY);
            isBig = true;
        }

        this.setImageMatrix(matrix);
    }

    private boolean matrixCheck() {
        float[] f = new float[9];
        matrix1.getValues(f);
        // 图片4个顶点的坐标
        float x1 = f[0] * 0 + f[1] * 0 + f[2];
        float y1 = f[3] * 0 + f[4] * 0 + f[5];
        float x2 = f[0] * mBitmap.getWidth() + f[1] * 0 + f[2];
        float y2 = f[3] * mBitmap.getWidth() + f[4] * 0 + f[5];
        float x3 = f[0] * 0 + f[1] * mBitmap.getHeight() + f[2];
        float y3 = f[3] * 0 + f[4] * mBitmap.getHeight() + f[5];
        float x4 = f[0] * mBitmap.getWidth() + f[1] * mBitmap.getHeight()
                + f[2];
        float y4 = f[3] * mBitmap.getWidth() + f[4] * mBitmap.getHeight()
                + f[5];
        // 图片现宽度
        double width = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        // 缩放比率判断
        if (width < widthScreen / 3 || width > widthScreen * 3) {
            return true;
        }
        // 出界判断
        if ((x1 < widthScreen / 3 && x2 < widthScreen / 3
                && x3 < widthScreen / 3 && x4 < widthScreen / 3)
                || (x1 > widthScreen * 2 / 3 && x2 > widthScreen * 2 / 3
                && x3 > widthScreen * 2 / 3 && x4 > widthScreen * 2 / 3)
                || (y1 < heightScreen / 3 && y2 < heightScreen / 3
                && y3 < heightScreen / 3 && y4 < heightScreen / 3)
                || (y1 > heightScreen * 2 / 3 && y2 > heightScreen * 2 / 3
                && y3 > heightScreen * 2 / 3 && y4 > heightScreen * 2 / 3)) {
            return true;
        }
        return false;
    }

    // 触碰两点间距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 取手势中心点
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    // 取旋转角度
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    // 将移动，缩放以及旋转后的图层保存为新图片
    // 本例中沒有用到該方法，需要保存圖片的可以參考
    /*public Bitmap CreatNewPhoto() {
        Bitmap bitmap = Bitmap.createBitmap(widthScreen, heightScreen,
                Config.ARGB_8888); // 背景图片
        Canvas canvas = new Canvas(bitmap); // 新建画布
        canvas.drawBitmap(bitmap, matrix, null); // 画图
        canvas.save(Canvas.ALL_SAVE_FLAG); // 保存画布
        canvas.restore();
        return bitmap;
    }*/
}
