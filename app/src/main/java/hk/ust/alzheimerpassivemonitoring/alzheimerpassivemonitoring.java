package hk.ust.alzheimerpassivemonitoring;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.fitbit.authentication.AuthenticationConfiguration;
import com.fitbit.authentication.AuthenticationConfigurationBuilder;
import com.fitbit.authentication.AuthenticationManager;
import com.fitbit.authentication.ClientCredentials;
import com.fitbit.authentication.Scope;

import static com.fitbit.authentication.Scope.activity;

/**
 * Created by henry on 2017-05-24.
 */

public class alzheimerpassivemonitoring extends Application {

    private static final String CLIENT_SECRET = "27fae3bfcb58e9432f01a5d9b18233e6";
    private static final String SECURE_KEY = "CVPdQNAT6fBI4rrPLEn9x0+UV84DoqLFiNHpKOPLRW0=";
    @Override
    public void onCreate() {
        super.onCreate();
        AuthenticationManager.configure(this, generateAuthenticationConfiguration(this, MainActivity.class));
    }

    public static AuthenticationConfiguration generateAuthenticationConfiguration(Context context, Class<? extends Activity> mainActivityClass) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;

            // Load clientId and redirectUrl from application manifest
            String clientId = bundle.getString("com.fitbit.oauth2.CLIENT_ID");
            String redirectUrl = bundle.getString("com.fitbit.oauth2.REDIRECT_URL");


            ClientCredentials CLIENT_CREDENTIALS = new ClientCredentials(clientId, CLIENT_SECRET, redirectUrl);

            return new AuthenticationConfigurationBuilder()

                    .setClientCredentials(CLIENT_CREDENTIALS)
                    .setEncryptionKey(SECURE_KEY)
                    .setTokenExpiresIn(2592000L) // 30 days
                    .setBeforeLoginActivity(new Intent(context, mainActivityClass))
                    .addRequiredScopes(Scope.profile, Scope.settings)
                    .addOptionalScopes(activity, Scope.weight, Scope.sleep)
                    .setLogoutOnAuthFailure(true)

                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
