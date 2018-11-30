package com.baolong.ble_tpms.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.bean.CarTypeBean;

import java.util.List;

public class CarTypeAdapter extends ArrayAdapter {
    private final int resourceId;

    public CarTypeAdapter(Context context, int textViewResourceId, List<CarTypeBean> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CarTypeBean carTypeBean = (CarTypeBean) getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);//实例化一个对象
        ImageView carTypeImage = (ImageView) view.findViewById(R.id.car_type_icon);//获取该布局内的图片视图
        TextView carTypeName = (TextView) view.findViewById(R.id.car_type_name);//获取该布局内的文本视图
        carTypeImage.setImageResource(carTypeBean.getImageId());//为图片视图设置图片资源
        carTypeName.setText(carTypeBean.getName());//为文本视图设置文本内容
        return view;
    }
}
