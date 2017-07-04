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
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.CallLog;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private static final int LOCATION_RETRIEVE_INTERVAL = 1800; //seconds
    private static final int DAILY_TASK_INTERVAL = 3600; //seconds
    private static final String LAST_UPLOAD_TIME = "LastUploadTime";
    private static final String LAST_LOCATION_TIME = "LastLocationTime";

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
        if(!existSharedPref(LAST_UPLOAD_TIME)){
            intentToDoDailyTask = true;
            return;
        }
        if(System.currentTimeMillis() - Long.parseLong(readSharedPref(LAST_UPLOAD_TIME)) > DAILY_TASK_INTERVAL){
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



        new UploadFirebase().execute(extractDailyData(context));

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

    private Map<String,String> extractDailyData(Context context) {
        String[] s = new String[5];
        String[] tableName = {"PhoneUsage","StepDistance","LocationRecord","SleepWakeCycle","HeartRate"};

        SQLiteCRUD database = new SQLiteCRUD(context);
        database.openDatabase();

        Type type = new TypeToken<List<PhoneUsage>>() {
        }.getType();
        s[0] = new Gson().toJson(database.readAllPhoneUsage(0), type);

        type = new TypeToken<List<StepDistance>>() {
        }.getType();
        s[1] = new Gson().toJson(database.readAllStepDistance(0), type);

        type = new TypeToken<List<LocationRecord>>() {
        }.getType();
        s[2] = new Gson().toJson(database.readAllLocationRecord(0), type);

        type = new TypeToken<List<SleepWakeCycle>>() {
        }.getType();
        s[3] = new Gson().toJson(database.readAllSleepWakeCycle(0), type);

        type = new TypeToken<List<HeartRate>>() {
        }.getType();
        s[4] = new Gson().toJson(database.readAllHeartRate(0), type);
        database.closeDatabase();

        JsonParser jsonParser = new JsonParser();
        Map<String,String> data = new HashMap<>();

        for (int i = 0; i < s.length;i++){
            JsonArray jsonArray = null;
            try{
                jsonArray = (JsonArray) jsonParser.parse(s[i]);
            }
            catch(RuntimeException e){
                jsonArray = new JsonArray();
            }
            for(JsonElement jsonElement : jsonArray){
                jsonElement.getAsJsonObject().addProperty("UserID","123456789");
            }
             data.put(tableName[i],new Gson().toJson(jsonArray));
        }

        return data;
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
        //String date = dateFormat.format(new Date());
       // String date = "2017-05-15";
        String date2 = "2017-05-16";

//        new FitbitStepDistance(this).execute(date,token);
     //   try{Thread.sleep(5000);}catch (Exception e){}
        new FitbitStepDistance(this).execute(date2,token);
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
        //String date = dateFormat.format(new Date());
        String date = "2017-05-15";

        new FitbitSleepWakeCycle(this).execute(date,token);
    }

    private class FitbitSleepWakeCycle extends AsyncTask<String,Void,List<SleepWakeCycle>> {
        private Context mContext;

        public FitbitSleepWakeCycle(Context context) {
            mContext = context;
        }

        @Override
        protected List<SleepWakeCycle> doInBackground(String... strings) {

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

            List<SleepWakeCycle> sleepWakeCycles = new ArrayList<>();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");


            for(JsonElement je :jo.getAsJsonArray("sleep")){
                for(JsonElement je2 : je.getAsJsonObject().getAsJsonObject("levels").getAsJsonArray("data")){
                    long date = 0;
                    try{
                        date = df.parse(je2.getAsJsonObject().getAsJsonObject("datetime").getAsString()).getTime();
                    }
                    catch(ParseException e){

                    }
                    sleepWakeCycles.add(new SleepWakeCycle(date,date + je2.getAsJsonObject().getAsJsonObject("seconds").getAsInt(), je2.getAsJsonObject().getAsJsonObject("level").getAsString()));
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

    private class UploadFirebase extends AsyncTask<Map<String, String>,Void,Void>{
        public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        @Override
        protected Void doInBackground(Map<String,String>... data) {

            OkHttpClient client = new OkHttpClient();

            for(Map.Entry<String,String> entry : data[0].entrySet()){
                RequestBody body = RequestBody.create(JSON, entry.getValue());
                Request request = new Request.Builder()
                        .url("https://ad-passive.firebaseio.com/data/" + entry.getKey() + ".json")
                        .post(body)
                        .build();
                try{
                    client.newCall(request).execute();
                }
                catch (IOException e){

                }
            }


            return null;
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
}
