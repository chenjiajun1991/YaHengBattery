package baidumapsdk.demo.demoapplication;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserBatList extends ActionBarActivity {
    List<String> groupList;
    List<String> childList;
    Map<String, List<String>> laptopCollection;
    ExpandableListView expListView;
    ExpandBatteryAdapter expListAdapter;
    private BroadcastReceiver mGetFollowerReceiver = null;
    public static String mFollowerPre = "共享给: ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expand_listview_main);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        createGroupList(getIntent());

        expListView = (ExpandableListView) findViewById(R.id.battery_list);
        List<String> tempList = getIntent().getStringArrayListExtra("BatList");
        expListAdapter = new ExpandBatteryAdapter(
                this,
                groupList,
                laptopCollection,
                getIntent().getStringExtra("userPhone"),
                tempList);
        expListView.setAdapter(expListAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final String selected = (String) expListAdapter.getChild(
                        groupPosition, childPosition);
                Toast.makeText(getBaseContext(), selected, Toast.LENGTH_LONG)
                        .show();

                return true;
            }
        });

        if (mGetFollowerReceiver == null) {
            mGetFollowerReceiver = new GetFollowerChangeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Login_main.ACTION_SHARE_BATTERY);
            filter.addAction(Login_main.ACTION_DEL_GROUP_CHILD_ITEM);
            LocalBroadcastManager.getInstance(UserBatList.this)
                    .registerReceiver(mGetFollowerReceiver, filter);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mGetFollowerReceiver != null) {
            LocalBroadcastManager.getInstance(UserBatList.this)
                    .unregisterReceiver(mGetFollowerReceiver);
            mGetFollowerReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public Intent getSupportParentActivityIntent () {
        Intent intent;
        intent = new Intent(this, UserView.class);
        return intent;
    }

    private void createGroupList(Intent intent) {
        groupList = new ArrayList<String>();
        laptopCollection = new LinkedHashMap<String, List<String>>();
        List<String> list = intent.getStringArrayListExtra("BatList");
        List<String> simList = intent.getStringArrayListExtra("BatSimList");
        for (String batSN : simList) {
            String temp = "IMEI号: " + batSN;
            groupList.add(temp);
        }

        List<String> tempList = null;
        for (int i = 0; i < list.size(); i++) {
            int count = 0;
            String temp2 = "";
            tempList = new ArrayList<String>();
            List<String> temp = intent.getStringArrayListExtra(list.get(i));
            if (temp != null) {
                for (String follower : temp) {
                    if (count++ % 2 == 0) {
                        temp2 = mFollowerPre + follower;
                    } else {
                        temp2 += " (" + follower + ")";
                        tempList.add(temp2);
                    }
                }
            }
            tempList.add(ExpandBatteryAdapter.emptyNote);
            laptopCollection.put(groupList.get(i), tempList);
        }
    }

    private void loadChild(String[] laptopModels) {
        childList = new ArrayList<String>();
        for (String model : laptopModels)
            childList.add(model);
    }

    private void setGroupIndicatorToRight() {
		/* Get the screen width */
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;

        expListView.setIndicatorBounds(width - getDipsFromPixel(35), width
                - getDipsFromPixel(5));
    }

    // Convert pixel to dip
    public int getDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    public void AlertDialogShow(String message) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(UserBatList.this);
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

    class GetFollowerChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Login_main.ACTION_DEL_GROUP_CHILD_ITEM)) {
                if (intent.getBooleanExtra("unShareBatSuccess", false)) {
                    int groupPos = intent.getIntExtra("groupPos", 0);
                    int childPos = intent.getIntExtra("childPos", 0);
                    expListAdapter.deleteGroupChildItem(groupPos, childPos);
                } else {
                    String msg = intent.getStringExtra("result");
                    expListAdapter.dismissProgressDialog();
                    AlertDialogShow(msg);
                }
            } else if (intent.getAction().equals(Login_main.ACTION_SHARE_BATTERY)) {
                if (intent.getBooleanExtra("shareBatSuccess", false)) {
                    int groupPos = intent.getIntExtra("groupPos", 0);
                    String friendPhone = intent.getStringExtra("friendPhone");
                    String friendName = intent.getStringExtra("friendName");
                    Bundle bundle = new Bundle();
                    bundle.putString("friendPhone", friendPhone);
                    bundle.putString("friendName", friendName);
                    expListAdapter.addGroupChildItem(groupPos, bundle);
                } else {
                    String msg = intent.getStringExtra("result");
                    expListAdapter.dismissProgressDialog();
                    AlertDialogShow(msg);
                }
            }
        }
    }
}
