package hk.ust.alzheimerpassivemonitoring;

import android.app.Notification;


/**
 * Created by henry on 2017-04-19.
 */

public class NotificationForForegroundService extends PassiveMonService{
    public Notification notification;
    public NotificationForForegroundService(){
        notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Passive Monitoring")
                .setContentText("In Progress")
                .build();
    }
}
