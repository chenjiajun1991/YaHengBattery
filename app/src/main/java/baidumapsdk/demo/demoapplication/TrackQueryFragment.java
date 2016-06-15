package baidumapsdk.demo.demoapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import trackutils.DateDialog;
import trackutils.DateUtils;
import trackutils.GsonService;
import trackutils.HistoryTrackData;

/**
 * 轨迹查询
 */
@SuppressLint("NewApi")
public class TrackQueryFragment extends Fragment {

    private Button btnDate = null;

    private TextView textBack;
    private TextView textChoseDate;

    private ImageView ivCalendar;

//    private Button btnProcessed = null;

    private int startTime = 0;
    private int endTime = 0;

    private int year = 0;
    private int month = 0;
    private int day = 0;

    // 起点图标
    private static BitmapDescriptor bmStart;
    // 终点图标
    private static BitmapDescriptor bmEnd;

    // 起点图标覆盖物
    private static MarkerOptions startMarker = null;
    // 终点图标覆盖物
    private static MarkerOptions endMarker = null;
    // 路线覆盖物
    private static PolylineOptions polyline = null;

    private MapStatusUpdate msUpdate = null;

//    private TextView tvDatetime = null;
//    private TextView tvDistance = null;

//    private static int isProcessed = 0;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trackquery,
                container, false);

        init(view);
        queryProcessedHistoryTrack();

        return view;
    }

    /**
     * 初始化
     */
    private void init(final View view) {
        textBack= (TextView) view.findViewById(R.id.track_back);

        textChoseDate= (TextView) view.findViewById(R.id.chose_date);

        ivCalendar= (ImageView) view.findViewById(R.id.iv_calendar);


        textBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getActivity(),UserView.class));
                getActivity().finish();
            }
        });


        ivCalendar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 查询轨迹
                queryTrack();
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long times = System.currentTimeMillis();
        Date date = new Date(times);
        String tim = sdf.format(date);


        textChoseDate.setText("轨迹日期:" + tim);

//        btnDate = (Button) view.findViewById(R.id.btn_date);

//        btnDate.setOnClickListener(this);

//        btnProcessed = (Button) view.findViewById(R.id.btn_isProcessed);

//        btnProcessed.setOnClickListener(this);

//        tvDatetime = (TextView) view.findViewById(R.id.tv_datetime);
//        tvDatetime.setText(" 当前日期 : " + DateUtils.getCurrentDate() + " ");
//        tvDistance= (TextView) view.findViewById(R.id.tv_distance);
//        tvDistance.setText(" 当前里程 : 0");


    }

    /**
     * 查询历史轨迹
     */
    private void queryHistoryTrack() {

        // entity标识
        String entityName = BaiduTranceActivity.entityName;
        // 是否返回精简的结果（0 : 否，1 : 是）
        int simpleReturn = 0;
        // 开始时间
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }
        // 分页大小
        int pageSize = 1000;
        // 分页索引
        int pageIndex = 1;

        BaiduTranceActivity.client.queryHistoryTrack(BaiduTranceActivity.serviceId, entityName, simpleReturn, startTime, endTime,
                pageSize,
                pageIndex,
                BaiduTranceActivity.trackListener);
    }

    /**
     * 查询纠偏后的历史轨迹
     */
    private void queryProcessedHistoryTrack() {

        // entity标识
        String entityName = BaiduTranceActivity.entityName;
        // 是否返回精简的结果（0 : 否，1 : 是）
        int simpleReturn = 0;
        // 是否返回纠偏后轨迹（0 : 否，1 : 是）
        int isProcessed = 1;
        // 开始时间
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }
        // 分页大小
        int pageSize = 1000;
        // 分页索引
        int pageIndex = 1;

        BaiduTranceActivity.client.queryProcessedHistoryTrack(BaiduTranceActivity.serviceId, entityName, simpleReturn, isProcessed,
                startTime, endTime,
                pageSize,
                pageIndex,
                BaiduTranceActivity.trackListener);
    }

    /**
     * 轨迹查询(先选择日期，再根据是否纠偏，发送请求)
     */
    public void queryTrack() {
        // 选择日期
        int[] date = null;
        DisplayMetrics dm = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        if (year == 0 && month == 0 && day == 0) {
            String curDate = DateUtils.getCurrentDate();
            date = DateUtils.getYMDArray(curDate, "-");
        }

        if (date != null) {
            year = date[0];
            month = date[1];
            day = date[2];
        }


        DateDialog dateDiolog = new DateDialog(this.getActivity(), new DateDialog.PriorityListener() {

            public void refreshPriorityUI(String sltYear, String sltMonth,
                                          String sltDay, DateDialog.CallBack back) {

                Log.d("TGA", sltYear + sltMonth + sltDay);
                year = Integer.parseInt(sltYear);
                month = Integer.parseInt(sltMonth);
                day = Integer.parseInt(sltDay);
                String st = year + "年" + month + "月" + day + "日0时0分0秒";
                String et = year + "年" + month + "月" + day + "日23时59分59秒";

                startTime = Integer.parseInt(DateUtils.getTimeToStamp(st));
                endTime = Integer.parseInt(DateUtils.getTimeToStamp(et));

                back.execute();
            }

        }, new DateDialog.CallBack() {

            public void execute() {

               textChoseDate.setText("轨迹日期:" + year + "-" + month + "-" + day + " ");
                queryProcessedHistoryTrack();
//                // 选择完日期，根据是否纠偏发送轨迹查询请求
//                if (0 == isProcessed) {
//                    Toast.makeText(getActivity(), "正在查询历史轨迹，请稍候", Toast.LENGTH_SHORT).show();
//                    queryHistoryTrack();
//                } else {
//                    Toast.makeText(getActivity(), "正在查询纠偏后的历史轨迹，请稍候", Toast.LENGTH_SHORT).show();
//                    queryProcessedHistoryTrack();
//                }
            }
        }, year, month, day, width, height, "轨迹日期", 1);

        Window window = dateDiolog.getWindow();
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置
        dateDiolog.setCancelable(true);
        dateDiolog.show();

    }

    /**
     * 显示历史轨迹
     *
     */
    protected void showHistoryTrack(String historyTrack) {
        final List<LatLng> latLngList = new ArrayList<LatLng>();
        double distance=0;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(historyTrack);
            distance=jsonObject.getDouble("distance");

            JSONArray jArray = jsonObject.getJSONArray("points");

            for(int i=0;i<jArray.length();i++){
                JSONObject  jsonObject1= (JSONObject) jArray.get(i);
                JSONArray point=jsonObject1.getJSONArray("location");
                for(int j=0;j<point.length();j++){
                    double lon=point.getDouble(1);
                    double lat=point.getDouble(0);
                    LatLng latLng=new LatLng(lon,lat);
                    latLngList.add(latLng);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("D4", "Json fault");
        }

        // 绘制历史轨迹
        drawHistoryTrack(latLngList,distance);
//        tvDistance.setText(" 当前里程 : "+distance);

    }

//    public void onClick(View v) {
//        // TODO Auto-generated method stub
//        switch (v.getId()) {
//
//
////            case R.id.btn_isProcessed:
////                isProcessed = isProcessed ^ 1;
////                if (0 == isProcessed) {
////                    btnProcessed.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
////                    btnProcessed.setTextColor(Color.rgb(0x00, 0x00, 0x00));
////                    Toast.makeText(getActivity(), "正在查询历史轨迹，请稍候", Toast.LENGTH_SHORT).show();
////                    queryHistoryTrack();
////                } else {
////                    btnProcessed.setBackgroundColor(Color.rgb(0x99, 0xcc, 0xff));
////                    btnProcessed.setTextColor(Color.rgb(0x00, 0x00, 0xd8));
////                    Toast.makeText(getActivity(), "正在查询纠偏后的历史轨迹，请稍候", Toast.LENGTH_SHORT).show();
////                    queryProcessedHistoryTrack();
////                }
////                break;
//
//            default:
//                break;
//        }
//    }

    /**
     * 绘制历史轨迹
     *
     * @param points
     */
    protected void drawHistoryTrack(final List<LatLng> points, final double distance) {
        // 绘制新覆盖物前，清空之前的覆盖物
        BaiduTranceActivity.mBaiduMap.clear();

        if (points == null || points.size() == 0) {
            Looper.prepare();
            Toast.makeText(getActivity(), "当前查询无轨迹点", Toast.LENGTH_SHORT).show();
            Looper.loop();
            resetMarker();
        } else if (points.size() > 1) {

            LatLng llC = points.get(0);
            LatLng llD = points.get(points.size() - 1);
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(llC).include(llD).build();

            msUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);

            bmStart = BitmapDescriptorFactory.fromResource(R.drawable.icon_start);
            bmEnd = BitmapDescriptorFactory.fromResource(R.drawable.icon_end);

            // 添加起点图标
            startMarker = new MarkerOptions()
                    .position(points.get(points.size() - 1)).icon(bmStart)
                    .zIndex(9).draggable(true);

            // 添加终点图标
            endMarker = new MarkerOptions().position(points.get(0))
                    .icon(bmEnd).zIndex(9).draggable(true);

            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(points);

            addMarker();
            BaiduTranceActivity.mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(16).build()));//设置缩放级别

            Looper.prepare();
            if(distance==0){
                Toast.makeText(getActivity(), "你今天骑行的距离为 : " + (int) distance + "米", Toast.LENGTH_SHORT).show();
            }
//            Toast.makeText(getActivity(), "你今天骑行的距离为 : " + (int) distance + "米", Toast.LENGTH_SHORT).show();
            Looper.loop();

        }

    }

    /**
     * 添加覆盖物
     */
    protected void addMarker() {

        if (null != msUpdate) {
            BaiduTranceActivity.mBaiduMap.setMapStatus(msUpdate);
        }

        if (null != startMarker) {
            BaiduTranceActivity.mBaiduMap.addOverlay(startMarker);
        }

        if (null != endMarker) {
            BaiduTranceActivity.mBaiduMap.addOverlay(endMarker);
        }

        if (null != polyline) {
            BaiduTranceActivity.mBaiduMap.addOverlay(polyline);
        }

    }

    /**
     * 重置覆盖物
     */
    private void resetMarker() {
        startMarker = null;
        endMarker = null;
        polyline = null;
    }

}
