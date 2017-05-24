package hk.ust.alzheimerpassivemonitoring;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fitbit.authentication.AuthenticationHandler;
import com.fitbit.authentication.AuthenticationManager;
import com.fitbit.authentication.AuthenticationResult;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AuthenticationHandler {

    private static final int PERMISSIONS_REQUEST_READ_CALL_LOG = 200;
    private static final int PERMISSION_REQUEST_PACKAGE_USAGE_STATS = 100;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 300;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (!AuthenticationManager.isLoggedIn()) {
            AuthenticationManager.login(this);
        }


        setContentView(R.layout.activity_main);
        Button graphButton = (Button) findViewById(R.id.graphButton);
        graphButton.setOnClickListener(this);

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
            default:
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AuthenticationManager.onActivityResult(requestCode, resultCode, data, (AuthenticationHandler) this);

        AuthenticationManager.getCurrentAccessToken().getAccessToken();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("fitbitToken",AuthenticationManager.getCurrentAccessToken().getAccessToken());
        editor.commit();
        Log.e("token",sharedPref.getString("fitbitToken",""));

        //get access to app usage permission
        if (!hasPermission()){
            requestPermission();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CALL_LOG);
            }
        }

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_FINE_LOCATION);
        }
        //}

        //Check if an alarm has been scheduled, if not , start the passive monitoring service 1 min later
        Intent pendingService = new Intent(getApplicationContext(),PassiveMonService.class);
        boolean alarmNotExist = (PendingIntent.getService(getApplicationContext(),0,pendingService,PendingIntent.FLAG_NO_CREATE) == null);
        if (alarmNotExist) {
            Log.e("Activity","No Alarm");
            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmIntent = PendingIntent.getService(this, 0, pendingService, 0);
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30 * 1000, alarmIntent);
        }
    }

    @Override
    public void onAuthFinished(AuthenticationResult result) {

    }

}
