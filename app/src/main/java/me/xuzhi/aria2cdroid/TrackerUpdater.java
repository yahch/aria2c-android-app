package me.xuzhi.aria2cdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.baidu.mobstat.StatService;
import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TrackerUpdater {

    public interface Callback {
        void onComplete(String trackers);

        void onError(Throwable e);

        void onReport(String message);
    }

    private Context mContext;
    private String mUrl;

    public TrackerUpdater(Context context) {
        this.mContext = context;
        try {
            SharedPreferences sp = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
            int trackersType = sp.getInt("trackers_type", 0);
            //byte[] trackersType = CacheDiskUtils.getInstance().getBytes("trackers_type");
            if (trackersType == 0x01) {
                mUrl = "https://raw.githubusercontent.com/ngosang/trackerslist/master/trackers_all_ip.txt";
            } else if (trackersType == 0x02) {
                mUrl = "https://gitee.com/OR120/BT-trackers/raw/master/trackers.txt";
            } else if (trackersType == 0x30) {
                mUrl = CacheDiskUtils.getInstance().getString("trackers_url_cust");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mUrl = null;
        }
        if (mUrl == null) {
            mUrl = "https://raw.githubusercontent.com/ngosang/trackerslist/master/trackers_all_ip.txt";
        }
    }

    public void update(final Context context, final Callback callback) {
        String url = mUrl;
        callback.onReport(mUrl);
        AsyncHttpClient client = new AsyncHttpClient();
        try {
            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    try {
                        StatService.onEvent(context, "UPDATE_TRACKERS", "", 1);
                        String result = new String(bytes);
                        File trackers = new File(context.getCacheDir(), "trackers.txt");
                        FileIOUtils.writeFileFromString(trackers, result);
                        List<String> lines = FileIOUtils.readFile2List(trackers);
                        if (lines == null || lines.size() < 1) {
                            callback.onComplete(null);
                            return;
                        }
                        StringBuilder sbLines = new StringBuilder();
                        for (int j = 0; j < lines.size(); j++) {
                            String line = lines.get(j);
                            if (TextUtils.isEmpty(line)) continue;
                            if (line.length() < 3) continue;
                            sbLines.append(line).append(",");
                        }
                        String tcrs = sbLines.toString();
                        tcrs = tcrs.substring(0, tcrs.length() - 1);
                        callback.onComplete(tcrs);
                    } catch (Exception e) {
                        callback.onError(e);
                        callback.onComplete(null);
                    }
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    callback.onError(throwable);
                    callback.onComplete(null);
                }
            });
        } catch (Exception e) {
            callback.onComplete(null);
            callback.onError(e);
        }
    }
}
