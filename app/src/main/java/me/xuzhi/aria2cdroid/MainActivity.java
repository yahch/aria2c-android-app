package me.xuzhi.aria2cdroid;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.NetworkUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.BreakIterator;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {


// 此部分代码由生成器自动生成

    private ImageView imgStart;
    private ImageView imgMore;
    private TextView tvTitle;
    private LogView3 logview3;

    private void initUIViews() {

        imgStart = (ImageView) findViewById(R.id.imgStart);
        imgMore = (ImageView) findViewById(R.id.imgMore);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        logview3 = (LogView3) findViewById(R.id.logview3);

        tvTitle.setTypeface(this.typeface);

    }

// 以上代码由设计器生成，请勿手动修改


    private Intent intentService;
    private AriaConfig ariaConfig;
    private Aria2Service aria2Service;
    private AlertDialog alertProgressDialog;
    private View viewProgressDialog;
    private TextView tvProgressText;
    private PermissionRequest permissionRequest;
    private Typeface typeface;

    private void loadConfig() {
        ariaConfig = new AriaConfig();
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        ariaConfig.setDir(sharedPreferences.getString("dir", Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()));
        ariaConfig.setDebugLog(sharedPreferences.getBoolean("log", false));
        ariaConfig.setSecret(sharedPreferences.getString("secret", "123456"));
        ariaConfig.setTrackers(sharedPreferences.getString("trackers", ""));
    }

    private void saveConfig() {
        if (ariaConfig == null) loadConfig();
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("dir", ariaConfig.getDir());
        editor.putString("secret", ariaConfig.getSecret());
        editor.putString("trackers", ariaConfig.getTrackers());
        editor.commit();
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Aria2Service.MyBinder binder = (Aria2Service.MyBinder) service;
            aria2Service = binder.getService();
            if (!aria2Service.isRunning()) {
                startService(intentService);
            } else {
                logview3.i(getString(R.string.service_is_running));
                imgStart.setTag("true");
                imgStart.setImageResource(R.drawable.ic_action_stop);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void updateProgressDialogText(String message) {
        tvProgressText.setText(message);
    }

    private PermissionRequest.PermissionRequestCallback permissionRequestCallback = new PermissionRequest.PermissionRequestCallback() {
        @Override
        public void onPermissionGranted() {
            onAllPermissionsGranted();
        }

        @Override
        public void onPermissionDenied() {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.typeface = Typeface.createFromAsset(getAssets(), "font.ttf");
        permissionRequest = new PermissionRequest(this, 3100);
        initUIViews();

        viewProgressDialog = LayoutInflater.from(this).inflate(R.layout.alg_progress, null);
        tvProgressText = (TextView) viewProgressDialog.findViewById(R.id.tvProgressText);
        tvProgressText.setText(getString(R.string.please_wait));
        alertProgressDialog = new AlertDialog.Builder(this).setView(viewProgressDialog).setCancelable(false).create();

        EventBus.getDefault().register(this);

        permissionRequest.request(permissionRequestCallback, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionRequest.processPermissionRequestResult(requestCode, permissions, grantResults);
    }

    private void onSwitchChanged(boolean ifStart) {
        if (ifStart) {
            logview3.i(getString(R.string.starting));
            alertProgressDialog.show();
            aria2Service.start();
        } else {
            logview3.i(getString(R.string.stopping));
            alertProgressDialog.show();
            aria2Service.stop();
        }
    }

    private void onMenuSetingsClick() {
        View vDialog = LayoutInflater.from(MainActivity.this).inflate(R.layout.dlg_settings,
                null);
        final EditText editSecret = vDialog.findViewById(R.id.edtSecret);
        final EditText editPath = vDialog.findViewById(R.id.edtPath);
        final EditText editTrackers = vDialog.findViewById(R.id.edtTrackers);
        final Switch aSwitchDebugLog = vDialog.findViewById(R.id.switchDebugLog);
        final Button btnUpdateTrackers = vDialog.findViewById(R.id.btnUpdateTrackers);

        editSecret.setText(ariaConfig.getSecret());
        editPath.setText(ariaConfig.getDir());
        aSwitchDebugLog.setChecked(ariaConfig.getDebugLog());
        editTrackers.setText(ariaConfig.getTrackers());

        btnUpdateTrackers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRemoteTrackers();
                editTrackers.setText(ariaConfig.getTrackers());
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.setting))
                .setView(vDialog)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String secret = editSecret.getText().toString();
                        String path = editPath.getText().toString();
                        String trackerstring = editTrackers.getText().toString();
                        ariaConfig.setSecret(secret);
                        ariaConfig.setDir(path);
                        ariaConfig.setDebugLog(aSwitchDebugLog.isChecked());
                        ariaConfig.setTrackers(trackerstring);
                        saveConfig();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    private void onAllPermissionsGranted() {


        loadConfig();
        String ip = NetworkUtils.getIPAddress(true);

        if (ip == null || ip.length() < 7) {
            ip = "127.0.0.1";
        }
        logview3.i("IP:" + ip + "," + getString(R.string.port) + ", :6800");

        imgStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object oTag = imgStart.getTag();
                if (oTag == null) {
                    imgStart.setTag("true");
                    imgStart.setImageResource(R.drawable.ic_action_stop);
                    onSwitchChanged(true);
                } else {
                    imgStart.setTag(null);
                    imgStart.setImageResource(R.drawable.ic_action_start_arrow);
                    onSwitchChanged(false);
                }
            }
        });

        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, imgMore);
                popupMenu.getMenu().add(1, Menu.FIRST + 1, 1, getString(R.string.setting));
                popupMenu.getMenu().add(1, Menu.FIRST + 2, 1, getString(R.string.about));
                popupMenu.getMenu().add(1, Menu.FIRST + 3, 1, getString(R.string.open_source_licence));
                popupMenu.getMenu().add(1, Menu.FIRST + 4, 1, getString(R.string.rate));
                popupMenu.getMenu().add(1, Menu.FIRST + 5, 1, getString(R.string.update_tracker));
                popupMenu.getMenu().add(1, Menu.FIRST + 6, 1, getString(R.string.open_aria2_client));
                popupMenu.getMenu().add(1, Menu.FIRST + 7, 1, "Advance settings");
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case (Menu.FIRST + 1):
                                onMenuSetingsClick();
                                break;
                            case (Menu.FIRST + 2):
                                View aboutDialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout
                                        .dlg_about, null);
                                AlertDialog aboutDialog = new AlertDialog.Builder(MainActivity.this).setTitle(getString(R.string.about))
                                        .setView(aboutDialogView)
                                        .create();
                                aboutDialog.show();
                                break;
                            case (Menu.FIRST + 3):
                                Intent intent = new Intent();
                                intent.setAction("android.intent.action.VIEW");
                                Uri content_url = Uri.parse("https://github.com/aria2/aria2");
                                intent.setData(content_url);
                                startActivity(intent);
                                break;
                            case (Menu.FIRST + 4):
                                try {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("market://details?id=" + getPackageName()));
                                    startActivity(i);
                                } catch (Exception __) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.request_google_play), Toast
                                            .LENGTH_SHORT).show();
                                }
                                break;
                            case (Menu.FIRST + 5):
                                updateRemoteTrackers();
                                break;
                            case (Menu.FIRST + 6):
                                Utils.launchapp(getApplicationContext());
                                break;
                            case (Menu.FIRST + 7):
                                Intent intentAdvance = new Intent(MainActivity.this, ConfSettingActivity.class);
                                startActivity(intentAdvance);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        logview3.i("RPC-Secret:" + ariaConfig.getSecret());

        intentService = new Intent(this, Aria2Service.class);
        intentService.putExtra("config", ariaConfig);

        bindService(intentService, conn, BIND_AUTO_CREATE);
    }

    private void updateRemoteTrackers() {
        final AlertDialog dialogDownloadingTrackers = new AlertDialog.Builder(MainActivity.this).setMessage(getString(R.string.please_wait)).create();
        dialogDownloadingTrackers.show();
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get("http://euch.gotoip1.com/trackers.txt", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dialogDownloadingTrackers.dismiss();
                String tstring = new String(responseBody);
                ariaConfig.setTrackers(tstring);
                saveConfig();
                Toast.makeText(getApplicationContext(), getString(R.string.update_tracker_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dialogDownloadingTrackers.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.update_tracker_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (aria2Service != null) {
            unbindService(conn);
            if (!aria2Service.isRunning()) stopService(intentService);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getEventName()) {
            case Aria2Service.ARIA2_SERVICE_START_SUCCESS:
                logview3.s(getString(R.string.service_start_alrerady) + event.getEventData().toString());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertProgressDialog.dismiss();
                    }
                }, 3000);
                break;
            case Aria2Service.ARIA2_SERVICE_START_FAIL:
                logview3.e(getString(R.string.listen_service_fail));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertProgressDialog.dismiss();
                    }
                }, 1000);
                break;
            case Aria2Service.ARIA2_SERVICE_START_STOPPED:
                logview3.s(getString(R.string.service_stop_alrerady));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertProgressDialog.dismiss();
                    }
                }, 3000);
                break;
            case Aria2Service.ARIA2_SERVICE_BIN_CONSOLE:
                if (ariaConfig.getDebugLog()) {
                    logview3.i(event.getEventData().toString());
                }
                break;
        }
    }


}
