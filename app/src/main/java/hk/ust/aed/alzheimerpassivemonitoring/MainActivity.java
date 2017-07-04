/**
 # COMP 4521    #  CHAN CHI HANG       20199170         chchanbq@connect.ust.hk
 # COMP 4521    #  KO CHING WAI          20199168         cwko@connect.ust.hk
 */

package hk.ust.aed.alzheimerpassivemonitoring;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PERMISSIONS_REQUEST_READ_CALL_LOG = 200;
    private static final int PERMISSION_REQUEST_PACKAGE_USAGE_STATS = 100;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 300;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if(intent.hasExtra("Test"))Log.e("Test",intent.getStringExtra("Test"));


        //get access to app usage permission
        if (!hasPermission()){
            requestPermission();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CALL_LOG);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_FINE_LOCATION);
            }
        }

        //Check if an alarm has been scheduled, if not , start the passive monitoring service 1 min later
        Intent pendingService = new Intent(getApplicationContext(),PassiveMonService.class);
        boolean alarmNotExist = (PendingIntent.getService(getApplicationContext(),0,pendingService,PendingIntent.FLAG_NO_CREATE) == null);
        if (alarmNotExist) {
            Log.e("Activity","No Alarm");
            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmIntent = PendingIntent.getService(this, 0, pendingService, 0);
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30 * 1000, alarmIntent);
        }

        Button graphButton = (Button) findViewById(R.id.graphButton);
        graphButton.setOnClickListener(this);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

    }



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
            case R.id.loginButton:
                    startActivity(new Intent(this,LoginFitbitActivity.class));
            default:
        }
    }


}
