package baidumapsdk.demo.demoapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class LauncherActivity extends Activity {

    public static String mVersionName = "0.0.1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setDisplayShowHomeEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayShowTitleEnabled(false);

        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(
                    this.getPackageName(), 0);
            mVersionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {

        }

        Login_main.preUrl = getString(R.string.pre_url);
        //Login_main.preUrl = getString(R.string.test_pre_url);

        SharedPreferences mainPref = getSharedPreferences(getString(R.string.shared_pref_pacakge),
                Context.MODE_PRIVATE);
        boolean login = mainPref.getBoolean("LoggedIn", false);
        if (login) {
            Intent intent = null;
            String account = mainPref.getString("lastAccount", "");
            if (mainPref.getInt("accountType", 0) == 0) {
                intent = new Intent(LauncherActivity.this, UserView.class);
            } else if (mainPref.getInt("accountType", 0) == 1) {
                intent = new Intent(LauncherActivity.this, DistributorView.class);
            } else if (mainPref.getInt("accountType", 0) == 2) {
                intent = new Intent(LauncherActivity.this, OverlayDemo.class);
            }
            if (intent != null) {
                intent.putExtra("userPhone", account);
            } else {
                intent = new Intent(LauncherActivity.this, Login_main.class);
            }
            startActivity(intent);
        } else {
            Intent intent = new Intent(LauncherActivity.this, Login_main.class);
            startActivity(intent);
        }
        finish();
    }

}
