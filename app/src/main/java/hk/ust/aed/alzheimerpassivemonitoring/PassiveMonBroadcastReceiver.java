/**
 # COMP 4521    #  CHAN CHI HANG       20199170         chchanbq@connect.ust.hk
 # COMP 4521    #  KO CHING WAI          20199168         cwko@connect.ust.hk
 */

package hk.ust.aed.alzheimerpassivemonitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PassiveMonBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            Intent startServiceIntent = new Intent(context, PassiveMonService.class);
            context.startService(startServiceIntent);
        }

    }
}