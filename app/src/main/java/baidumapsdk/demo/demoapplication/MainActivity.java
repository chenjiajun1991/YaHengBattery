package baidumapsdk.demo.demoapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class MainActivity extends Activity implements NumberPicker.OnValueChangeListener {
    private String[] mProvinces = new String[] {
            "","北京", "上海", "天津", "重庆",
            "安徽", "福建", "甘肃", "广东",
            "广西", "贵州", "海南", "河北",
            "河南", "黑龙江", "湖北", "湖南",
            "江苏", "江西", "吉林", "辽宁",
            "内蒙古", "宁夏", "青海", "山东",
            "山西", "陕西", "四川", "西藏",
            "新疆", "云南", "浙江",""
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void enterDistributorPanel(View view) {
        Intent intent = null;
        intent = new Intent(MainActivity.this, Login_main.class);
        this.startActivity(intent);
    }

    public void enterCustomerPanel(View view) {
        if (isNetworkAvailable()) {
            Intent intent = null;
            intent = new Intent(MainActivity.this, OverlayDemo.class);
            this.startActivity(intent);
        } else {
            /*Toast.makeText(getApplicationContext(),
                    "网络不可用！", Toast.LENGTH_SHORT).show();*/
            AlertDialog.Builder builder =  new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View DialogView = inflater.inflate(R.layout.number_picker, null);
            builder.setTitle("提示")
                    .setView(DialogView)
                    .setIcon(R.drawable.ic_launcher)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setCancelable(true);
            Dialog dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            dialog.show();

/*
            DialogFragment newFragment = new DatePickerFragmentTest();
            newFragment.show(getFragmentManager(), "datePicker");*/
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

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }

    private void changeValueByOne(final NumberPicker higherPicker, final boolean increment) {

        Method method;
        try {
            // refelction call for
            // higherPicker.changeValueByOne(true);
            method = higherPicker.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(higherPicker, increment);

        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
