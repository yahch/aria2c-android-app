package me.xuzhi.aria2cdroid;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf_setting);

        InputStream ins = getResources().openRawResource(R.raw.aria2_config);
        Map<String, String> config = new HashMap<>();

        try {
            List<String> lines = CharStreams.readLines(new InputStreamReader(ins));
            for (String line : lines) {
                if (Strings.isNullOrEmpty(line)) continue;
                if (line.length() < 2) continue;
                String[] lineArray = line.split("=");
                if (lineArray.length != 2) continue;
                String key = lineArray[0].trim();
                String value = lineArray[1].trim();
                config.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        initUI(config);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, "Reset default");
        menu.add(0, 2, 0, "Save settings");
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 1:
                Toast.makeText(getApplicationContext(), "not support", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                saveSettings();
                break;
            default:
                break;
        }
        return true;
    }

    private void saveSettings() {

        LinearLayout view = findViewById(R.id.configLayout);
        StringBuilder stringBuilder = new StringBuilder();

        if (view != null && view.getChildCount() > 0) {
            for (int i = 0; i < view.getChildCount(); i++) {
                LinearLayout item = (LinearLayout) view.getChildAt(i);

                CheckBox checkBox = (CheckBox) item.getChildAt(0);
                TextView tv = (TextView) item.getChildAt(1);
                EditText editText = (EditText) item.getChildAt(2);
                if (checkBox != null && checkBox.isChecked()) {
                    stringBuilder.append(tv.getText()).append("=").append(editText.getText().toString()).append("\n");
                }
            }
        }

        Toast.makeText(getApplicationContext(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();
    }

    private void initUI(Map<String, String> config) {

        Typeface fontface = Typeface.createFromAsset(getAssets(), "font.ttf");
        LinearLayout view = findViewById(R.id.configLayout);

        for (Map.Entry<String, String> s : config.entrySet()) {

            LinearLayout linearLayout = new LinearLayout(getApplicationContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            TextView tv = new TextView(getApplicationContext());
            tv.setTypeface(fontface);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

            EditText editText = new EditText(getApplicationContext());
            String paraKey = s.getKey();
            tv.setText(paraKey.replace("#", ""));
            editText.setText(s.getValue(), TextView.BufferType.EDITABLE);
            editText.setTypeface(fontface);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

            CheckBox checkBox = new CheckBox(getApplicationContext());
            checkBox.setChecked(!s.getKey().startsWith("#"));


            linearLayout.addView(checkBox);
            linearLayout.addView(tv);
            linearLayout.addView(editText);
            view.addView(linearLayout);

        }

    }
}
