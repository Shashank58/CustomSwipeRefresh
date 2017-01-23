package shashank.com.customrefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * Created by shashankm on 12/01/17.
 */

public class RefreshLayout extends FrameLayout implements View.OnTouchListener {
    private static final String TAG = RefreshLayout.class.getSimpleName();
    private static final Interpolator LARGE_OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(10f);
    private static final Interpolator SMALL_OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(1f);

    private static int screenHeight;
    private static int breakPoint;
    private static int slowPoint;

    private float initialTouchPos;
    private ProgressBar progressBar;
    private View content;
    private Refresh refreshListener;

    interface Refresh {
        void onRefresh();
    }

    public RefreshLayout(Context context) {
        super(context);
        init();
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (progressBar == null || content == null) return true;

        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initialTouchPos = event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                final float y = event.getRawY() - initialTouchPos;
                if (y <= 0) return true;

                progressBar.setProgress((int) y);

                if (y < slowPoint) {
                    content.setTranslationY(y);
                    progressBar.setTranslationY(y);
                    return true;
                }

                if (y < breakPoint) {
                    float slowedY = slowPoint + ((y - slowPoint) / 2);
                    content.setTranslationY(slowedY);
                    progressBar.setTranslationY(slowedY);
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                final float finalY = event.getRawY() - initialTouchPos;
                initialTouchPos = 0;

                if (finalY > breakPoint) {
                    progressBar.animate().scaleX(0.9f).scaleY(0.9f)
                            .setInterpolator(LARGE_OVERSHOOT_INTERPOLATOR)
                            .setDuration(250)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    animateBackContentAndRefresh();
                                }
                            })
                            .start();
                    return true;
                }

                content.animate().y(0).setDuration(200).start();
                progressBar.animate().y(0).setDuration(200).start();
                return true;
        }
        return true;
    }

    public void registerRefreshLayout(View content, ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.content = content;
        this.progressBar.setMax(breakPoint);
    }

    public void setOnRefreshListener(Refresh refreshListener) {
        this.refreshListener = refreshListener;
    }

    public void deRegisterRefreshLayout() {
        progressBar = null;
        content = null;
        refreshListener = null;
    }

    private void init() {
        setOnTouchListener(this);
        if (screenHeight == 0 || breakPoint == 0 || slowPoint == 0) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            screenHeight = displaymetrics.heightPixels;
            breakPoint = screenHeight / 5;
            slowPoint = screenHeight / 7;
        }
    }

    private void animateBackContentAndRefresh() {
        progressBar.setIndeterminate(true);
        content.animate().y(slowPoint)
                .setInterpolator(SMALL_OVERSHOOT_INTERPOLATOR)
                .setDuration(200).setListener(null).start();
        progressBar.animate().y(breakPoint - slowPoint)
                .setInterpolator(SMALL_OVERSHOOT_INTERPOLATOR)
                .setDuration(200).setListener(null).start();
        if (refreshListener != null) refreshListener.onRefresh();
    }
}
