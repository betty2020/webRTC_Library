package com.xiaoqiang.online.customview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class WhiteBoardView extends View {

	private Bitmap mBitmap;
	private Canvas mCanvas;
	private Path mPath;
	private Paint mBitmapPaint;// 画布的画笔
	private Paint mPaint;// 真实的画笔
	private int screenWidth, screenHeight;// 屏幕长宽
	private Path totalPath;// 全部的路径
	private DrawPath total_rubberPath;// 全部的橡皮路径

	private int flag_isRubber = 0;// 0:画刷;1:橡皮
	private Paint paint_rubber;
	private Paint paint_pen;

	// 保存Path路径的集合,用List集合来模拟栈
	private static List<DrawPath> savePath;// 撤销掉的Path信息
	private static List<DrawPath> deletePath;// 需要重做的Path信息(即撤销掉的path)
	// 记录Path路径的对象
	private DrawPath dp;

	public WhiteBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
        //1080:1800
		//1080:790
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		screenHeight = getResources().getDisplayMetrics().heightPixels;
//		screenWidth=1080;
//		screenHeight=790;
		Log.d("callactivity", "linpeng,WhiteBoardView-screenWidth=" + screenWidth + "=screenHeight=" + screenHeight);
		mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.RGB_565);
		mBitmap.eraseColor(Color.TRANSPARENT);// 设置Bitmap透明


		// 保存一次一次绘制出来的图形
		mCanvas = new Canvas(mBitmap);
		mCanvas.drawColor(Color.TRANSPARENT);
		Log.d("callactivity", "linpeng,WhiteBoardView-mCanvas-getWidth=" + mCanvas.getWidth() + "=getHeight=" + mCanvas.getHeight());
		totalPath = new Path();// 初始化
		total_rubberPath = new DrawPath();// 初始化

		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		mPaint = new Paint();
		paint_pen = new Paint();
		paint_rubber = new Paint();

		initPaintPen();
		initPaintRubber();
		savePath = new ArrayList<DrawPath>();
		deletePath = new ArrayList<DrawPath>();

	}

	// 初始化普通画笔
	public void initPaintPen() {
		paint_pen.setAntiAlias(true);
		paint_pen.setStyle(Paint.Style.STROKE);
		paint_pen.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		paint_pen.setStrokeCap(Paint.Cap.SQUARE);// 形状
		paint_pen.setStrokeWidth(5);// 画笔宽度
		paint_pen.setColor(Color.GREEN);
	}

	// 初始化橡皮
	public void initPaintRubber() {
		paint_rubber.setAntiAlias(true);
		paint_rubber.setStyle(Paint.Style.STROKE);
		paint_rubber.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		paint_rubber.setStrokeCap(Paint.Cap.SQUARE);// 形状
		paint_rubber.setStrokeWidth(5);// 画笔宽度
		paint_rubber.setColor(Color.BLUE);

		paint_rubber.setXfermode(new PorterDuffXfermode(Mode.DST_OUT)); // 设置画笔的痕迹是透明的，从而可以看到背景图片

	}

	public void onDraw(Canvas canvas) {
		// canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
		canvas.drawColor(Color.TRANSPARENT);

		// 将前面已经画过得显示出来
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		if (mPath != null) {
			// 实时的显示
			canvas.drawPath(mPath, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		static final int NONE = 0;
//		static final int DRAG = 1;
//		static final int ZOOM = 2;
//		int mode = NONE;
//		float oldDist = 1f;
//		float mr=0;
//		float mx;
//		float my;

//		switch (event.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			System.out.println("linpeng,action_down-x="+ event.getX()+"-y="+event.getY());
//			break;
//		case MotionEvent.ACTION_MOVE:
//			System.out.println("linpeng,ACTION_MOVE-x="+ event.getX()+"-y="+event.getY());
//			break;
//		case MotionEvent.ACTION_UP:
//			System.out.println("linpeng,ACTION_UP-x="+ event.getX()+"-y="+event.getY());
//		default:
//			break;
//		}
//		float size = event.getSize();
//		int szi = (int) size;
//		int dxi = szi >> 12;
//		int dyit = ((1 << 12) - 1);
//		int dyi = szi & dyit;
//
//		DisplayMetrics metrics = getResources().getDisplayMetrics();
//		float dx = metrics.widthPixels * dxi / (float) dyit;
//		float dy = metrics.heightPixels * dyi / (float) dyit;
//
//		x1 = event.getX();
//		y1 = event.getY();
//
//		x2 = x1 + dx;
//		y2 = y1 + dy;

		invalidate();

		return true;
	}
	// 设置开始画
		public void startPaint(float x, float y,float x1, float y1) {
			if (flag_isRubber == 0)
				mPaint = paint_pen;
			else
				mPaint = paint_rubber;
			mPath = new Path();// 每次down下去重新new一个Path
			// 每一次记录的路径对象是不一样的
			dp = new DrawPath();
			dp.path = mPath;
			dp.paint = mPaint;
			dp.paintColor = mPaint.getColor();
			dp.paintWidth = mPaint.getStrokeWidth();
			mCanvas.drawLine(x, y,x1, y1, mPaint);
			invalidate();
		}
	// 设置绘画字
	public void drawingText(float x, float y,String text) {
		if (flag_isRubber == 0)
			mPaint = paint_pen;
		else
			mPaint = paint_rubber;
		mPath = new Path();// 每次down下去重新new一个Path
		// 每一次记录的路径对象是不一样的
		dp = new DrawPath();
		dp.path = mPath;
		dp.paint = mPaint;
		dp.paintColor = mPaint.getColor();
		dp.paintWidth = mPaint.getStrokeWidth();
		mPaint.setStrokeWidth(3);
		mPaint.setTextSize(34);
//		mPaint.setColor(Color.RED);
		mPaint.setTextAlign(Paint.Align.LEFT);
		Rect bounds = new Rect();
		mPaint.getTextBounds(text, 0, text.length(), bounds);

		mCanvas.drawText(text,x, y, mPaint);
		invalidate();
//		mCanvas.drawText(testString, getMeasuredWidth()/2 - bounds.width()/2, getMeasuredHeight()/2 + bounds.height()/2, mPaint);
	}
	public DrawPath getRubberPath() {

		Iterator<DrawPath> iter = savePath.iterator();
		while (iter.hasNext()) {
			DrawPath dp = iter.next();
			total_rubberPath.paint = paint_rubber;
			if (dp.paint == paint_rubber) {
				total_rubberPath.path.addPath(dp.path);
			}
		}
		return total_rubberPath;
	}

	public Path getpath() {
		for (int i = 0; i < savePath.toArray().length; i++) {
			Iterator<DrawPath> iter = savePath.iterator();
			while (iter.hasNext()) {
				DrawPath dp = iter.next();
				if (dp.paint == paint_pen) {
					totalPath.addPath(dp.path);
				}
			}
		}
		return totalPath;
	}

	public Paint getpaint() {
		Paint p = new Paint(dp.paint);
		return p;
	}

	// -------------------------------------功能-------------------------------------------------
	// 撤销的核心思想就是将画布清空， 将保存下来的Path路径最后一个移除掉， 重新将路径画在画布上面。
	public void undo() {
		mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.RGB_565);
		mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
		// 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉...
		if (savePath != null && savePath.size() > 0) {// 判断savePath不为空

			deletePath.add(savePath.get(savePath.size() - 1));// 将要撤销的路径保存在deletePath中
			savePath.remove(savePath.size() - 1);// 移除最后一个path,相当于出栈操作

			Iterator<DrawPath> iter = savePath.iterator();
			while (iter.hasNext()) {
				DrawPath drawPath = iter.next();
				drawPath.paint.setColor(drawPath.paintColor);// 因为颜色
																// 宽度是可变的,所以要再设置一下
				drawPath.paint.setStrokeWidth(drawPath.paintWidth);
				// totalPath.reset();
				// totalPath.set(savePath.path);
				mCanvas.drawPath(drawPath.path, drawPath.paint);
			}// 不是最后一个路径就画上
			invalidate();// 刷新
		}
	}

	// 重做的核心思想就是将撤销的路径保存到另外一个集合里面(栈)， 然后从redo的集合里面取出最顶端对象， 画在画布上面即可。
	public void redo() {
		if (deletePath.size() > 0) {
			DrawPath dp = deletePath.get(deletePath.size() - 1);
			dp.paint.setColor(dp.paintColor);// 因为颜色 宽度是可变的,所以要再设置一下
			dp.paint.setStrokeWidth(dp.paintWidth);
			mCanvas.drawPath(dp.path, dp.paint);
			savePath.add(dp);
			deletePath.remove(dp);
			mPath = null;
			// totalPath.reset();
			// totalPath.set(dp.path);
			invalidate();// 刷新
		}
	}

	// 清空画板
	public void clear() {
		mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.RGB_565);
		mCanvas.setBitmap(mBitmap);
		if (savePath != null && savePath.size() > 0) {// 判断savePath不为空
			// 清空savePath中所有元素,相当于出栈操作
			savePath.clear();
		}
		if (deletePath != null && deletePath.size() > 0) {// 判断deletePath不为空
			deletePath.clear();
		}
		mBitmap=null;
		invalidate();
	}

	// 设置当前画笔为普通画笔
	public void setPaintPen() {
		flag_isRubber = 0;
	}

	// 设置当前画笔为橡皮
	public void setPaintRubber() {
		flag_isRubber = 1;
	}
	
	public boolean isRubber() {
		if(flag_isRubber == 1){
			return true;
		}
		else{
			return false;
		}
	}

	// 设置画刷颜色
	public void setPaintColor(int mycolor) {
		paint_pen.setColor(mycolor);
	}

	// 设置画笔宽度
	public void setPaintWidth(int mywidth) {
		paint_pen.setStrokeWidth(mywidth);// 画刷宽度
		paint_rubber.setStrokeWidth(mywidth);// 橡皮宽度
	}

	public Bitmap getbitmap() {
		return mBitmap;
	}



}
