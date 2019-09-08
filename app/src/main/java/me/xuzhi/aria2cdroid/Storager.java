package me.xuzhi.aria2cdroid;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

import java.lang.reflect.Method;

import static android.content.Context.STORAGE_SERVICE;

public class Storager {

    private static final String TAG = "Storager";

    public static String getPrimaryStoragePath(Context context) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
            // first element in paths[] is primary storage path
            return paths[0];
        } catch (Exception e) {
            Log.e(TAG, "getPrimaryStoragePath() failed", e);
        }
        return null;
    }

    public static String getSecondaryStoragePath(Context context) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(STORAGE_SERVICE);
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
            String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
            // second element in paths[] is secondary storage path
            return paths.length <= 1 ? null : paths[1];
        } catch (Exception e) {
            Log.e(TAG, "getSecondaryStoragePath() failed", e);
        }
        return null;
    }

    public static String getStorageState(String path, Context context) {
        try {
            StorageManager sm = (StorageManager) context.getSystemService(STORAGE_SERVICE);
            Method getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState", new Class[]{String.class});
            String state = (String) getVolumeStateMethod.invoke(sm, path);
            return state;
        } catch (Exception e) {
            Log.e(TAG, "getStorageState() failed", e);
        }
        return null;
    }
}
