package baidumapsdk.demo.demoapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
//import android.support.v4.view.MenuItemCompat;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
//import android.widget.ShareActionProvider;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//import android.app.ActionBar;
import android.support.v7.app.ActionBar;

import org.json.*;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.CoordinateConverter;

/**
 * 演示覆盖物的用法
 */
public class DistributorView extends ActionBarActivity {

    /**
     * MapView 是地图主控件
     */
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private InfoWindow mInfoWindow;
    private CoordinateConverter coorConverter;
    private BroadcastReceiver mGetGPSDataReceiver = null;

    private static String mUserPhone;

    public static String mUrl = null;
    private String mDisLocUrl = null;


    private Spinner mProvince;
    private Spinner mCity;
    private ArrayAdapter<String> provinceAdapter = null;
    private ArrayAdapter<String> cityAdapter = null;

    public static LatLng mCurrLoc = null;
    public static float mCurrZoom = 6.0f;
    private int mDotColor = 0xFFFF0000;

    public static List<UserLocInfo> mUserInfoList = null;
    public static boolean mGetUserInfoDone = false;
    public static int mUserTotalCount = 0;
    public static int mCurUserCount = 0;
    public static int mPageOffset = 1;
    private static int mPreUserOffset = 0;
    public final int mPageSize = 5;
    private static boolean mFirstGetUser = true;
    private static boolean mGetTotalSuccess= false;
    private final int mDisZIndex = 1024;
    private int mItemOffset = 0;
    public static LatLng mNewBatLoc = null;

    private int mAboutCode = 0x01;
    private int mUpdateCode = 0x02;

    public final String[] mSettingsList = new String[]{
            "添加用户信息",
            "关于",
            "退出"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oem_view);

        mUrl = Login_main.preUrl + getString(R.string.reseller_btyinfo);
        mDisLocUrl = Login_main.preUrl + getString(R.string.reseller_site);

        //mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //getSupportActionBar().setDisplayShowHomeEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SharedPreferences mainPref = getSharedPreferences(getString(R.string.shared_pref_pacakge),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mainPref.edit();
        editor.putString("loginView", "DistributorView");
        editor.putBoolean("LoggedIn", true);
        editor.commit();
        mUserPhone = mainPref.getString("lastAccount", "");

        String updateUrl = Login_main.preUrl + Login_main.updateEndpoint;
        new TestNetworkAsyncTask(DistributorView.this,
                TestNetworkAsyncTask.TYPE_GET_APP_VERSION,
                null).execute(updateUrl);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        if (mCurrLoc == null) {
            MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(6.0f);
            mBaiduMap.setMapStatus(msu);

            Bundle bundle = new Bundle();
            bundle.putString("resellerPhone", mUserPhone);
            new TestNetworkAsyncTask(DistributorView.this,
                    TestNetworkAsyncTask.TYPE_GET_DIS_LOC,
                    bundle).execute(mDisLocUrl);
        } else {
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(mCurrLoc, mCurrZoom);
            mBaiduMap.setMapStatus(msu);
        }
        //mBaiduMap.getUiSettings().setZoomGesturesEnabled(false);

        coorConverter = new CoordinateConverter();
        coorConverter.from(CoordinateConverter.CoordType.GPS);

        if (mUserInfoList == null) {
            mUserInfoList = new ArrayList<UserLocInfo>();
        }
        if (mUserInfoList != null && mUserInfoList.size() > 0) {
            new Thread() {
                @Override
                public void run() {
                    addAllUsers();
                }
            }.start();
        }

        if (mGetGPSDataReceiver == null) {
            mGetGPSDataReceiver = new GetGPSDataMsgReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Login_main.ACTION_DIS_GET_USER_INFO);
            filter.addAction(Login_main.ACTION_GET_DIS_LOC);
            filter.addAction(Login_main.ACTION_GET_APP_VERSION);
            LocalBroadcastManager.getInstance(DistributorView.this)
                    .registerReceiver(mGetGPSDataReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        if (!isNetworkAvailable()) {
            AlertDialogShow(getString(R.string.network_disconnect));
            return;
        }

        if ((mUserInfoList != null && mUserInfoList.size() < mUserTotalCount)
                || mFirstGetUser) {
            mFirstGetUser = false;
            Bundle bundle = new Bundle();
            bundle.putString("resellerPhone", mUserPhone);
            bundle.putInt("pageNo", mPageOffset++);
            bundle.putInt("size", mPageSize);
            new TestNetworkAsyncTask(DistributorView.this,
                    TestNetworkAsyncTask.TYPE_DIS_GET_USER_INFO,
                    bundle).execute(mUrl);
        }

        addNewBatToMap();
    }

    @Override
    protected void onDestroy() {
        if (mGetGPSDataReceiver != null) {
            LocalBroadcastManager.getInstance(DistributorView.this)
                    .unregisterReceiver(mGetGPSDataReceiver);
            mGetGPSDataReceiver = null;
        }
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addAllUsers() {
        synchronized (mUserInfoList) {
            for (int i = 0; i < mUserInfoList.size(); i++) {
                OverlayOptions testP;
                UserLocInfo temp = mUserInfoList.get(i);
                LatLng lltemp = new LatLng(temp.lat, temp.lon);
                coorConverter.coord(lltemp);
                LatLng llBaidu = coorConverter.convert();

                OverlayOptions ooDot = new DotOptions().center(llBaidu).radius(6)
                        .color(mDotColor);
                mBaiduMap.addOverlay(ooDot);
            }
        }
    }

    private void addNewBatToMap() {
        if (mNewBatLoc != null) {
            coorConverter.coord(mNewBatLoc);
            LatLng llBaidu = coorConverter.convert();
            OverlayOptions ooDot = new DotOptions().center(llBaidu).radius(6)
                    .color(mDotColor);
            mBaiduMap.addOverlay(ooDot);
            synchronized (mUserInfoList) {
                UserLocInfo userLocInfo = new UserLocInfo();
                userLocInfo.lat = mNewBatLoc.latitude;
                userLocInfo.lon = mNewBatLoc.longitude;
                mUserInfoList.add(userLocInfo);
            }
            mNewBatLoc = null;
        }
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public void AlertDialogShow(String message) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(DistributorView.this);
        builder.setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(true);
        Dialog dialog = builder.create();
        dialog.show();
    }

    public void AlertUpdateDialogShow(String message, final Intent intent) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(DistributorView.this);
        builder.setTitle("提示")
                .setMessage(message)
                .setPositiveButton(getString(R.string.update_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                AlertDialogShow(getString(R.string.update_confirm), intent);
                            }
                        })
                .setNegativeButton(getString(R.string.non_update_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .setCancelable(true);
        Dialog dialog = builder.create();
        dialog.show();
    }

    public void AlertDialogShow(String message, final Intent intent) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(DistributorView.this);
        builder.setTitle("提示")
                .setMessage(message)
                .setPositiveButton(getString(R.string.positive_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivityForResult(intent, mUpdateCode);
                                dialogInterface.dismiss();
                            }
                        })
                .setNegativeButton(getString(R.string.negative_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .setCancelable(true);
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_distributor, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        return intent;
    }

    private void resetStaticMembers() {
        mUserInfoList.clear();
        mUserTotalCount = 0;
        mCurUserCount = 0;
        mPageOffset = 1;
        mPreUserOffset = 0;
        mFirstGetUser = true;
        mItemOffset = 0;
        mCurrLoc = null;
        mCurrZoom = 6.0f;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_dis_info:
                View view = findViewById(R.id.action_dis_info);
                PopupMenu popupMenu = new PopupMenu(DistributorView.this, view);
                for (int i =0; i < mSettingsList.length; i++) {
                    popupMenu.getMenu().add(0, i, i, mSettingsList[i]);
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Bundle bundle = new Bundle();
                        switch (menuItem.getItemId()) {
                            case 0:
                                Intent intent = new Intent(DistributorView.this, AddCustomerInfo.class);
                                startActivity(intent);
                                break;
                            case 1:
                                Intent intent3 = new Intent(DistributorView.this,
                                        AboutThisApp.class);
                                startActivityForResult(intent3, mAboutCode);
                                break;
                            case 2:
                                MapStatus ms = mBaiduMap.getMapStatus();
                                SharedPreferences mainPref =
                                        getSharedPreferences(getString(R.string.shared_pref_pacakge),
                                        Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = mainPref.edit();
                                editor.putBoolean("LoggedIn", false);
                                editor.putFloat("dis_zoom", ms.zoom);
                                editor.putFloat("dis_lat", (float)ms.target.latitude);
                                editor.putFloat("dis_lon", (float) ms.target.longitude);
                                editor.commit();

                                resetStaticMembers();
                                Intent intent2 = new Intent(DistributorView.this, Login_main.class);
                                startActivity(intent2);
                                finish();
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public Intent getSupportParentActivityIntent () {
        Intent intent;
        intent = new Intent(DistributorView.this, Login_main.class);
        MapStatus ms = mBaiduMap.getMapStatus();
        mCurrLoc = ms.target;
        mCurrZoom = ms.zoom;
        return intent;
    }

    @Override
    public void onBackPressed() {
        MapStatus ms = mBaiduMap.getMapStatus();
        mCurrLoc = ms.target;
        mCurrZoom = ms.zoom;
        super.onBackPressed();
    }

    class GetGPSDataMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Login_main.ACTION_DIS_GET_USER_INFO)) {
                if (intent.getBooleanExtra("getUserInfoSuccess", false)) {
                    mGetTotalSuccess = true;
					for (int i = mPreUserOffset; i < mUserInfoList.size(); i++) {
						OverlayOptions testP;
						UserLocInfo temp = mUserInfoList.get(i);
						LatLng lltemp = new LatLng(temp.lat,temp.lon);

                        coorConverter.coord(lltemp);
                        LatLng llBaidu = coorConverter.convert();
                        OverlayOptions ooDot = new DotOptions().center(llBaidu).radius(6)
                                .color(mDotColor);
                        mBaiduMap.addOverlay(ooDot);
					}
                    mPreUserOffset = mUserInfoList.size();

                    if (intent.getIntExtra("userCount", 0) == mPageSize) {
                        Bundle bundle = new Bundle();
                        bundle.putString("resellerPhone", mUserPhone);
                        bundle.putInt("pageNo", mPageOffset++);
                        bundle.putInt("size", mPageSize);
                        new TestNetworkAsyncTask(DistributorView.this,
                                TestNetworkAsyncTask.TYPE_DIS_GET_USER_INFO, bundle)
                                .execute(mUrl);
                    } else {
                        mUserTotalCount = mCurUserCount;
                        mPageOffset = mUserTotalCount / mPageSize + 1;
                    }
                } else {
                    String msg = intent.getStringExtra("result");
                    AlertDialogShow(msg);
                }
            } else if (intent.getAction().equals(Login_main.ACTION_GET_DIS_LOC)) {
                if (intent.getBooleanExtra("getDisLoc", false)) {
                    Double lat = Double.parseDouble(intent.getStringExtra("latitude"));
                    Double lon = Double.parseDouble(intent.getStringExtra("longitude"));
                    LatLng ll = new LatLng(lat, lon);
                    MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(ll, 9.0f);
                    mBaiduMap.animateMapStatus(msu, 300);
                } else {
                    String msg = intent.getStringExtra("result");
                    AlertDialogShow(msg);
                }
            }  else if (intent.getAction().equals(Login_main.ACTION_GET_APP_VERSION)) {
                processAppUpdate(intent);
            }
        }

        public void processAppUpdate(Intent intent) {
            if (intent.getBooleanExtra("getAppVersion", false)) {
                String apkVersion = intent.getStringExtra("apkVersion");
                String downloadUrl = intent.getStringExtra("downloadUrl");
                String updateMsg = "最新版本：V" + apkVersion + " "
                        + "已经可用，为保证此应用程序以后能正常工作，请您进行下载更新。";
                Intent intent4 = new Intent(DistributorView.this, UpdateApp.class);
                intent4.putExtra("apkUrl", downloadUrl);
                AlertUpdateDialogShow(updateMsg, intent4);
            }
        }
    }

    public static class UserLocInfo {
        UserLocInfo() {}
        double lat;
        double lon;
    }
}
