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
    private static final Interpolator LARGE_OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(10f);
    private static final Interpolator SMALL_OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(1f);

    /**
     * Point till which the user can pull content in pixels.
     */
    private static int breakPoint;

    /**
     * Point after which pulling of content slows down to give a natural experience of
     * stopping instead of stopping the pull abruptly.
     */
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
        if (progressBar == null || content == null || progressBar.isIndeterminate()) return true;

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
                    //Slow down pull
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

                initialPosition();
                return true;
        }
        return true;
    }

    /**
     * Register pull to refresh listener.
     * @param content the parent view which contains related content.
     * @param progressBar present in root frame layout.
     * @param refreshListener callback listener to communicate when content should be refreshed.
     */
    public void registerRefreshLayout(View content, ProgressBar progressBar, Refresh refreshListener) {
        this.progressBar = progressBar;
        this.content = content;
        this.progressBar.setMax(breakPoint);
        this.refreshListener = refreshListener;
        setOnTouchListener(this);
    }

    /**
     * To be called onDestroy or related events to prevent memory leak.
     */
    public void deRegisterRefreshLayout() {
        progressBar = null;
        content = null;
        refreshListener = null;
    }

    /**
     * Stop refreshing when a terminating event from your refresh is received.
     */
    public void stopRefreshing() {
        if (null != progressBar && null != content) {
            progressBar.setIndeterminate(false);
            initialPosition();
        }
    }

    private void init() {
        int screenHeight;
        if (breakPoint == 0 || slowPoint == 0) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            screenHeight = displaymetrics.heightPixels;

            //Content pull able till one fifth of screen
            breakPoint = screenHeight / 5;

            //Slow point is one seventh of screen
            slowPoint = screenHeight / 7;
        }
    }

    /**
     * Refresh content by animating progressbar and content bar to slow point and calling onRefresh
     * if registered.
     */
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

    /**
     * Animate progressbar and content back to initial position.
     */
    private void initialPosition() {
        content.animate().y(0).setDuration(250).start();
        progressBar.animate().y(0).setDuration(250).start();
    }
}
