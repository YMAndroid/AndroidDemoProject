package com.baolong.ble_tpms.ui.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baolong.ble_tpms.R;

public abstract class BaseTitleActivity extends Activity {
    protected ImageView backImage;
    protected ImageView ivType;
    protected TextView tvTitle;
    protected TextView tvRightTitle;
    protected Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.common_title_bar);
        mContext = this;
        backImage = (ImageView) findViewById(R.id.back_image);
        ivType = (ImageView) findViewById(R.id.iv_type);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvRightTitle = (TextView) findViewById(R.id.tv_right_title);
        if (getBackOnClickLisener() == null) {
            backImage.setOnClickListener(new BackOnClickLisener());
        } else {
            backImage.setOnClickListener(getBackOnClickLisener());
        }
        init();
    }

    /**
     * 公共的标题栏
     *
     * @param title
     * @param layoutId
     */
    public void setTitleAndContentLayoutId(String title, int layoutId) {
        getLayoutInflater().inflate(layoutId,
                (ViewGroup) tvTitle.getParent().getParent());
        tvTitle.setText(title);
    }

    public abstract void init();

    public abstract View.OnClickListener getBackOnClickLisener();

    class BackOnClickLisener implements View.OnClickListener {

        @Override
        public void onClick(View arg0) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            backImage.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
