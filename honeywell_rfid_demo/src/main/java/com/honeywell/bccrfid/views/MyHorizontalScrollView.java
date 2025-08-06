package com.honeywell.bccrfid.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import com.honeywell.bccrfid.utils.LogUtils;

public class MyHorizontalScrollView extends HorizontalScrollView {
    private float xDistance, yDistance, xLast, yLast;
    public MyHorizontalScrollView(Context context) {
        super(context);
    }

    public MyHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public MyHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;

                LogUtils.e("xDistance:"+xDistance+" yDistance:"+yDistance);
                if (xDistance < yDistance) {
                    return false;
                }
        }

        return super.onInterceptTouchEvent(ev);
    }
}
