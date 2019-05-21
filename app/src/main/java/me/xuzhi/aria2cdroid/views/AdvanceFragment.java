package me.xuzhi.aria2cdroid.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.jkb.fragment.rigger.annotation.LazyLoad;
import com.jkb.fragment.rigger.annotation.Puppet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.nio.channels.NotYetBoundException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.xuzhi.aria2cdroid.MainActivity;
import me.xuzhi.aria2cdroid.R;
import me.xuzhi.aria2cdroid.Utils;


@Puppet
public class AdvanceFragment extends Fragment {

    public static final int SAVE_CONFIG_SUCCESS = 937;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private View view = null;
    private ProgressBar pbar;
    private LinearLayout mainLayout;

    public AdvanceFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdvanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdvanceFragment newInstance(String param1, String param2) {
        AdvanceFragment fragment = new AdvanceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_advance, container, false);
        pbar = view.findViewById(R.id.pbarwaitting);
        mainLayout = view.findViewById(R.id.configsLayout);
        return view;

    }

    @SuppressLint("CheckResult")
    private void loadandshowConfig() {
        Observable<Map<String, String>> observable = Observable.create(new ObservableOnSubscribe<Map<String, String>>() {
            @Override
            public void subscribe(final ObservableEmitter<Map<String, String>> emitter) throws Exception {
                Map<String, String> config = Utils.readAria2Config(getContext());
                emitter.onNext(config);
            }
        });

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<Map<String, String>>() {
                    @Override
                    public void accept(Map<String, String> s) throws Exception {
                        if (s != null && s.size() > 0) {
                            initUI(s, view);
                            pbar.setVisibility(View.INVISIBLE);
                            mainLayout.setVerticalGravity(View.VISIBLE);
                        }
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            pbar.setVisibility(View.VISIBLE);
            mainLayout.setVerticalGravity(View.INVISIBLE);
            loadandshowConfig();
        } else {
            LinearLayout vw = view.findViewById(R.id.configsLayout);
            if (vw != null)
                vw.removeAllViews();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(null);
        }
    }

    public void saveSettings() {

        try {

            LinearLayout view = getActivity().findViewById(R.id.configsLayout);
            StringBuilder stringBuilder = new StringBuilder();

            if (view != null && view.getChildCount() > 0) {
                for (int i = 0; i < view.getChildCount(); i++) {
                    LinearLayout item = (LinearLayout) view.getChildAt(i);

                    CheckBox checkBox = (CheckBox) item.getChildAt(0);
                    TextView tv = (TextView) item.getChildAt(1);
                    EditText editText = (EditText) item.getChildAt(2);
                    if (checkBox != null && checkBox.isChecked()) {
                        stringBuilder.append(tv.getText()).append(" = ").append(editText.getText().toString()).append("\n");
                    } else {
                        stringBuilder.append("#").append(tv.getText()).append(" = ").append(editText.getText().toString()).append("\n");
                    }
                }
            }

            Files.write(stringBuilder.toString().getBytes(), Utils.getConfigFile(getContext()));
            Message message = new Message();
            message.what = SAVE_CONFIG_SUCCESS;
            mListener.onFragmentInteraction(message);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), getString(R.string.unknow_error), Toast.LENGTH_LONG).show();
        }
    }

    public void reloadSettings() {

        loadandshowConfig();
    }

    private void clearAllNodes() {

        try {
            LinearLayout view = getActivity().findViewById(R.id.configsLayout);

            if (view != null && view.getChildCount() > 0) {
                for (int i = 0; i < view.getChildCount(); i++) {
                    LinearLayout item = (LinearLayout) view.getChildAt(i);
                    item.removeAllViews();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initUI(Map<String, String> config, View viewLayout) {

        clearAllNodes();

        LinearLayout view = viewLayout.findViewById(R.id.configsLayout);
        int textColor = Color.parseColor("#2B2B2B");

        for (Map.Entry<String, String> s : config.entrySet()) {

            LinearLayout linearLayout = new LinearLayout(getActivity().getApplicationContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            TextView tv = new TextView(getActivity().getApplicationContext());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

            EditText editText = new EditText(getActivity().getApplicationContext());
            String paraKey = s.getKey();
            tv.setText(paraKey.replace("#", ""));
            editText.setText(s.getValue(), TextView.BufferType.EDITABLE);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

            CheckBox checkBox = new CheckBox(getActivity().getApplicationContext());
            checkBox.setChecked(!s.getKey().startsWith("#"));

            checkBox.setTextColor(textColor);
            tv.setTextColor(textColor);
            editText.setTextColor(textColor);

            linearLayout.addView(checkBox);
            linearLayout.addView(tv);
            linearLayout.addView(editText);
            view.addView(linearLayout);

        }

        //Utils.writeConsoleLog(mListener, 3, "read config success");

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
