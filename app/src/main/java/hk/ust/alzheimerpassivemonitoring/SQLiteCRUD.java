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
        if(cursor != null){
            cursor.moveToFirst();
        }
        else return null;

        StepDistance sd = new StepDistance(cursor.getString(0),cursor.getInt(1),cursor.getFloat(2));
        cursor.close();
        return sd;
    }

    public List<StepDistance> readAllStepDistance(){
        Cursor cursor = db.query("StepDistance",null,null, null, null, null, null);
        if(cursor != null){
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
}
