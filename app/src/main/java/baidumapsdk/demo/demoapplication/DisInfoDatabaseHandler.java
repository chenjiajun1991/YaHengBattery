package baidumapsdk.demo.demoapplication;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

public class DisInfoDatabaseHandler extends SQLiteOpenHelper {

    private static final String DIS_INFO_DATABASE = "dis_info.db";
    private static final int DIS_DATABASE_VERSION = 1;
    private static final String TABLE_DIS_INFO = "dis_info";

    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "resellerName";
    private static final String KEY_PHONE = "resellerPhone";
    private static final String KEY_PROVINCE = "resellerProvince";
    private static final String KEY_CITY = "resellerCity";
    private static final String KEY_ADDRESS = "resellerAddress";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_PROVINCE_ID = "provinceId";

    private static final String[] COLUMNS = {
            KEY_NAME,
            KEY_PHONE,
            KEY_PROVINCE,
            KEY_CITY,
            KEY_ADDRESS,
            KEY_LONGITUDE,
            KEY_LATITUDE,
            KEY_PROVINCE_ID
    };

    private static final String[] SIMPLE_COLUMNS = {
            KEY_NAME,
            KEY_PHONE,
            KEY_LONGITUDE,
            KEY_LATITUDE,
            KEY_PROVINCE_ID
    };

    private static final String CREATE_DIS_INFO_TABLE = "create table " + TABLE_DIS_INFO
            + "(" + KEY_ID + " integer primary key," + KEY_NAME + " text,"
            + KEY_PHONE + " text," + KEY_PROVINCE + " text," + KEY_CITY + " text,"
            + KEY_ADDRESS + " text," + KEY_LONGITUDE + " text," + KEY_LATITUDE
            + " text," + KEY_PROVINCE_ID + " integer" + ")";

    public DisInfoDatabaseHandler(Context context) {
        super(context, DIS_INFO_DATABASE, null, DIS_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DIS_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int olderVersion, int newerVersion) {
        db.execSQL("drop table if exists " + TABLE_DIS_INFO);
        onCreate(db);
    }

    public void saveDisInfoToDb(DistributorInfo distributorInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, distributorInfo.resellerName);
        values.put(KEY_PHONE, distributorInfo.resellerPhone);
        values.put(KEY_PROVINCE, distributorInfo.resellerProvince);
        values.put(KEY_CITY, distributorInfo.resellerCity);
        values.put(KEY_ADDRESS, distributorInfo.resellerAddress);
        values.put(KEY_LONGITUDE, distributorInfo.longitude);
        values.put(KEY_LATITUDE, distributorInfo.latitude);
        values.put(KEY_PROVINCE_ID, distributorInfo.provinceId);

        db.insert(TABLE_DIS_INFO, null, values);
        //db.close();
    }

    public void resetDisInfoTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("drop table if exists " + TABLE_DIS_INFO);
        onCreate(db);
    }

    public void delDisInfoFromDb(DistributorInfo distributorInfo) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = KEY_PHONE + "= ?";
        String[] selectionArgs = { distributorInfo.resellerPhone };

        db.delete(TABLE_DIS_INFO, selection, selectionArgs);

        //db.close();
    }

    public void updateDisInfoForDb (DistributorInfo distributorInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, distributorInfo.resellerName);
        values.put(KEY_PHONE, distributorInfo.resellerPhone);
        values.put(KEY_PROVINCE, distributorInfo.resellerProvince);
        values.put(KEY_CITY, distributorInfo.resellerCity);
        values.put(KEY_ADDRESS, distributorInfo.resellerAddress);
        values.put(KEY_LONGITUDE, distributorInfo.longitude);
        values.put(KEY_LATITUDE, distributorInfo.latitude);
        values.put(KEY_PROVINCE_ID, distributorInfo.provinceId);

        String selection = KEY_PHONE + "= ?";
        String[] selectionArgs = { distributorInfo.resellerPhone };

        db.update(TABLE_DIS_INFO, values, selection, selectionArgs);
    }

    public boolean isSomeDisInfoExist(String resellerPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = KEY_PHONE + "= ?";
        String[] selectionArgs = { resellerPhone };
        Cursor cursor = db.query(TABLE_DIS_INFO, COLUMNS, selection, selectionArgs,
                null, null, null, null);

        if (cursor != null && cursor.getCount() > 0)
            return true;

        return false;
    }

    public DistributorInfo getDisInfoByPhone(String resellerPhone) {
        DistributorInfo disInfo = new DistributorInfo();
        SQLiteDatabase db = getReadableDatabase();
        String selection = KEY_PHONE + "= ?";
        String[] selectionArgs = { resellerPhone };
        Cursor cursor = db.query(TABLE_DIS_INFO, COLUMNS, selection, selectionArgs,
                null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            disInfo.resellerName = cursor.getString(0);
            disInfo.resellerPhone = cursor.getString(1);
            disInfo.resellerProvince = cursor.getString(2);
            disInfo.resellerCity = cursor.getString(3);
            disInfo.resellerAddress = cursor.getString(4);
            disInfo.longitude = cursor.getString(5);
            disInfo.latitude = cursor.getString(6);
            disInfo.provinceId = (int)cursor.getLong(7);
            return disInfo;
        }
        return null;
    }

    public int buildDisInfoList(List<DistributorInfo> list) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_DIS_INFO, COLUMNS, null, null,
                null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DistributorInfo disInfo = new DistributorInfo();
                disInfo.resellerName = cursor.getString(0);
                disInfo.resellerPhone = cursor.getString(1);
                disInfo.resellerProvince = cursor.getString(2);
                disInfo.resellerCity = cursor.getString(3);
                disInfo.resellerAddress = cursor.getString(4);
                disInfo.longitude = cursor.getString(5);
                disInfo.latitude = cursor.getString(6);
                disInfo.provinceId = (int)cursor.getLong(7);
                list.add(disInfo);
            } while (cursor.moveToNext());
        }

        return list.size();
    }

    public int buildSimpleDisInfoList(List<OverlayDemo.SimpleDisInfo> list) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_DIS_INFO, SIMPLE_COLUMNS, null, null,
                null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                OverlayDemo.SimpleDisInfo disInfo = new OverlayDemo.SimpleDisInfo();
                disInfo.name = cursor.getString(0);
                disInfo.phoneNumber = cursor.getString(1);
                disInfo.longitude = Double.parseDouble(cursor.getString(2));
                disInfo.latitude = Double.parseDouble(cursor.getString(3));
                disInfo.provinceId = (int)cursor.getLong(4);
                list.add(disInfo);
            } while (cursor.moveToNext());
        }

        return list.size();
    }

    public long itemCountOfDisInfo() {
       return DatabaseUtils.queryNumEntries(getWritableDatabase(), TABLE_DIS_INFO);
    }

}
