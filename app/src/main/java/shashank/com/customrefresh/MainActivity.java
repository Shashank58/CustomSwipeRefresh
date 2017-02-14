package shashank.com.customrefresh;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements RefreshLayout.Refresh, View.OnClickListener {
    private RefreshLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (RefreshLayout) findViewById(R.id.main_layout);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        View random = findViewById(R.id.random);

        random.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.random) {
            mainLayout.stopRefreshing();
        }
    }
}
