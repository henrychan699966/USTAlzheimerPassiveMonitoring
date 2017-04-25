package hk.ust.alzheimerpassivemonitoring;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;

import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by henry on 2017-04-18.
 */

public class PassiveMonService extends Service {

    private Notification notification;
    private int ONGOING_NOTIFICATION_ID;
    private Context context;

    CursorLoader cursorLoader;
    private static final String[] callLogItem = {CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION};

    @Override
    public void onCreate() {

        notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Passive Monitoring")
                .setContentText("In Progress")
                .setSmallIcon(R.drawable.notification_icon)
                .build();
        //an arbitrary id
        ONGOING_NOTIFICATION_ID = (int) System.currentTimeMillis();
        context = this;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(ONGOING_NOTIFICATION_ID, notification);


        SQLiteCRUD database = new SQLiteCRUD(this);
        database.openDatabase();

        List<PhoneUsage> appUsage = getAppUsage(120);
        if(!appUsage.isEmpty()){
            for (PhoneUsage pu : appUsage) {
                database.createPhoneUsage(pu);
            }
        }


        List<PhoneUsage> callHistory = getCallHistory(120);
        if(callHistory != null){
            for (PhoneUsage pu : callHistory) {
                database.createPhoneUsage(pu);
            }
        }


        database.closeDatabase();

        getCallHistory(120);

        FileOutputStream test = null;
        try {
            test = openFileOutput("test", Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
        }

        try {
            test.write(extractAllData(context).getBytes());
        } catch (IOException e) {
        }


        Intent alarm = new Intent(getApplicationContext(), this.getClass());
        setAlarm(alarm, 120);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //get app usage from s seconds ago to now
    private List<PhoneUsage> getAppUsage(int s) {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(s), System.currentTimeMillis());

        PackageManager packageManager = getApplicationContext().getPackageManager();

        List<UsageEvents.Event> events = new ArrayList<>();
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND || event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                events.add(event);
            }
        }

        List<PhoneUsage> appUsage = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) continue;

            String appName = null;
            long endTime = 0;

            try {
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(events.get(i).getPackageName(), 0));
            } catch (PackageManager.NameNotFoundException e) {
                appName = events.get(i).getPackageName();
            }

            //find corresponding move_to_background time
            for (int j = i + 1; j < events.size(); j++) {
                if (!events.get(i).getPackageName().equals(events.get(j).getPackageName())) break;
                if (!(events.get(j).getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND)) break;
                endTime = events.get(j).getTimeStamp();
                break;
            }

            if (endTime != 0) {
                PhoneUsage au = new PhoneUsage(appName, events.get(i).getTimeStamp(), endTime);
                appUsage.add(au);
                Log.e("getAppUsage", au.getActivity() + " " + Long.toString(au.getStartTime()) + " " + Long.toString(au.getEndTime()));
            }
        }

        return appUsage;
    }

    //set an alarm which triggered after s seconds
    private void setAlarm(Intent intent, int s) {
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(this, 0, intent, 0);
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + s * 1000, alarmIntent);
    }

    private String extractAllData(Context context) {
        String[] s = new String[4];

        SQLiteCRUD database = new SQLiteCRUD(context);
        database.openDatabase();

        Type type = new TypeToken<List<PhoneUsage>>() {
        }.getType();
        s[0] = new Gson().toJson(database.readAllPhoneUsage(0), type);

        type = new TypeToken<List<StepDistance>>() {
        }.getType();
        s[1] = new Gson().toJson(database.readAllStepDistance(), type);

        type = new TypeToken<List<LocationRecord>>() {
        }.getType();
        s[2] = new Gson().toJson(database.readAllLocationRecord(0), type);

        type = new TypeToken<List<SleepWakeCycle>>() {
        }.getType();
        s[3] = new Gson().toJson(database.readAllSleepWakeCycle(0), type);
        database.closeDatabase();

        JsonParser jp = new JsonParser();
        JsonArray[] ja = new JsonArray[4];
        for (int i = 0; i < 4; i++) {
            try {
                ja[i] = (JsonArray) jp.parse(s[i]);
            } catch (RuntimeException e) {
                ja[i] = new JsonArray();
            }
        }


        JsonObject data = new JsonObject();
        data.add("PhoneUsage", ja[0]);
        data.add("StepDistance", ja[1]);
        data.add("LocationRecord", ja[2]);
        data.add("SleepWakeCycle", ja[3]);

        JsonObject output = new JsonObject();
        output.add("data", data);
        output.addProperty("PhoneNum", "12345678");

        return new Gson().toJson(output);
    }

    //get call log s seconds before current time
    private List<PhoneUsage> getCallHistory(int s) {
        List<PhoneUsage> records = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, callLogItem,CallLog.Calls.DATE + ">=" + (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(s)), null, null);

        Log.e("getCallHistory",Integer.toString(cursor.getCount()));
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                PhoneUsage pu;
                switch (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))) {
                    case 1:
                        pu = new PhoneUsage("PhoneCall IN", cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)), cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)) + cursor.getColumnIndex(CallLog.Calls.DURATION) * 1000);
                        records.add(pu);
                        Log.e("getCallHistory", pu.getActivity() + " " + Long.toString(pu.getStartTime()) + " " + Long.toString(pu.getEndTime()));
                        break;
                    case 2:
                        pu = new PhoneUsage("PhoneCall OUT", cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)), cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)) + cursor.getColumnIndex(CallLog.Calls.DURATION) * 1000);
                        records.add(pu);
                        Log.e("getCallHistory", pu.getActivity() + " " + Long.toString(pu.getStartTime()) + " " + Long.toString(pu.getEndTime()));
                        break;
                }
                cursor.moveToNext();

            }
            cursor.close();
            return records;
        }
        cursor.close();
        return null;
    }

}
