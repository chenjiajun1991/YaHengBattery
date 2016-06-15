package baidumapsdk.demo.demoapplication;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.os.Looper;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;


import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnTrackListener;
import com.baidu.trace.TraceLocation;
import com.baidu.trace.Trace;




@SuppressLint("NewApi")
public class BaiduTranceActivity extends FragmentActivity implements OnClickListener {

    /**
     * 轨迹服务
     */
    protected static Trace trace = null;

    /**
     * entity标识
     */
    protected static String entityName = null;

    /**
     * 鹰眼服务ID，开发者创建的鹰眼服务对应的服务ID
     */
    protected static long serviceId = 117658; // serviceId为开发者创建的鹰眼服务ID

    /**
     * 轨迹服务类型（0 : 不建立socket长连接， 1 : 建立socket长连接但不上传位置数据，2 : 建立socket长连接并上传位置数据）
     */
    private int traceType = 2;

    /**
     * 轨迹服务客户端
     */
    protected static LBSTraceClient client = null;

    /**
     * Entity监听器
     */
    protected static OnEntityListener entityListener = null;

    /**
     * Track监听器
     */
    protected static OnTrackListener trackListener = null;

    private Button btnTrackUpload;
    private Button btnTrackQuery;

//    private TextView textBack;
//    private TextView textChoseDate;

    protected static MapView mMapView = null;
    protected static BaiduMap mBaiduMap = null;

    /**
     * 用于对Fragment进行管理
     */
    private FragmentManager fragmentManager;

    private TrackUploadFragment mTrackUploadFragment;

    private TrackQueryFragment mTrackQueryFragment;

    protected static Context mContext = null;

    private static String imei="12345";

    public LatLng mCurrLoc = null;

    public float mCurrZoom;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_baidu_trance);

        mContext = getApplicationContext();

        // 初始化轨迹服务客户端
        client = new LBSTraceClient(mContext);

        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);

        Intent intent=getIntent();
        if(intent!=null){
            imei=intent.getStringExtra("imei");
        }

        //  初始化entity标识
        entityName = imei;



        // 初始化轨迹服务
        trace = new Trace(getApplicationContext(), serviceId, entityName,
                traceType);

        // 初始化组件
        initComponent();

        // 初始化OnEntityListener
        initOnEntityListener();

        // 初始化OnTrackListener
        initOnTrackListener();

        client.setOnTrackListener(trackListener);

        // 添加entity
        addEntity();

        // 设置默认的Fragment
        setDefaultFragment();



    }




    @Override
    protected void onResume() {
//        mMapView.onResume();
        super.onResume();

    }

    /**
     * 添加Entity
     */
    private void addEntity() {
        Geofence.addEntity();
    }

    /**
     * 初始化组件
     */
    private void initComponent() {
        // 初始化控件
        btnTrackUpload = (Button) findViewById(R.id.btn_trackUpload);
        btnTrackQuery = (Button) findViewById(R.id.btn_trackQuery);
//        textBack= (TextView) findViewById(R.id.track_back);
//        textChoseDate= (TextView) findViewById(R.id.chose_date);


        btnTrackUpload.setOnClickListener(this);
        btnTrackQuery.setOnClickListener(this);
//         textBack.setOnClickListener(this);
//        textChoseDate.setOnClickListener(this);
        fragmentManager = getSupportFragmentManager();


       mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mMapView.showZoomControls(false);


        //当初始化没有轨迹点时加载默认的百度地图
        MapStatus ms = mBaiduMap.getMapStatus();
        mCurrLoc = ms.target;
        mCurrZoom = ms.zoom;

        MapStatusUpdate msu;
        msu = MapStatusUpdateFactory.newLatLngZoom(mCurrLoc, mCurrZoom);
        mBaiduMap.setMapStatus(msu);

        BaiduTranceActivity.mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(6).build()));//设置缩放级别


    }

    /**
     * 设置默认的Fragment
     */
    private void setDefaultFragment() {
        handlerButtonClick(R.id.btn_trackQuery);
    }

    /**
     * 点击事件
     */
    public void onClick(View v) {
        // TODO Auto-generated method stub
        handlerButtonClick(v.getId());
    }

    /**
     * 初始化OnEntityListener
     */
    private void initOnEntityListener() {
        entityListener = new OnEntityListener() {

            // 请求失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                // TODO Auto-generated method stub
                Looper.prepare();
//                Toast.makeText(getApplicationContext(),
//                        "entity请求失败回调接口消息 : " + arg0, Toast.LENGTH_SHORT)
//                        .show();
                Looper.loop();
            }

            // 添加entity回调接口
            @Override
            public void onAddEntityCallback(String arg0) {
                // TODO Auto-generated method stub
                Looper.prepare();
//                Toast.makeText(getApplicationContext(),
//                        "添加entity回调接口消息 : " + arg0, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            // 查询entity列表回调接口
            @Override
            public void onQueryEntityListCallback(String message) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onReceiveLocation(TraceLocation location) {
                // TODO Auto-generated method stub
                if (mTrackUploadFragment != null) {
                    mTrackUploadFragment.showRealtimeTrack(location);
                }
            }

        };
    }

    /**
     * 初始化OnTrackListener
     */
    private void initOnTrackListener() {

        trackListener = new OnTrackListener() {

            // 请求失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                // TODO Auto-generated method stub
                Looper.prepare();
//                Toast.makeText(BaiduTranceActivity.this, "track请求失败回调接口消息 : " + arg0, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            // 查询历史轨迹回调接口
            @Override
            public void onQueryHistoryTrackCallback(String arg0) {
                // TODO Auto-generated method stub
                super.onQueryHistoryTrackCallback(arg0);
                mTrackQueryFragment.showHistoryTrack(arg0);

            }

        };
    }

    /**
     * 处理tab点击事件
     *
     * @param id
     */
    private void handlerButtonClick(int id) {
        // 重置button状态
        onResetButton();
        // 开启Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 隐藏Fragment
        hideFragments(transaction);

        switch (id) {

            case R.id.btn_trackQuery:

                TrackUploadFragment.isInUploadFragment = false;

                if (mTrackQueryFragment == null) {
                    mTrackQueryFragment = new TrackQueryFragment();
                    transaction.add(R.id.fragment_content, mTrackQueryFragment);
                } else {
                    transaction.show(mTrackQueryFragment);
                }
                mTrackQueryFragment.addMarker();
                btnTrackQuery.setTextColor(Color.rgb(0x00, 0x00, 0xd8));
                btnTrackQuery.setBackgroundColor(Color.rgb(0x99, 0xcc, 0xff));
                mBaiduMap.setOnMapClickListener(null);
                break;

//            case R.id.btn_trackUpload:
//                startActivity(new Intent(BaiduTranceActivity.this,UserView.class));
//              finish();
//
////                TrackUploadFragment.isInUploadFragment = true;
////
////                if (mTrackUploadFragment == null) {
////                    mTrackUploadFragment = new TrackUploadFragment();
////                    transaction.add(R.id.fragment_content, mTrackUploadFragment);
////                } else {
////                    transaction.show(mTrackUploadFragment);
////                }
////
////                TrackUploadFragment.addMarker();
////                mTrackUploadFragment.startRefreshThread(true);
////                btnTrackUpload.setTextColor(Color.rgb(0x00, 0x00, 0xd8));
////                btnTrackUpload.setBackgroundColor(Color.rgb(0x99, 0xcc, 0xff));
////                mBaiduMap.setOnMapClickListener(null);
//                break;

//            case R.id.track_back:
//                startActivity(new Intent(BaiduTranceActivity.this,UserView.class));
//                this.finish();
//
//                break;
//
//            case R.id.chose_date:
//                mTrackQueryFragment.queryTrack();
//                mBaiduMap.setOnMapClickListener(null);
//                break;

        }
        // 事务提交
        transaction.commit();

    }

    /**
     * 重置button状态
     */
    private void onResetButton() {
        btnTrackQuery.setTextColor(Color.rgb(0x00, 0x00, 0x00));
        btnTrackQuery.setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
        btnTrackUpload.setTextColor(Color.rgb(0x00, 0x00, 0x00));
        btnTrackUpload.setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
    }

    /**
     * 隐藏Fragment
     */
    private void hideFragments(FragmentTransaction transaction) {

        if (mTrackQueryFragment != null) {
            transaction.hide(mTrackQueryFragment);
        }
        if (mTrackUploadFragment != null) {
            transaction.hide(mTrackUploadFragment);
        }
        // 清空地图覆盖物
        mBaiduMap.clear();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
//        mMapView.onPause();
        super.onPause();
        TrackUploadFragment.isInUploadFragment = false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
//        mMapView.onDestroy();
        super.onDestroy();
        client.onDestroy();
//        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
         if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){

             startActivity(new Intent(BaiduTranceActivity.this,UserView.class));
             this.finish();
         }else if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()>0){
             finish();
         }

        return super.onKeyDown(keyCode, event);
    }
}
