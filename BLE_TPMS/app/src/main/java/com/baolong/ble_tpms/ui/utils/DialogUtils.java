package com.baolong.ble_tpms.ui.utils;

import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baolong.ble_tpms.R;

public class DialogUtils {
    public static Vibrator vibrator;

    //带图片的dialog
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void showExitDialog07(Context context, String info) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        ImageView img = new ImageView(context);
        img.setForegroundGravity(Gravity.CENTER);
        //img.setMaxWidth();
        TextView title = new TextView(context);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setText(info);
        title.setTextSize(23);
        img.setImageResource(R.mipmap.warning_bell);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCustomTitle(title)
                .setView(img);
                //.setPositiveButton("确定", null)
        final AlertDialog dialog =builder.create();
        dialog.show();
        //此处设置位置窗体大小
        dialog.getWindow().setLayout(DensityUtil.dp2px(context,300), LinearLayout.LayoutParams.WRAP_CONTENT);
        //vibrator.vibrate(500);//震动30毫秒
    }
}
