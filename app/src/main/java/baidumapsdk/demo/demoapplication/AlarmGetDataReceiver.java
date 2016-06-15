package baidumapsdk.demo.demoapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AlarmGetDataReceiver extends BroadcastReceiver {
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Bundle bundle = new Bundle();
        bundle.putString("phoneNum", intent.getStringExtra("phoneNum"));
        String url = intent.getStringExtra("url");
        new TestNetworkAsyncTask(context,
                TestNetworkAsyncTask.TYPE_TEST,
                bundle).execute(url);
        //scheduleAlarm();
    }

/*    public void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent getData = new Intent(mContext, AlarmGetDataReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                0,
                getData,
                PendingIntent.FLAG_ONE_SHOT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, 5000, pendingIntent);
    }*/
}
