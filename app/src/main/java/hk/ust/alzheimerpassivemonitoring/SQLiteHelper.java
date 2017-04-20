package hk.ust.alzheimerpassivemonitoring;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by henry on 2017-04-20.
 */

public class SQLiteHelper extends SQLiteOpenHelper{

    private static final String CREATE_TABLE_StepDistance =
            "CREATE TABLE StepDistance (Date CHARACTER(8) PRIMARY KEY, Step INTEGER, Distance REAL);";
    private static final String CREATE_TABLE_LocationRecord =
            "CREATE TABLE LocationRecord (RecordTime BIGINT PRIMARY KEY, Latitude REAL, Longitude REAL);";
    private static final String CREATE_TABLE_PhoneUsage =
            "CREATE TABLE PhoneUsage (EventID INTEGER PRIMARY KEY, Activity VARCHAR(20), StartTime BIGINT, EndTime BIGINT);";
    private static final String CREATE_TABLE_SleepWakeCycle =
            "CREATE TABLE SleepWakeCycle (StartTime BIGINT PRIMARY KEY, EndTime BIGINT, SleepStage VARCHAR(5));";

    private static final String DROP_TABLE_StepDistance= "DROP TABLE StepDistance;";
    private static final String DROP_TABLE_LocationRecord = "DROP TABLE LocationRecord;";
    private static final String DROP_TABLE_PhoneUsage = "DROP TABLE PhoneUsage;";
    private static final String DROP_TABLE_SleepWakeCycle = "DROP TABLE SleepWakeCycle;";

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_StepDistance);
        sqLiteDatabase.execSQL(CREATE_TABLE_LocationRecord);
        sqLiteDatabase.execSQL(CREATE_TABLE_PhoneUsage);
        sqLiteDatabase.execSQL(CREATE_TABLE_SleepWakeCycle);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_TABLE_StepDistance);
        sqLiteDatabase.execSQL(DROP_TABLE_LocationRecord);
        sqLiteDatabase.execSQL(DROP_TABLE_PhoneUsage);
        sqLiteDatabase.execSQL(DROP_TABLE_SleepWakeCycle);
        onCreate(sqLiteDatabase);
    }
}
