package baidumapsdk.demo.demoapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.*;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

/**
 * 演示覆盖物的用法
 */
public class OverlayDemo extends ActionBarActivity {

	/**
	 * MapView 是地图主控件
	 */
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private CoordinateConverter coorConverter;
	private BroadcastReceiver mGetDisInfoReceiver = null;
	private InfoWindow mInfoWindow;

	private static String mUserPhone;

	public static String jsonStr = null;
	public static String mBatCountJsonStr = null;
	public LatLng mCurrLoc;
	public float mCurrZoom;
	public float mOriZoom;
	public LatLng mOriLoc;
	public boolean mRecover = false;
	private boolean isMunicipality = false;
	private int mCurProvince = -1;
	private JSONArray mCurProCities;
	private LatLng mCurProvLoc;

	private Spinner mProvince;
	private ArrayAdapter<String> provinceAdapter = null;
	private int mProvinceIndex = 1;

	public static List<DistributorInfo> mDisInfoList = null;
	public static List<SimpleDisInfo> mSimpleDisInfoList = null;
	public static int mDisTotalCount = 0;
	private static long mDatabaseItemsCount = 0;
	public static int mCurDisCount = 0;
	public static int mPageOffset = 1;
	private static int mPreDisOffset = 0;
	public final int mPageSize = 4;
	private static int mFailedTries = 0;
	private final int mDisZIndex = 1024;
	public static SimpleDisInfo mSimpleDisInfo = null;
	private boolean mResetDisInfoTable = false;

	private String mGetDisInfoUrl = null;
	private String mGetCityBatCountUrl = null;
	private Marker mCurMarker = null;
	private JSONArray mCurJsonArray = null;
	private DisInfoDatabaseHandler mDatabaseHandler = null;
	private static boolean mDatabaseSyncDone = false;

	private int mAboutCode = 0x01;
	private int mUpdateCode = 0x02;

	public final String[] mSettingsList = new String[]{
			"添加经销商信息",
			"查看经销商信息",
			"关于",
			"退出"
	};

	private int[] mCityBatArray = null;

	BitmapDescriptor bdA = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_marka);
	BitmapDescriptor bd = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_gcoding_large);
	BitmapDescriptor bdGround = BitmapDescriptorFactory
			.fromResource(R.drawable.ground_overlay);
	BitmapDescriptor bdCities = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_geo);
	BitmapDescriptor bdDis = BitmapDescriptorFactory
			.fromResource(R.drawable.download_2);


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.oem_view2);
		//getSupportActionBar().setDisplayShowHomeEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		SharedPreferences mainPref = getSharedPreferences(getString(R.string.shared_pref_pacakge),
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mainPref.edit();
		editor.putString("loginView", "OemView");
		editor.putBoolean("LoggedIn", true);
		editor.commit();

		String updateUrl = Login_main.preUrl + Login_main.updateEndpoint;
		new TestNetworkAsyncTask(OverlayDemo.this,
				TestNetworkAsyncTask.TYPE_GET_APP_VERSION,
				null).execute(updateUrl);

		mProvince = (Spinner)findViewById(R.id.distributor_province);
		provinceAdapter = new ArrayAdapter<String>(OverlayDemo.this,
				R.layout.customize_dropdown_item, AddDistributorInfo.mProvinces);
		mProvince.setAdapter(provinceAdapter);
		mProvince.setSelection(mProvinceIndex, true);
		mProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				mProvinceIndex = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		//mUserPhone = getIntent().getStringExtra("userPhone");
		mUserPhone = mainPref.getString("lastAccount", "");

		//mGetDisInfoUrl = Login_main.preUrl + "/reseller/infos.json";
		//mGetCityBatCountUrl = Login_main.preUrl + "/reseller/sales.json";
		mGetDisInfoUrl = Login_main.preUrl + getString(R.string.reseller_infos);
		mGetCityBatCountUrl = Login_main.preUrl + getString(R.string.reseller_sales);

		//mDatabaseSyncDone = mainPref.getBoolean("DisInfoDatabaseSynced", false);

		if (mCityBatArray == null) {
			mCityBatArray = new int[50];
		}

		mDatabaseHandler = new DisInfoDatabaseHandler(OverlayDemo.this);

		coorConverter = new CoordinateConverter();
		coorConverter.from(CoordinateConverter.CoordType.GPS);

		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(6.0f);
		mBaiduMap.setMapStatus(msu);

		MapStatus ms = mBaiduMap.getMapStatus();
		mOriLoc = ms.target;
		mOriZoom = ms.zoom;

		mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
			public void onMapStatusChangeStart(MapStatus status) {

			}

			public void onMapStatusChangeFinish(MapStatus status) {
				MapStatus ms = mBaiduMap.getMapStatus();
				if (mRecover && !isMunicipality &&
						((ms.zoom - mCurrZoom < -0.1) || (ms.zoom - mCurrZoom > 0.1))) {
					mCurrZoom = ms.zoom;
					if (ms.zoom < 8.0) {
						modifyLatOffset(0.11);
					} else if (ms.zoom < 11.0) {
						modifyLatOffset(0.08);
					} else if (ms.zoom < 13) {
						modifyLatOffset(0.04);
					} else {
						modifyLatOffset(0.02);
					}
				}
			}

			public void onMapStatusChange(MapStatus status) {

			}
		});

		readCityInfo();
		//addProvinceMarker();
		if (mDisInfoList == null) {
			mDisInfoList = new ArrayList<DistributorInfo>();
		}

		if (mSimpleDisInfoList == null) {
			mSimpleDisInfoList = new ArrayList<SimpleDisInfo>();
		}

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				DistributorInfo distributorInfo =
						mDatabaseHandler.getDisInfoByPhone(marker.getTitle());
				if (distributorInfo != null) {
					showMarkerInfoWindow(marker, distributorInfo);
				}
				return true;
			}
		});

		mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				if (mInfoWindow != null) {
					mBaiduMap.hideInfoWindow();
					mInfoWindow = null;
				}
			}

			@Override
			public boolean onMapPoiClick(MapPoi mapPoi) {
				return false;
			}
		});

		if (mGetDisInfoReceiver == null) {
			mGetDisInfoReceiver = new GetDisInfoReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Login_main.ACTION_GET_DISTRIBUTOR_RESULT);
			filter.addAction(Login_main.ACTION_GET_CITY_BAT_COUNT);
			filter.addAction(Login_main.ACTION_GET_APP_VERSION);
			LocalBroadcastManager.getInstance(OverlayDemo.this)
					.registerReceiver(mGetDisInfoReceiver, filter);
		}

		if (mDatabaseSyncDone && mSimpleDisInfoList.size() != 0) {
			addDis();
		}
	}

	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mDatabaseItemsCount = mDatabaseHandler.itemCountOfDisInfo();
		if (!mDatabaseSyncDone) {
			Bundle bundle = new Bundle();
			bundle.putString("adminPhone", mUserPhone);
			bundle.putInt("pageNo", mPageOffset++);
			bundle.putInt("size", mPageSize);
			new TestNetworkAsyncTask(OverlayDemo.this,
					TestNetworkAsyncTask.TYPE_GET_DIS_INFO, bundle)
					.execute(mGetDisInfoUrl);
		} else {
			if (mSimpleDisInfoList.size() != mDatabaseItemsCount) {
				buildDisListFromDatabase();
			}
		}
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		addNewDisToCurrentProvince();
		mMapView.onResume();
		super.onResume();
	}

	public void buildDisListFromDatabase() {
		mSimpleDisInfoList.clear();
		new Thread() {
			@Override
			public void run() {
				mDatabaseHandler.buildSimpleDisInfoList(mSimpleDisInfoList);
				addDis();
			}
		}.start();
	}

	public void enterProvince(View view) {
		if (mProvinceIndex != mCurProvince) {
			mRecover = true;
			mCurProvince = mProvinceIndex;
			if (mProvinceIndex < 4) {
				isMunicipality = true;
			} else {
				isMunicipality = false;
			}
			int provinceIndex = mProvinceIndex;
			int size = AddDistributorInfo.mCities[provinceIndex].length;
			int cityIdArray[] = new int[size];
			for (int i = 0; i < size; i++) {
				cityIdArray[i] = provinceIndex * 100 + i;
			}
			Bundle bundle = new Bundle();
			bundle.putIntArray("citys", cityIdArray);
			new TestNetworkAsyncTask(OverlayDemo.this,
					TestNetworkAsyncTask.TYPE_GET_CITY_BAT_COUNT, bundle)
					.execute(mGetCityBatCountUrl);
		}
	}

	private void addDis() {
		for (int i = 0; i < mSimpleDisInfoList.size(); i++) {
			OverlayOptions testP;
			SimpleDisInfo temp = mSimpleDisInfoList.get(i);
			LatLng lltemp = new LatLng(temp.latitude, temp.longitude);

			testP = new MarkerOptions().position(lltemp).icon(bdDis)
					.zIndex(mDisZIndex).draggable(true);
			((Marker) mBaiduMap.addOverlay(testP)).setTitle(temp.phoneNumber);
		}
	}

	private void addDisPerProvince(int provinceId) {
		for (int i = 0; i < mSimpleDisInfoList.size(); i++) {
			SimpleDisInfo temp = mSimpleDisInfoList.get(i);
			if (provinceId == temp.provinceId) {
				OverlayOptions testP;
				LatLng lltemp = new LatLng(temp.latitude,temp.longitude);
				testP = new MarkerOptions().position(lltemp).icon(bdDis)
						.zIndex(mDisZIndex).draggable(true);
				((Marker) mBaiduMap.addOverlay(testP)).setTitle(temp.phoneNumber);
			}
		}
	}

	private void addNewDisToCurrentProvince() {
		if (mSimpleDisInfo != null) {
			OverlayOptions testP;
			LatLng lltemp = new LatLng(mSimpleDisInfo.latitude, mSimpleDisInfo.longitude);

			testP = new MarkerOptions().position(lltemp).icon(bdDis)
					.zIndex(mDisZIndex).draggable(true);
			((Marker) mBaiduMap.addOverlay(testP)).setTitle(mSimpleDisInfo.phoneNumber);
			mSimpleDisInfo = null;
		}
	}

	public void showMarkerInfoWindow(Marker marker, DistributorInfo userInfo) {
		TextView textView = (TextView) View.inflate(OverlayDemo.this,
				R.layout.marker_info_window,
				null);

		String info = "名字：" + userInfo.resellerName + "\n" +
				"省市：" + userInfo.resellerProvince + " " + userInfo.resellerCity + "\n" +
				"地址：" + userInfo.resellerAddress + "\n" +
				"号码：" + userInfo.resellerPhone;
		textView.setText(info);
		LatLng ll = marker.getPosition();
		mInfoWindow =
				new InfoWindow(BitmapDescriptorFactory.fromView(textView),
						ll, -47, null);
		mBaiduMap.showInfoWindow(mInfoWindow);
	}

	public void addCityBatCount(String provinceName, JSONArray jsonArray) {
		mBaiduMap.clear();
		JSONArray jArr;
		OverlayOptions testP;
		try {
			JSONObject jObj = new JSONObject(jsonStr);
			if (isMunicipality) {
				jArr = jObj.getJSONArray("municipalities");
			} else {
				jArr = jObj.getJSONArray("provinces");
			}
			int count, index1 = 0, index2 = 0;
			for (count = 0; count < jArr.length(); count++) {
				JSONObject tempProvince = (JSONObject) jArr.get(count);
				String proName = tempProvince.getString("n");
				if (proName.equals(provinceName)) {
					index1 = tempProvince.getString("g").indexOf(',');
					index2 = tempProvince.getString("g").indexOf('|');
					String sub1 = tempProvince.getString("g").substring(0, index1);
					String sub2 = tempProvince.getString("g").substring(index1 + 1, index2);
					LatLng lltemp = new LatLng(Double.parseDouble(sub2), Double.parseDouble(sub1));

					//coorConverter.coord(lltemp);
					//LatLng llBaidu = coorConverter.convert();

					mCurProvLoc = lltemp;
					MapStatus ms = mBaiduMap.getMapStatus();
					mCurrLoc = ms.target;

					MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(lltemp, 9.0f);

					mBaiduMap.animateMapStatus(msu, 500);
					addDisPerProvince(mCurProvince);

					if (isMunicipality) {
						int batCount = jsonArray.getInt(0);
						if (batCount == 0)
							return;
						LatLng temp = new LatLng(lltemp.latitude - 0.08, lltemp.longitude);
						String cityCount = Integer.valueOf(batCount).toString();
						OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00)
								.fontSize(20).fontColor(0xFFFF0000).text(cityCount).rotate(0)
								.position(temp).zIndex(-1);
						OverlayOptions ooDot = new DotOptions().center(temp).radius(8)
								.color(0xFF0000FF);
						mBaiduMap.addOverlay(ooText);

						return;
					}

					mCurProCities = tempProvince.getJSONArray("cities");
					if (mCurProCities.length() != jsonArray.length()) {
						/*Log.d("test2", " *** json city array length does not match");*/
						return;
					}
					for (int k = 0; k < mCurProCities.length(); k++) {
						JSONObject temp = (JSONObject) mCurProCities.get(k);
						index1 = temp.getString("g").indexOf(',');
						index2 = temp.getString("g").indexOf('|');
						sub1 = temp.getString("g").substring(0, index1);
						sub2 = temp.getString("g").substring(index1 + 1, index2);

						lltemp = new LatLng((Double.parseDouble(sub2) - 0.08), Double.parseDouble(sub1));

						//coorConverter.coord(lltemp);
						//LatLng llBaidu2 = coorConverter.convert();

						testP = new MarkerOptions().position(lltemp).icon(bdCities)
								.zIndex(0).draggable(true);
						int batCount = mCurJsonArray.getInt(k);
						if (batCount == 0)
							continue;
						String cityCount = Integer.valueOf(batCount).toString();
						OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00)
								.fontSize(20).fontColor(0xFFFF0000).text(cityCount).rotate(0)
								.position(lltemp).zIndex(-1);
						OverlayOptions ooDot = new DotOptions().center(lltemp).radius(8)
								.color(0xFF0000FF);
						mBaiduMap.addOverlay(ooText);
					}
					break;
				}
			}
		} catch (JSONException e) {
			Log.d("test2", " *** addcity bat count json exception");
		}
	}

	private void modifyLatOffset(double offset) {
		OverlayOptions testP;
		LatLng lltemp;
		int index1 = 0, index2 = 0;
		String sub1, sub2;
		mBaiduMap.clear();
/*		testP = new MarkerOptions().position(mCurProvLoc).icon(bdA)
				.zIndex(10).draggable(true);
		mBaiduMap.addOverlay(testP);*/

		try {
			if (mCurProCities.length() != mCurJsonArray.length()) {
				Log.d("test2", " *** modify lat json array lenght not match");
				return;
			}

			if (isMunicipality) {
				int count = mCurJsonArray.getInt(0);
				if (count == 0)
					return;
				String cityCount = Integer.valueOf(count).toString();
				LatLng temp = new LatLng(mCurProvLoc.latitude - offset, mCurProvLoc.longitude);
				OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00)
						.fontSize(20).fontColor(0xFFFF0000).text(cityCount).rotate(0)
						.position(temp).zIndex(-1);
				OverlayOptions ooDot = new DotOptions().center(temp).radius(8)
						.color(0xFF0000FF);
				mBaiduMap.addOverlay(ooText);

				return;
			}
			for (int k = 0; k < mCurProCities.length(); k++) {
				JSONObject temp = (JSONObject) mCurProCities.get(k);
				index1 = temp.getString("g").indexOf(',');
				index2 = temp.getString("g").indexOf('|');
				sub1 = temp.getString("g").substring(0, index1);
				sub2 = temp.getString("g").substring(index1 + 1, index2);

				lltemp = new LatLng((Double.parseDouble(sub2) - offset), Double.parseDouble(sub1));

				//coorConverter.coord(lltemp);
				//LatLng llBaidu2 = coorConverter.convert();

				testP = new MarkerOptions().position(lltemp).icon(bdCities)
						.zIndex(0).draggable(true);
				int count = mCurJsonArray.getInt(k);
				if (count == 0)
					continue;
				String cityCount = Integer.valueOf(count).toString();
				OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00)
						.fontSize(20).fontColor(0xFFFF0000).text(cityCount).rotate(0)
						.position(lltemp).zIndex(-1);
				OverlayOptions ooDot = new DotOptions().center(lltemp).radius(8)
						.color(0xFF0000FF);
				mBaiduMap.addOverlay(ooText);
			}
		} catch (JSONException e) {

		}
		addDisPerProvince(mCurProvince);
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
		AlertDialog.Builder builder =  new AlertDialog.Builder(OverlayDemo.this);
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

	public void AlertUpdateDialogShow(String message, final Intent intent) {
		AlertDialog.Builder builder =  new AlertDialog.Builder(OverlayDemo.this);
		builder.setTitle("提示")
				.setMessage(message)
				.setPositiveButton(getString(R.string.update_button),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
								AlertDialogShow(getString(R.string.update_confirm), intent);
							}
						})
				.setNegativeButton(getString(R.string.non_update_button),
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

	public void AlertDialogShow(String message, final Intent intent) {
		AlertDialog.Builder builder =  new AlertDialog.Builder(OverlayDemo.this);
		builder.setTitle("提示")
				.setMessage(message)
				.setPositiveButton(getString(R.string.positive_button),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								startActivityForResult(intent, mUpdateCode);
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

	@Override
	protected void onDestroy() {
		// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		if (mGetDisInfoReceiver != null) {
			LocalBroadcastManager.getInstance(OverlayDemo.this)
					.unregisterReceiver(mGetDisInfoReceiver);
			mGetDisInfoReceiver = null;
		}
		mMapView.onDestroy();
		super.onDestroy();
		// 回收 bitmap 资源
		bdA.recycle();
		bd.recycle();
		bdGround.recycle();
		bdDis.recycle();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void readCityInfo() {
		try {
			InputStream input = getApplicationContext().getAssets().open("BaiduMap_cityCenter.txt");
			Reader reader = new InputStreamReader(input, "GBK");
			StringBuilder buffer = new StringBuilder();
			char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
			jsonStr = buffer.toString();
		} catch (IOException e) {

		}
	}
	public void addProvinceMarker() {
		JSONArray jArr;
		try {
			JSONObject jObj = new JSONObject(jsonStr);
			for (int i=0; i<2; i++) {
				if (i == 0) {
					jArr = jObj.getJSONArray("municipalities");
				} else {
					jArr = jObj.getJSONArray("provinces");
				}
				int count, index1 = 0, index2 = 0;
				for (count = 0; count < jArr.length(); count++) {
					JSONObject tempProvince = (JSONObject) jArr.get(count);
					String proName = tempProvince.getString("n");
					index1 = tempProvince.getString("g").indexOf(',');
					index2 = tempProvince.getString("g").indexOf('|');
					String sub1 = tempProvince.getString("g").substring(0, index1);
					String sub2 = tempProvince.getString("g").substring(index1 + 1, index2);
					OverlayOptions testP;
					LatLng lltemp = new LatLng(Double.parseDouble(sub2), Double.parseDouble(sub1));

					coorConverter.coord(lltemp);
					LatLng llBaidu2 = coorConverter.convert();

					testP = new MarkerOptions().position(llBaidu2).icon(bd)
							.zIndex(i+9).draggable(true);
					((Marker) mBaiduMap.addOverlay(testP)).setTitle(proName);
				}
			}

		}  catch (JSONException e) {

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("*/*");
		return intent;
	}

	private void resetStaticMembers() {
		jsonStr = null;
		mBatCountJsonStr = null;
		mDisInfoList.clear();
		mSimpleDisInfoList.clear();
		mDisTotalCount = 0;
		mCurDisCount = 0;
		mPageOffset = 1;
		mPreDisOffset = 0;
		mFailedTries = 0;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_user_info:
				View view = findViewById(R.id.add_user_info);
				android.support.v7.widget.PopupMenu popupMenu =
						new android.support.v7.widget.PopupMenu(OverlayDemo.this, view);
				for (int i =0; i < mSettingsList.length; i++) {
					popupMenu.getMenu().add(0, i, i, mSettingsList[i]);
				}
				popupMenu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						Bundle bundle = new Bundle();
						switch (menuItem.getItemId()) {
							case 0:
								Intent intent = new Intent(OverlayDemo.this, AddDistributorInfo.class);
								startActivity(intent);
								break;
							case 1:
								Intent intent1 = new Intent(OverlayDemo.this,
										DistributorInfoList.class);
								intent1.putExtra("type", 0);
								startActivity(intent1);
								break;
							case 2:
								Intent intent3 = new Intent(OverlayDemo.this,
										AboutThisApp.class);
								startActivityForResult(intent3, mAboutCode);
								break;
							case 3:
								MapStatus ms = mBaiduMap.getMapStatus();
								SharedPreferences mainPref =
										getSharedPreferences(getString(R.string.shared_pref_pacakge),
												Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = mainPref.edit();
								editor.putBoolean("LoggedIn", false);
								editor.putFloat("oem_zoom", ms.zoom);
								editor.putFloat("oem_lat", (float)ms.target.latitude);
								editor.putFloat("oem_lon", (float) ms.target.longitude);
								editor.commit();
								mDatabaseSyncDone = false;
								mSimpleDisInfoList.clear();
								mCurDisCount = 0;
								mPageOffset = 1;
								Intent intent2 = new Intent(OverlayDemo.this, Login_main.class);
								startActivity(intent2);
								finish();
								break;
							default:
								break;
						}
						return true;
					}
				});
				popupMenu.show();
				break;
			default:
				break;
		}
		return false;
	}

	@Override
	public Intent getSupportParentActivityIntent () {
		Intent intent;
		if (mRecover) {
			mBaiduMap.clear();
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(mOriLoc, mOriZoom);
			mBaiduMap.animateMapStatus(msu, 300);
			//addProvinceMarker();
			addDis();
			mCurProvince = -1;
			mRecover = false;
			intent = null;
		} else {
			intent = new Intent(OverlayDemo.this, Login_main.class);
		}
		return intent;
	}

	@Override
	public void onBackPressed() {
		if (mRecover) {
			mBaiduMap.clear();
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(mOriLoc, mOriZoom);
			mBaiduMap.animateMapStatus(msu, 300);
			//addProvinceMarker();
			addDis();
			mCurProvince = -1;
			mRecover = false;
		} else {
			super.onBackPressed();
		}
	}

	class GetDisInfoReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Login_main.ACTION_GET_DISTRIBUTOR_RESULT)) {
				if (intent.getBooleanExtra("getDisInfoSuccess", false)) {
					if (!mResetDisInfoTable) {
						if (mDisTotalCount == mDatabaseItemsCount) {
							mDatabaseSyncDone = true;
							buildDisListFromDatabase();
							return;
						} else {
							mDatabaseHandler.resetDisInfoTable();
							mResetDisInfoTable = true;
						}
					}
					mFailedTries = 0;
					for (int i = 0; i < mDisInfoList.size(); i++) {
						DistributorInfo disInfo = mDisInfoList.get(i);
						if (!mDatabaseHandler.isSomeDisInfoExist(disInfo.resellerPhone)) {
							mDatabaseHandler.saveDisInfoToDb(mDisInfoList.get(i));
						}
						SimpleDisInfo disInfo1 = new SimpleDisInfo();
						disInfo1.name = disInfo.resellerName;
						disInfo1.phoneNumber = disInfo.resellerPhone;
						disInfo1.latitude = Double.parseDouble(disInfo.latitude);
						disInfo1.longitude = Double.parseDouble(disInfo.longitude);
						disInfo1.provinceId = disInfo.provinceId;
						mSimpleDisInfoList.add(disInfo1);
					}
					mCurDisCount += mDisInfoList.size();

					if (mCurDisCount < mDisTotalCount) {
						Bundle bundle = new Bundle();
						bundle.putString("adminPhone", "13621812239");
						bundle.putInt("pageNo", mPageOffset++);
						bundle.putInt("size", mPageSize);
						new TestNetworkAsyncTask(OverlayDemo.this,
								TestNetworkAsyncTask.TYPE_GET_DIS_INFO, bundle)
								.execute(mGetDisInfoUrl);
					} else {
						SharedPreferences mainPref =
								getSharedPreferences(getString(R.string.shared_pref_pacakge),
								Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = mainPref.edit();
						editor.putBoolean("DisInfoDatabaseSynced", true);
						editor.commit();
						mDatabaseSyncDone = true;
						mDisInfoList.clear();
						mPageOffset = mDisTotalCount / mPageSize + 1;
						addDis();
					}
				} else {
					if (mFailedTries++ < 3) {
						Bundle bundle = new Bundle();
						bundle.putString("adminPhone", "13621812239");
						bundle.putInt("pageNo", mPageOffset - 1);
						bundle.putInt("size", mPageSize);
						new TestNetworkAsyncTask(OverlayDemo.this,
								TestNetworkAsyncTask.TYPE_GET_DIS_INFO, bundle)
								.execute(mGetDisInfoUrl);
					} else {
						String msg = intent.getStringExtra("result");
						AlertDialogShow(msg);
					}
				}
			} else if (intent.getAction().equals(Login_main.ACTION_GET_CITY_BAT_COUNT)) {
				String msg = null;
				if (intent.getBooleanExtra("getBatCount", false)) {
					try {
						JSONObject jsonObject = new JSONObject(mBatCountJsonStr);
						JSONObject jObect1 = jsonObject.getJSONObject("data");
						JSONArray jsonArray = jObect1.getJSONArray("citySales");
						mCurJsonArray = jsonArray;
						addCityBatCount(AddDistributorInfo.mProvinces[mCurProvince], jsonArray);
						return;
					} catch (JSONException e) {
						msg = "网络数据解析异常";
					}
				} else {
					msg = intent.getStringExtra("result");
				}
				AlertDialogShow(msg);
			} else if (intent.getAction().equals(Login_main.ACTION_GET_APP_VERSION)) {
				processAppUpdate(intent);
			}
		}

		public void processAppUpdate(Intent intent) {
			if (intent.getBooleanExtra("getAppVersion", false)) {
				String apkVersion = intent.getStringExtra("apkVersion");
				String downloadUrl = intent.getStringExtra("downloadUrl");
				String updateMsg = "最新版本：V" + apkVersion + " "
						+ "已经可用，为保证此应用程序以后能正常工作，请您进行下载更新。";
				Intent intent4 = new Intent(OverlayDemo.this, UpdateApp.class);
				intent4.putExtra("apkUrl", downloadUrl);
				AlertUpdateDialogShow(updateMsg, intent4);
			}
		}
	}

	public static class SimpleDisInfo {
		SimpleDisInfo() {}
		String phoneNumber;
		String name;
		int provinceId;
		double latitude;
		double longitude;
	}
}
