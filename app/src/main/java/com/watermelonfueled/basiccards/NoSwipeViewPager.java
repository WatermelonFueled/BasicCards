package com.watermelonfueled.basiccards;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

/**
 * Created by dapar on 2017-04-05.
 */

public class NoSwipeViewPager extends ViewPager {
    private final int SCROLL_DURATION_FACTOR = 2;

    public NoSwipeViewPager(Context context) {
        super(context);
        initialize();
    }

    public NoSwipeViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialize();
    }

    private void initialize() {
        this.setPageTransformer(true, new DelaySwipePageTransformer());
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);
            CustomDurationScroller customScroller = new CustomDurationScroller(getContext(), (Interpolator) interpolator.get(null));
            customScroller.setScrollFactor(SCROLL_DURATION_FACTOR);
            scroller.set(this, customScroller);
        } catch (Exception e) {}
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    private class DelaySwipePageTransformer implements PageTransformer {
        @Override
        public void transformPage(View page, float position) {
            if (position <= -1) {
                page.setAlpha(0);
                page.setTranslationX(-1);
            } else if (position <= 0) {
                page.setAlpha(1+position);
                pageSwipeUp(page, position);
            } else if (position <= 1) {
                page.setAlpha(1-position);
                pageSwipeUp(page, position);
            } else {
                page.setAlpha(0);
            }
        }
        private void pageSwipeUp(View page, float position) {
            page.setTranslationX(page.getWidth() * -position);
            page.setTranslationY(page.getHeight() * position);
        }
    }

    private class CustomDurationScroller extends Scroller {
        private double scrollFactor;

        public CustomDurationScroller(Context context, Interpolator inter) {
            super(context, inter);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, (int)(duration*scrollFactor));
        }

        public void setScrollFactor(double scrollFactor) {
            this.scrollFactor = scrollFactor;
        }
    }
}

