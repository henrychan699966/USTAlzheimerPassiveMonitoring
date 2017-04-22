package hk.ust.alzheimerpassivemonitoring;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.SystemClock;

import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by henry on 2017-04-18.
 */

public class PassiveMonService extends Service {

    private Notification notification;
    private int ONGOING_NOTIFICATION_ID;

    @Override
    public void onCreate() {

        notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Passive Monitoring")
                .setContentText("In Progress")
                .setSmallIcon(R.drawable.notification_icon)
                .build();
        //an arbitrary id
        ONGOING_NOTIFICATION_ID = (int)System.currentTimeMillis();
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(ONGOING_NOTIFICATION_ID,notification);

        SQLiteCRUD database = new SQLiteCRUD(this);
        database.openDatabase();

        List<PhoneUsage> appUsage = getAppUsage(120);
        for(PhoneUsage pu : appUsage){
            database.createPhoneUsage(pu);
        }

        database.closeDatabase();


        Intent alarm = new Intent(getApplicationContext(),this.getClass());
        setAlarm(alarm,120);

        try{Thread.sleep(5000);}catch (InterruptedException e){}
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
    private List<PhoneUsage> getAppUsage(int s){
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(System.currentTimeMillis()- TimeUnit.SECONDS.toMillis(s),System.currentTimeMillis());

        PackageManager packageManager = getApplicationContext().getPackageManager();

        List<UsageEvents.Event> events= new ArrayList<>();
        while(usageEvents.hasNextEvent()){
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            if(event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND || event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND){
                events.add(event);
            }
        }

        List<PhoneUsage> appUsage = new ArrayList<>();
        for(int i = 0; i < events.size();i++){
            if(events.get(i).getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) continue;

            String appName = null;
            long endTime = 0;

            try{
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(events.get(i).getPackageName(),0));
            }
            catch(PackageManager.NameNotFoundException e){
                appName = events.get(i).getPackageName();
            }

            //find corresponding move_to_background time
            for(int j = i+1; j < events.size();j++){
                if(!events.get(i).getPackageName().equals(events.get(j).getPackageName()))break;
                if(!(events.get(j).getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND))break;
                endTime = events.get(j).getTimeStamp();
                break;
            }

            if(endTime != 0){
                PhoneUsage au = new PhoneUsage(appName,events.get(i).getTimeStamp(),endTime);
                appUsage.add(au);
                Log.e("getAppUsage",au.getActivity() + " " + Long.toString(au.getStartTime()) + " " + Long.toString(au.getEndTime()));
            }
        }

        return appUsage;
    }

    //set an alarm which triggered after s seconds
    private void setAlarm(Intent intent, int s){
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(this,0,intent,0);
        am.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + s*1000,alarmIntent);
    }
}
