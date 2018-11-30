package com.baolong.ble_tpms.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.bean.CarRecode;
import com.baolong.ble_tpms.ui.bean.TripPressureDevice;
import com.baolong.ble_tpms.ui.db.Config;

import java.util.List;

public class DeviceUpgradeAdapter extends RecyclerView.Adapter<DeviceUpgradeAdapter.MyViewHolder>{
    private Context mContext;
    private List<TripPressureDevice> mDatas;
    private MyOnItemClickListener myOnItemClickListener;
    private MyOnItemLongClickListener myOnItemLongClickListener;
    private View mHeaderView;
    public final static int TYPE_HEADER = 0;
    public final static int TYPE_BODY = 1;

    public DeviceUpgradeAdapter(Context context, List<TripPressureDevice> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    public void addHeaderView(View headerView){
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

//    @Override
//    public int getItemViewType(int position) {
//        if(mHeaderView == null)
//            return TYPE_BODY;
//        if(position == 0) {
//            return TYPE_HEADER;
//        }
//        return TYPE_BODY;
//    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

//    @Override
//    public int getItemCount() {
//        return mHeaderView != null ? mDatas.size() + 1 : mDatas.size();
//    }

    @Override
    public void onBindViewHolder(final DeviceUpgradeAdapter.MyViewHolder viewHolder, int position) {
//        if(getItemViewType(position) == TYPE_HEADER){
//            return;
//        } else {
//            if(mHeaderView != null){
//                position--;
//            }
//        }
        viewHolder.tvCarRemark.setText(mDatas.get(position).getCarName());
        viewHolder.tvDeviceName.setText(mDatas.get(position).getDeviceName()+"("+mDatas.get(position).getMacAddress() +")");
        if(mDatas.get(position).getIsUpgrade() == Config.NOT_UPGRADED){
            viewHolder.tvUpgradeStstus.setText(mContext.getResources().getString(R.string.not_upgraded));
        } else if(mDatas.get(position).getIsUpgrade() == Config.UPGRADED_3049_HEX){
            viewHolder.tvUpgradeStstus.setText(mContext.getResources().getString(R.string.upgraded_3049));
        } else if(mDatas.get(position).getIsUpgrade() == Config.UPGRADED_3011_HEX){
            viewHolder.tvUpgradeStstus.setText(mContext.getResources().getString(R.string.upgraded_3011));
        } else if(mDatas.get(position).getIsUpgrade() == Config.UPGRADED_3049_AND_3011_HEX){
            viewHolder.tvUpgradeStstus.setText(mContext.getResources().getString(R.string.upgraded_3049_and_3011));
        }
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

        /*自定义item的长按事件不为null，设置监听事件*/
        if (myOnItemLongClickListener != null) {

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    myOnItemLongClickListener.OnItemLongClickListener(viewHolder.itemView, viewHolder.getLayoutPosition());
                    return true;
                }
            });
        }
    }

    @Override
    public DeviceUpgradeAdapter.MyViewHolder onCreateViewHolder(ViewGroup arg0, int viewType) {
//        if(viewType == TYPE_HEADER && mHeaderView != null){
//            return new MyViewHolder(mHeaderView);
//        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.device_upgrade_item, arg0, false);
        DeviceUpgradeAdapter.MyViewHolder holder = new DeviceUpgradeAdapter.MyViewHolder(view);
        return holder;
    }


    public void setOnItemClickListener(MyOnItemClickListener itemClickListener) {
        this.myOnItemClickListener = itemClickListener;
    }

    public void setOnItemLongClickListener(MyOnItemLongClickListener itemLongClickListener) {
        this.myOnItemLongClickListener = itemLongClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        // Item子布局上的一个元素
        TextView tvCarRemark,tvDeviceName,tvUpgradeStstus;
        RelativeLayout rlCurrentItem;

        public MyViewHolder(View itemView) {
            super(itemView);
            // 关联引动该元素 ，在item.xml中findView，注意不要忘写(itemview.)
            tvCarRemark = (TextView) itemView.findViewById(R.id.tv_car_remark);
            tvDeviceName = (TextView) itemView.findViewById(R.id.tv_device_name);
            tvUpgradeStstus = (TextView) itemView.findViewById(R.id.tv_upgrade_ststus);
            rlCurrentItem = itemView.findViewById(R.id.rl_current_item);
        }
    }
}
