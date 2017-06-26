package hk.ust.alzheimerpassivemonitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import okhttp3.HttpUrl;

/**
 * Created by henry on 2017-06-26.
 */

public class LoginFitbitActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "2289MK";
    private static final String CLIENT_SECRET = "27fae3bfcb58e9432f01a5d9b18233e6";
    private static final String OAUTH2_AUTH_URL = "https://www.fitbit.com/oauth2/authorize";
    private static final String OAUTH2_TOKEN_URL = "https://api.fitbit.com/oauth2/token";
    private static final String OAUTH2_CALLBACK_URL = "https://finished";
    private static final String FITBIT_SCOPE = "activity+heartrate+location+nutrition+profile+settings+sleep+social+weight";
    private static final String OAUTH2_RESPONSE_TYPE = "token";
    private static final String TOKEN_EXPIRE = "31536000";   //1 year

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginfitbit);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        HttpUrl authorizeUrl = HttpUrl.parse(OAUTH2_AUTH_URL) //
                .newBuilder() //
                .addQueryParameter("client_id", CLIENT_ID)
                .addEncodedQueryParameter("scope",FITBIT_SCOPE)
                .addQueryParameter("redirect_uri", OAUTH2_CALLBACK_URL)
                .addQueryParameter("response_type", OAUTH2_RESPONSE_TYPE)
                .addQueryParameter("access_type","offline")
                .addQueryParameter("expires_in",TOKEN_EXPIRE)
                .build();
        Log.i("URL",authorizeUrl.toString());


        WebView webview = (WebView) findViewById(R.id.loginwebview);
        webview.clearCache(true);
        webview.clearHistory();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        webview.setWebViewClient(new WebViewClient(){


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(url.startsWith(OAUTH2_CALLBACK_URL)){
                    String[] data;
                    HttpUrl callbackURL = HttpUrl.parse(url);

                    data = callbackURL.fragment().split("&");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("fitbitToken",data[0].substring(data[0].indexOf("=")+1));
                    Log.e("Token",sharedPreferences.getString("fitbitToken",""));
                    editor.commit();
                    finish();
                }
            }

        });
        webview.loadUrl(authorizeUrl.toString());
    }

}
