package com.company.my.chatapp.typingIndicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.company.my.chatapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TypingIndicatorView extends LinearLayout {
    private static final String TAG = TypingIndicatorView.class.getSimpleName();

    private static final int BACKGROUND_TYPE_DEF_VALUE = BackgroundType.ROUNDED;
    private static final int DOT_COUNT_DEF_VALUE = 3;
    private static final int DOT_SIZE_DEF_VALUE = 14;
    private static final float DOT_MAX_COMPRESS_RATIO_DEF_VALUE = 0.5F;
    private static final int DOT_ANIMATION_DURATION_DEF_VALUE = 600;
    private static final int DOT_HORIZONTAL_SPACING_DEF_VALUE = 10;
    private static final int ANIMATE_FREQUENCY_DEF_VALUE = 100;
    private final int BACKGROUND_COLOR_DEF_VALUE = ResourcesCompat.getColor(getResources(), R.color.gray, null);
    private final int DOT_COLOR_DEF_VALUE = ResourcesCompat.getColor(getResources(), R.color.blue, null);
    private final Handler handler = new Handler();
    private final Random random = new Random();
    private final List<DotView> dotViewList = new ArrayList<>();

    @IntRange(from = 1)
    private int numOfDots;

    private boolean isAnimationStarted;

    @IntRange(from = 0)
    private int dotHorizontalSpacing;

    @IntRange(from = 1)
    private int dotSize;

    @FloatRange(from = 0F, to = 1F)
    private float dotMaxCompressRatio;

    private boolean isShowBackground;

    @BackgroundType
    private int backgroundType;

    @ColorInt
    private int backgroundColor;

    @ColorInt
    private int dotColor;

    @ColorInt
    private int dotSecondColor;

    @IntRange(from = 1)
    private int dotAnimationDuration;

    @IntRange(from = 1)
    private int animateFrequency;

    private Paint backgroundPaint;
    private SequenceGenerator sequenceGenerator;
    private final Runnable dotAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            int nextAnimateDotIndex = sequenceGenerator.nextIndex(numOfDots);
            dotViewList.get(nextAnimateDotIndex).startDotAnimation();
            long delayMillis = (animateFrequency < 0L) ? (long) (500L + (2000L * random.nextFloat())) : animateFrequency;
            if (isAnimationStarted) {
                handler.postDelayed(dotAnimationRunnable, delayMillis);
            }
        }
    };

    public TypingIndicatorView(Context context) {
        this(context, null);
    }

    public TypingIndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TypingIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttributes();
        init();
    }

    public static Path composeRoundedRectPath(float left, float top, float right, float bottom, float radius) {
        Log.d(TAG, "composeRoundedRectPath() called with: left = [" + left + "], top = [" + top + "], right = [" + right + "], bottom = [" + bottom + "], radius = [" + radius + "]");
        Path path = new Path();

        path.moveTo(left + radius, top);
        path.lineTo(right - radius, top);
        path.quadTo(right - radius / 2, top - radius / 2, right, top + radius);
        path.lineTo(right, bottom - radius);
        path.quadTo(right, bottom, right - radius, bottom);
        path.lineTo(left + radius, bottom);
        path.quadTo(left, bottom, left, bottom - radius);
        path.lineTo(left, top + radius);
        path.quadTo(left, top, left + radius, top);
        path.close();

        return path;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (getVisibility() == VISIBLE) {
                startDotAnimation();
            }
        } else {
            stopDotAnimation();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            startDotAnimation();
        } else {
            stopDotAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == VISIBLE) {
            startDotAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopDotAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int radius = Math.min(getWidth(), getHeight()) / 2;
        switch (backgroundType) {
            case BackgroundType.ROUNDED:
                canvas.drawCircle(radius, radius, radius, backgroundPaint);
                canvas.drawCircle(getWidth() - radius, radius, radius, backgroundPaint);
                canvas.drawRect(radius, 0, getWidth() - radius, getHeight(), backgroundPaint);
                break;
            case BackgroundType.SQUARE:
            default:
                canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
                break;
        }
    }

    @UiThread
    public void stopDotAnimation() {
        isAnimationStarted = false;

        // There is a report saying NPE here.  Not sure how is it possible.
        try {
            handler.removeCallbacks(dotAnimationRunnable);
        } catch (Exception ex) {
            Log.e(TAG, "stopDotAnimation: weird crash", ex);
        }
    }

    @UiThread
    public void startDotAnimation() {
        if (isAnimationStarted) {
            return;
        }

        isAnimationStarted = true;
        handler.post(dotAnimationRunnable);
    }

    public boolean isAnimationStarted() {
        return isAnimationStarted;
    }

    public void setAnimationOrder() {
        sequenceGenerator = new CircularSequenceGenerator();
    }

    private void parseAttributes() {
        dotSize = DOT_SIZE_DEF_VALUE;
        numOfDots = DOT_COUNT_DEF_VALUE;
        dotHorizontalSpacing = DOT_HORIZONTAL_SPACING_DEF_VALUE;
        dotColor = DOT_COLOR_DEF_VALUE;
        dotSecondColor = dotColor;
        dotMaxCompressRatio = DOT_MAX_COMPRESS_RATIO_DEF_VALUE;
        dotAnimationDuration = DOT_ANIMATION_DURATION_DEF_VALUE;
        isShowBackground = false;
        backgroundType = BACKGROUND_TYPE_DEF_VALUE;
        backgroundColor = BACKGROUND_COLOR_DEF_VALUE;
        animateFrequency = Math.max(dotAnimationDuration, ANIMATE_FREQUENCY_DEF_VALUE);

        if (dotMaxCompressRatio > 1F || dotMaxCompressRatio < 0F) {
            throw new IllegalArgumentException("dotMaxCompressRatio must be between 0% and 100%");
        }

        setAnimationOrder();
    }

    private void init() {
        setClipToPadding(false);
        setClipChildren(false);
        if (isShowBackground) {
            setWillNotDraw(false);
            backgroundPaint = new Paint();
            backgroundPaint.setColor(backgroundColor);
        }

        int halfHorizontalSpacing = dotHorizontalSpacing / 2;
        for (int i = 0; i < numOfDots; i++) {
            DotView dotView = createDotView();
            LinearLayout.LayoutParams layoutParams = new LayoutParams(dotSize, dotSize);
            layoutParams.setMargins(halfHorizontalSpacing, 0, halfHorizontalSpacing, 0);
            addView(dotView, layoutParams);
            dotViewList.add(dotView);
        }
    }

    private DotView createDotView() {
        Context context = getContext();
        DotView dotView;
        dotView = new GrowDotView(context);
        dotView.setAnimationDuration(dotAnimationDuration);
        dotView.setMaxCompressRatio(dotMaxCompressRatio);
        dotView.setColor(dotColor);
        dotView.setSecondColor(dotSecondColor);
        return dotView;
    }
}