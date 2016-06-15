package baidumapsdk.demo.demoapplication;

import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class AddDistributorInfo extends ActionBarActivity implements
        OnGetGeoCoderResultListener {

    private Spinner mProvince;
    private Spinner mCity;
    private int mProvinceIndex = 1, mCityIndex = 0;
    private ArrayAdapter<String> provinceAdapter = null;
    private ArrayAdapter<String> cityAdapter = null;
    private BroadcastReceiver mGetOEMInfoReceiver = null;
    private String addDisInfoUrl = null;
    private ProgressDialog dialog = null;
    private Bundle mBundle;

    private DisInfoDatabaseHandler mDatabaseHandler = null;

    GeoCoder mSearch = null;

    public final static String[] mProvinces = new String[] {
            "北京", "上海", "天津", "重庆",
            "安徽", "福建", "甘肃", "广东",
            "广西", "贵州", "海南", "河北",
            "河南", "黑龙江", "湖北", "湖南",
            "江苏", "江西", "吉林", "辽宁",
            "内蒙古", "宁夏", "青海", "山东",
            "山西", "陕西", "四川", "西藏",
            "新疆", "云南", "浙江"
    };

    public final static String[][] mCities = new String[][] {
            {"北京"}, {"上海"}, {"天津"}, {"重庆"},
            {"合肥","安庆","蚌埠","亳州","巢湖","池州","滁州","阜阳","淮北","淮南","黄山","六安","马鞍山","宿州","铜陵","芜湖","宣城"},
            {"福州","龙岩","南平","宁德","莆田","泉州","三明","厦门","漳州"},
            {"兰州","白银","定西","甘南州","嘉峪关","金昌","酒泉","临夏州","陇南","平凉","庆阳","天水","张掖"},
            {"广州","潮州","东莞","佛山","河源","惠州","江门","揭阳","茂名","梅州","清远","汕头","汕尾","韶关","深圳","阳江","云浮","湛江","肇庆","中山","珠海"},
            {"南宁","百色","北海","崇左","防城港","桂林","贵港","河池","贺州","来宾","柳州","钦州","梧州","玉林"},
            {"贵阳","安顺","毕节地区","六盘水","铜仁地区","遵义","黔西南州","黔东南州","黔南州"},
            {"海口","白沙","保亭","昌江","儋州","澄迈","东方","定安","琼海","琼中","乐东","临高","陵水","三亚","屯昌","万宁","文昌","五指山"},
            {"石家庄","保定","沧州","承德","邯郸","衡水","廊坊","秦皇岛","唐山","邢台","张家口"},
            {"郑州","安阳","鹤壁","焦作","开封","洛阳","漯河","南阳","平顶山","濮阳","三门峡","商丘","新乡","信阳","许昌","周口","驻马店"},
            {"哈尔滨","大庆","大兴安岭地区","鹤岗","黑河","鸡西","佳木斯","牡丹江","七台河","齐齐哈尔","双鸭山","绥化","伊春"},
            {"武汉","鄂州","恩施","黄冈","黄石","荆门","荆州","潜江","神农架林区","十堰","随州","天门","仙桃","咸宁","襄阳","孝感","宜昌"},
            {"长沙","常德","郴州","衡阳","怀化","娄底","邵阳","湘潭","湘西州","益阳","永州","岳阳","张家界","株洲"},
            {"南京","常州","淮安","连云港","南通","苏州","宿迁","泰州","无锡","徐州","盐城","扬州","镇江"},
            {"南昌","抚州","赣州","吉安","景德镇","九江","萍乡","上饶","新余","宜春","鹰潭"},
            {"长春","白城","吉林市","辽源","四平","松原","通化","延边"},
            {"沈阳","鞍山","本溪","朝阳","大连","丹东","抚顺","阜新","葫芦岛","锦州","辽阳","盘锦","铁岭","营口"},
            {"呼和浩特","阿拉善盟","包头","巴彦淖尔","赤峰","鄂尔多斯","呼伦贝尔","通辽","乌海","乌兰察布","锡林郭勒盟","兴安盟"},
            {"银川","固原","石嘴山","吴忠","中卫","青海","西宁","果洛州","海东地区","海北州","海南州","海西州","黄南州","玉树州"},
            {"西宁","果洛州","海东地区","海北州","海南州","海西州","黄南州","玉树州"},
            {"济南","滨州","东营","德州","菏泽","济宁","莱芜","聊城","临沂","青岛","日照","泰安","威海","潍坊","烟台","枣庄","淄博"},
            {"太原","长治","大同","晋城","晋中","临汾","吕梁","朔州","忻州","阳泉","运城"},
            {"西安","安康","宝鸡","汉中","商洛","铜川","渭南","咸阳","延安","榆林"},
            {"成都","阿坝州","巴中","达州","德阳","甘孜州","广安","广元","乐山","凉山州","泸州","南充","眉山","绵阳","内江","攀枝花","遂宁","雅安","宜宾","资阳","自贡"},
            {"拉萨","阿里地区","昌都地区","林芝地区","那曲地区","日喀则地区","山南地区"},
            {"乌鲁木齐","阿拉尔","阿克苏地区","阿勒泰地区","巴音郭楞","博尔塔拉州","昌吉州","哈密地区","和田地区","喀什地区","克拉玛依","克孜勒苏州","石河子","塔城地区","图木舒克","吐鲁番地区","五家渠","伊犁州"},
            {"昆明","保山","楚雄州","大理州","德宏州","迪庆州","红河州","丽江","临沧","怒江州","普洱","曲靖","昭通","文山","西双版纳","玉溪"},
            {"杭州","湖州","嘉兴","金华","丽水","宁波","衢州","绍兴","台州","温州","舟山"}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_distributor);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addDisInfoUrl = Login_main.preUrl + "/reseller/info.json";
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        mDatabaseHandler = new DisInfoDatabaseHandler(AddDistributorInfo.this);


        mProvince = (Spinner)findViewById(R.id.distributor_province);
        mCity = (Spinner)findViewById(R.id.distributor_city);
        provinceAdapter = new ArrayAdapter<String>(AddDistributorInfo.this,
                R.layout.customize_dropdown_item, mProvinces);
        mProvince.setAdapter(provinceAdapter);
        mProvince.setSelection(mProvinceIndex, true);

        cityAdapter = new ArrayAdapter<String>(AddDistributorInfo.this,
                R.layout.customize_dropdown_item, mCities[1]);
        mCity.setAdapter(cityAdapter);
        mCity.setSelection(mCityIndex, true);

        mProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {

                cityAdapter = new ArrayAdapter<String>(
                        AddDistributorInfo.this,
                        R.layout.customize_dropdown_item,
                        mCities[position]);
                mCity.setAdapter(cityAdapter);
                mProvinceIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        mCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                mCityIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        if (mGetOEMInfoReceiver == null) {
            mGetOEMInfoReceiver = new GetOEMInfoReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Login_main.ACTION_ADD_DISTRIBUTOR_RESULT);
            LocalBroadcastManager.getInstance(AddDistributorInfo.this)
                    .registerReceiver(mGetOEMInfoReceiver, filter);
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
        if (mGetOEMInfoReceiver != null) {
            LocalBroadcastManager.getInstance(AddDistributorInfo.this)
                    .unregisterReceiver(mGetOEMInfoReceiver);
        }
        super.onDestroy();
    }

    @Override
    public Intent getSupportParentActivityIntent () {
        Intent intent;
        intent = new Intent(this, OverlayDemo.class);
        return intent;
    }

    public void addDistributorInfo(View view) {
        if (!isNetworkAvailable()) {
            AlertDialogShow(getString(R.string.network_disconnect));
            return;
        }

        ensureDialog();

        EditText disText = (EditText) findViewById(R.id.distributorName);
        String disName = disText.getText().toString().trim();
        EditText disPnText = (EditText) findViewById(R.id.distributorPN);
        String disPhoneNumber = disPnText.getText().toString().trim();
        String city = mCities[mProvinceIndex][mCityIndex];
        String province = mProvinces[mProvinceIndex];
        EditText disAddressText = (EditText) findViewById(R.id.distributorStreetAddress);
        String disStreetAddress = disAddressText.getText().toString().trim();
        EditText disLatText = (EditText) findViewById(R.id.distributorLat);
        String disLat = disLatText.getText().toString().trim();
        EditText disLonText = (EditText) findViewById(R.id.distributorLon);
        String disLon = disLonText.getText().toString().trim();

        Bundle bundle = new Bundle();
        bundle.putString("adminPhone", "13621812239");
        int cityId = mProvinceIndex * 100 + mCityIndex;
        bundle.putInt("cityId", cityId);
        bundle.putString("cityName", city);
        bundle.putString("latitude", disLat);
        bundle.putString("longitude", disLon);
        bundle.putInt("provinceId", mProvinceIndex);
        bundle.putString("provinceName", province);
        bundle.putString("resellerAddress", disStreetAddress);
        bundle.putString("resellerName", disName);
        bundle.putString("resellerPhone", disPhoneNumber);
        SharedPreferences mainPref =
                getSharedPreferences(getString(R.string.shared_pref_pacakge),
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mainPref.edit();
        editor.putBoolean("DisInfoDatabaseSynced", false);
        editor.commit();

        mBundle = bundle;

        new TestNetworkAsyncTask(AddDistributorInfo.this,
                TestNetworkAsyncTask.TYPE_ADD_DIS_INFO,
                bundle).execute(addDisInfoUrl);
    }

    public void getAddressGeoCode(View view) {
        String editCity = mCities[mProvinceIndex][mCityIndex];
        EditText editGeoCodeKey = (EditText) findViewById(R.id.distributorStreetAddress);
        mSearch.geocode(new GeoCodeOption().city(
                editCity).address(
                editGeoCodeKey.getText().toString().trim()));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            AlertDialogShow(getString(R.string.address_to_latlon));
            return;
        }
        String Lat = new Float(result.getLocation().latitude).toString();
        String Lon = new Float(result.getLocation().longitude).toString();

        EditText latText = (EditText) findViewById(R.id.distributorLat);
        latText.setText(Lat);
        EditText lonText = (EditText) findViewById(R.id.distributorLon);
        lonText.setText(Lon);
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

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
        AlertDialog.Builder builder =  new AlertDialog.Builder(AddDistributorInfo.this);
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

    private void ensureDialog() {
        if (dialog == null) {
            String title = getString(R.string.process_wait_title);
            String msg = getString(R.string.process_wait_msg);

            dialog = ProgressDialog.show(AddDistributorInfo.this, title, msg, true, true);
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

    class GetOEMInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            dismissProgressDialog();
            if (intent.getAction().equals(Login_main.ACTION_ADD_DISTRIBUTOR_RESULT)) {
                String msg;
                if (intent.getBooleanExtra("addDisInfoSuccess", false)) {
                    msg = "添加经销商信息成功";
                    synchronized (OverlayDemo.mSimpleDisInfoList) {
                        OverlayDemo.SimpleDisInfo distInfo = new OverlayDemo.SimpleDisInfo();
                        distInfo.name = mBundle.getString("resellerName");
                        distInfo.phoneNumber = mBundle.getString("resellerPhone");
                        distInfo.longitude = Double.parseDouble(mBundle.getString("longitude"));
                        distInfo.latitude = Double.parseDouble(mBundle.getString("latitude"));
                        distInfo.provinceId = mProvinceIndex;
                        OverlayDemo.mSimpleDisInfoList.add(distInfo);

                        OverlayDemo.mSimpleDisInfo = distInfo;

                        DistributorInfo distributorInfo = new DistributorInfo();
                        distributorInfo.resellerName = mBundle.getString("resellerName");
                        distributorInfo.resellerPhone = mBundle.getString("resellerPhone");
                        distributorInfo.resellerProvince = mBundle.getString("provinceName");
                        distributorInfo.resellerCity = mBundle.getString("cityName");
                        distributorInfo.resellerAddress = mBundle.getString("resellerAddress");
                        distributorInfo.longitude = mBundle.getString("longitude");
                        distributorInfo.latitude = mBundle.getString("latitude");
                        distributorInfo.provinceId = mProvinceIndex;
                        mDatabaseHandler.saveDisInfoToDb(distributorInfo);
                    }
                } else {
                    msg = "添加失败: " + intent.getStringExtra("result");
                }

                SharedPreferences mainPref =
                        getSharedPreferences(getString(R.string.shared_pref_pacakge),
                                Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mainPref.edit();
                editor.putBoolean("DisInfoDatabaseSynced", true);
                editor.commit();
                AlertDialogShow(msg);
            }
        }
    }
}
