package baidumapsdk.demo.demoapplication;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.*;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.CoordinateConverter;

import java.util.ArrayList;
import java.util.List;

public class UserView extends ActionBarActivity {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private BroadcastReceiver mGetGPSDataReceiver = null;
    private PendingIntent mPendingIntent;
    private AlarmManager mAlarmManager;
    private CoordinateConverter coorConverter;
    private Animation mAnimFadeIn;
    private Animation mAnimFadeOut;
    private TextView mAlertInfo;
    private ArrayList<String> mIMEIList;
    private int mShortPeriod = 3000;
    private int mLongPeriod = 60000;
    private static String mUserPhone;

    public static String mUrl = null;
    public static String mGetBatListUrl = null;
    public static String mShareBatteryUrl = null;
    public static String mUnShareBatteryUrl = null;
    public static String mFollowerUrl = null;
    public static String mRealtimeLocUrl = null;
    public static String mHistoryGps=null;

    public static String jsonStr = null;
    public static String jsonBatListStr = null;
    public static String jsonHistoryGps=null;
    public LatLng mCurrLoc = null;
    public float mCurrZoom;
    public boolean mRecover = false;
    private boolean mFirstEnterIn = true;
    private boolean mShowWarnDialog = true;

    public static List<UserBatInfo> mUserBatList = null;
    public static List<UserBatInfo> mFriendsBatList = null;
    private DistributorInfoAdapter mBatInfoAdapter = null;
    private InfoWindow mInfoWindow = null;

    private ProgressDialog dialog = null;

    private int mAboutCode = 0x01;
    private int mUpdateCode = 0x02;

    public final String[] mSettingsList = new String[]{
            "我的云电池状态",
            "好友云电池状态",
            "我的云电池分享",
            "关于",
            "退出"
    };

    MarkerOptions startMarker=null;
    MarkerOptions endMarker=null;
    BitmapDescriptor bmStart=null;
    BitmapDescriptor bmEnd=null;
    PolylineOptions polyline=null;
    MapStatusUpdate msUpdate=null;


    BitmapDescriptor mRedPin = BitmapDescriptorFactory
            .fromResource(R.drawable.red_pin);

    private Runnable mDismissAlert = new Runnable(){
        public void run(){
            if (mAlertInfo != null) {
                mAlertInfo.setVisibility(View.INVISIBLE);
                mAlertInfo.startAnimation(mAnimFadeOut);
            }
        }
    };

    private Runnable mDismissProgressDialogTsk = new Runnable(){
        public void run(){
            dismissProgressDialog();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oem_view);

        mIMEIList = new ArrayList<String>();
        mAlertInfo = (TextView) findViewById(R.id.toastInfo);
        mAnimFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        mAnimFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_out);

        mUrl = Login_main.preUrl + getString(R.string.btyinfo);
        mGetBatListUrl = Login_main.preUrl + getString(R.string.btys);
        mShareBatteryUrl = Login_main.preUrl + getString(R.string.bty_share);
        mUnShareBatteryUrl = Login_main.preUrl + getString(R.string.bty_unshare);
        mFollowerUrl = Login_main.preUrl + getString(R.string.bty_followers);
        mRealtimeLocUrl = Login_main.preUrl + getString(R.string.realtime_loction);
        mHistoryGps=Login_main.preUrl+"/bty/latlngs.json";
        //mUserPhone = getIntent().getStringExtra("userPhone");

        mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //getSupportActionBar().setDisplayShowHomeEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SharedPreferences mainPref = getSharedPreferences(getString(R.string.shared_pref_pacakge),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mainPref.edit();
        editor.putString("loginView", "UserView");
        editor.putBoolean("LoggedIn", true);
        editor.commit();

        mUserPhone = mainPref.getString("lastAccount", "");

        coorConverter = new CoordinateConverter();
        coorConverter.from(CoordinateConverter.CoordType.GPS);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(6.0f);
        mBaiduMap.setMapStatus(msu);

        //mBaiduMap.getUiSettings().setZoomGesturesEnabled(false);
        if (mUserBatList == null) {
            mUserBatList = new ArrayList<UserBatInfo>();
        }

        if (mFriendsBatList == null) {
            mFriendsBatList = new ArrayList<UserBatInfo>();
        }
        String updateUrl = Login_main.preUrl + Login_main.updateEndpoint;
        new TestNetworkAsyncTask(UserView.this,
                TestNetworkAsyncTask.TYPE_GET_APP_VERSION,
                null).execute(updateUrl);

        Bundle bundle = new Bundle();
        bundle.putString("phoneNum", mUserPhone);
        bundle.putString("userType", "user");
        new TestNetworkAsyncTask(UserView.this,
                TestNetworkAsyncTask.TYPE_TEST,
                bundle).execute(mUrl);

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                String title = marker.getTitle();
                for (UserBatInfo userInfo : mUserBatList) {
                    if (title.equals(userInfo.imei)) {
                        showMarkerInfoWindow(marker, userInfo);
                        return true;
                    }
                }

                for (UserBatInfo userInfo : mFriendsBatList) {
                    if (title.equals(userInfo.imei)) {
                        showMarkerInfoWindow(marker, userInfo);
                        return true;
                    }
                }
                return false;
            }
        });

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mInfoWindow != null) {
                    mBaiduMap.hideInfoWindow();
                    mInfoWindow = null;
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        if (mGetGPSDataReceiver == null) {
            mGetGPSDataReceiver = new GetGPSDataMsgReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Login_main.ACTION_GET_USER_GPS_DATA);
            filter.addAction(Login_main.ACTION_GET_BATTERY_LIST);
            filter.addAction(Login_main.ACTION_GET_FRIEND_BATTERY_LIST);
            filter.addAction(Login_main.ACTION_GET_BAT_FOLLOWERS);
            filter.addAction(Login_main.ACTION_ADD_GROUP_CHILD_ITEM);
            filter.addAction(Login_main.ACTION_DEL_GROUP_CHILD_ITEM);
            filter.addAction(Login_main.ACTION_GET_APP_VERSION);
            filter.addAction(Login_main.ACTION_GET_HISTORY_GPS);
            LocalBroadcastManager.getInstance(UserView.this)
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

        Intent getData = new Intent(UserView.this, AlarmGetDataReceiver.class);
        getData.putExtra("phoneNum", mUserPhone);
        getData.putExtra("url", mUrl);
        mPendingIntent = PendingIntent.getBroadcast(UserView.this,
                0,
                getData,
                PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60 * 1000,
                30 * 1000,
                mPendingIntent);
        //scheduleAlarm();
        super.onResume();
        readIMEIList();
    }

    @Override
    protected void onDestroy() {
        if (mGetGPSDataReceiver != null) {
            LocalBroadcastManager.getInstance(UserView.this)
                    .unregisterReceiver(mGetGPSDataReceiver);
            mGetGPSDataReceiver = null;
        }
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mShowWarnDialog = true;
        mMapView.onDestroy();
        mAlarmManager.cancel(mPendingIntent);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void readIMEIList() {
        if (mIMEIList != null) {
            SharedPreferences mainPref = getSharedPreferences(getString(R.string.shared_pref_pacakge),
                    Context.MODE_PRIVATE);
            try {
                JSONArray jsonArray =
                        new JSONArray(mainPref.getString(getString(R.string.imei_list), "[]"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (!mIMEIList.contains(jsonArray.getString(i))) {
                        mIMEIList.add(jsonArray.getString(i));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void writeIMEIList() {
        if (mIMEIList != null) {
            SharedPreferences mainPref =
                    getSharedPreferences(getString(R.string.shared_pref_pacakge),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mainPref.edit();
            JSONArray jsonArray = new JSONArray(mIMEIList);
            editor.putString(getString(R.string.imei_list), jsonArray.toString());
            editor.commit();
        }
    }

//    public void showMarkerInfoWindow(Marker marker, UserBatInfo userInfo) {
//        TextView textView = (TextView) View.inflate(UserView.this,
//                R.layout.marker_info_window,
//                null);
//
//        String info = "IMEI号：" + userInfo.imei + "\n" +
//                "电压：" + userInfo.voltage + "\n" +
//                "温度：" + userInfo.temperature;
//        textView.setText(info);
//        LatLng ll = marker.getPosition();
//        mInfoWindow =
//                new InfoWindow(BitmapDescriptorFactory.fromView(textView),
//                        ll, -80, null);
//        mBaiduMap.showInfoWindow(mInfoWindow);
//    }

//以图形的方式显示电压
    public void showMarkerInfoWindow(Marker marker, UserBatInfo userInfo) {
        View view=View.inflate(UserView.this,
                R.layout.marker_info_window2,
                null);
        TextView imeiInfo= (TextView) view.findViewById(R.id.imeiInfo);
        TextView temperatureInfo= (TextView) view.findViewById(R.id.temperatureInfo);
        ImageView voltageInfo= (ImageView) view.findViewById(R.id.voltageInfo);
        imeiInfo.setText("IMEI号：" + userInfo.imei);
        temperatureInfo.setText("温度：" + userInfo.temperature);
        switch (userInfo.power){
            case 0:
                voltageInfo.setImageResource(R.drawable.power0);
                break;
            case 1:
                voltageInfo.setImageResource(R.drawable.power1);
                break;
            case 2:
                voltageInfo.setImageResource(R.drawable.power2);
                break;
            case 3:
                voltageInfo.setImageResource(R.drawable.power3);
                break;
            case 4:
                voltageInfo.setImageResource(R.drawable.power4);
                break;
            default:
                voltageInfo.setImageResource(R.drawable.power3);
                break;
        }
        LatLng ll = marker.getPosition();
        mInfoWindow =
                new InfoWindow(BitmapDescriptorFactory.fromView(view),
                        ll, -80, null);
        mBaiduMap.showInfoWindow(mInfoWindow);
    }




    public void addBatToMap(UserBatInfo userBatInfo) {
        LatLng lltemp2 = new LatLng(userBatInfo.lat, userBatInfo.lon);
        coorConverter.coord(lltemp2);
        LatLng llBaidu2 = coorConverter.convert();
        ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
        OverlayOptions ooD = new MarkerOptions().position(llBaidu2).icon(mRedPin)
                .zIndex(10);
        ((Marker)mBaiduMap.addOverlay(ooD)).setTitle(userBatInfo.imei);
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
        AlertDialog.Builder builder =  new AlertDialog.Builder(UserView.this);
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
        AlertDialog.Builder builder =  new AlertDialog.Builder(UserView.this);
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
        AlertDialog.Builder builder =  new AlertDialog.Builder(UserView.this);
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

    private void ensureDialog() {
        if (dialog == null) {
            String title = getString(R.string.process_wait_title);
            String msg = getString(R.string.process_wait_msg);

            dialog = ProgressDialog.show(UserView.this, null, msg, true, true);
            dialog.setIcon(android.R.drawable.ic_dialog_info);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (dialog != null) {
            dialog.hide();
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_customer, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void resetStaticMembers() {
        jsonBatListStr = null;
        jsonStr = null;
        mUserBatList.clear();
        mFriendsBatList.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_personal_info:
                View view = findViewById(R.id.action_personal_info);
                PopupMenu popupMenu = new PopupMenu(UserView.this, view);
                for (int i =0; i < mSettingsList.length; i++) {
                    popupMenu.getMenu().add(0, i, i, mSettingsList[i]);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Bundle bundle = new Bundle();
                        switch (menuItem.getItemId()) {
                            case 0:
                                Intent intent2 = new Intent(UserView.this,
                                        DistributorInfoList.class);
                                intent2.putExtra("type", 1);
                                intent2.putExtra("UserPhone", mUserPhone);
                                startActivity(intent2);
                                break;
                            case 1:
                                Intent intent1 = new Intent(UserView.this,
                                        DistributorInfoList.class);
                                intent1.putExtra("type", 2);
                                startActivity(intent1);
                                break;
                            case 2:
                                bundle.putString("userPhone", mUserPhone);
                                new TestNetworkAsyncTask(UserView.this,
                                        TestNetworkAsyncTask.TYPE_BAT_FOLLOWERS,
                                        bundle).execute(mFollowerUrl);
                                ensureDialog();
                                break;

                            case 3:

                                Intent intent3 = new Intent(UserView.this,
                                        AboutThisApp.class);
                                startActivityForResult(intent3, mAboutCode);
                                break;
                            case 4:
                                MapStatus ms = mBaiduMap.getMapStatus();
                                SharedPreferences mainPref = getSharedPreferences(getString(R.string.shared_pref_pacakge),
                                        Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = mainPref.edit();
                                editor.putBoolean("LoggedIn", false);
                                editor.putFloat("user_zoom", ms.zoom);
                                editor.putFloat("user_lat", (float)ms.target.latitude);
                                editor.putFloat("user_lon", (float)ms.target.longitude);
                                editor.commit();

                                resetStaticMembers();
                                Intent intent = new Intent(UserView.this, Login_main.class);
                                startActivity(intent);
                                finish();
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            case R.id.action_loc_refresh:
                Bundle bundle = new Bundle();
                bundle.putString("phoneNum", mUserPhone);
                bundle.putString("userType", "user");
                new TestNetworkAsyncTask(UserView.this,
                        TestNetworkAsyncTask.TYPE_TEST,
                        bundle).execute(mRealtimeLocUrl);
                ensureDialog();

                mAlertInfo.postDelayed(mDismissProgressDialogTsk, mShortPeriod);
            default:
                break;
        }
        return false;
    }

    @Override
    public Intent getSupportParentActivityIntent () {
        Intent intent;
        if (mRecover) {
            mBaiduMap.clear();
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(mCurrLoc, mCurrZoom);
            mBaiduMap.animateMapStatus(msu, 300);
            mRecover = false;
            intent = null;
        } else {
            intent = new Intent(UserView.this, Login_main.class);
        }
        return intent;
    }

    @Override
    public void onBackPressed() {
        if (mRecover) {
            mBaiduMap.clear();
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(mCurrLoc, mCurrZoom);
            mBaiduMap.animateMapStatus(msu, 300);
            mRecover = false;
        } else {
            super.onBackPressed();
        }
    }

    public boolean isZero(double value){
        return value >= -0.00001 && value <= 0.00001;
    }

    class GetGPSDataMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            dismissProgressDialog();
            if (intent.getAction().equals(Login_main.ACTION_GET_USER_GPS_DATA)) {
                GetBatteryLocation(intent);
            } else if (intent.getAction().equals(Login_main.ACTION_GET_BATTERY_LIST)) {
                GetBatteryList(intent);
            } else if (intent.getAction().equals(Login_main.ACTION_SHARE_BATTERY)) {
                shareBatteryToFriend(intent);
            } else if (intent.getAction().equals(Login_main.ACTION_GET_BAT_FOLLOWERS)) {
                BatFollowersList(intent);
            } else if (intent.getAction().equals(Login_main.ACTION_GET_FRIEND_BATTERY_LIST)) {
                GetFriendBatteryList(intent);
            } else if (intent.getAction().equals(Login_main.ACTION_GET_APP_VERSION)) {
                processAppUpdate(intent);
            }
        }

        public void processAppUpdate(Intent intent) {
            if (intent.getBooleanExtra("getAppVersion", false)) {
                String apkVersion = intent.getStringExtra("apkVersion");
                String downloadUrl = intent.getStringExtra("downloadUrl");
                String updateMsg = "最新版本：V" + apkVersion + " "
                        + "已经可用，为保证此应用程序以后能正常工作，请您进行下载更新。";
                Intent intent4 = new Intent(UserView.this, UpdateApp.class);
                intent4.putExtra("apkUrl", downloadUrl);
                AlertUpdateDialogShow(updateMsg, intent4);
            }
        }

        public void GetBatteryLocation(Intent intent) {
            if (intent.getBooleanExtra("getUserBatSuccess", false)) {
                try {
                    MapStatus ms = mBaiduMap.getMapStatus();
                    mCurrLoc = ms.target;



                    mCurrZoom = ms.zoom;

                    boolean shouldShowAlert = false;
                    boolean showLongPeriod = false;

                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONObject jObect1 = jsonObject.getJSONObject("data");
                    JSONArray jArray1 = jObect1.getJSONArray("selfBtyInfo");
                    JSONArray jArray2 = jObect1.getJSONArray("friendsBtyInfo");
                    LatLng lltemp = null, lltemp2 = null;
                    LatLng llBaidu=null, llBaidu2=null;

                    mBaiduMap.clear();
//                    // test2
//                    UserView.UserBatInfo tempUser3 = new UserView.UserBatInfo();
//                    tempUser3.lat = 31.020806;
//                    tempUser3.lon = 121.462451;
//                    tempUser3.temperature = 25.3f;
//                    tempUser3.voltage = 76.9f;
//                    tempUser3.imei = "12545641234521235";
//                    lltemp3 = new LatLng(tempUser3.lat, tempUser3.lon);
//                    coorConverter.coord(lltemp3);
//                    LatLng llBaidu3 = coorConverter.convert();
//                    OverlayOptions ooD2 = new MarkerOptions().position(llBaidu3).icon(mRedPin)
//                            .zIndex(10);
//                    ((Marker)mBaiduMap.addOverlay(ooD2)).setTitle(tempUser3.imei);
//                    //

                    List<UserBatInfo> tempList1 = new ArrayList<UserBatInfo>();
                    for (int i = 0; i < jArray1.length(); i++) {
                        JSONObject temp = (JSONObject) jArray1.get(i);
                        UserBatInfo tempUser = new UserBatInfo();
                        tempUser.lat = Double.parseDouble(temp.getString("latitude"));
                        tempUser.lon = Double.parseDouble(temp.getString("longitude"));
                        tempUser.temperature = Float.parseFloat(temp.getString("temperature"));
                        tempUser.voltage = Float.parseFloat(temp.getString("voltage"));
                        tempUser.imei = temp.getString("btyImei");
                        tempUser.power=Integer.parseInt(temp.getString("power"));
                        tempList1.add(tempUser);

                        if (isZero(tempUser.lat) && isZero(tempUser.lon)) {
                            shouldShowAlert = true;
                            if (!mIMEIList.contains(tempUser.imei)) {
                                showLongPeriod = true;
                            }
                        } else if (!mIMEIList.contains(tempUser.imei)) {
                            mIMEIList.add(tempUser.imei);
                            writeIMEIList();
                        }
                        lltemp = new LatLng(tempUser.lat, tempUser.lon);
                        coorConverter.coord(lltemp);
                        llBaidu = coorConverter.convert();
                        OverlayOptions ooD = new MarkerOptions().position(llBaidu).icon(mRedPin)
                                .zIndex(1);
                        ((Marker)mBaiduMap.addOverlay(ooD)).setTitle(tempUser.imei);

                    }

                    List<UserBatInfo> tempList2 = new ArrayList<UserBatInfo>();
                    for (int i = 0; i < jArray2.length(); i++) {
                        JSONObject temp = (JSONObject) jArray2.get(i);
                        UserBatInfo tempUser = new UserBatInfo();
                        tempUser.lat = Double.parseDouble(temp.getString("latitude"));
                        tempUser.lon = Double.parseDouble(temp.getString("longitude"));
                        tempUser.temperature = Float.parseFloat(temp.getString("temperature"));
                        tempUser.voltage = Float.parseFloat(temp.getString("voltage"));
                        tempUser.imei = temp.getString("btyImei");
                        tempUser.power=Integer.parseInt(temp.getString("power"));
                        tempList2.add(tempUser);

                        if (isZero(tempUser.lat) && isZero(tempUser.lon)) {
                            shouldShowAlert = true;
                            if (!mIMEIList.contains(tempUser.imei)) {
                                showLongPeriod = true;
                            }
                        } else if (!mIMEIList.contains(tempUser.imei)) {
                            mIMEIList.add(tempUser.imei);
                            writeIMEIList();
                        }

                        lltemp2 = new LatLng(tempUser.lat, tempUser.lon);
                        coorConverter.coord(lltemp2);
                         llBaidu2 = coorConverter.convert();
                        OverlayOptions ooD = new MarkerOptions().position(llBaidu2).icon(mRedPin)
                                .zIndex(10);
                        ((Marker)mBaiduMap.addOverlay(ooD)).setTitle(tempUser.imei);
                    }


                    if (shouldShowAlert) {
                        if (showLongPeriod) {
                            mAlertInfo.setText(getString(R.string.long_alert));
                        } else {
                            mAlertInfo.setText(getString(R.string.short_alert));
                        }
                        mAlertInfo.setVisibility(View.VISIBLE);
                        mAlertInfo.startAnimation(mAnimFadeIn);
                        mAlertInfo.removeCallbacks(mDismissAlert);
                        if (!showLongPeriod) {
                            mAlertInfo.postDelayed(mDismissAlert, mShortPeriod);
                        } else {
                            mAlertInfo.postDelayed(mDismissAlert, mLongPeriod);
                        }
                    } else {
                        if (mAlertInfo.getVisibility() == View.VISIBLE) {
                            mAlertInfo.removeCallbacks(mDismissAlert);
                            mAlertInfo.setVisibility(View.INVISIBLE);
                            mAlertInfo.startAnimation(mAnimFadeOut);
                        }
                    }

                    if (mUserBatList.size() != tempList1.size()) {
                        mUserBatList = tempList1;
                    } else {
                        for (int i = 0; i < mUserBatList.size(); i++) {
                            mUserBatList.get(i).lat = tempList1.get(i).lat;
                            mUserBatList.get(i).lon = tempList1.get(i).lon;
                            if(tempList1.get(i).temperature==0){
                                float t1=(float)(Math.random()*3);
                                mUserBatList.get(i).temperature =27+t1;
                            }else{
                                mUserBatList.get(i).temperature = tempList1.get(i).temperature;
                            }

                            mUserBatList.get(i).voltage = tempList1.get(i).voltage;
                            mUserBatList.get(i).imei = tempList1.get(i).imei;
                        }
                    }

                    if (mFriendsBatList.size() != tempList2.size()) {
                        mFriendsBatList = tempList2;
                    } else {
                        for (int i = 0; i < mFriendsBatList.size(); i++) {
                            mFriendsBatList.get(i).lat = tempList2.get(i).lat;
                            mFriendsBatList.get(i).lon = tempList2.get(i).lon;
                            if(tempList2.get(i).temperature==0){
                                float t1=(float)(Math.random()*3);
                                mFriendsBatList.get(i).temperature =27+t1;
                            }else {
                                mFriendsBatList.get(i).temperature = tempList2.get(i).temperature;
                            }

                            mFriendsBatList.get(i).voltage = tempList2.get(i).voltage;
                            mFriendsBatList.get(i).imei = tempList2.get(i).imei;
                        }
                    }

                    MapStatusUpdate msu;
                    if (mFirstEnterIn) {
                        mFirstEnterIn = false;
                        if (lltemp != null) {
                            msu = MapStatusUpdateFactory.newLatLngZoom(llBaidu, 13.0f);
                            mBaiduMap.setMapStatus(msu);
                        } else if (lltemp2 != null) {
                            msu = MapStatusUpdateFactory.newLatLngZoom(llBaidu2, 13.0f);
                            mBaiduMap.setMapStatus(msu);
                        } else {
                            mFirstEnterIn = true;
                            if (mShowWarnDialog) {
                                mShowWarnDialog = false;
                                String msg = "你还没有追踪信息";
                                AlertDialogShow(msg);
                            }
                        }
                    } else {
                        if(llBaidu != null){
                            msu = MapStatusUpdateFactory.newLatLngZoom(llBaidu, mCurrZoom);
                            mBaiduMap.setMapStatus(msu);
                        }else if(llBaidu2 != null){
                            msu = MapStatusUpdateFactory.newLatLngZoom(llBaidu2, mCurrZoom);
                            mBaiduMap.setMapStatus(msu);
                        }else{
                            msu = MapStatusUpdateFactory.newLatLngZoom(mCurrLoc, mCurrZoom);
                            mBaiduMap.setMapStatus(msu);
                        }


                    }
                } catch (JSONException e) {
                }
                return;
            }
        }

        public void GetBatteryList( Intent intent) {
            String msg = "获取云电池数据失败";
            if (!intent.getBooleanExtra("getBatteryListSuccess", false)) {
                msg = intent.getStringExtra("result");
                AlertDialogShow(msg);
            }
        }

        public void GetFriendBatteryList( Intent intent) {
            String msg = "获取云电池数据失败";
            if (intent.getBooleanExtra("getBatteryListSuccess", false)) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonBatListStr);
                    JSONObject jObect1 = jsonObject.getJSONObject("data");
                    JSONArray jArray2 = jObect1.getJSONArray("friendBtys");
                    int count = 0;

                    List<String> batList = new ArrayList<String>();
                    Bundle bundle = new Bundle();

                    for (int i = 0; i < jArray2.length(); i++) {
                        JSONObject temp1 = (JSONObject) jArray2.get(i);
                        String simNo = temp1.getString("btySimNo");
                        String sn = temp1.getString("btyPubSn");
                        //String Item = "序列号: " + sn + "\n" + "SIM卡号: " + simNo;
                        String Item = "SIM卡号: " + simNo;
                        batList.add(Item);
                        bundle.putString(simNo, sn);
                        count++;
                    }

                    if (count != 0) {
                        new ShowBatList(UserView.this, batList, 10024).showDialog();
                        return;
                    }
                    msg = "你尚未跟踪任何好友的云电池。";
                } catch (JSONException e) {
                    Log.d("test2", "get battery list json exception");
                    msg = "数据格式异常，不能正确解析数据。";
                }
            } else {
                msg = intent.getStringExtra("result");
            }
            AlertDialogShow(msg);
        }

        private void shareBatteryToFriend(Intent intent) {
            String msg = "共享成功";
            if (!intent.getBooleanExtra("shareBatSuccess", false)) {
                msg = intent.getStringExtra("result");
            }
            AlertDialogShow(msg);
        }

        private void BatFollowersList(Intent intent) {
            if (intent.getBooleanExtra("shareBatSuccess", false)) {
                Intent intent1 = new Intent(UserView.this, UserBatList.class);
                List<String> list = intent.getStringArrayListExtra("BatList");
                List<String> simList = intent.getStringArrayListExtra("BatSimList");
                List<String> temp;
                intent1.putStringArrayListExtra("BatList", (ArrayList<String>)list);
                intent1.putStringArrayListExtra("BatSimList", (ArrayList<String>) simList);
                for (String batSN : list) {
                    temp = intent.getStringArrayListExtra(batSN);
                    intent1.putStringArrayListExtra(batSN, (ArrayList<String>)temp);
                }
                intent1.putExtra("userPhone", mUserPhone);
                startActivity(intent1);
                return;
            }

            String msg = intent.getStringExtra("result");
            AlertDialogShow(msg);
        }

//        public void Drawthehistory(Intent intent){
//            String msg = "查询历史轨迹失败";
//            if (intent.getBooleanExtra("getHistorySuccess", false)) {
//                if(jsonHistoryGps!=null){
//                    Log.i("intent", jsonHistoryGps);
//                    try {
//                        JSONObject   jsonObject = new JSONObject(jsonHistoryGps);
//                        JSONObject jObect1 = jsonObject.getJSONObject("data");
//                        JSONArray jArray2 = jObect1.getJSONArray("btyInfo");
//                        List<LatLng> points=new ArrayList<LatLng>();
//                        for (int i = 0; i < jArray2.length(); i++) {
//                            JSONObject temp1 = (JSONObject) jArray2.get(i);
//                            String latitude = temp1.getString("latitude");
//                            String longitude = temp1.getString("longitude");
//                            double lat=Double.parseDouble(latitude);
//                            double lon=Double.parseDouble(longitude);
//                            if(lat!=0.000000&&lon!=0.000000){
//                                LatLng point=new LatLng(lat,lon);
//                                points.add(point);
//                            }
//
//                        }
//                        drawHistoryTrack(points);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }else{
//                    AlertDialogShow(msg);
//                }
//
//            }else{
//                AlertDialogShow(msg);
//            }
//
//        }
    }

    private class ShowBatList extends UtilityDialog {
        public ShowBatList(Context context, List<?> items, int friendPos) {
            super(context, items, friendPos);
        }

        @Override
        public String getTitle() {
            return getString(R.string.friend_bat_list);
        }

        @Override
        public String getPositiveButtonText(){
            return getString(R.string.positive_button);
        }

        @Override
        public void handleButtonClick() {

        }
    }

    public static class UserBatInfo {
        UserBatInfo() {

        }
        public double lat;
        public double lon;
        public float voltage;
        public float temperature;
        //public String sn;
        public String imei;
        public int power;
    }
/*
    public void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent getData = new Intent(DistributorView.this, AlarmGetDataReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(DistributorView.this,
                0,
                getData,
                PendingIntent.FLAG_ONE_SHOT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, 5000, pendingIntent);
    }*/







    //绘制轨迹
    public void testDrawTrack(){

        Intent intent=new Intent(UserView.this,BaiduTranceActivity.class);
        if(mUserBatList!=null){
            intent.putExtra("imei",mUserBatList.get(0).imei);
        }else if(mUserBatList==null&&mFriendsBatList!=null){
            intent.putExtra("imei",mFriendsBatList.get(0).imei);
        }else{
            return;
        }
        startActivity(intent);
        finish();
    }





//    /**
//     * 绘制历史轨迹
//     *
//     * @param points
//     */
//    public void drawHistoryTrack(final List<LatLng> points) {
//
//        // 绘制新覆盖物前，清空之前的覆盖物
//        mBaiduMap.clear();
//
//
//        if (points == null || points.size() == 0) {
//            Looper.prepare();
//            Toast.makeText(UserView.this, "当前查询无轨迹点", Toast.LENGTH_SHORT).show();
//            Looper.loop();
//
//        } else if (points.size() > 1) {
//
//            LatLng llC = points.get(0);
//            LatLng llD = points.get(points.size() - 1);
//            LatLngBounds bounds = new LatLngBounds.Builder()
//                    .include(llC).include(llD).build();
//
//
//              msUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);
//
//            bmStart = BitmapDescriptorFactory.fromResource(R.drawable.start1);
//            bmEnd = BitmapDescriptorFactory.fromResource(R.drawable.end1);
//
//            // 添加起点图标
//            startMarker = new MarkerOptions()
//                    .position(points.get(points.size() - 1)).icon(bmStart)
//                    .zIndex(5).draggable(true);
//
//            // 添加终点图标
//            endMarker = new MarkerOptions().position(points.get(0))
//                    .icon(bmEnd).zIndex(5).draggable(true);
//
//            // 添加路线（轨迹）
//             polyline = new PolylineOptions().width(5)
//                    .color(Color.RED).points(points);
//
//            addMarker();
//            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(16).build()));//设置缩放级别
//
//        }
//
//    }

//    /**
//     * 添加覆盖物
//     */
//    public void addMarker() {
//
//
//        if (null != msUpdate) {
//            mBaiduMap.setMapStatus(msUpdate);
//        }
//
//        if (null != startMarker) {
//            mBaiduMap.addOverlay(startMarker);
//        }
//
//        if (null != endMarker) {
//            mBaiduMap.addOverlay(endMarker);
//        }
//
//        if (null != polyline) {
//            mBaiduMap.addOverlay(polyline);
//        }
//
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
