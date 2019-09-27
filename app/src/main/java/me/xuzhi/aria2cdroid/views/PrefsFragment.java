package me.xuzhi.aria2cdroid.views;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.jkb.fragment.rigger.annotation.Puppet;

import java.io.File;
import java.util.Map;

import me.xuzhi.aria2cdroid.R;
import me.xuzhi.aria2cdroid.Storager;
import me.xuzhi.aria2cdroid.Utils;

import static android.content.Context.POWER_SERVICE;

@Puppet
public class PrefsFragment extends Fragment {

    public static final int BATTERY_OPT = 270;

    private OnFragmentInteractionListener mListener;

    private Switch swAutoUpdateTrackers, swUseSdcard, swIgnoreBattery;
    private EditText editTrackersUrl;
    private Button btnSaveTrackers;
    private RadioButton rdoSource1, rdoSource2, rdoSourceCust;

    public PrefsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vw = inflater.inflate(R.layout.fragment_prefs, container, false);

        swAutoUpdateTrackers = vw.findViewById(R.id.swAutoUpdateTrackers);
        swIgnoreBattery = vw.findViewById(R.id.swIgnoreBattery);
        swUseSdcard = vw.findViewById(R.id.swUseSDcard);

        swAutoUpdateTrackers.setOnCheckedChangeListener(onCheckedChangeListener);
        swIgnoreBattery.setOnCheckedChangeListener(onCheckedChangeListener);
        swUseSdcard.setOnCheckedChangeListener(onCheckedChangeListener);

        rdoSource1 = vw.findViewById(R.id.rdoSource1);
        rdoSource2 = vw.findViewById(R.id.rdoSource2);
        rdoSourceCust = vw.findViewById(R.id.rdoSourceCust);

        rdoSource1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    CacheDiskUtils.getInstance().put("trackers_type", new byte[]{(byte) 0x01});
                    editTrackersUrl.setVisibility(View.GONE);
                    btnSaveTrackers.setVisibility(View.GONE);
                }
            }
        });

        rdoSource2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    CacheDiskUtils.getInstance().put("trackers_type", new byte[]{(byte) 0x02});
                    editTrackersUrl.setVisibility(View.GONE);
                    btnSaveTrackers.setVisibility(View.GONE);
                }
            }
        });

        rdoSourceCust.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    CacheDiskUtils.getInstance().put("trackers_type", new byte[]{(byte) 0x03});
                    editTrackersUrl.setVisibility(View.VISIBLE);
                    btnSaveTrackers.setVisibility(View.VISIBLE);
                }
            }
        });

        editTrackersUrl = vw.findViewById(R.id.edtTrackersUrl);
        String trackersUrl = CacheDiskUtils.getInstance().getString("trackers_url_cust");
        if (TextUtils.isEmpty(trackersUrl)) {
            editTrackersUrl.setText("https://raw.githubusercontent.com/ngosang/trackerslist/master/trackers_all_ip.txt");
        } else {
            editTrackersUrl.setText(trackersUrl);
        }

        btnSaveTrackers = vw.findViewById(R.id.btnSaveTrackersUrl);
        btnSaveTrackers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTrackersUrl();
            }
        });

        loadConfig();

        return vw;
    }

    private void saveTrackersUrl() {
        String trackersUrl = editTrackersUrl.getText().toString();
        boolean validate = false;
        if ((trackersUrl.startsWith("http://") || trackersUrl.startsWith("https://") ||
                trackersUrl.startsWith("ftp://")) && trackersUrl.endsWith(".txt")) {
            validate = true;
        }
        if (validate) {
            CacheDiskUtils.getInstance().put("trackers_url_cust", trackersUrl);
            Toast.makeText(getContext(), getString(R.string.string_trackers_url_saved), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.string_error_trackers_url), Toast.LENGTH_SHORT).show();
        }
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            boolean autoUpdateTrackers = swAutoUpdateTrackers.isChecked();
            boolean ignoreBattery = swIgnoreBattery.isChecked();
            boolean useSdcard = swUseSdcard.isChecked();

            CacheDiskUtils.getInstance().put("checkbox_status", new byte[]{autoUpdateTrackers ? (byte) 0x01 : (byte) 0x00
                    , ignoreBattery ? (byte) 0x01 : (byte) 0x00, useSdcard ? (byte) 0x01 : (byte) 0x00});

            if (useSdcard) {
                try {
                    String sdRoot = Storager.getSecondaryStoragePath(getContext());
                    if (!TextUtils.isEmpty(sdRoot)) {
                        File sdpath = new File(sdRoot, "download");
                        if (!sdpath.exists()) sdpath.mkdir();
                        Map<String, String> srcCfg = Utils.readAria2Config(getContext());
                        srcCfg.put("dir", sdpath.getAbsolutePath());
                        String conf = Utils.dumpAria2Config(srcCfg);
                        File fileConf = Utils.getConfigFile(getContext());
                        FileIOUtils.writeFileFromString(fileConf, conf);
                    }
                } catch (Exception e) {

                }
            }
            if (ignoreBattery) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        PowerManager powerManager = (PowerManager) getActivity().getSystemService(POWER_SERVICE);
                        boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(getActivity().getPackageName());
                        if (!hasIgnored) {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    void loadConfig() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getActivity().getSystemService(POWER_SERVICE);
            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(getActivity().getPackageName());
            swIgnoreBattery.setChecked(hasIgnored);
        }

        byte[] brr = CacheDiskUtils.getInstance().getBytes("checkbox_status");
        if (brr == null || brr.length != 3) {
            swAutoUpdateTrackers.setChecked(true);
            swUseSdcard.setChecked(false);
            return;
        }
        swAutoUpdateTrackers.setChecked(brr[0] == 0x01);
        swUseSdcard.setChecked(brr[2] == 0x01);

        String sdPath = Storager.getSecondaryStoragePath(getContext());
        if (TextUtils.isEmpty(sdPath)) {
            swUseSdcard.setEnabled(false);
            swUseSdcard.setChecked(false);
        }

        try {
            byte[] trackrtsType = CacheDiskUtils.getInstance().getBytes("trackers_type");
            if (trackrtsType[0] == 0x01) {
                rdoSource1.setChecked(true);
            } else if (trackrtsType[0] == 0x02) {
                rdoSource2.setChecked(true);
            } else if (trackrtsType[0] == 0x03) {
                rdoSourceCust.setChecked(true);
            }
        } catch (Exception e) {
            rdoSource1.setChecked(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
