package hk.ust.aed.alzheimerpassivemonitoring;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by henry on 7/4/17.
 */

public class PMContentProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI("hk.ust.aed.alzheimerpassivemonitoring.PMContentProvider","PhoneUsage",1);
        sUriMatcher.addURI("hk.ust.aed.alzheimerpassivemonitoring.PMContentProvider","LocationRecord",2);
        sUriMatcher.addURI("hk.ust.aed.alzheimerpassivemonitoring.PMContentProvider","StepDistance",3);
        sUriMatcher.addURI("hk.ust.aed.alzheimerpassivemonitoring.PMContentProvider","SleepWakeCycle",4);
        sUriMatcher.addURI("hk.ust.aed.alzheimerpassivemonitoring.PMContentProvider","HeartRate",5);
    }

    SQLiteHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new SQLiteHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor mCursor = null;
        switch(sUriMatcher.match(uri)){
            case 1:
                database.query("PhoneUsage", null, selection, selectionArgs, null, null, null);
                break;
            case 2:
                database.query("LocationRecord", null, selection, selectionArgs, null, null, null);
                break;
            case 3:
                database.query("StepDistance", null, selection, selectionArgs, null, null, null);
                break;
            case 4:
                database.query("SleepWakeCycle", null, selection, selectionArgs, null, null, null);
                break;
            case 5:
                database.query("HeartRate", null, selection, selectionArgs, null, null, null);
                break;
            default:
                Log.e("SQLite_Query","Unknown Uri");
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
