package com.xiaoqiang.online.customview;



import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class DrawPath {
		public Path path;// 路径
		public Paint paint;// 画笔
		public int paintColor = Color.GREEN;// 颜色
		public float paintWidth = 5;// 画笔宽度
		public DrawPath(DrawPath a){
			path =  a.path;
			paint = a.paint;
			paintColor = a.paintColor;
			paintWidth = a.paintWidth;
		}
		public DrawPath(){
			path =  new Path();
			paint = new Paint();
			paintColor = Color.BLACK;
			paintWidth = 5;
		}
}
