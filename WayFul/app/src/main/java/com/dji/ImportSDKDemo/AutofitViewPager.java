package com.dji.ImportSDKDemo;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * 根据View的内容自动适应高度的ViewPager
 * Created by mChenys on 2017/1/11.
 */
public class AutofitViewPager extends ViewPager {

//    private static final String TAG = "AutofitViewPager";
//
//    public AutofitViewPager(Context context) {
//        this(context,null);
//    }
//
//    public AutofitViewPager(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        addOnPageChangeListener(new OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                requestLayout();
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//            }
//        });
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        Log.d(TAG, "onMeasure");
//        // find the current child view
//        View view;
//        view = getChildAt(getCurrentItem());
//        if (view != null) {
//            // measure the current child view with the specified measure spec
//            view.measure(widthMeasureSpec, heightMeasureSpec);
//        }
//
//        setMeasuredDimension(getMeasuredWidth(), measureHeight(heightMeasureSpec, view));
//    }
//
//    /**
//     * Determines the height of this view
//     *
//     * @param measureSpec A measureSpec packed into an int
//     * @param view the base view with already measured height
//     *
//     * @return The height of the view, honoring constraints from measureSpec
//     */
//    private int measureHeight(int measureSpec, View view) {
//        int result = 0;
//        int specMode = MeasureSpec.getMode(measureSpec);
//        int specSize = MeasureSpec.getSize(measureSpec);
//
//        if (specMode == MeasureSpec.EXACTLY) {
//            result = specSize;
//        } else {
//            // set the height from the base view if available
//            if (view != null) {
//                result = view.getMeasuredHeight();
//            }
//            if (specMode == MeasureSpec.AT_MOST) {
//                result = Math.min(result, specSize);
//            }
//        }
//        return result;
//    }
    public AutofitViewPager(Context context) {
        super(context);
    }

    public AutofitViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = 0;
        //下面遍历所有child的高度
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if (h > height) //采用最大的view的高度。
                height = h;
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}

