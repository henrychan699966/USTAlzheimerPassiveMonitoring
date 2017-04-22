package hk.ust.alzheimerpassivemonitoring;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by henry on 2017-04-21.
 */
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

    public StepDistance readStepDistance(String date){

        Cursor cursor = db.query("StepDistance",null,"Date = " + date, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        StepDistance sd = new StepDistance(cursor.getString(0),cursor.getInt(1),cursor.getFloat(2));
        cursor.close();
        return sd;
    }

    public List<StepDistance> readAllStepDistance(){
        Cursor cursor = db.query("StepDistance",null,null, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        List<StepDistance> records = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            StepDistance sd = new StepDistance(cursor.getString(0),cursor.getInt(1),cursor.getFloat(2));
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

    public LocationRecord readLocationRecord(long recordTime){

        Cursor cursor = db.query("LocationRecord",null,"RecordTime = " + recordTime, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        LocationRecord lr = new LocationRecord(cursor.getLong(0),cursor.getFloat(1),cursor.getFloat(2));
        cursor.close();
        return lr;
    }

    //read all records since recordTime, pass 0 for all records without constraints
    public List<LocationRecord> readAllStepDistance(long recordTime){
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

    public PhoneUsage readPhoneUsage(int eventID){

        Cursor cursor = db.query("PhoneUsage",null,"EventID =" + eventID, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        PhoneUsage pu = new PhoneUsage(cursor.getInt(0),cursor.getString(1),cursor.getLong(2),cursor.getLong(3));
        cursor.close();
        return pu;
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

    public SleepWakeCycle readSleepWakeCycle(long startTime){

        Cursor cursor = db.query("SleepWakeCycle",null,"StartTime = " + startTime, null, null, null, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        }
        else return null;

        SleepWakeCycle swc = new SleepWakeCycle(cursor.getLong(0),cursor.getLong(1),cursor.getString(2));
        cursor.close();
        return swc;
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
}
