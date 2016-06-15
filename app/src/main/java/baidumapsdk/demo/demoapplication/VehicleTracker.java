package baidumapsdk.demo.demoapplication;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by jizhenlo on 2015/6/4.
 */
public class VehicleTracker extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
    }
}
