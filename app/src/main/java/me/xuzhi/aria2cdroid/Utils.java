package me.xuzhi.aria2cdroid;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.xuzhi.aria2cdroid.views.LogsFragment;
import me.xuzhi.aria2cdroid.views.OnFragmentInteractionListener;

public class Utils {

    public static final String APP_PACKAGE_NAME = "com.gianlu.aria2app";//包名

    /**
     * 启动薄荷App
     *
     * @param context
     */
    public static void launchapp(Context context) {
// 判断是否安装过App,否则去市场下载
        if (isAppInstalled(context, APP_PACKAGE_NAME)) {
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(APP_PACKAGE_NAME));
        } else {
            goToMarket(context, APP_PACKAGE_NAME);
        }
    }

    /**
     * 检测某个应用是否安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 去市场下载页面
     */
    public static void goToMarket(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
        }
    }

    public static void writeConsoleLog(OnFragmentInteractionListener mListener, int type, String message) {
        Message mg = new Message();
        mg.what = 0x6000;
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putString("msg", message);
        mg.setData(bundle);
        if (mListener != null) mListener.onFragmentInteraction(mg);
    }

    public static void writeConsoleLog(LogsFragment logsFragment, int type, String message) {
        if (logsFragment == null) return;
        if (logsFragment.getLogView3() == null) return;
        if (Strings.isNullOrEmpty(message)) return;
        if (type == 0) {
            logsFragment.getLogView3().d(message);
        } else if (type == 1) {
            logsFragment.getLogView3().i(message);
        } else if (type == 2) {
            logsFragment.getLogView3().e(message);
        } else if (type == 3) {
            logsFragment.getLogView3().s(message);
        } else if (type == 4) {
            logsFragment.getLogView3().w(message);
        }
    }

    public static String createDefaultConfig(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#all-proxy = http://127.0.0.1:1080/pac?auth=\n");
        stringBuilder.append("#proxy-method = tunnel\n");
        stringBuilder.append("#no-proxy = <DOMAINS>\n");
        stringBuilder.append("#rpc-user = admin\n");
        stringBuilder.append("#rpc-passwd = passwd\n");
        stringBuilder.append("rpc-secret = 123456\n");
        stringBuilder.append("enable-rpc = true\n");
        stringBuilder.append("rpc-allow-origin-all = true\n");
        stringBuilder.append("rpc-listen-all = true\n");
        stringBuilder.append("#rpc-secure = true\n");
        stringBuilder.append("#rpc-certificate = example.crt\n");
        stringBuilder.append("#rpc-private-key = example.key\n");
        stringBuilder.append("rpc-listen-port = 6800\n");
        stringBuilder.append("disable-ipv6 = true\n");
        stringBuilder.append("#max-concurrent-downloads = 3\n");
        stringBuilder.append("continue = true\n");
        stringBuilder.append("#retry-wait = 10\n");
        stringBuilder.append("#max-tries = 0\n");
        stringBuilder.append("#max-file-not-found = 10\n");
        stringBuilder.append("#max-connection-per-server = 16\n");
        stringBuilder.append("#min-split-size = 2M\n");
        stringBuilder.append("#split = 8\n");
        stringBuilder.append("#remote-time = true\n");
        stringBuilder.append("#content-disposition-default-utf8 = true\n");
        stringBuilder.append("#auto-file-renaming = 0\n");
        stringBuilder.append("#always-resume = 0\n");
        stringBuilder.append("#max-resume-failure-tries = 5\n");
        stringBuilder.append("#referer = <REFERER>\n");
        stringBuilder.append("#parameterized-uri = true\n");
        stringBuilder.append("#max-overall-download-limit = 0\n");
        stringBuilder.append("#max-download-limit = 0\n");
        stringBuilder.append("#max-overall-upload-limit = 100K\n");
        stringBuilder.append("#max-upload-limit = 0\n");
        stringBuilder.append("#lowest-speed-limit = 0\n");
        stringBuilder.append("dir =" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "\n");
        stringBuilder.append("#disk-cache = 64M\n");
        stringBuilder.append("#enable-mmap = true\n");
        stringBuilder.append("#max-mmap-limit = 1280M\n");
        stringBuilder.append("#file-allocation = falloc\n");
        stringBuilder.append("#no-file-allocation-limit = 4096K\n");
        stringBuilder.append("check-certificate = false\n");
        stringBuilder.append("#ca-certificate = <FILE>\n");
        stringBuilder.append("input-file = " + new File(context.getFilesDir(), "aria2.session").getAbsolutePath() + "\n");
        stringBuilder.append("save-session = " + new File(context.getFilesDir(), "aria2.session").getAbsolutePath() + "\n");
        stringBuilder.append("save-session-interval = 60\n");
        stringBuilder.append("#force-save = true\n");
        stringBuilder.append("#on-download-complete = del.bat\n");
        stringBuilder.append("#on-bt-download-complete = del_bt.bat\n");
        stringBuilder.append("#on-download-complete = rm.sh\n");
        stringBuilder.append("#on-bt-download-complete = rm_bt.sh\n");
        stringBuilder.append("#log-level = warn\n");
        stringBuilder.append("#log = aria2.log\n");
        stringBuilder.append("#event-poll =<poll>\n");
        stringBuilder.append("bt-enable-lpd = true\n");
        stringBuilder.append("#follow-torrent = flase\n");
        stringBuilder.append("#listen-port = 6800\n");
        stringBuilder.append("#bt-force-encryption = true\n");
        stringBuilder.append("#bt-require-crypto = true\n");
        stringBuilder.append("#bt-min-crypto-level = arc4\n");
        stringBuilder.append("#bt-max-open-files = 100\n");
        stringBuilder.append("#bt-max-peers = 55\n");
        stringBuilder.append("#bt-request-peer-speed-limit = 50K\n");
        stringBuilder.append("bt-tracker = udp://tracker.coppersurfer.tk:6969/announce,udp://tracker.open-internet.nl:6969/announce,udp://tracker.leechers-paradise.org:6969/announce,http://tracker.internetwarriors.net:1337/announce,udp://tracker.internetwarriors.net:1337/announce,udp://tracker.opentrackr.org:1337/announce,http://tracker.opentrackr.org:1337/announce,udp://9.rarbg.to:2710/announce,udp://9.rarbg.me:2710/announce,http://tracker3.itzmx.com:6961/announce,http://tracker1.itzmx.com:8080/announce,udp://exodus.desync.com:6969/announce,udp://explodie.org:6969/announce,http://explodie.org:6969/announce,udp://ipv4.tracker.harry.lu:80/announce,udp://denis.stalker.upeer.me:6969/announce,udp://tracker.tiny-vps.com:6969/announce,udp://open.demonii.si:1337/announce,http://vps02.net.orel.ru:80/announce,http://re-tracker.uz:80/announce,udp://tracker.torrent.eu.org:451/announce,udp://tracker.filemail.com:6969/announce,udp://thetracker.org:80/announce,udp://bt.xxx-tracker.com:2710/announce,http://open.acgnxtracker.com:80/announce,udp://tracker.uw0.xyz:6969/announce,udp://tracker.trackton.ga:7070/announce,udp://tracker.filepit.to:6969/announce,udp://tracker.cyberia.is:6969/announce,udp://retracker.netbynet.ru:2710/announce,udp://retracker.lanta-net.ru:2710/announce,udp://bt.oiyo.tk:6969/announce,udp://tracker.novg.net:6969/announce,http://tracker.novg.net:6969/announce,udp://tracker.moeking.me:6969/announce,udp://tracker.dyn.im:6969/announce,udp://open.stealth.si:80/announce,https://tracker.fastdownload.xyz:443/announce,https://t.quic.ws:443/announce,https://opentracker.xyz:443/announce,http://opentracker.xyz:80/announce,http://open.trackerlist.xyz:80/announce,udp://tracker2.itzmx.com:6961/announce,udp://tracker.tvunderground.org.ru:3218/announce,http://tracker2.itzmx.com:6961/announce,http://tracker.tvunderground.org.ru:3218/announce,udp://tracker.port443.xyz:6969/announce,udp://tracker.iamhansen.xyz:2000/announce,http://tracker.port443.xyz:6969/announce,http://torrent.nwps.ws:80/announce,udp://tracker4.itzmx.com:2710/announce,udp://tracker.skynetcloud.tk:6969/announce,udp://torrentclub.tech:6969/announce,udp://retracker.baikal-telecom.net:2710/announce,https://tracker.gbitt.info:443/announce,http://tracker4.itzmx.com:2710/announce,http://tracker.gbitt.info:80/announce,udp://zephir.monocul.us:6969/announce,udp://tracker.swateam.org.uk:2710/announce,udp://tracker.nyaa.uk:6969/announce,udp://retracker.sevstar.net:2710/announce,udp://pubt.in:2710/announce,http://tracker.bz:80/announce,http://t.nyaatracker.com:80/announce,http://open.acgtracker.com:1096/announce,http://fxtt.ru:80/announce,wss://tracker.openwebtorrent.com:443/announce,udp://tracker.justseed.it:1337/announce,udp://packages.crunchbangplusplus.org:6969/announce,udp://chihaya.toss.li:9696/announce,https://tracker.vectahosting.eu:2053/announce,https://1337.abcvg.info:443/announce,http://tracker.torrentyorg.pl:80/announce,http://tracker.openzim.org:80/announce,http://t.acg.rip:6699/announce,http://sub4all.org:2710/announce,http://share.camoe.cn:8080/announce,http://retracker.mgts.by:80/announce,http://agusiq-torrents.pl:6969/announce\n");
        stringBuilder.append("#bt-exclude-tracker = <URI>\n");
        stringBuilder.append("#bt-tracker-connect-timeout = 60\n");
        stringBuilder.append("#bt-tracker-timeout = 60\n");
        stringBuilder.append("#enable-dht = true\n");
        stringBuilder.append("#enable-dht6 = true\n");
        stringBuilder.append("#bt-external-ip = <IPADDRESS>\n");
        stringBuilder.append("#dht-listen-port = <PORT>\n");
        stringBuilder.append("enable-peer-exchange = true\n");
        stringBuilder.append("#seed-ratio = 0.0\n");
        stringBuilder.append("#dht-file-path = " + new File(context.getFilesDir(), "dht.dat").getAbsolutePath() + "\n");
        stringBuilder.append("#dht-file-path6 = " + new File(context.getFilesDir(), "dht6.dat").getAbsolutePath() + "\n");
        stringBuilder.append("#user-agent = Transmission/2.77\n");
        stringBuilder.append("#peer-id-prefix = <PEER_ID_PREFIX>\n");
        stringBuilder.append("#bt-save-metadata = true\n");
        stringBuilder.append("#bt-seed-unverified = true\n");
        stringBuilder.append("#bt-hash-check-seed = true\n");
        stringBuilder.append("#check-integrity = false\n");

        return stringBuilder.toString();
    }

    public static Map<String, String> readAria2Config(Context context) {
        Map<String, String> config = new HashMap<>();
        try {
            List<String> lines = new ArrayList<>();
            if (Utils.getConfigFile(context).exists()) {
                lines = Files.readLines(Utils.getConfigFile(context), Charsets.UTF_8);
            } else {
                StringReader sr = new StringReader(Utils.createDefaultConfig(context));
                lines = CharStreams.readLines(sr);
            }
            for (String line : lines) {
                if (Strings.isNullOrEmpty(line)) continue;
                if (line.length() < 2) continue;
                String[] lineArray = line.split("=");
                if (lineArray.length != 2) continue;
                String key = lineArray[0].trim();
                String value = lineArray[1].trim();
                config.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    public static String dumpAria2Config(Map<String, String> paras) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : paras.entrySet()) {
            sb.append(entry.getKey()).append(" ").append("=").append(" ").append(entry.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static File getConfigFile(Context context) {
        return new File(context.getFilesDir(), "aria2.conf");
    }

}
