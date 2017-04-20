package hk.ust.alzheimerpassivemonitoring;

import android.app.Notification;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;

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
        getAppUsage();
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

    private List<List<String>> getAppUsage(){
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(System.currentTimeMillis()- TimeUnit.SECONDS.toMillis(120),System.currentTimeMillis());

        PackageManager packageManager = getApplicationContext().getPackageManager();

        List<UsageEvents.Event> events= new ArrayList<>();
        while(usageEvents.hasNextEvent()){
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            if(event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND || event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND){
                events.add(event);
            }
        }

        List<List<String>> appUsage = new ArrayList<>();
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
                List<String> record = new ArrayList<>();
                record.add(appName);
                record.add(Long.toString(events.get(i).getTimeStamp()));
                record.add(Long.toString(endTime));
                appUsage.add(record);
                Log.e("PMService",appName + " " + Long.toString(events.get(i).getTimeStamp()) + " " + Long.toString(endTime));
            }
        }

        return appUsage;
    }
}
