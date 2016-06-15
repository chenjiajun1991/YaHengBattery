package baidumapsdk.demo.demoapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;

public class UserSignUp extends ActionBarActivity {
    private String signupVeriUrl = null;
    private String signupUrl = null;

    private Button mButtonVerificationGet;
    private EditText mPhoneNumber;
    private EditText mPassword, mPassword2;
    private EditText mVerifyCode;
    private Button mButtonSignUp;

    private BroadcastReceiver mSignUpResultReceiver = null;

    private int[][] mColorStates = new int[][] {
            new int[] { android.R.attr.state_enabled}, // enabled
            new int[] {-android.R.attr.state_enabled}, // disabled
    };
    private int[] mColors = new int[] {
            Color.BLACK,
            Color.GRAY
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        signupUrl = Login_main.preUrl + "/user/signup.json";
        signupVeriUrl = Login_main.preUrl + "/open/sms.json";

        mPhoneNumber = (EditText) findViewById(R.id.textUserPhoneNumber);
        mPassword = (EditText) findViewById(R.id.textUserPassword);
        mPassword2 = (EditText) findViewById(R.id.textUserPassword2);
        mVerifyCode = (EditText) findViewById(R.id.textVerificationCode);

        mButtonVerificationGet = (Button) findViewById(R.id.verificationiCodeGet);
        ColorStateList colorStateList = new ColorStateList(mColorStates, mColors);
        mButtonVerificationGet.setTextColor(colorStateList);
        mButtonVerificationGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()) {
                    AlertDialogShow(getString(R.string.network_disconnect));
                    return;
                }
                String phoNo = mPhoneNumber.getText().toString();
                if (phoNo.length() != 11) {
                    return;
                }
                mButtonVerificationGet.setEnabled(false);
                Bundle bundle = new Bundle();
                bundle.putString("phoneNum", phoNo);
                bundle.putString("authType", "1");
                new TestNetworkAsyncTask(UserSignUp.this,
                        TestNetworkAsyncTask.TYPE_GET_VERIFICATION_CODE,
                        bundle).execute(signupVeriUrl);

                new CountDownTimer(60000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        mButtonVerificationGet.
                                setText(millisUntilFinished / 1000 + "s后重新获取");
                    }

                    public void onFinish() {
                        mButtonVerificationGet.setText("获取验证码");
                        mButtonVerificationGet.setEnabled(true);
                    }
                }.start();

            }
        });

        mButtonSignUp = (Button)findViewById(R.id.buttonSignUp);
        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()) {
                    AlertDialogShow(getString(R.string.network_disconnect));
                    return;
                }
                String phoNo = mPhoneNumber.getText().toString();
                String password = mPassword.getText().toString();
                String password2 = mPassword2.getText().toString();
                String verifyCode = mVerifyCode.getText().toString();

                if(phoNo.trim().length() == 0
                        || password.trim().length() == 0
                        || password2.trim().length() == 0
                        || verifyCode.trim().length() == 0) {
                    Toast.makeText(getApplicationContext(),
                            "选项不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!password.equals(password2)) {
                    Toast.makeText(getApplicationContext(),
                            "两次输入的密码不一致！", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString("phoneNum", phoNo.trim());
                bundle.putString("password", password.trim());
                bundle.putString("verifyCode", verifyCode.trim());
                new TestNetworkAsyncTask(UserSignUp.this,
                        TestNetworkAsyncTask.TYPE_USER_SIGN_UP,
                        bundle).execute(signupUrl);
            }
        });

        if (mSignUpResultReceiver == null) {
            mSignUpResultReceiver = new GetSignUpResultReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Login_main.ACTION_USER_SIGN_UP_RESULT);
            LocalBroadcastManager.getInstance(UserSignUp.this)
                    .registerReceiver(mSignUpResultReceiver, filter);
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
        if (mSignUpResultReceiver != null) {
            LocalBroadcastManager.getInstance(UserSignUp.this)
                    .unregisterReceiver(mSignUpResultReceiver);
            mSignUpResultReceiver = null;
        }
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

    public void AlertDialogShow(String message) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(UserSignUp.this);
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
    public Intent getSupportParentActivityIntent () {
        Intent intent;
        intent = new Intent(UserSignUp.this, Login_main.class);
        return intent;
    }

    class GetSignUpResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String msg;
            boolean state = false;
            if (intent.getBooleanExtra("signupSuccess", false)) {
                msg = "注册成功";
                state = true;
            } else {
                msg = "注册失败: " + intent.getStringExtra("result");
            }
            final boolean signupState = state;

            AlertDialog.Builder builder =  new AlertDialog.Builder(UserSignUp.this);
            builder.setTitle("提示")
                    .setMessage(msg)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            if (signupState) {
                                Intent intent1 = new Intent(UserSignUp.this, Login_main.class);
                                startActivity(intent1);
                            }
                        }
                    })
                    .setCancelable(true);
            Dialog dialog = builder.create();
            dialog.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            dialog.show();
        }
    }
}
