/**
 # COMP 4521    #  CHAN CHI HANG       20199170         chchanbq@connect.ust.hk
 # COMP 4521    #  KO CHING WAI          20199168         cwko@connect.ust.hk
 */

package hk.ust.alzheimerpassivemonitoring;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper{

    private static final String databaseName = "PassiveMonitoringRecord";
    private static final int version = 1;

    private static final String CREATE_TABLE_StepDistance =
            "CREATE TABLE StepDistance (Date BIGINT PRIMARY KEY, Step INTEGER, Distance REAL);";
    private static final String CREATE_TABLE_LocationRecord =
            "CREATE TABLE LocationRecord (RecordTime BIGINT PRIMARY KEY, Latitude REAL, Longitude REAL);";
    private static final String CREATE_TABLE_PhoneUsage =
            "CREATE TABLE PhoneUsage (EventID INTEGER PRIMARY KEY AUTOINCREMENT, Activity VARCHAR(40), StartTime BIGINT, EndTime BIGINT);";
    private static final String CREATE_TABLE_SleepWakeCycle =
            "CREATE TABLE SleepWakeCycle (StartTime BIGINT PRIMARY KEY, EndTime BIGINT, SleepStage VARCHAR(5));";
    private static final String CREATE_TABLE_HeartRate =
            "CREATE TABLE HeartRate (RecordTime BIGINT PRIMARY KEY, HeartRate INTEGER);";

    private static final String DROP_TABLE_StepDistance= "DROP TABLE StepDistance;";
    private static final String DROP_TABLE_LocationRecord = "DROP TABLE LocationRecord;";
    private static final String DROP_TABLE_PhoneUsage = "DROP TABLE PhoneUsage;";
    private static final String DROP_TABLE_SleepWakeCycle = "DROP TABLE SleepWakeCycle;";
    private static final String DROP_TABLE_HeartRate = "DROP TABLE HeartRate;";

    public SQLiteHelper(Context context) {
        super(context, databaseName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_StepDistance);
        sqLiteDatabase.execSQL(CREATE_TABLE_LocationRecord);
        sqLiteDatabase.execSQL(CREATE_TABLE_PhoneUsage);
        sqLiteDatabase.execSQL(CREATE_TABLE_SleepWakeCycle);
        sqLiteDatabase.execSQL(CREATE_TABLE_HeartRate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_TABLE_StepDistance);
        sqLiteDatabase.execSQL(DROP_TABLE_LocationRecord);
        sqLiteDatabase.execSQL(DROP_TABLE_PhoneUsage);
        sqLiteDatabase.execSQL(DROP_TABLE_SleepWakeCycle);
        sqLiteDatabase.execSQL(DROP_TABLE_HeartRate);
        onCreate(sqLiteDatabase);
    }

}
