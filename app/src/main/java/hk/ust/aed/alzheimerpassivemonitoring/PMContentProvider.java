package hk.ust.aed.alzheimerpassivemonitoring;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by henry on 7/4/17.
 */

//sampleURI: content://hk.ust.aed.alzheimerpassivemonitoring.PMContentProvider/20170613

public class PMContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        throw new UnsupportedOperationException("Not supported by this provider");
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "application/pdf";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("Not supported by this provider");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not supported by this provider");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not supported by this provider");
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        Log.e("TestJson","CP of PM");
        String fileName = uri.getLastPathSegment();
        File FilesDir = getContext().getFilesDir();
        File data = new File(FilesDir, fileName+".json");

        return ParcelFileDescriptor.open(data, ParcelFileDescriptor.MODE_READ_ONLY);
    }
}
