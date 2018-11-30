package com.baolong.ble_tpms.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.baolong.ble_tpms.R;

public class SendDataDialog extends Dialog {
    private NumberProgressBar numberProgressBar;
    private TextView textView;

    public SendDataDialog(Context context) {
        super(context, R.style.Custom_Progress);
        initLayout();
    }

    public SendDataDialog(Context context, int theme) {
        super(context, R.style.Custom_Progress);
        initLayout();
    }

    private void initLayout() {
        this.setContentView(R.layout.number_progress_bar_layout);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        numberProgressBar = (NumberProgressBar) findViewById(R.id.number_progress);
        textView = (TextView)findViewById(R.id.tv_send_type);
        this.setCanceledOnTouchOutside(false);//点击dialog背景部分不消失
//        this.setCancelable(false);//dialog出现时，点击back键不消失
    }

    public void setProgress(int mProgress) {
        numberProgressBar.setProgress(mProgress);
    }

    public void setTextView(String str) {
        textView.setText(str);
    }
}
