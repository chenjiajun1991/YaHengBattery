package baidumapsdk.demo.demoapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;

public class AboutThisApp extends ActionBarActivity {
    private ComponentName mParentActivity;
    private String mVersion;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_info);

        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(
                    this.getPackageName(), 0);
            mVersion = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {

        }
        mParentActivity = getCallingActivity();
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView log_des = (TextView) findViewById(R.id.logo_description);
        /*TextView sam_des = (TextView) findViewById(R.id.sam_info);
        TextView app_sta = (TextView) findViewById(R.id.app_statement);*/
        String des = "版本号：" + mVersion;
        log_des.setText(des);
        /*app_sta.setText(getString(R.string.sam_statement));
        sam_des.setText(getString(R.string.sam_info));*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public Intent getSupportParentActivityIntent () {
        Intent intent;
        intent = new Intent(this, mParentActivity.getClass());
        return intent;
    }
}
