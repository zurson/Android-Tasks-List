package com.example.tasks_list.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.example.tasks_list.R;

import java.util.ArrayList;
import java.util.List;

public class SizeAwareTextView extends AppCompatTextView {

    private float lastTextSize = 0F;
    private TypedArray viewRefs = null;
    private List<SizeAwareTextView> views = new ArrayList<>();
    private final OnTextSizeChangedListener onTextSizeChangedListener = new OnTextSizeChangedListener() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onTextSizeChanged(SizeAwareTextView view, float textSize) {
            resolveViews();

            if (views.isEmpty())
                return;

            float minTextSize = views.get(0).getTextSize();
            float size;

            for (SizeAwareTextView it : views) {
                size = it.getTextSize();
                minTextSize = Math.min(size, minTextSize);
            }

            for (SizeAwareTextView it : views) {
                if (it.getTextSize() != minTextSize)
                    it.setAutoSizeTextTypeUniformWithPresetSizes(new int[]{(int) minTextSize}, TypedValue.COMPLEX_UNIT_PX);
            }

        }
    };

    public SizeAwareTextView(Context context) {
        super(context);
        lastTextSize = getTextSize();
    }

    public SizeAwareTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        lastTextSize = getTextSize();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SizeAwareTextView);
        int groupResId = a.getResourceId(R.styleable.SizeAwareTextView_text_group, 0);
        if (groupResId > 0) {
            viewRefs = getResources().obtainTypedArray(groupResId);
        }
        a.recycle();
    }

    public SizeAwareTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        lastTextSize = getTextSize();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SizeAwareTextView);
        int groupResId = a.getResourceId(R.styleable.SizeAwareTextView_text_group, 0);
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
                SizeAwareTextView v = root.findViewById(resId);
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
        if (lastTextSize != getTextSize()) {
            lastTextSize = getTextSize();
            onTextSizeChangedListener.onTextSizeChanged(this, lastTextSize);
        }
    }

    public interface OnTextSizeChangedListener {
        void onTextSizeChanged(SizeAwareTextView view, float textSize);
    }

    private static final String TAG = SizeAwareTextView.class.getSimpleName();
}