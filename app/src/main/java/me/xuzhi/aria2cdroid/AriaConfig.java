package me.xuzhi.aria2cdroid;

import java.io.Serializable;

/**
 * Created by xuzhi on 2018/3/12.
 */

public class AriaConfig implements Serializable {

    private String dir;

    private String secret;

    private boolean debugLog;

    private String trackers;

    public boolean getDebugLog() {
        return debugLog;
    }

    public void setDebugLog(boolean debugLog) {
        this.debugLog = debugLog;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getTrackers() {
        return trackers;
    }

    public void setTrackers(String trackers) {
        this.trackers = trackers;
    }

    @Override
    public String toString() {
        String trackerParas = "";
        if (trackers != null && trackers.length() > 10) {
            StringBuilder stringBuilderTrackers = new StringBuilder();
            String[] trackArr = trackers.split(",");
            if (trackArr.length > 0) {
                for (int i = 0; i < trackArr.length; i++) {
                    stringBuilderTrackers.append(trackArr[i]);
                    if (i <= trackArr.length - 1) {
                        stringBuilderTrackers.append(",");
                    }
                }
                trackerParas = " --bt-tracker=" + stringBuilderTrackers.toString();
            }
        }
        return "--check-certificate=false --enable-rpc --rpc-listen-all --dir=" + dir + " " +
                "--rpc-allow-origin-all --rpc-secret=" + secret + trackerParas;
    }
}
