package baidumapsdk.demo.demoapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

public class Login_main extends Activity {
    public static final String ACTION_GET_USER_GPS_DATA = "com.sam.action.GET_USER_GPS_DATA";
    public static final String ACTION_GET_DIS_GPS_DATA = "com.sam.action.GET_DIS_GPS_DATA";
    public static final String ACTION_USER_SIGN_UP_RESULT = "com.sam.action.SIGN_UP_RESULT";
    public static final String ACTION_USER_SIGN_IN_RESULT = "com.sam.action.SIGN_IN_RESULT";
    public static final String ACTION_ADD_USER_RESULT = "com.sam.action.ADD_USER_RESULT";
    public static final String ACTION_ADD_DISTRIBUTOR_RESULT = "com.sam.action.ADD_DISTRIBUTOR_RESULT";
    public static final String ACTION_GET_DISTRIBUTOR_RESULT = "com.sam.action.GET_DISTRIBUTOR_RESULT";
    public static final String ACTION_GET_BATTERY_LIST = "com.sam.action.GET_BATTERY_LIST";
    public static final String ACTION_GET_FRIEND_BATTERY_LIST = "com.sam.action.GET_FRIEND_BATTERY_LIST";
    public static final String ACTION_SHARE_BATTERY = "com.sam.action.SHARE_BATTERY";
    public static final String ACTION_GET_BAT_FOLLOWERS = "com.sam.action.BAT_FOLLOWERS";
    public static final String ACTION_ADD_GROUP_CHILD_ITEM = "com.sam.action.ADD_GROUP_CHILD_ITEM";
    public static final String ACTION_DEL_GROUP_CHILD_ITEM = "com.sam.action.DEL_GROUP_CHILD_ITEM";
    public static final String ACTION_DIS_GET_USER_INFO = "com.sam.action.DIS_GET_USRE_INFO";
    public static final String ACTION_GET_CITY_BAT_COUNT = "com.sam.action.GET_CITY_BAT_COUNT";
    public static final String ACTION_GET_DIS_LOC = "com.sam.action.GET_DIS_LOC";
    public static final String ACTION_GET_BAT_LOC = "com.sam.action.GET_BAT_LOC";
    public static final String ACTION_GET_APP_VERSION = "com.sam.action.GET_APP_VERSION";
    public static final String ACTION_LOCK_BATTERY = "com.sam.action.LOCK_BATTERY";
    public static final String ACTION_GET_HISTORY_GPS = "com.sam.action.GET_HISTORY_GPS";


    //public static String preUrl = "http://sam.yahengcloud.com:8080/sam/";
    //public static String preUrl = "http://101.231.206.30:9002/sam/";
    public static String preUrl;

    //public static String updateEndpoint = "/open/ver.json";
    public static String updateEndpoint;

    private String signinUrl = null;
    private String mAccountName = null;
    private String mAccountType = null;

    private String[] mUserGroups = new String[]{
            "普通用户",
            "经销商",
            "生产厂家"
    };

    public static String[] mChinaValidPN = new String[]{
            "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
            "150", "151", "152", "153", "154", "155", "156", "157", "158", "159",
            "180", "181", "182", "183", "184", "185", "186", "187", "188", "189"
    };

    private Spinner mDistributor;
    private ArrayAdapter<String> mDistributorAdapter = null;

    private EditText mAccount;
    private EditText mPassWord;
    private ImageView mAccountImage;
    private ImageView mPasswordImage;
    private ImageView mAccountDel;
    private ImageView mPasswordDel;
    private int mChoice = 0;
    private ProgressReceiver mProgressReceiver;

    private BroadcastReceiver mGetSigninResultReceiver = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_main2);

//        mAccountImage = (ImageView) findViewById(R.id.accountImage);
//        mAccountDel = (ImageView) findViewById(R.id.accountDelImage);
//        mAccountDel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAccount.setText("");
//            }
//        });
//        mPasswordDel = (ImageView) findViewById(R.id.passwordDelImage);
        mAccount = (EditText) findViewById(R.id.textAccount2);
//        mAccount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    mAccountImage.setImageResource(R.drawable.user_new_select);
//                    mAccountDel.setVisibility(View.VISIBLE);
//                } else {
//                    mAccountImage.setImageResource(R.drawable.user_new);
//                    mAccountDel.setVisibility(View.INVISIBLE);
//                }
//            }
//        });

//        mPasswordImage = (ImageView) findViewById(R.id.passwordImage);
//        mPasswordDel = (ImageView) findViewById(R.id.passwordDelImage);
//        mPasswordDel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPassWord.setText("");
//            }
//        });
        mPassWord = (EditText) findViewById(R.id.textPassword2);
//        mPassWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    mPasswordImage.setImageResource(R.drawable.pwd_new_select);
//                    mPasswordDel.setVisibility(View.VISIBLE);
//                } else {
//                    mPasswordImage.setImageResource(R.drawable.pwd_new);
//                    mPasswordDel.setVisibility(View.INVISIBLE);
//                }
//            }
//        });

        signinUrl = preUrl + getString(R.string.user_signin);
        updateEndpoint = getString(R.string.open_ver);

        mProgressReceiver = new ProgressReceiver(new Handler());

        SharedPreferences mainPref = getSharedPreferences(getString(R.string.shared_pref_pacakge),
                Context.MODE_PRIVATE);
        String account = mainPref.getString("lastAccount", "");
        int accountType = mainPref.getInt("accountType", 0);
        mChoice = accountType;

        mDistributor = (Spinner) findViewById(R.id.userGroup2);
        mDistributor.setBackgroundResource(R.drawable.spinner_default_holo_light_am);
        mDistributorAdapter = new ArrayAdapter<String>(Login_main.this,
                R.layout.customize_dropdown_item_bl, mUserGroups);
        mDistributor.setAdapter(mDistributorAdapter);
        //mDistributor.setSelection(0, true);
        mDistributor.requestFocus();

        mDistributor.setSelection(accountType, true);
        mAccount.setText(account);

        mDistributor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                /*if (position != 0) {
                    menuItem.setVisible(false);
                } else {
                   menuItem.setVisible(true);
                }*/
                mChoice = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    @Override
    protected void onResume() {
        if (mGetSigninResultReceiver == null) {
            mGetSigninResultReceiver = new GetSignInResultReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Login_main.ACTION_USER_SIGN_IN_RESULT);
            LocalBroadcastManager.getInstance(Login_main.this)
                    .registerReceiver(mGetSigninResultReceiver, filter);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mGetSigninResultReceiver != null) {
            LocalBroadcastManager.getInstance(Login_main.this)
                    .unregisterReceiver(mGetSigninResultReceiver);
            mGetSigninResultReceiver = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    public void enterOverlayPanel(View view) {
        EditText account = (EditText) findViewById(R.id.textAccount2);
        String strAccount = account.getText().toString();
        EditText password = (EditText) findViewById(R.id.textPassword2);
        String strPassword = password.getText().toString();

        if (!isNetworkAvailable()) {
            AlertDialogShow("网络未连接");
            return;
        }

        if (strAccount.isEmpty() || strPassword.isEmpty()) {
            String msg = "帐号或密码不能为空！";
            AlertDialogShow(msg);
        } else {

            Bundle bundle = new Bundle();
            bundle.putString("phoneNum", strAccount);
            bundle.putString("password", strPassword);

            mProgressReceiver.showDialog(Login_main.this);

            new TestNetworkAsyncTask(Login_main.this,
                    TestNetworkAsyncTask.TYPE_USER_SIGN_IN,
                    bundle).execute(signinUrl);
        }
    }

    public void enterForgetPWPanel(View view) {
        Intent intent = new Intent(Login_main.this, ResetPassword.class);
        this.startActivity(intent);
    }

    public void enterUserSignUp(View view) {
        Intent intent = new Intent(Login_main.this, UserSignUp.class);
        this.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
/*        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);

        menuItem = menu.findItem(R.id.action_sign_up);*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_up:
                Intent intent = new Intent(Login_main.this, UserSignUp.class);
                this.startActivity(intent);
                return true;
        }
        return false;
    }

    public void AlertDialogShow(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login_main.this);
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    class GetSignInResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            mProgressReceiver.send(ProgressReceiver.STATUS_COMPLETE, null);

            if (intent.getBooleanExtra("signinSuccess", false)) {
                String userPhone = intent.getStringExtra("userPhone");
                String userType = intent.getStringExtra("userType");
                Intent intent2 = null;
                SharedPreferences mainPref = getSharedPreferences(getString(R.string.shared_pref_pacakge),
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mainPref.edit();
                editor.putString("lastAccount", userPhone);
                editor.putInt("accountType", mChoice);
                editor.commit();

                if ("0".equals(userType)) {
                    if (mChoice == 1 || mChoice == 2) {
                        String msg = "你无权登录经销商或生产厂家界面!";
                        AlertDialogShow(msg);
                        return;
                    }
                    intent2 = new Intent(Login_main.this, UserView.class);
                    finish();
                } else if ("1".equals(userType)) {
                    if (mChoice == 2) {
                        String msg = "你无权登录生产厂家界面!";
                        AlertDialogShow(msg);
                        return;
                    }
                    if (mChoice == 0) {
                        intent2 = new Intent(Login_main.this, UserView.class);
                        finish();
                    } else if (mChoice == 1) {
                        intent2 = new Intent(Login_main.this, DistributorView.class);
                        finish();
                    }
                } else {
                    if (mChoice == 0) {
                        intent2 = new Intent(Login_main.this, UserView.class);
                        finish();
                    } else if (mChoice == 1) {
                        intent2 = new Intent(Login_main.this, DistributorView.class);
                        finish();
                    } else {
                        intent2 = new Intent(Login_main.this, OverlayDemo.class);
                        finish();
                    }
                }

                intent2.putExtra("userPhone", userPhone);
                startActivity(intent2);
                finish();
            } else {
                String msg = "登录失败: " + intent.getStringExtra("result");
                AlertDialogShow(msg);
            }
        }
    }

    public static boolean isPhoneNumberValid(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        /*int value = Login_main.mChinaValidPN.length;
        for (int i = 0; i < value; i++) {
            if (phoneNumber.startsWith(Login_main.mChinaValidPN[i])) {
                return true;
            }
        }*/
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
