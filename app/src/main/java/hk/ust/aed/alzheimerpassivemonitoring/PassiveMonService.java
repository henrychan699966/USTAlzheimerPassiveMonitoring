/**
 # COMP 4521    #  CHAN CHI HANG       20199170         chchanbq@connect.ust.hk
 # COMP 4521    #  KO CHING WAI          20199168         cwko@connect.ust.hk
 */

package hk.ust.aed.alzheimerpassivemonitoring;

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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;


import android.support.annotation.Nullable;

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PassiveMonService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private boolean intentToGetLocation;
    private boolean intentToDoDailyTask;

    private static final int LOCATION_RETRIEVE_INTERVAL = 60; //seconds
    private static final int DAILY_TASK_INTERVAL = 120; //seconds
    private static final String LAST_DAILY_TASK_TIME = "LastDailyTaskTime"; // Last time to do daily task
    private static final String LAST_LOCATION_TIME = "LastLocationTime";

    private static final String LAST_STEP_DISTANCE = "LastStepDistanceRetrieved";
    private static final String LAST_SLEEP = "LastSleepWakeCycleRetrieved";

    private Notification notification;
    private int ONGOING_NOTIFICATION_ID;
    private Context context;

    private static final String[] callLogItem = {CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION};

    @Override
    public void onCreate() {
        Log.e("Service","onCreate");
        context = this;

        intentToDoDailyTask = false;
        intentToGetLocation =true;

        notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Passive Monitoring")
                .setContentText("In Progress")
                .setSmallIcon(R.drawable.notification_icon)
                .build();
        //an arbitrary id
        ONGOING_NOTIFICATION_ID = (int) System.currentTimeMillis();

        scheduler();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        Log.e("Service","onStartCommand");

        if(intentToDoDailyTask){
            dailyTask();
        }

        Intent alarm = new Intent(getApplicationContext(), this.getClass());
        setAlarm(alarm, LOCATION_RETRIEVE_INTERVAL);

        try {
                Thread.sleep(5000);

        } catch (InterruptedException e) {
        }

        if(checkPlayServices() && intentToGetLocation){
            buildGoogleApiClient();
        }
        else{
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if(googleApiClient != null)googleApiClient.disconnect();

        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void scheduler(){
        if(!existSharedPref(LAST_DAILY_TASK_TIME)){
            intentToDoDailyTask = true;
            return;
        }
        if(System.currentTimeMillis() - Long.parseLong(readSharedPref(LAST_DAILY_TASK_TIME)) > DAILY_TASK_INTERVAL){
            intentToDoDailyTask= true;
        }
        else{
            intentToDoDailyTask = false;
        }
    }

    private boolean existSharedPref(String key){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.contains(key);
    }

    private void writeSharedPref(String key,String value){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key,value);
        editor.commit();
    }

    private String readSharedPref(String key){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getString(key,"");
    }

    private void dailyTask(){
        int time = DAILY_TASK_INTERVAL;

        SQLiteCRUD database = new SQLiteCRUD(this);
        database.openDatabase();
        List<PhoneUsage> appUsage = getAppUsage(time);
        if (!appUsage.isEmpty()) {
            for (PhoneUsage pu : appUsage) {
                database.createPhoneUsage(pu);
            }
        }

        List<PhoneUsage> callHistory = getCallHistory(time);
        if (callHistory != null) {
            for (PhoneUsage pu : callHistory) {
                database.createPhoneUsage(pu);
            }
        }
        database.closeDatabase();


        getStepDistance();
        getSleepWakeCycle();

        extractDataToJson();

        writeSharedPref(LAST_DAILY_TASK_TIME,Long.toString(System.currentTimeMillis()));
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


    //get call log s seconds before current time
    private List<PhoneUsage> getCallHistory(int s) {
        List<PhoneUsage> records = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, callLogItem, CallLog.Calls.DATE + ">=" + (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(s)), null, null);

        Log.e("getCallHistory", Integer.toString(cursor.getCount()));
        if (cursor.getCount() > 0) {
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


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("getLocation","onConnected");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("getLocation","Permission Fail");
                return;
            } else {Log.e("getLocation","Permission OK");}
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,this);

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {

            //No need to insert record when the last known location did not change
            if (existSharedPref(LAST_LOCATION_TIME)){
                if(location.getTime() <= Long.parseLong(readSharedPref(LAST_LOCATION_TIME))){
                    Log.e("GetLocation","Repeated");
                    stopSelf();
                    return;
                }
            }


            SQLiteCRUD database = new SQLiteCRUD(this);
            database.openDatabase();
            LocationRecord locationRecord = new LocationRecord(location.getTime(),(float)location.getLatitude(),(float)location.getLongitude());
            database.createLocationRecord(locationRecord);
            Log.e("getLocation", locationRecord.getRecordTime() + " " + locationRecord.getLatitude() + " " + locationRecord.getLongitude());

            //Update SharedPref
            writeSharedPref(LAST_LOCATION_TIME,Long.toString(location.getTime()));
            database.closeDatabase();
        }
        else{
            Log.e("getLocation","Location NULL");
        }

        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("getLocation","onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("getLocation","onConnectionFailed");
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                Log.e("PlayService", "Fail");
            }
            return false;
        }
        Log.e("PlayService", "OK");
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        Log.e("GoogleApi","Try Connect");
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private void getStepDistance(){
        if(!existSharedPref("fitbitToken")) return;
        String token = readSharedPref("fitbitToken");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        calendar.add(Calendar.DATE,-1);      //Yesterday

        if(existSharedPref(LAST_STEP_DISTANCE)){
            Date lastRetrieved = null;
            try {
                lastRetrieved = dateFormat.parse(readSharedPref(LAST_STEP_DISTANCE));
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }


            if(!dateFormat.format(calendar.getTime()).equals(dateFormat.format(lastRetrieved))){
                new FitbitStepDistance(this).execute(dateFormat.format(calendar.getTime()),token);
                writeSharedPref(LAST_STEP_DISTANCE,dateFormat.format(calendar.getTime()));
            }
            else{
                Log.i("StepDistance","Repeated");
                return;
            }

        }else{
            //First time of running this Service
            new FitbitStepDistance(this).execute(dateFormat.format(calendar.getTime()),token);
            writeSharedPref(LAST_STEP_DISTANCE,dateFormat.format(calendar.getTime()));
        }
    }

    private class FitbitStepDistance extends AsyncTask<String,Void,StepDistance> {
        private Context mContext;

        public FitbitStepDistance (Context context){
            mContext = context;
        }

        @Override
        protected StepDistance doInBackground(String... strings) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://api.fitbit.com/1/user/-/activities/date/" + strings[0] +".json")
                    .header("Authorization","Bearer "+ strings[1].trim())
                    .build();

            Response response =null;
            try{
                response = client.newCall(request).execute();
            }
            catch (IOException e){

            }

            String result = "";

            JsonParser jp = new JsonParser();
            JsonObject jo;

            try {
                jo = (JsonObject) jp.parse(response.body().string());
            } catch (IOException e) {
                jo = new JsonObject();
            }

            result += jo.getAsJsonObject("summary").get("steps").toString() + ",";
            for(JsonElement je :jo.getAsJsonObject("summary").getAsJsonArray("distances")){
                if(je.getAsJsonObject().get("activity").toString().equals("\"total\"")){
                    result += je.getAsJsonObject().get("distance").toString();
                }
            }

            StepDistance sd = new StepDistance(dateToEpoch(strings[0].replaceAll("-","")) + TimeUnit.HOURS.toMillis(3),Integer.parseInt(result.substring(0,result.indexOf(","))),Float.parseFloat(result.substring(result.indexOf(",")+1)));

            return sd;
        }

        @Override
        protected void onPostExecute(StepDistance sd) {

            SQLiteCRUD database = new SQLiteCRUD(mContext);
            database.openDatabase();
            database.createStepDistance(sd);
            Log.e("record",Float.toString(sd.getDistance()) + Integer.toString(sd.getStep()));
            database.closeDatabase();

            super.onPostExecute(sd);
        }


    }

    private void getSleepWakeCycle(){
        if(!existSharedPref("fitbitToken")) return;
        String token = readSharedPref("fitbitToken");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        calendar.add(Calendar.DATE,-1);      //Yesterday

        if(existSharedPref(LAST_SLEEP)){
            Date lastRetrieved = null;
            try {
                lastRetrieved = dateFormat.parse(readSharedPref(LAST_SLEEP));
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }

            if(!dateFormat.format(calendar.getTime()).equals(dateFormat.format(lastRetrieved))){
                new FitbitSleepWakeCycle(this).execute(dateFormat.format(calendar.getTime()),token);
                writeSharedPref(LAST_SLEEP,dateFormat.format(calendar.getTime()));
            }
            else{
                Log.i("Sleep","Repeated");
                return;
            }

        }else{
            //First time of running this Service
            new FitbitSleepWakeCycle(this).execute(dateFormat.format(calendar.getTime()),token);
            writeSharedPref(LAST_SLEEP,dateFormat.format(calendar.getTime()));
            Log.i("Sleep","No Share Pref");
        }
    }

    private class FitbitSleepWakeCycle extends AsyncTask<String,Void,List<SleepWakeCycle>> {
        private Context mContext;

        public FitbitSleepWakeCycle(Context context) {
            mContext = context;
        }

        @Override
        protected List<SleepWakeCycle> doInBackground(String... strings) {
            List<SleepWakeCycle> sleepWakeCycles = new ArrayList<>();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://api.fitbit.com/1.2/user/-/sleep/date/" + strings[0] +".json")
                    .header("Authorization","Bearer "+ strings[1].trim())
                    .build();

            Response response =null;
            try{
                response = client.newCall(request).execute();
            }
            catch (IOException e){

            }

            JsonParser jp = new JsonParser();
            JsonObject jo;

            try {
                jo = (JsonObject) jp.parse(response.body().string());
            } catch (IOException e) {
                jo = new JsonObject();
            }

            for(JsonElement je :jo.getAsJsonArray("sleep")) {
                for (JsonElement je2 : je.getAsJsonObject().getAsJsonObject("levels").getAsJsonArray("data")) {
                    long date = 0;
                    try {
                        date = df.parse(je2.getAsJsonObject().getAsJsonPrimitive("dateTime").getAsString()).getTime();
                        Log.e("Sleep",Long.toString(date));
                    } catch (ParseException e) {
                        Log.e("FitbitSleepWakeCycle", "Parse Data fail");
                    }
                    sleepWakeCycles.add(new SleepWakeCycle(date, date + je2.getAsJsonObject().getAsJsonPrimitive("seconds").getAsInt()*1000, je2.getAsJsonObject().getAsJsonPrimitive("level").getAsString()));
                }
            }
            return sleepWakeCycles;
        }

        @Override
        protected void onPostExecute(List<SleepWakeCycle> sleepWakeCycles) {

            SQLiteCRUD database = new SQLiteCRUD(mContext);
            database.openDatabase();
            for(SleepWakeCycle swc: sleepWakeCycles){
                database.createSleepWakeCycle(swc);
            }
            database.closeDatabase();

            super.onPostExecute(sleepWakeCycles);
        }
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

    //read ID from sharedPreference and extract data after this ID
    private void extractDataToJson(){
        String[] tables = {"PhoneUsage","StepDistance","LocationRecord","SleepWakeCycle","HeartRate"};
        int[] idStartFrom = {1,1,1,1,1}; // default starting point of extraction of each table

        SQLiteHelper dbHelper = new SQLiteHelper(context);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor[] cursors = new Cursor[tables.length];

        List<PhoneUsage> phoneUsages = new ArrayList<>();
        List<StepDistance> stepDistances = new ArrayList<>();
        List<LocationRecord> locationRecords = new ArrayList<>();
        List<SleepWakeCycle> sleepWakeCycles = new ArrayList<>();
        List<HeartRate> heartRates = new ArrayList<>();

        for(int i = 0;i < tables.length;i++){
            if(existSharedPref(tables[i])){
                idStartFrom[i] = Integer.parseInt(readSharedPref(tables[i]));
            }
        }

        for(int i = 0;i < tables.length;i++){
            cursors[i] = database.query(tables[i],null,"ID > " + Integer.toString(idStartFrom[i]), null, null, null, "ID ASC");

            Log.e("TestJson",i + " " +  Boolean.toString(cursors[i] == null)+ " " );
            if(cursors[i] == null) continue;
            if(cursors[i].getCount() <= 0) continue;
            cursors[i].moveToFirst();
        }

        if(cursors[0].getCount() > 0){
            while (!cursors[0].isAfterLast()) {
                PhoneUsage pu = new PhoneUsage(
                        cursors[0].getInt(cursors[0].getColumnIndex("ID")),
                        cursors[0].getString(cursors[0].getColumnIndex("Activity")),
                        cursors[0].getLong(cursors[0].getColumnIndex("StartTime")),
                        cursors[0].getLong(cursors[0].getColumnIndex("EndTime")));
                phoneUsages.add(pu);
                cursors[0].moveToNext();
            }
        }

        if(cursors[1].getCount() > 0){
            while (!cursors[1].isAfterLast()) {
                StepDistance sd = new StepDistance(
                        cursors[1].getInt(cursors[1].getColumnIndex("ID")),
                        cursors[1].getLong(cursors[1].getColumnIndex("Date")),
                        cursors[1].getInt(cursors[1].getColumnIndex("Step")),
                        cursors[1].getFloat(cursors[1].getColumnIndex("Distance")));
                stepDistances.add(sd);
                cursors[1].moveToNext();
            }
        }

        if(cursors[2].getCount() > 0){
            while (!cursors[2].isAfterLast()) {
                LocationRecord lr = new LocationRecord(
                        cursors[2].getInt(cursors[2].getColumnIndex("ID")),
                        cursors[2].getLong(cursors[2].getColumnIndex("RecordTime")),
                        cursors[2].getFloat(cursors[2].getColumnIndex("Latitude")),
                        cursors[2].getFloat(cursors[2].getColumnIndex("Longitude")));
                locationRecords.add(lr);
                cursors[2].moveToNext();
            }
        }

        if(cursors[3].getCount() > 0){
            while (!cursors[3].isAfterLast()) {
                SleepWakeCycle swc = new SleepWakeCycle(
                        cursors[3].getInt(cursors[3].getColumnIndex("ID")),
                        cursors[3].getLong(cursors[3].getColumnIndex("StartTime")),
                        cursors[3].getLong(cursors[3].getColumnIndex("EndTime")),
                        cursors[3].getString(cursors[3].getColumnIndex("SleepStage")));
                sleepWakeCycles.add(swc);
                cursors[3].moveToNext();
            }
        }


        if(cursors[4].getCount() > 0){
            while (!cursors[4].isAfterLast()) {
                HeartRate hr = new HeartRate(
                        cursors[4].getInt(cursors[4].getColumnIndex("ID")),
                        cursors[4].getLong(cursors[4].getColumnIndex("RecordTime")),
                        cursors[4].getInt(cursors[4].getColumnIndex("HeartRate")));
                heartRates.add(hr);
                cursors[4].moveToNext();
            }
        }

        for(int i = 0; i < cursors.length;i++){
            if(cursors[i].getCount()>0){
                cursors[i].moveToLast();
                writeSharedPref(tables[i],Integer.toString(cursors[i].getInt(cursors[i].getColumnIndex("ID")) +1));
                cursors[i].close();
            }
        }


        String[] data = new String[tables.length];
        Type type;

        type= new TypeToken<List<PhoneUsage>>() {}.getType();
        data[0] = new Gson().toJson(phoneUsages, type);

        type = new TypeToken<List<StepDistance>>() {}.getType();
        data[1] = new Gson().toJson(stepDistances, type);

        type = new TypeToken<List<LocationRecord>>() {}.getType();
        data[2] = new Gson().toJson(locationRecords, type);

        type = new TypeToken<List<SleepWakeCycle>>() {}.getType();
        data[3] = new Gson().toJson(sleepWakeCycles, type);

        type = new TypeToken<List<HeartRate>>() {}.getType();
        data[4] = new Gson().toJson(heartRates, type);

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = new JsonObject();

        for (int i = 0; i < data.length;i++){
            JsonArray jsonArray = null;
            try{
                jsonArray = (JsonArray) jsonParser.parse(data[i]);
            }
            catch(RuntimeException e){
                jsonArray = new JsonArray();
            }
            jsonObject.add(tables[i],jsonArray);
        }

        Log.e("TestJson",new Gson().toJson(jsonObject));

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        try {
            File filesDir = getFilesDir();
            File outFile = new File(filesDir, dateFormat.format(new Date(System.currentTimeMillis())) + ".json");

            OutputStream os = new FileOutputStream(outFile.getAbsolutePath());

            os.write(new Gson().toJson(jsonObject).toString().getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File filesDir = getFilesDir();
        File outFile = new File(filesDir, dateFormat.format(new Date(System.currentTimeMillis())) + ".json");

        Log.e("TestJson",Long.toString(outFile.length()));
        database.close();
    }
}
