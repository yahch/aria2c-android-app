package me.xuzhi.aria2cdroid.views;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import com.blankj.utilcode.util.FileIOUtils;
import com.jkb.fragment.rigger.annotation.Puppet;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.io.File;
import java.util.List;
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
    private EditText editTrackersUrl, edtSavePath;
    private Button btnSaveTrackers, btnChooseSavePath;
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

        edtSavePath = vw.findViewById(R.id.edt_savepath);
        btnChooseSavePath = vw.findViewById(R.id.btn_choose_savepath);

        swAutoUpdateTrackers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("autoUpdateTrackers", isChecked);
                editor.commit();
            }
        });
        swIgnoreBattery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
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
        });
        swUseSdcard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("useSdcard", isChecked);
                editor.commit();
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
        });

        rdoSource1 = vw.findViewById(R.id.rdoSource1);
        rdoSource2 = vw.findViewById(R.id.rdoSource2);
        rdoSourceCust = vw.findViewById(R.id.rdoSourceCust);

        rdoSource1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    changeTrackerType(1);
                    showHideCustFields(View.GONE);

                }
            }
        });

        rdoSource2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    changeTrackerType(2);
                    showHideCustFields(View.GONE);
                }
            }
        });

        rdoSourceCust.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    changeTrackerType(30);
                    showHideCustFields(View.VISIBLE);
                }
            }
        });

        editTrackersUrl = vw.findViewById(R.id.edtTrackersUrl);
        SharedPreferences sp = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String trackersUrl = sp.getString("trackers_url_cust", null);
        if (!TextUtils.isEmpty(trackersUrl)) {
            editTrackersUrl.setText(trackersUrl);
        }

        btnSaveTrackers = vw.findViewById(R.id.btnSaveTrackersUrl);
        btnSaveTrackers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTrackersUrl();
            }
        });

        final Fragment me = this;

        btnChooseSavePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LFilePicker().withSupportFragment(me)
                        .withRequestCode(13)
                        .withChooseMode(false)
                        .withStartPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                        .withTitle(getString(R.string.string_choose))
                        .start();
            }
        });

        loadConfig();

        return vw;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 13) {
                List<String> list = data.getStringArrayListExtra("paths");
                for (String s : list) {
                    Toast.makeText(getActivity(), s + "", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showHideCustFields(int status) {
        editTrackersUrl.setVisibility(status);
        btnSaveTrackers.setVisibility(status);
    }

    private void changeTrackerType(int type) {
        SharedPreferences sp = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("trackers_type", type);
        editor.commit();
    }

    private void saveTrackersUrl() {
        String trackersUrl = editTrackersUrl.getText().toString();
        boolean validate = false;
        if ((trackersUrl.startsWith("http://") || trackersUrl.startsWith("https://") ||
                trackersUrl.startsWith("ftp://")) && trackersUrl.endsWith(".txt")) {
            validate = true;
        }
        if (validate) {
            SharedPreferences sp = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("trackers_url_cust", trackersUrl);
            editor.commit();
            Toast.makeText(getContext(), getString(R.string.string_trackers_url_saved), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.string_error_trackers_url), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadConfig();
    }


    void loadConfig() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getActivity().getSystemService(POWER_SERVICE);
            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(getActivity().getPackageName());
            swIgnoreBattery.setChecked(hasIgnored);
        }

        SharedPreferences sp = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        boolean autoUpdateTrackers = sp.getBoolean("autoUpdateTrackers", true);
        boolean ignoreBattery = sp.getBoolean("ignoreBattery", false);
        boolean useSdcard = sp.getBoolean("useSdcard", false);

        swAutoUpdateTrackers.setChecked(autoUpdateTrackers);
        swUseSdcard.setChecked(useSdcard);

        String sdPath = Storager.getSecondaryStoragePath(getContext());
        if (TextUtils.isEmpty(sdPath)) {
            swUseSdcard.setEnabled(false);
            swUseSdcard.setChecked(false);
        }

        try {
            int trackrtsType = sp.getInt("trackers_type", 1);
            if (trackrtsType == 1) {
                rdoSource1.setChecked(true);
            } else if (trackrtsType == 2) {
                rdoSource2.setChecked(true);
            } else if (trackrtsType == 30) {
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
