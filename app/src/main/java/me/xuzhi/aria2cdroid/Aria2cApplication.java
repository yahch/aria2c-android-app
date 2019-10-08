package me.xuzhi.aria2cdroid;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.baidu.mobstat.StatService;
import com.blankj.utilcode.util.Utils;

/**
 * Created by xuzhi on 2018/3/12.
 */

public class Aria2cApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        StatService.autoTrace(this, true, false);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
