package me.xuzhi.aria2cdroid;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

/**
 * Created by xuzhi on 2018/3/12.
 */

public class Aria2cApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
