package me.xuzhi.aria2cdroid.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jkb.fragment.rigger.annotation.Puppet;

import me.xuzhi.aria2cdroid.R;

@Puppet
public class LicenceFragment extends Fragment {


    public LicenceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vw = inflater.inflate(R.layout.fragment_licence, container, false);
        return vw;
    }

}
