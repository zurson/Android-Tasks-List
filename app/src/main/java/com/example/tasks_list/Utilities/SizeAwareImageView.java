package com.example.tasks_list.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.tasks_list.R;

import java.util.ArrayList;
import java.util.List;

public class SizeAwareImageView extends AppCompatImageView {

    private float lastTextSize = 0F;
    private TypedArray viewRefs = null;
    private List<View> views = new ArrayList<>();
    private final OnSizeChangedListener onSizeChangedListener = new OnSizeChangedListener() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onSizeChanged(View view, float size) {
            resolveViews();

            if (views.isEmpty())
                return;

            float minSize = views.get(0).getWidth();
            float viewSize;

            for (View it : views) {
                viewSize = it.getWidth();
                minSize = Math.min(viewSize, minSize);
            }

            for (View it : views) {
                if (it.getWidth() != minSize && it instanceof ImageView) {
                    ((ImageView) it).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }

        }
    };

    public SizeAwareImageView(Context context) {
        super(context);
    }

    public SizeAwareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SizeAwareImageView);
        int groupResId = a.getResourceId(R.styleable.SizeAwareImageView_img_group, 0);
        if (groupResId > 0) {
            viewRefs = getResources().obtainTypedArray(groupResId);
        }
        a.recycle();
    }

    public SizeAwareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SizeAwareImageView);
        int groupResId = a.getResourceId(R.styleable.SizeAwareImageView_img_group, 0);
        if (groupResId > 0) {
            viewRefs = getResources().obtainTypedArray(groupResId);
        }
        a.recycle();
    }

    public void resolveViews() {
        if (viewRefs != null) {
            View root = (View) getParent();
            while (root.getParent() instanceof View) {
                root = (View) root.getParent();
            }
            for (int i = 0; i < viewRefs.length(); i++) {
                int resId = viewRefs.getResourceId(i, 0);
                View v = root.findViewById(resId);
                if (v != null) {
                    views.add(v);
                } else {
                    Log.w(TAG, "Resource: " + resId + " not found at idx: " + i);
                }
            }
            viewRefs.recycle();
            viewRefs = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lastTextSize != getWidth()) {
            lastTextSize = getWidth();
            onSizeChangedListener.onSizeChanged(this, lastTextSize);
        }
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(View view, float size);
    }

    private static final String TAG = SizeAwareImageView.class.getSimpleName();
}
