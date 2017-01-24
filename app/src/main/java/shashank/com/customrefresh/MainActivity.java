package shashank.com.customrefresh;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements RefreshLayout.Refresh{
    private RefreshLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (RefreshLayout) findViewById(R.id.main_layout);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mainLayout.registerRefreshLayout(mainLayout, progressBar, this);
    }

    @Override
    protected void onDestroy() {
        mainLayout.deRegisterRefreshLayout();
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        
    }
}
