/**
 # COMP 4521    #  CHAN CHI HANG       20199170         chchanbq@connect.ust.hk
 # COMP 4521    #  KO CHING WAI          20199168         cwko@connect.ust.hk
 */

package hk.ust.aed.alzheimerpassivemonitoring;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

//CRUD = create , read, update, delete
public class SQLiteCRUD {
    private SQLiteDatabase db= null;
    private SQLiteHelper helper = null;

    public SQLiteCRUD(Context context){
        helper = new SQLiteHelper(context);
    }

    public boolean openDatabase(){
        db = helper.getWritableDatabase();
        if(db == null) return false;
        return true;
    }



    public void closeDatabase(){
        helper.close();
    }

    //return true when successful
    public boolean createStepDistance(StepDistance sd){

        ContentValues record = new ContentValues();
        record.put("Date",sd.getDate());
        record.put("Step",sd.getStep());
        record.put("Distance",sd.getDistance());

        long insertID = db.insert("StepDistance",null,record);
        if(insertID == -1) return false;
        return true;
    }

    public List<StepDistance> readStepDistance(String date){

        Cursor cursor = db.query("StepDistance",null,"Date > " + dateToEpoch(date) + " and Date < " + (dateToEpoch(date)+ TimeUnit.DAYS.toMillis(1)), null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<StepDistance> records = new ArrayList<>();
        while(!cursor.isAfterLast()){
            StepDistance sd = new StepDistance(cursor.getLong(0),cursor.getInt(1),cursor.getFloat(2));
            records.add(sd);
            cursor.moveToNext();
        }

        cursor.close();
        return records;
    }

    public List<StepDistance> readAllStepDistance(long date){
        Cursor cursor = db.query("StepDistance",null,"Date > " + date, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<StepDistance> records = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            StepDistance sd = new StepDistance(cursor.getLong(0),cursor.getInt(1),cursor.getFloat(2));
            records.add(sd);
            cursor.moveToNext();
        }

        cursor.close();
        return records;
    }

    //return true when successful
    public boolean createLocationRecord(LocationRecord lr){

        ContentValues record = new ContentValues();
        record.put("RecordTime",lr.getRecordTime());
        record.put("Latitude",lr.getLatitude());
        record.put("Longitude",lr.getLongitude());

        long insertID = db.insert("LocationRecord",null,record);
        if(insertID == -1) return false;
        return true;
    }

    //YYYYMMDD
    public LocationRecord readLocationRecord(String date){

        Cursor cursor = db.query("LocationRecord",null,"RecordTime > " + dateToEpoch(date) + " and RecordTime < " + (dateToEpoch(date)+ TimeUnit.DAYS.toMillis(1)), null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        LocationRecord lr = new LocationRecord(cursor.getLong(0),cursor.getFloat(1),cursor.getFloat(2));
        cursor.close();
        return lr;
    }

    //read all records since recordTime, pass 0 for all records without constraints
    public List<LocationRecord> readAllLocationRecord(long recordTime){
        Cursor cursor = db.query("LocationRecord",null,"RecordTime > " + recordTime, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<LocationRecord> records = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            LocationRecord lr = new LocationRecord(cursor.getLong(0),cursor.getFloat(1),cursor.getFloat(2));
            records.add(lr);
            cursor.moveToNext();
        }

        cursor.close();
        return records;
    }

    //return true when successful
    public boolean createPhoneUsage(PhoneUsage pu){

        ContentValues record = new ContentValues();

        record.put("Activity",pu.getActivity());
        record.put("StartTime",pu.getStartTime());
        record.put("EndTime",pu.getEndTime());

        long insertID = db.insert("PhoneUsage",null,record);
        if(insertID == -1) return false;
        return true;
    }

    public List<PhoneUsage> readPhoneUsage(String date){

        Cursor cursor = db.query("PhoneUsage",null,"StartTime > " + dateToEpoch(date) + " and StartTime < "+ (dateToEpoch(date)+ TimeUnit.DAYS.toMillis(1)), null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<PhoneUsage> records = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            PhoneUsage pu = new PhoneUsage(cursor.getInt(0),cursor.getString(1),cursor.getLong(2),cursor.getLong(3));
            records.add(pu);
            cursor.moveToNext();
        }
        cursor.close();
        return records;
    }

    //read all records since startTime, pass 0 for all records without constraints
    public List<PhoneUsage> readAllPhoneUsage(long startTime){
        Cursor cursor = db.query("PhoneUsage",null,"StartTime > " + startTime, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<PhoneUsage> records = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            PhoneUsage pu = new PhoneUsage(cursor.getInt(0),cursor.getString(1),cursor.getLong(2),cursor.getLong(3));
            records.add(pu);
            cursor.moveToNext();
        }

        cursor.close();
        return records;
    }

    //return true when successful
    public boolean createSleepWakeCycle(SleepWakeCycle swc){

        ContentValues record = new ContentValues();
        record.put("StartTime",swc.getStartTime());
        record.put("EndTime",swc.getEndTime());
        record.put("SleepStage",swc.getSleepStage());

        long insertID = db.insert("SleepWakeCycle",null,record);
        if(insertID == -1) return false;
        return true;
    }

    public List<SleepWakeCycle> readSleepWakeCycle(String date){

        Cursor cursor = db.query("SleepWakeCycle",null,"StartTime > " + dateToEpoch(date) + " and StartTime < "+ (dateToEpoch(date)+ TimeUnit.DAYS.toMillis(1)), null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<SleepWakeCycle> records = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            SleepWakeCycle swc = new SleepWakeCycle(cursor.getLong(0),cursor.getLong(1),cursor.getString(2));
            records.add(swc);
            cursor.moveToNext();
        }
        cursor.close();
        return records;
    }

    //read all records since recordTime, pass 0 for all records without constraints
    public List<SleepWakeCycle> readAllSleepWakeCycle(long startTime){
        Cursor cursor = db.query("SleepWakeCycle",null,"StartTime > " + startTime, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<SleepWakeCycle> records = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            SleepWakeCycle swc = new SleepWakeCycle(cursor.getLong(0),cursor.getLong(1),cursor.getString(2));
            records.add(swc);
            cursor.moveToNext();
        }

        cursor.close();
        return records;
    }

    public boolean createHeartRate(HeartRate hr){
        ContentValues record = new ContentValues();
        record.put("RecordTime",hr.getRecordTime());
        record.put("HeartRate",hr.getHeartRate());

        long insertID = db.insert("HeartRate",null,record);
        if(insertID == -1) return false;
        return true;
    }

    public List<HeartRate> readHeartRate(String date){

        Cursor cursor = db.query("HeartRate",null,"RecordTime > " + dateToEpoch(date) + " and RecordTime < "+ (dateToEpoch(date)+ TimeUnit.DAYS.toMillis(1)), null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<HeartRate> records = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            HeartRate hr = new HeartRate(cursor.getLong(0), cursor.getInt(1));
            records.add(hr);
            cursor.moveToNext();
        }
        cursor.close();
        return records;
    }

    //read all records since recordTime, pass 0 for all records without constraints
    public List<HeartRate> readAllHeartRate(long startTime){
        Cursor cursor = db.query("HeartRate",null,"RecordTime > " + startTime, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<HeartRate> records = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            HeartRate hr = new HeartRate(cursor.getLong(0), cursor.getInt(1));
            records.add(hr);
            cursor.moveToNext();
        }

        cursor.close();
        return records;
    }

    public long dateToEpoch(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date d;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date: ", e);
        }

        return d.getTime();
    }


    public void achieveDatabase(String date){
        db.delete("StepDistance","Date < " + (dateToEpoch(date)+ TimeUnit.DAYS.toMillis(1)),null);
        db.delete("LocationRecord","RecordTime < " + (dateToEpoch(date)+ TimeUnit.DAYS.toMillis(1)),null);
        db.delete("PhoneUsage","StartTime < " + (dateToEpoch(date)+ TimeUnit.DAYS.toMillis(1)),null);
        db.delete("SleepWakeCycle","StartTime < " + (dateToEpoch(date)+ TimeUnit.DAYS.toMillis(1)),null);

    }
}
