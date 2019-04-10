package me.xuzhi.aria2cdroid;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XZ on 2016-8-2.
 */
public class PermissionRequest {


    public interface PermissionRequestCallback {
        void onPermissionGranted();

        void onPermissionDenied();
    }

    private int _requestCode = 0;
    private Activity _activity;
    private PermissionRequestCallback _callback;
    private List<String> _deniedPermissions;

    public PermissionRequest(Activity activity, int requestCode) {
        _activity = activity;
        _deniedPermissions = new ArrayList<>();
        _requestCode = requestCode;
    }

    public void processPermissionRequestResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != _requestCode) {
            _callback.onPermissionDenied();
            return;
        }
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }
        if (deniedPermissions.size() > 0) {
            _callback.onPermissionDenied();
        } else {
            _callback.onPermissionGranted();
        }
    }

    public void request(PermissionRequestCallback callback, String... permissions) {
        _callback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String value : permissions) {
                if (_activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED) {
                    _deniedPermissions.add(value);
                }
            }
            if (_deniedPermissions.size() > 0) {
                _activity.requestPermissions(_deniedPermissions.toArray(new String[_deniedPermissions.size()]), _requestCode);
            } else {
                _callback.onPermissionGranted();
            }
        } else {
            if (_callback != null) {
                _callback.onPermissionGranted();
            }
        }
    }

}
