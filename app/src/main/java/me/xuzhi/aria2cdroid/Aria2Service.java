package me.xuzhi.aria2cdroid;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.FileIOUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by xuzhi on 2018/3/12.
 */

public class Aria2Service extends Service {

    private boolean running;

    public boolean isRunning() {
        return running;
    }

    private File fileAria2c;

    private AriaConfig ariaConfig;

    private BinExecuter binExecuter;

    private static final String TAG = "Aria2Service";

    public static final String ARIA2_SERVICE_START_SUCCESS = "ARIA2_SERVICE_START_SUCCESS";

    public static final String ARIA2_SERVICE_START_FAIL = "ARIA2_SERVICE_START_FAIL";

    public static final String ARIA2_SERVICE_START_STOPPED = "ARIA2_SERVICE_START_STOPPED";

    public static final String ARIA2_SERVICE_BIN_CONSOLE = "ARIA2_SERVICE_BIN_CONSOLE";

    public static final String ARIA2_CONFIG_MISS = "ARIA2_CONFIG_MISS";

    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";

    private NotificationManager notificationManager = null;

    boolean isCreateChannel = false;

    public class MyBinder extends Binder {

        public Aria2Service getService() {
            return Aria2Service.this;
        }
    }

    private MyBinder binder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        fileAria2c = new File(getFilesDir(), "aria2c");
        sendMessage(ARIA2_SERVICE_BIN_CONSOLE, "aria2 version:v1.34.0");
        sendMessage(ARIA2_SERVICE_BIN_CONSOLE, "app version:v" + BuildConfig.VERSION_NAME + "(" + BuildConfig.BUILD_TYPE + ")");

        try {
            File sessionFile = new File(getFilesDir(), "aria2.session");
            if (!sessionFile.exists()) {
                sessionFile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean exist = fileAria2c.exists() && (fileAria2c.length() == 4349452);

        if (!exist) {
            try {
                fileAria2c.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                InputStream ins = getResources().openRawResource(R.raw.aria2c);
                FileIOUtils.writeFileFromIS(fileAria2c, ins);
                Runtime.getRuntime().exec("chmod 777 " + fileAria2c.getAbsolutePath());
                sendMessage(ARIA2_SERVICE_BIN_CONSOLE, getString(R.string.aria_updated));
            } catch (Exception e) {
                Log.e(TAG, "onCreate: ", e);
            }
        }

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("aria2app", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        long lastUpdate = sharedPreferences.getLong("lastUpdate", 0);

        byte[] prefsSwitch = CacheDiskUtils.getInstance().getBytes("sp_config_switch_state");
        boolean autoUpdate = true;
        if (prefsSwitch != null && prefsSwitch.length > 0) {
            autoUpdate = (prefsSwitch[0] == 0x01);
        }

        if (!autoUpdate) {
            sendMessage(ARIA2_SERVICE_BIN_CONSOLE, getString(R.string.string_auto_update_trackers_off));
        }

        if (System.currentTimeMillis() - lastUpdate > 43200000 && autoUpdate) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    TrackerUpdater tcu = new TrackerUpdater();
                    tcu.update(getApplicationContext(), new TrackerUpdater.Callback() {
                        @Override
                        public void onComplete(String trackers) {
                            try {
                                Map<String, String> aria2config = Utils.readAria2Config(getApplicationContext());
                                aria2config.put("bt-tracker", trackers);
                                String confText = Utils.dumpAria2Config(aria2config);
                                FileIOUtils.writeFileFromString(Utils.getConfigFile(getApplicationContext()), confText);
                                sendMessage(ARIA2_SERVICE_BIN_CONSOLE, getString(R.string.update_tracker_success));
                                editor.putLong("lastUpdate", System.currentTimeMillis());
                                editor.commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });

                }
            }, 5000);
        } else {
            Log.d(TAG, "may not update now");
        }

    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void start() {
        if (binExecuter == null) return;
        binExecuter.start();
        if (binExecuter.getPid() > 0) {
            sendMessage(ARIA2_SERVICE_START_SUCCESS, String.valueOf(binExecuter.getPid()));
            running = true;
            try {
                startForeground(660, buildNotification());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sendMessage(ARIA2_SERVICE_START_FAIL, String.valueOf(0));
            running = false;
            try {
                stopForeground(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (binExecuter == null) return;
        binExecuter.stop();
        sendMessage(ARIA2_SERVICE_START_STOPPED, String.valueOf(0));
        running = false;
        try {
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            File configFile = Utils.getConfigFile(getApplicationContext());
            if (!configFile.exists()) {
                sendMessage(ARIA2_CONFIG_MISS, String.valueOf(0));
                return super.onStartCommand(intent, flags, startId);
            }
            binExecuter = new BinExecuter(fileAria2c.getAbsolutePath(), "--conf-path=" + configFile.getAbsolutePath());
            binExecuter.setBinExecuteCallback(new BinExecuter.BinExecuteCallback() {
                @Override
                public void onConsoleResponse(String text) {
                    sendMessage(ARIA2_SERVICE_BIN_CONSOLE, text);
                }
            });
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendMessage(String name, String message) {
        MessageEvent genericEvent = new MessageEvent(name, message);
        EventBus.getDefault().post(genericEvent);
    }

    @SuppressLint("NewApi")
    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            if (null == notificationManager) {
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = getPackageName();
            if (!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.BLUE);
                notificationChannel.setShowBadge(true);
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_string_service_running))
                .setWhen(System.currentTimeMillis());

        notification = builder.build();
        return notification;
    }

}
