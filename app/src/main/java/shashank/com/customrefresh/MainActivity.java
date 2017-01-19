package shashank.com.customrefresh;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RefreshLayout mainLayout;
    private ProgressBar progressBarLeft;
    private ProgressBar progressBarRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (RefreshLayout) findViewById(R.id.main_layout);
        progressBarLeft = (ProgressBar) findViewById(R.id.progressBarLeft);
        progressBarRight = (ProgressBar) findViewById(R.id.progressBarRight);

        mainLayout.registerRefreshLayout(mainLayout, progressBarLeft, progressBarRight);
    }

    @Override
    protected void onDestroy() {
        mainLayout.deRegisterRefreshLayout();
        super.onDestroy();
    }
}
