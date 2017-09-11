package com.xiaoqiang.online.customview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class DrawingWithBezier extends View
{
    private float mX;
    private float mY;

    private final Paint mGesturePaint = new Paint();
    private final Path mPath = new Path();
    
    
    boolean isClick = false;
    
    
    public DrawingWithBezier(Context context)
    {
        super(context);
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setStyle(Style.STROKE);
        mGesturePaint.setStrokeWidth(5);
        mGesturePaint.setColor(Color.BLUE);
        
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // TODO Auto-generated method stub
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                 break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            	clickEvent(event);
            	break;
        }
        //���»���
        invalidate();
        return true;
    }
    public void startPanit(float x, float y){
        mPath.lineTo(x, y);
        invalidate();
    }
    private void clickEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		float moveX = Math.abs(x- mX);
		float moveY = Math.abs(y-mY);
		if(moveX == 0 && moveY ==0)
		{
			isClick = true;
			mPath.lineTo(x+2, y+2); 
			mX = x+2; 
			mY = y+2;
			invalidate();
		}
		
	}

	@Override
    protected void onDraw(Canvas canvas)
    {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        canvas.drawPath(mPath, mGesturePaint);
    }

    private void touchDown(MotionEvent event)
    {
      
        //mPath.rewind();
//        mPath.reset();
        float x = event.getX();
        float y = event.getY();
        
        mX = x;
        mY = y;
        mPath.moveTo(x, y);
    }
    
    private void touchMove(MotionEvent event)
    {
        final float x = event.getX();
        final float y = event.getY();

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);
        
        if (dx >= 3 || dy >= 3)
        {
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;

            mPath.quadTo(previousX, previousY, cX, cY);

            mX = x;
            mY = y;
            isClick = false;
        }
    }
    
}
