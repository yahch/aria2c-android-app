package me.xuzhi.aria2cdroid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * LogView3
 * Created by xuzhi on 2017/8/8.
 */

public class LogView3 extends RecyclerView {

    private Context context;

    private List<Log> logList;

    private SimpleDateFormat simpleDateFormat;

    private LogAdapter3 logAdapter3;

    private LinearLayoutManager linearLayoutManager;

    private int logTextViewId = 0;

    private Typeface fontface;

    private enum LogType {
        Warn, Error, Debug, Information, Success
    }

    private class Log {
        private String logText;
        private LogType logType;
        private Long time;

        public Log(String logText, LogType logType, Long time) {
            this.logText = logText;
            this.logType = logType;
            this.time = time;
        }

        public String getLogText() {
            return logText;
        }

        public void setLogText(String logText) {
            this.logText = logText;
        }

        public LogType getLogType() {
            return logType;
        }

        public void setLogType(LogType logType) {
            this.logType = logType;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }
    }


    private class LogAdapter3 extends Adapter<LogAdapter3.LogViewHolder> {

        @Override
        public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View vw = getLineLayout();
            LogViewHolder logViewHolder = new LogViewHolder(vw);
            return logViewHolder;
        }

        @Override
        public void onBindViewHolder(LogViewHolder holder, int position) {
            Log log = logList.get(position);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(simpleDateFormat.format(new Date(log.getTime()))).append(" - ").append(log.getLogText());

            if (log.getLogType() == LogType.Debug) {
                holder.textViewLogText.setTextColor(Color.parseColor("#271FE3"));
            } else if (log.getLogType() == LogType.Information) {
                holder.textViewLogText.setTextColor(Color.parseColor("#4D4D4D"));
            } else if (log.getLogType() == LogType.Warn) {
                holder.textViewLogText.setTextColor(Color.parseColor("#D85800"));
            } else if (log.getLogType() == LogType.Error) {
                holder.textViewLogText.setTextColor(Color.parseColor("#FF0000"));
            } else if (log.getLogType() == LogType.Success) {
                holder.textViewLogText.setTextColor(Color.parseColor("#1F860A"));
            }

            holder.textViewLogText.setText(stringBuilder.toString());
        }

        @Override
        public int getItemCount() {
            return logList.size();
        }

        class LogViewHolder extends ViewHolder {

            TextView textViewLogText;

            public LogViewHolder(View view) {
                super(view);
                textViewLogText = (TextView) view.findViewById(logTextViewId);
            }
        }
    }

    private View getLineLayout() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setBackgroundColor(Color.WHITE);
        logTextViewId = View.generateViewId();
        TextView textView = new TextView(getContext());
        textView.setTypeface(fontface);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setId(logTextViewId);
        linearLayout.addView(textView);
        return linearLayout;
    }

    public LogView3(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public LogView3(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public LogView3(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {
        this.setBackgroundColor(Color.WHITE);
        this.logList = new ArrayList<>();
        this.simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        logAdapter3 = new LogAdapter3();
        this.setAdapter(logAdapter3);
        linearLayoutManager = new LinearLayoutManager(this.context);
        this.setLayoutManager(linearLayoutManager);
        this.fontface = Typeface.createFromAsset(getContext().getAssets(), "font.ttf");
    }


    private void addLog(String text, LogType type) {
        if (logList.size() >= 500) {
            logList.clear();
        }
        logList.add(new Log(text, type, System.currentTimeMillis()));
    }

    private void refreshLog() {
        logAdapter3.notifyDataSetChanged();
        this.smoothScrollToPosition(logList.size() - 1);
    }

    private void clearLog() {
        logList.clear();
    }

    public void e(String errorString) {
        addLog(errorString, LogType.Error);
        refreshLog();
    }

    public void d(String debugString) {
        addLog(debugString, LogType.Debug);
        refreshLog();
    }

    public void i(String infoString) {
        addLog(infoString, LogType.Information);
        refreshLog();
    }

    public void w(String warnString) {
        addLog(warnString, LogType.Warn);
        refreshLog();
    }

    public void s(String successString) {
        addLog(successString, LogType.Success);
        refreshLog();
    }

}
