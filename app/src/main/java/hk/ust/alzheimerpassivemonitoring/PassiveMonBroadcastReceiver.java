package hk.ust.alzheimerpassivemonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by henry on 2017-04-18.
 */

public class PassiveMonBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            Intent startServiceIntent = new Intent(context, PassiveMonService.class);
            context.startService(startServiceIntent);
        }

    }
}