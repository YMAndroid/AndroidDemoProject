package com.baolong.ble_tpms.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.bean.TripPressureDevice;
import com.baolong.ble_tpms.ui.db.Config;
import com.baolong.ble_tpms.ui.utils.Utils;

import java.util.List;

public class UnBindDeviceAdapter extends RecyclerView.Adapter<UnBindDeviceAdapter.MyViewHolder> {
    private Context mContext;
    private List<TripPressureDevice> mDatas;
    private MyOnItemClickListener myOnItemClickListener;

    public UnBindDeviceAdapter(Context context, List<TripPressureDevice> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }


    @Override
    public void onBindViewHolder(final UnBindDeviceAdapter.MyViewHolder viewHolder, int position) {
        viewHolder.tvCarRemark.setText(mDatas.get(position).getDeviceName()+"("+ mDatas.get(position).getMacAddress()+ ")");
        viewHolder.tvDeviceName.setText(Utils.tripType(mDatas.get(position).getTripType()));
        if(mDatas.get(position).getBindStatus() == Config.BIND_VALUE){
            viewHolder.tvUpgradeStstus.setText(Config.BIND);
        } else if(mDatas.get(position).getBindStatus() == Config.UN_BIND_VALUE){
            viewHolder.tvUpgradeStstus.setText(Config.UN_BIND);
        }
        //viewHolder.iv_right_arrow_next.setVisibility(View.GONE);
        viewHolder.itemView.setTag(position);
        /*自定义item的点击事件不为null，设置监听事件*/
        if (myOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myOnItemClickListener.onItemClick(viewHolder.itemView, viewHolder.getLayoutPosition());
                }
            });
        }
    }

    @Override
    public UnBindDeviceAdapter.MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.device_upgrade_item, arg0, false);
        UnBindDeviceAdapter.MyViewHolder holder = new UnBindDeviceAdapter.MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        // Item子布局上的一个元素
        TextView tvCarRemark,tvDeviceName,tvUpgradeStstus;
        RelativeLayout rlCurrentItem;
        ImageView iv_right_arrow_next;

        public MyViewHolder(View itemView) {
            super(itemView);
            // 关联引动该元素 ，在item.xml中findView，注意不要忘写(itemview.)
            tvCarRemark = (TextView) itemView.findViewById(R.id.tv_car_remark);
            tvDeviceName = (TextView) itemView.findViewById(R.id.tv_device_name);
            tvUpgradeStstus = (TextView) itemView.findViewById(R.id.tv_upgrade_ststus);
            rlCurrentItem = itemView.findViewById(R.id.rl_current_item);
            //iv_right_arrow_next = itemView.findViewById(R.id.iv_right_arrow_next);
        }
    }

    public void setOnItemClickListener(MyOnItemClickListener itemClickListener) {
        this.myOnItemClickListener = itemClickListener;
    }
}
