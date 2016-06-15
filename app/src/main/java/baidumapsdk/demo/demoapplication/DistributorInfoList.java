package baidumapsdk.demo.demoapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



public class DistributorInfoList extends ActionBarActivity
        implements CompoundButton.OnCheckedChangeListener {

    private ProgressDialog dialog = null;
    private List<?> mDisList = null;
    private ListView mDisListView = null;
    private DistributorInfoAdapter mDisInfoAdapter = null;
    private DisInfoDatabaseHandler mDatabaseHandler = null;
    private int mType;
    private String mUserPhone = null;

    private BroadcastReceiver mSetBatteryLockState = null;

    private final static String mLockUrl = Login_main.preUrl + "/user/bty/lock.json";
    private final static String mUnlockUrl = Login_main.preUrl + "/user/bty/unlock.json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_list);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabaseHandler = new DisInfoDatabaseHandler(DistributorInfoList.this);
        mType = getIntent().getIntExtra("type", 0);

        if (mType == 0) {
            mDisList = OverlayDemo.mSimpleDisInfoList;
            setTitle(getString(R.string.dis_list));
        } else if (mType == 1) {
            setTitle(getString(R.string.my_bat_info));
            mUserPhone = getIntent().getStringExtra("UserPhone");
            mDisList = UserView.mUserBatList;
        } else {
            setTitle(getString(R.string.friends_bat_info));
            mDisList = UserView.mFriendsBatList;
        }
        mDisListView = (ListView) findViewById(android.R.id.list);
        mDisInfoAdapter = new DistributorInfoAdapter(DistributorInfoList.this,
                mDisList, mType);
        mDisListView.setAdapter(mDisInfoAdapter);

        mDisListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView itemSummary = (TextView) view.findViewById(R.id.item_summary);
                ImageView voltageInfoList= (ImageView) view.findViewById(R.id.voltageInfoList);

                Switch switch_lock = (Switch) view.findViewById(R.id.battery_lock);

                TextView track= (TextView) view.findViewById(R.id.text_track);

                LinearLayout summaryItems =
                        (LinearLayout) view.findViewById(R.id.summary_items);
                TextView itemHeader = (TextView) view.findViewById(R.id.item_header);
                if (summaryItems.getVisibility() == View.GONE) {
                    itemHeader.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.dx_expander_maximized, 0, 0, 0);
                    String summary = "";
                    if (mType == 0) {
                        List<OverlayDemo.SimpleDisInfo> tempList =
                                (List<OverlayDemo.SimpleDisInfo>) mDisList;
                        DistributorInfo disInfo =
                                mDatabaseHandler.getDisInfoByPhone(tempList.get(i).phoneNumber);
                        summary = "省市：" + disInfo.resellerProvince
                                + " " + disInfo.resellerCity + "\n"
                                + "地址：" + disInfo.resellerAddress
                                + "\n" + "号码：" + disInfo.resellerPhone;
                        voltageInfoList.setVisibility(View.GONE);

                        track.setVisibility(View.GONE);
                    } else {
                        final List<UserView.UserBatInfo> tempList =
                                (List<UserView.UserBatInfo>) mDisList;
//                        summary = "电压：" + tempList.get(i).voltage + " 伏\n"
//                                + "温度：" + tempList.get(i).temperature + " 度\n";
                        summary = "温度：" +"     "+ tempList.get(i).temperature + " 度";
                        final String imei=tempList.get(i).imei;

                        voltageInfoList.setVisibility(View.VISIBLE);
                        switch (tempList.get(i).power){
                            case 0:
                                voltageInfoList.setImageResource(R.drawable.power0);
                                break;
                            case 1:
                                voltageInfoList.setImageResource(R.drawable.power1);
                                break;
                            case 2:
                                voltageInfoList.setImageResource(R.drawable.power2);
                                break;
                            case 3:
                                voltageInfoList.setImageResource(R.drawable.power3);
                                break;
                            case 4:
                                voltageInfoList.setImageResource(R.drawable.power4);
                                break;
                            default:
                                voltageInfoList.setImageResource(R.drawable.power3);
                                break;
                        }
                        track.setVisibility(View.VISIBLE);
                        track.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(DistributorInfoList.this,BaiduTranceActivity.class);
                                if(imei!=null){
                                    intent.putExtra("imei",imei);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(DistributorInfoList.this,"你还没有电池",Toast.LENGTH_LONG).show();
                                    return;
                                }

                            }
                        });

                    }

                    if (mType == 1) {
                        switch_lock.setVisibility(View.VISIBLE);
//                        String str = "布防" + ":";
//                        switch_lock.setText(str);
                        SharedPreferences mainPref =
                                getSharedPreferences(getString(R.string.shared_pref_pacakge),
                                        Context.MODE_PRIVATE);
                        String imei = ((UserView.UserBatInfo) mDisList.get(i)).imei;

                        boolean lockState = mainPref.getBoolean(imei, false);
                        switch_lock.setChecked(lockState);
                        switch_lock.setOnCheckedChangeListener(DistributorInfoList.this);
                    } else {
                        switch_lock.setVisibility(View.GONE);
                    }

                    itemSummary.setText(summary);
                    //itemSummary.setVisibility(View.VISIBLE);
                    summaryItems.setVisibility(View.VISIBLE);
                } else {
                    itemHeader.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.dx_expander_minimized, 0, 0, 0);
                    //itemSummary.setVisibility(View.GONE);
                    summaryItems.setVisibility(View.GONE);
                }
            }
        });

        if (mType == 1) {
            if (mSetBatteryLockState == null) {
                mSetBatteryLockState = new SetBatteryLockState();
                IntentFilter filter = new IntentFilter();
                filter.addAction(Login_main.ACTION_LOCK_BATTERY);
                LocalBroadcastManager.getInstance(DistributorInfoList.this)
                        .registerReceiver(mSetBatteryLockState, filter);
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mDisList == null || mDisList.size() == 0) {
            String msg = "未能查询到任何信息";
            AlertDialogShow(msg);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mSetBatteryLockState != null) {
            LocalBroadcastManager.getInstance(DistributorInfoList.this)
                    .unregisterReceiver(mSetBatteryLockState);
            mSetBatteryLockState = null;
        }
        super.onDestroy();
    }

    @Override
    public Intent getSupportParentActivityIntent () {
        Intent intent;
        intent = new Intent(this, OverlayDemo.class);
        return intent;
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
        AlertDialog.Builder builder =  new AlertDialog.Builder(DistributorInfoList.this);
        builder.setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
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

            dialog = ProgressDialog.show(DistributorInfoList.this, title, msg, true, true);
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
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        String imei = " ";
        ViewGroup vg = (ViewGroup) compoundButton.getParent().getParent();
        if (vg != null) {
            TextView tv = (TextView) vg.findViewById(R.id.item_header);
            if (tv != null) {
                String str2 = tv.getText().toString().trim();
                int index2 = str2.indexOf('：');
                imei = str2.substring(index2 + 1);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString("userPhone", mUserPhone);
        bundle.putString("btyImei", imei);
        if (checked) {
            bundle.putString("operation", "lock");
            new TestNetworkAsyncTask(DistributorInfoList.this,
                    TestNetworkAsyncTask.TYPE_LOCK_BATTERY,
                    bundle).execute(mLockUrl);
        } else {
            bundle.putString("operation", "unlock");
            new TestNetworkAsyncTask(DistributorInfoList.this,
                    TestNetworkAsyncTask.TYPE_LOCK_BATTERY,
                    bundle).execute(mUnlockUrl);
        }
    }


    class SetBatteryLockState extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Login_main.ACTION_LOCK_BATTERY)) {
                if (intent.getBooleanExtra("setLockState", false)) {
                    SharedPreferences mainPref =
                            getSharedPreferences(getString(R.string.shared_pref_pacakge),
                                    Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mainPref.edit();
                    if (intent.getStringExtra("resCode").equals("10002")) {
                        editor.putBoolean(intent.getStringExtra("imei"), true);
                    } else {
                        if (intent.getStringExtra("operation").equals("lock")) {
                            editor.putBoolean(intent.getStringExtra("imei"), true);
                        } else {
                            editor.putBoolean(intent.getStringExtra("imei"), false);
                        }
                    }
                    editor.commit();
                } else {
                    String message = "IMEI: " + intent.getStringExtra("imei") + "\n\n"
                            + intent.getStringExtra("result");
                    AlertDialogShow(message);
                }
            }
        }
    }
}
