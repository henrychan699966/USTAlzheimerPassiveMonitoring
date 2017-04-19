package hk.ust.alzheimerpassivemonitoring;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.support.annotation.Nullable;
import android.widget.Toast;


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
        ONGOING_NOTIFICATION_ID = (int)System.currentTimeMillis();
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(ONGOING_NOTIFICATION_ID,notification);
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
