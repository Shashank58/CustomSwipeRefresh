package shashank.com.customrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * Created by shashankm on 12/01/17.
 */

public class RefreshLayout extends FrameLayout implements View.OnTouchListener {
    private static final String TAG = RefreshLayout.class.getSimpleName();

    private float initialTouchPos;
    private ProgressBar progressBarLeft;
    private ProgressBar progressBarRight;
    private View content;

    public RefreshLayout(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
    }

    public void registerRefreshLayout(View content, ProgressBar progressBarLeft, ProgressBar progressBarRight) {
        this.progressBarLeft = progressBarLeft;
        this.progressBarRight = progressBarRight;
        this.content = content;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (progressBarLeft == null || progressBarRight == null ||content == null) return true;

        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initialTouchPos = event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                final float y = event.getRawY() - initialTouchPos;
                if (y <= 0) return true;

                Log.d(TAG, "onTouch: Before - " + progressBarLeft.getHeight());
                progressBarLeft.setProgress((int) y);
                progressBarRight.setProgress((int) y);

                if (y < 150) {
                    content.setTranslationY(y);
                    return true;
                }

                if (y < 300) {
                    float slowedY = 150 + (y - 150) / 2;
                    content.setTranslationY(slowedY);
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                final float finalY = event.getRawY() - initialTouchPos;
                initialTouchPos = 0;

                if (finalY > 150) {
                    progressBarLeft.setIndeterminate(true);
                    progressBarRight.setIndeterminate(true);

                    Log.d(TAG, "onTouch: After - " + progressBarLeft.getHeight());
                    return true;
                }

                content.animate().y(0).setDuration(150).start();
                return true;
        }
        return true;
    }

    public void deRegisterRefreshLayout() {
        progressBarLeft = null;
        progressBarRight = null;
        content = null;
    }
}
