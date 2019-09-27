package me.xuzhi.aria2cdroid;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.mobstat.StatService;
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
    }

    private String mUrl;

    public TrackerUpdater(String mUrl) {
        this.mUrl = mUrl;
    }

    public void update(final Context context, final Callback callback) {
        String url = mUrl;
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
