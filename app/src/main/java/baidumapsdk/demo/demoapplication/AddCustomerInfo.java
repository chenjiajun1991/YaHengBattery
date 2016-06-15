package baidumapsdk.demo.demoapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;

import jim.h.common.android.zxinglib.integrator.IntentIntegrator;
import jim.h.common.android.zxinglib.integrator.IntentResult;

public class AddCustomerInfo extends Activity {
    private String addUserInfoUrl = null;
    private String mGetBatLocUrl = null;
    private Bundle mBundle;

    private Button mAddCustomerInfo;
    private BroadcastReceiver mAddUserReceiver = null;
    private Spinner mBtyCount;
    private ArrayAdapter<String> mDistributorAdapter = null;
    private String[] mUserGroups = new String[]{
            "4",
            "5"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery_user_info);

        //getSupportActionBar().setDisplayShowHomeEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayShowHomeEnabled(false);

        addUserInfoUrl = Login_main.preUrl + "/reseller/btyspec.json";
        mGetBatLocUrl = Login_main.preUrl + "/bty/info.json";


        mBtyCount= (Spinner) findViewById(R.id.spinnerBtyCount);
        mDistributorAdapter = new ArrayAdapter<String>(AddCustomerInfo.this,
                R.layout.customize_dropdown_item_bl, mUserGroups);
        mBtyCount.setAdapter(mDistributorAdapter);


        mAddCustomerInfo = (Button) findViewById(R.id.buttonCustomerInfo);
        mAddCustomerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()) {
                    AlertDialogShow(getString(R.string.network_disconnect));
                    return;
                }
                EditText imei = (EditText) findViewById(R.id.batteryDevId);
                String strIMEI = imei.getText().toString();
                EditText sn = (EditText) findViewById(R.id.batterySN);
                String strSN = sn.getText().toString();
                EditText simNo = (EditText) findViewById(R.id.batterySIMCard);
                String strSIMNo = simNo.getText().toString();
                String strBtyCount=mBtyCount.getSelectedItem().toString();
                EditText disPhoneNo = (EditText) findViewById(R.id.distributorPhoneNumber);
                String strDisPhone = disPhoneNo.getText().toString();
                EditText cusName = (EditText) findViewById(R.id.batteryCustomerName);
                String strCusName = cusName.getText().toString();
                EditText cusPhone = (EditText) findViewById(R.id.batteryCustomerPhoneNumber);
                String strCusPhone = cusPhone.getText().toString();
                RadioButton rButton = (RadioButton) findViewById(R.id.radioCommonUser);

                Bundle bundle = new Bundle();
                bundle.putString("IMEI", strIMEI);
                bundle.putString("SN", strSN);
                bundle.putString("SimNo", strSIMNo);
                bundle.putString("BtyCount", strBtyCount);
                bundle.putString("ResPhone", strDisPhone);
                bundle.putString("userName", strCusName);
                bundle.putString("userPhone", strCusPhone);
                if (rButton.isChecked())
                    bundle.putString("userGroup", "普通用户");
                else
                    bundle.putString("userGroup", "特殊用户");

                mBundle = bundle;

                new TestNetworkAsyncTask(AddCustomerInfo.this,
                        TestNetworkAsyncTask.TYPE_ADD_USER_INFO,
                        bundle).execute(addUserInfoUrl);
            }
        });

        if (mAddUserReceiver == null) {
            mAddUserReceiver = new AddUserInfoReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Login_main.ACTION_ADD_USER_RESULT);
            filter.addAction(Login_main.ACTION_GET_BAT_LOC);
            LocalBroadcastManager.getInstance(AddCustomerInfo.this)
                    .registerReceiver(mAddUserReceiver, filter);
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
        if (mAddCustomerInfo != null) {
            LocalBroadcastManager.getInstance(AddCustomerInfo.this)
                    .unregisterReceiver(mAddUserReceiver);
            mAddCustomerInfo = null;
        }
        super.onDestroy();
    }

    @Override
    public Intent getParentActivityIntent () {
        Intent intent;
        intent = new Intent(this, DistributorView.class);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scan_input, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan_input:
                IntentIntegrator.initiateScan(AddCustomerInfo.this,
                        R.layout.capture,
                        R.id.viewfinder_view,
                        R.id.preview_view, true);
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,
                        resultCode, data);
                if (scanResult == null) {
                    return;
                }
                final String result = scanResult.getContents();
                if (result != null && result.length() > 0) {
                    String[] subStrArr = result.split(" +");
                    if (subStrArr.length == 3) {
                        String imei = subStrArr[0];
                        String sn = subStrArr[1];
                        String simNo = subStrArr[2];
                        String msg = "IMEI号:  " + imei + "\n\n"
                                + "序列号:  " + sn + "\n\n"
                                + "SIM卡号:  " + simNo;
                        AlertDialogShowWithCancel(msg, imei, sn, simNo);
                        return;
                    }
                }

                AlertDialogShow(getString(R.string.scan_err));
                break;
            default:
        }
    }

    public void AlertDialogShow(String message) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(AddCustomerInfo.this);
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

    public void AlertDialogShowWithCancel(String message,
                                          final String imei,
                                          final String sn,
                                          final String simNo) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(AddCustomerInfo.this);
        builder.setTitle("提示")
                .setMessage(message)
                .setPositiveButton(getString(R.string.positive_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText imeiEdit = (EditText) findViewById(R.id.batteryDevId);
                                EditText snEdit = (EditText) findViewById(R.id.batterySN);
                                EditText simNoEdit = (EditText) findViewById(R.id.batterySIMCard);
                                EditText disPhoneNo =
                                        (EditText) findViewById(R.id.distributorPhoneNumber);

                                imeiEdit.setText(imei);
                                snEdit.setText(sn);
                                simNoEdit.setText(simNo);
                                disPhoneNo.requestFocus();

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

/*    @Override
    public Intent getSupportParentActivityIntent () {
        Intent intent;
        intent = new Intent(AddCustomerInfo.this, DistributorView.class);
        return intent;
    }*/

    class AddUserInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(Login_main.ACTION_ADD_USER_RESULT)) {
                String msg;
                if (intent.getBooleanExtra("addUserInfoSuccess", false)) {
                    msg = "添加用户信息成功";

                    Bundle bundle = new Bundle();
                    bundle.putString("btySimNo", mBundle.getString("SimNo"));
                    new TestNetworkAsyncTask(AddCustomerInfo.this,
                            TestNetworkAsyncTask.TYPE_GET_BAT_LOC,
                            bundle).execute(mGetBatLocUrl);
                } else {
                    msg = "添加失败: " + intent.getStringExtra("result");
                }

                AlertDialogShow(msg);
            } else if (intent.getAction().equals(Login_main.ACTION_GET_BAT_LOC)) {
                if (intent.getBooleanExtra("getBatLoc", false)) {
                    /*Double lat = Double.parseDouble(intent.getStringExtra("latitude"));
                    Double lon = Double.parseDouble(intent.getStringExtra("longitude"));
                    LatLng ll = new LatLng(lat, lon);
                    DistributorView.mNewBatLoc = ll;*/
                } else {
                    String msg = intent.getStringExtra("result");
                    AlertDialogShow(msg);
                }
            }
        }
    }
}
