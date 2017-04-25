package hk.ust.alzheimerpassivemonitoring;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get access to app usage permission
        if (!hasPermission()){
            requestPermission();
        }

        //Check if an alarm has been scheduled, if not , start the passive monitoring service
        Intent pendingService = new Intent(getApplicationContext(),PassiveMonService.class);
        boolean alarmNotExist = (PendingIntent.getService(getApplicationContext(),0,pendingService,PendingIntent.FLAG_NO_CREATE) == null);
        if (alarmNotExist) {
            Log.e("Activity","No Alarm");
            startService(pendingService);
        }

        setContentView(R.layout.activity_main);
        Button graphButton = (Button) findViewById(R.id.graphButton);
        graphButton.setOnClickListener(this);
    }


    private static final int PERMISSION_REQUEST_PACKAGE_USAGE_STATS = 100;
    public void requestPermission() {
        Toast.makeText(this, "Need to request permission", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), PERMISSION_REQUEST_PACKAGE_USAGE_STATS);
    }

    public boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.graphButton:
                startActivity(new Intent(this, GraphPlotter.class));
                break;
            default:
        }
    }
}
