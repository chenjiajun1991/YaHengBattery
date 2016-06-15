package baidumapsdk.demo.demoapplication;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestNetworkAsyncTask extends AsyncTask<String, Void, String>  {

    public final static int TYPE_TEST = 0;
    public final static int TYPE_GET_VERIFICATION_CODE = 1;
    public final static int TYPE_USER_SIGN_UP = 2;
    public final static int TYPE_USER_SIGN_IN = 3;
    public final static int TYPE_ADD_USER_INFO = 4;
    public final static int TYPE_GET_BATTERY_LIST = 5;
    public final static int TYPE_SHARE_BATTERY = 6;
    public final static int TYPE_BAT_FOLLOWERS = 7;
    public final static int TYPE_GET_FRIEND_BATTERY_LIST = 8;
    public final static int TYPE_DEL_GROUP_CHILD = 9;
    public final static int TYPE_ADD_DIS_INFO = 10;
    public final static int TYPE_GET_DIS_INFO = 11;
    public final static int TYPE_DIS_GET_USER_INFO = 12;
    public final static int TYPE_GET_CITY_BAT_COUNT = 13;
    public final static int TYPE_GET_DIS_LOC = 14;
    public final static int TYPE_GET_BAT_LOC = 15;
    public final static int TYPE_GET_APP_VERSION = 16;
    public final static int TYPE_LOCK_BATTERY = 17;
    public final static int TYPE_GET_DIS_COUNT = 18;
    public final static int TYPE_GET_REAL_TIME_LOC = 19;
    public final static int TYPE_GET_HISTORY=20;

    private final int mTriesCount = 3;
    private int mType;
    private Context mContext;
    private Bundle mBundle = null;

    private List<String> mBatList = null;
    private List<String> mBatSimList = null;
    private List<String> mFollowerList = null;
    private String mGetBatListUrl = null;

    TestNetworkAsyncTask(Context context, int type, Bundle bundle) {
        mType = type;
        mContext = context;
        mBundle = bundle;
        mBatList = new ArrayList<String>();
        mBatSimList = new ArrayList<String>();
        mGetBatListUrl = Login_main.preUrl + "/user/btys.json";
    }
    @Override
    protected String doInBackground(String... urls) {
        String srcUrl = urls[0];
        switch (mType) {
            case TYPE_TEST:
                testGetJsonFunction(srcUrl);
                break;
            case TYPE_GET_VERIFICATION_CODE:
                testGetVerificationCode(srcUrl);
                break;
            case TYPE_USER_SIGN_UP:
                testUserSignUp(srcUrl);
                break;
            case TYPE_USER_SIGN_IN:
                testUserSignIn(srcUrl);
                break;
            case TYPE_ADD_USER_INFO:
                testAddUserInfo(srcUrl);
                break;
            case TYPE_SHARE_BATTERY:
                testShareBattery(srcUrl);
                break;
            case TYPE_BAT_FOLLOWERS:
                getBatAndSharePersons(srcUrl);
                break;
            case TYPE_DEL_GROUP_CHILD:
                testUnShareBattery(srcUrl);
                break;
            case TYPE_GET_FRIEND_BATTERY_LIST:
                testGetFriendBatteryList(srcUrl);
                break;
            case TYPE_ADD_DIS_INFO:
                testAddDistributorInfo(srcUrl);
                break;
            case TYPE_GET_DIS_INFO:
                testGetDistributorInfo(srcUrl);
                break;
            case TYPE_DIS_GET_USER_INFO:
                testDisGetUserInfo(srcUrl);
                break;
            case TYPE_GET_CITY_BAT_COUNT:
                testGetCityBatCount(srcUrl);
                break;
            case TYPE_GET_DIS_LOC:
                testGetDisLoc(srcUrl);
                break;
            case TYPE_GET_BAT_LOC:
                testGetBatLoc(srcUrl);
                break;
            case TYPE_GET_APP_VERSION:
                testGetAppVersion(srcUrl);
                break;
            case TYPE_LOCK_BATTERY:
                testLockBattery(srcUrl);
                break;
            case TYPE_GET_DIS_COUNT:
                testGetDistributorCount(srcUrl);
                break;
            case TYPE_GET_HISTORY:
                testGetHistoryGps(srcUrl);
            default:
                break;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {

    }

    private void testGetJsonFunction(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_GET_USER_GPS_DATA);
        String result = "未能获取您的云电池信息。";
        for (int i = 0; i < mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("deviceType", "android");
                jsonObject.put("userPhone", mBundle.getString("phoneNum"));
                jsonObject.put("version", LauncherActivity.mVersionName);
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(6144);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    is.close();
                    connection.disconnect();
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    Log.i("resCode",resCode);
                    if ("10000".equals(resCode)) {
                        UserView.jsonStr = temp;

                        intent.putExtra("getUserBatSuccess", true);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法链接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "网络传输数据格式异常";
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        intent.putExtra("getUserBatSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testGetVerificationCode(String srcUrl) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("appName", "samyh");
            jsonObject.put("authType", mBundle.getString("authType"));
            jsonObject.put("deviceType", "android");
            String phoneNum = mBundle.getString("phoneNum");
            jsonObject.put("userPhone", phoneNum);
            jsonObject.put("version", LauncherActivity.mVersionName);
            String message = "jsonReq=" + jsonObject.toString();
            URL url = new URL(srcUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);
            //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("Accept", "*/*");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setFixedLengthStreamingMode(message.getBytes().length);
            connection.connect();
            OutputStream os = new BufferedOutputStream(
                    connection.getOutputStream());
            os.write(message.getBytes());
            os.close();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ByteArrayBuffer baf = new ByteArrayBuffer(512);
                byte[] buffer = new byte[512];
                InputStream is = new BufferedInputStream(connection.getInputStream());

                int read = 0, offset = 0;
                while ((read = is.read(buffer, 0, 512)) != -1) {
                    baf.append(buffer, 0, read);
                }
                String temp = new String(baf.toByteArray());
                is.close();
                connection.disconnect();
            } else {
                Log.d("test2", "connection failed");
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("test2", "UnsupportedEncodingException");
        } catch (MalformedURLException e) {
            Log.d("test2", "MalformedURLException");
        } catch (IOException e) {
            Log.d("test2", "IOException");
        } catch (JSONException e) {
            Log.d("test2", "JsonException");
        }
    }

    private void testUserSignUp(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_USER_SIGN_UP_RESULT);
        String result = "注册失败，请您再试一次。";
        for (int i = 0; i < mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("authCode", mBundle.getString("verifyCode"));
                jsonObject.put("deviceInfo", "AAAAAAAAAAAA");
                jsonObject.put("deviceType", "android");
                jsonObject.put("password1", mBundle.getString("password"));
                jsonObject.put("password2", mBundle.getString("password"));
                String phoneNum = mBundle.getString("phoneNum");
                jsonObject.put("userPhone", phoneNum);
                jsonObject.put("version", LauncherActivity.mVersionName);
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(512);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        intent.putExtra("signupSuccess", true);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法链接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "网络传输数据格式异常";
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("signupSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testUserSignIn(String srcUrl) {
        String result = "用户登录失败";
        Intent intent = new Intent(Login_main.ACTION_USER_SIGN_IN_RESULT);
        for (int i=0; i<3; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("deviceType", "android");
                String phoneNum = mBundle.getString("phoneNum");
                jsonObject.put("userPhone", phoneNum);
                jsonObject.put("password", mBundle.getString("password"));
                jsonObject.put("version", LauncherActivity.mVersionName);
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(512);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        JSONObject jObj2 = jsonObject.getJSONObject("data");
                        intent.putExtra("signinSuccess", true);
                        intent.putExtra("userPhone", phoneNum);
                        intent.putExtra("userType", jObj2.getString("userType"));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法链接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "网络传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        intent.putExtra("result", result);
        intent.putExtra("signupSuccess", false);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testAddUserInfo(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_ADD_USER_RESULT);
        String result = "添加用户信息失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("btyImei", mBundle.getString("IMEI"));
                jsonObject.put("btySN", mBundle.getString("SN"));
                jsonObject.put("btySimNo", mBundle.getString("SimNo"));
                jsonObject.put("btyQuantity", mBundle.getString("BtyCount"));
                jsonObject.put("deviceType", "android");
                jsonObject.put("resellerPhone", mBundle.getString("ResPhone"));
                jsonObject.put("userName", mBundle.getString("userName"));
                jsonObject.put("userPhone", mBundle.getString("userPhone"));
                jsonObject.put("userGroup", mBundle.getString("userGroup"));
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(512);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        intent.putExtra("addUserInfoSuccess", true);
                        intent.putExtra("result", result);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("addUserInfoSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testGetFriendBatteryList(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_GET_FRIEND_BATTERY_LIST);
        String result = "获取云电池数据失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("deviceType", "android");
                jsonObject.put("userPhone", mBundle.getString("userPhone"));
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        intent.putExtra("getBatteryListSuccess", true);
                        UserView.jsonBatListStr = temp;
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        intent.putExtra("getBatteryListSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private boolean testGetBatteryList(String srcUrl, String userPhone) {
        Intent intent = new Intent(Login_main.ACTION_GET_BATTERY_LIST);
        String result = "获取云电池数据失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("deviceType", "android");
                jsonObject.put("userPhone", userPhone);
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0;
                    while ((read = is.read(buffer)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        if (parseBatJsonStr(temp)) {
                            return true;
                        } else {
                            result = "解析网络数据失败";
                        }
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        intent.putExtra("getBatteryListSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        return false;
    }

    private void testShareBattery(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_SHARE_BATTERY);
        String result = "添加云电池关注人失败";
        boolean state = false;
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("btyPubSn", mBundle.getString("batterySN"));
                jsonObject.put("deviceType", "android");
                jsonObject.put("friendPhone", mBundle.getString("friendPhone"));
                jsonObject.put("userPhone", mBundle.getString("userPhone"));
                jsonObject.put("friendNickName", mBundle.getString("friendNickName"));
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        intent.putExtra("shareBatSuccess", true);
                        intent.putExtra("groupPos", mBundle.getInt("groupPos"));
                        intent.putExtra("friendPhone", mBundle.getString("friendPhone"));
                        intent.putExtra("friendName", mBundle.getString("friendNickName"));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("shareBatSuccess", state);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testUnShareBattery(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_DEL_GROUP_CHILD_ITEM);
        boolean state = false;
        String result = "删除云电池关注人失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("btyPubSn", mBundle.getString("batSN"));
                jsonObject.put("deviceType", "android");
                jsonObject.put("friendPhone", mBundle.getString("friendPhone"));
                jsonObject.put("userPhone", mBundle.getString("userPhone"));
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        intent.putExtra("unShareBatSuccess", true);
                        intent.putExtra("groupPos", mBundle.getInt("groupPos"));
                        intent.putExtra("childPos", mBundle.getInt("childPos"));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    } else {
                        //intent.putExtra("unShareBatSuccess", false);
                        //intent.putExtra("result", result);
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("unShareBatSuccess", state);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void getBatAndSharePersons(String srcUrl) {
        String result = "获取电池数据失败";
        String userPhone = mBundle.getString("userPhone");
            if (testGetBatteryList(mGetBatListUrl, userPhone)) {
                for (int i=0; i<mTriesCount; i++) {
                    Intent intent = new Intent(Login_main.ACTION_GET_BAT_FOLLOWERS);
                    intent.putStringArrayListExtra("BatList", (ArrayList<String>) mBatList);
                    intent.putStringArrayListExtra("BatSimList", (ArrayList<String>) mBatSimList);
                    int k = 0;
                    if (mBatList.size() == 0) {
                        result = "未查到您的云电池信息，您是否尚未购买亚亨云电池？";
                    }
                    for (String batSN : mBatList) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("appName", "samyh");
                            jsonObject.put("btyPubSn", batSN);
                            jsonObject.put("deviceType", "android");
                            jsonObject.put("userPhone", userPhone);
                            jsonObject.put("version", "0.0.1");
                            String message = "jsonReq=" + jsonObject.toString();
                            URL url = new URL(srcUrl);
                            HttpURLConnection connection = (HttpURLConnection) url
                                    .openConnection();
                            connection.setReadTimeout(10000 /* milliseconds */);
                            connection.setConnectTimeout(15000 /* milliseconds */);
                            //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                            connection.setRequestProperty("Accept", "*/*");
                            connection.setDoOutput(true);
                            connection.setDoInput(true);
                            connection.setRequestMethod("POST");
                            connection.setFixedLengthStreamingMode(message.getBytes().length);
                            connection.connect();
                            OutputStream os = new BufferedOutputStream(
                                    connection.getOutputStream());
                            os.write(message.getBytes());
                            os.close();
                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                                byte[] buffer = new byte[512];
                                InputStream is = new BufferedInputStream(connection.getInputStream());

                                int read = 0, offset = 0;
                                while ((read = is.read(buffer, 0, 512)) != -1) {
                                    baf.append(buffer, 0, read);
                                }
                                String temp = new String(baf.toByteArray());
                                jsonObject = new JSONObject(temp);
                                String resCode = jsonObject.getString("resCode");
                                result = jsonObject.getString("result");
                                is.close();
                                connection.disconnect();

                                if ("10000".equals(resCode)) {
                                    if (parseFollowerJsonStr(temp)) {
                                        intent.putStringArrayListExtra(batSN,
                                                (ArrayList<String>) mFollowerList);
                                        if (k++ == mBatList.size() - 1) {
                                            intent.putExtra("shareBatSuccess", true);
                                            LocalBroadcastManager
                                                    .getInstance(mContext).sendBroadcast(intent);
                                            return;
                                        }
                                    } else {
                                        result = "解析网络数据出错";
                                        break;
                                    }
                                }
                            } else {
                                Log.d("test2", "connection failed");
                                result = "网络连接失败";
                                break;
                            }
                        } catch (UnsupportedEncodingException e) {
                            Log.d("test2", "UnsupportedEncodingException");
                            result = "数据编码异常";
                            break;
                        } catch (MalformedURLException e) {
                            Log.d("test2", "MalformedURLException");
                            result = "非法连接异常";
                            break;
                        } catch (IOException e) {
                            Log.d("test2", "IOException");
                            result = "网络连接失败";
                            break;
                        } catch (JSONException e) {
                            Log.d("test2", "JsonException");
                            result = "传输数据格式异常";
                            break;
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }
            } else {
                return;
            }

        Intent intent = new Intent(Login_main.ACTION_GET_BAT_FOLLOWERS);
        intent.putExtra("shareBatSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private boolean parseBatJsonStr(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONObject jObect1 = jsonObject.getJSONObject("data");
            JSONArray jArray1 = jObect1.getJSONArray("myBtys");
            Bundle bundle = new Bundle();
            for (int i = 0; i < jArray1.length(); i++) {
                JSONObject temp1 = (JSONObject) jArray1.get(i);
                String simNo = temp1.getString("bytImei");
                String sn = temp1.getString("btyPubSn");
                //String Item = "序列号: " + sn + "\n" + "SIM卡号: " + simNo;
                //String Item = "SIM卡号: " + simNo;
                mBatList.add(sn);
                mBatSimList.add(simNo);
            }
        } catch (JSONException e) {
            return  false;
        }
        return true;
    }

    private boolean parseFollowerJsonStr(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONObject jObect1 = jsonObject.getJSONObject("data");
            JSONArray jArray1 = jObect1.getJSONArray("followers");
            mFollowerList = new ArrayList<String>();
            Bundle bundle = new Bundle();
            for (int i = 0; i < jArray1.length(); i++) {
                JSONObject temp1 = (JSONObject) jArray1.get(i);
                //String simNo = temp1.getString("btySimNo");
                String fn = temp1.getString("followerName");
                String fp = temp1.getString("followerPhone");
                //String Item = "序列号: " + sn + "\n" + "SIM卡号: " + simNo;
                //String Item = "SIM卡号: " + simNo;
                mFollowerList.add(fn);
                mFollowerList.add(fp);
            }
        } catch (JSONException e) {
            return  false;
        }
        return true;
    }

    private void testAddDistributorInfo(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_ADD_DISTRIBUTOR_RESULT);
        String result = "添加经销商信息失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("adminPhone", mBundle.getString("adminPhone"));
                jsonObject.put("appName", "samyh");
                jsonObject.put("cityId", mBundle.getInt("cityId"));
                jsonObject.put("cityName", mBundle.getString("cityName"));
                jsonObject.put("deviceType", "android");
                jsonObject.put("latitude", mBundle.getString("latitude"));
                jsonObject.put("longitude", mBundle.getString("longitude"));
                jsonObject.put("provinceId", mBundle.getInt("provinceId"));
                jsonObject.put("provinceName", mBundle.getString("provinceName"));
                jsonObject.put("resellerAddress", mBundle.getString("resellerAddress"));
                jsonObject.put("resellerName", mBundle.getString("resellerName"));
                jsonObject.put("resellerPhone", mBundle.getString("resellerPhone"));
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        intent.putExtra("addDisInfoSuccess", true);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("addDisInfoSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testGetDistributorInfo(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_GET_DISTRIBUTOR_RESULT);
        String result = "获取经销商信息失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("adminPhone", mBundle.getString("adminPhone"));
                jsonObject.put("appName", "samyh");
                jsonObject.put("deviceType", "android");
                jsonObject.put("pageNo", mBundle.getInt("pageNo"));
                jsonObject.put("size", mBundle.getInt("size"));
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    //Log.d("test2", temp);
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        synchronized (OverlayDemo.mDisInfoList) {
                            OverlayDemo.mDisInfoList.clear();
                            if (parseDisInfoJsonStr(OverlayDemo.mDisInfoList, temp)) {
                                intent.putExtra("getDisInfoSuccess", true);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                return;
                            } else {
                                result = "解析网络数据出错";
                            }
                        }
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        intent.putExtra("getDisInfoSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testGetDistributorCount(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_GET_DISTRIBUTOR_RESULT);
        String result = "获取经销商信息失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("adminPhone", mBundle.getString("adminPhone"));
                jsonObject.put("appName", "samyh");
                jsonObject.put("deviceType", "android");
                jsonObject.put("pageNo", mBundle.getInt("pageNo"));
                jsonObject.put("size", mBundle.getInt("size"));
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    //Log.d("test2", temp);
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        JSONObject jObject1 = jsonObject.getJSONObject("data");
                        OverlayDemo.mDisTotalCount = jObject1.getInt("total");
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
        intent.putExtra("getDisInfoSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private boolean parseDisInfoJsonStr(List<DistributorInfo> list, String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONObject jObect1 = jsonObject.getJSONObject("data");

            OverlayDemo.mDisTotalCount = jObect1.getInt("total");

            JSONArray jArray1 = jObect1.getJSONArray("resellers");
            for (int i = 0; i < jArray1.length(); i++) {
                JSONObject temp1 = (JSONObject) jArray1.get(i);
                DistributorInfo disInfo = new DistributorInfo();
                disInfo.resellerName = temp1.getString("resellerName");
                disInfo.resellerProvince = temp1.getString("resellerProvince");
                disInfo.resellerCity = temp1.getString("resellerCity");
                disInfo.resellerAddress = temp1.getString("resellerAddress");
                disInfo.resellerPhone = temp1.getString("resellerPhone");
                disInfo.latitude = temp1.getString("latitude");
                disInfo.longitude = temp1.getString("longitude");
                disInfo.provinceId = temp1.getInt("provinceId");
                list.add(disInfo);
            }
        } catch (JSONException e) {
            return  false;
        }
        return true;
    }

    private void testDisGetUserInfo(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_DIS_GET_USER_INFO);
        String result = "未能获取您的客户云电池位置信息。";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("deviceType", "android");
                jsonObject.put("resellerPhone", mBundle.getString("resellerPhone"));
                jsonObject.put("pageNo", mBundle.getInt("pageNo"));
                jsonObject.put("size", mBundle.getInt("size"));
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        synchronized (DistributorView.mUserInfoList) {
                            int count = parseUserInfoJsonStr(DistributorView.mUserInfoList, temp);
                            if (count >= 0) {
                                intent.putExtra("getUserInfoSuccess", true);
                                intent.putExtra("userCount", count);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                return;
                            } else {
                                result = "解析网络数据出错";
                            }
                        }
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("getUserInfoSuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private int parseUserInfoJsonStr(List<DistributorView.UserLocInfo> list,
                                         String jsonStr) {
        int count = 0;
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONObject jObect1 = jsonObject.getJSONObject("data");

            DistributorView.mUserTotalCount = jObect1.getInt("total");

            JSONArray jArray1 = jObect1.getJSONArray("btyInfo");
            count = jArray1.length();
            for (int i = 0; i < count; i++) {
                JSONObject temp1 = (JSONObject) jArray1.get(i);
                DistributorView.UserLocInfo userInfo = new DistributorView.UserLocInfo();
                userInfo.lat = Double.parseDouble(temp1.getString("latitude"));
                userInfo.lon = Double.parseDouble(temp1.getString("longitude"));
                list.add(userInfo);
                DistributorView.mCurUserCount++;
            }
        } catch (JSONException e) {
            return  -1;
        }
        return count;
    }

    private void testGetCityBatCount(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_GET_CITY_BAT_COUNT);
        String result = "未能获取您的客户云电池信息。";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                JSONArray jsonArray = new JSONArray();
                int[] array = mBundle.getIntArray("citys");
                for (int k = 0; k < array.length; k++) {
                    jsonArray.put(array[k]);
                }
                jsonObject.put("citys", jsonArray);
                jsonObject.put("deviceType", "android");
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    OverlayDemo.mBatCountJsonStr = temp;
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        intent.putExtra("getBatCount", true);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("getBatCount", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


    private void testGetDisLoc(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_GET_DIS_LOC);
        String result = "未能获取您的位置信息。";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("deviceType", "android");
                jsonObject.put("resellerPhone", mBundle.getString("resellerPhone"));
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        intent.putExtra("getDisLoc", true);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                        intent.putExtra("longitude", jsonObject1.getString("longitude"));
                        intent.putExtra("latitude", jsonObject1.getString("latitude"));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("getDisLoc", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testGetBatLoc(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_GET_BAT_LOC);
        String result = "未能获取此云电池的位置信息。";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("btySimNo", mBundle.getString("btySimNo"));
                jsonObject.put("deviceType", "android");
                jsonObject.put("version", "0.0.1");
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode)) {
                        intent.putExtra("getBatLoc", true);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                        Double lat = Double.parseDouble(jsonObject1.getString("latitude"));
                        Double lon = Double.parseDouble(jsonObject1.getString("longitude"));
                        LatLng ll = new LatLng(lat, lon);
                        DistributorView.mNewBatLoc = ll;
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("getBatLoc", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testGetAppVersion(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_GET_APP_VERSION);
        String result = "获取更新信息失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", "samyh");
                jsonObject.put("deviceType", "android");
                jsonObject.put("version", LauncherActivity.mVersionName);
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(4096);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10003".equals(resCode) || "10004".equals(resCode)) {
                        intent.putExtra("getAppVersion", true);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                        intent.putExtra("apkVersion", jsonObject1.getString("latestVer"));
                        intent.putExtra("downloadUrl", jsonObject1.getString("downloadUrl"));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    } else if ("10000".equals(resCode)) {
                        break;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("getAppVersion", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void testLockBattery(String srcUrl) {
        Intent intent = new Intent(Login_main.ACTION_LOCK_BATTERY);
        String result = "设置云电池锁定状态失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userPhone", mBundle.getString("userPhone"));
                jsonObject.put("btyImei", mBundle.getString("btyImei"));
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(512);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();

                    if ("10000".equals(resCode) || "10002".equals(resCode)) {
                        intent.putExtra("setLockState", true);
                        intent.putExtra("resCode", resCode);
                        intent.putExtra("operation", mBundle.getString("operation"));
                        intent.putExtra("imei", mBundle.getString("btyImei"));
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("setLockState", false);
        intent.putExtra("imei", mBundle.getString("btyImei"));
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }



    public void testGetHistoryGps(String srcUrl){
        Intent intent = new Intent(Login_main.ACTION_GET_HISTORY_GPS);
        String result = "查询历史轨迹失败";
        for (int i=0; i<mTriesCount; i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("imei", mBundle.getString("imei"));
                jsonObject.put("date", mBundle.getString("date"));
                String message = "jsonReq=" + jsonObject.toString();
                URL url = new URL(srcUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.connect();
                OutputStream os = new BufferedOutputStream(
                        connection.getOutputStream());
                os.write(message.getBytes());
                os.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    ByteArrayBuffer baf = new ByteArrayBuffer(512);
                    byte[] buffer = new byte[512];
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    int read = 0, offset = 0;
                    while ((read = is.read(buffer, 0, 512)) != -1) {
                        baf.append(buffer, 0, read);
                    }
                    String temp = new String(baf.toByteArray());
                    jsonObject = new JSONObject(temp);
                    String resCode = jsonObject.getString("resCode");
                    result = jsonObject.getString("result");
                    is.close();
                    connection.disconnect();
                    if ("10000".equals(resCode) || "10002".equals(resCode)) {
                        Log.i("ff", temp);
                        UserView.jsonHistoryGps=temp;
                        intent.putExtra("getHistorySuccess", true);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        return;
                    }
                } else {
                    Log.d("test2", "connection failed");
                    result = "网络连接失败";
                }
            } catch (UnsupportedEncodingException e) {
                Log.d("test2", "UnsupportedEncodingException");
                result = "数据编码异常";
            } catch (MalformedURLException e) {
                Log.d("test2", "MalformedURLException");
                result = "非法连接异常";
            } catch (IOException e) {
                Log.d("test2", "IOException");
                result = "网络连接失败";
            } catch (JSONException e) {
                Log.d("test2", "JsonException");
                result = "传输数据格式异常";
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

        intent.putExtra("getHistorySuccess", false);
        intent.putExtra("result", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }
}
