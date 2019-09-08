package me.xuzhi.aria2cdroid.views;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.jkb.fragment.rigger.annotation.Puppet;

import me.xuzhi.aria2cdroid.R;
import me.xuzhi.aria2cdroid.Storager;

import static android.content.Context.POWER_SERVICE;

@Puppet
public class PrefsFragment extends Fragment {

    public static final int BATTERY_OPT = 270;

    private OnFragmentInteractionListener mListener;

    private Switch swAutoUpdateTrackers, swUseSdcard, swIgnoreBattery;

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

        loadConfig();

        return vw;
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            boolean autoUpdateTrackers = swAutoUpdateTrackers.isChecked();
            boolean ignoreBattery = swIgnoreBattery.isChecked();
            boolean useSdcard = swUseSdcard.isChecked();
            byte[] brr = new byte[]{0x00, 0x00, 0x00};
            brr[0] = autoUpdateTrackers ? (byte) 0x01 : (byte) 0x00;
            brr[1] = ignoreBattery ? (byte) 0x01 : (byte) 0x00;
            brr[2] = useSdcard ? (byte) 0x01 : (byte) 0x00;
            CacheDiskUtils.getInstance().put("sp_config_switch_state", brr);
        }
    };

    void loadConfig() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getActivity().getSystemService(POWER_SERVICE);
            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(getActivity().getPackageName());
            swIgnoreBattery.setChecked(hasIgnored);
        }

        byte[] brr = CacheDiskUtils.getInstance().getBytes("sp_config_switch_state");
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
