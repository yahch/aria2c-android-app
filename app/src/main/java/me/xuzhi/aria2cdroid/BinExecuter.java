package me.xuzhi.aria2cdroid;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 * Created by xuzhi on 2018/3/12.
 */

public class BinExecuter {

    private int pid;

    private String bin;

    private String paras;

    private Process process;

    public interface BinExecuteCallback {

        void onConsoleResponse(String text);

    }

    private BinExecuteCallback binExecuteCallback;

    public void setBinExecuteCallback(BinExecuteCallback binExecuteCallback) {
        this.binExecuteCallback = binExecuteCallback;
    }

    public int getPid() {
        return pid;
    }

    public BinExecuter(String bin, String paras) {

        this.bin = bin;
        this.paras = paras;

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg == null || msg.getData() == null) return;
            int what = msg.what;
            if (what == 1 && binExecuteCallback != null) {
                String message = msg.getData().getString("message").trim();
                if (message.length() > 0) {
                    binExecuteCallback.onConsoleResponse(message);
                }
            }
        }
    };


    public void start() {

        try {
            process = Runtime.getRuntime().exec(bin + " " + paras);
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            pid = f.getInt(process);
            f.setAccessible(false);
        } catch (Exception ex) {

        }

        if (pid > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream outs = process.getInputStream();
                    InputStreamReader isrout = new InputStreamReader(outs);
                    BufferedReader brout = new BufferedReader(isrout);
                    String line;
                    try {
                        while ((line = brout.readLine()) != null) {
                            Message message = new Message();
                            message.what = 1;
                            Bundle bundle = new Bundle();
                            bundle.putString("message", line);
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    } catch (Exception ex) {
                    }
                }
            }).start();
        }
    }

    public void stop() {
        if (pid > 0) {
            try {
                Runtime.getRuntime().exec("kill -9 " + pid);
            } catch (Exception ex) {
            }
        }
    }
}
