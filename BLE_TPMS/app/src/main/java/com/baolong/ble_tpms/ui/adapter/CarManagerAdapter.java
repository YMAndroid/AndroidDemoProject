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

import java.util.List;

public class CarManagerAdapter extends RecyclerView.Adapter<CarManagerAdapter.MyViewHolder> {
    private Context mContext;
    private List<CarRecode> mDatas;
    private MyOnItemClickListener myOnItemClickListener;
    private MyOnItemLongClickListener myOnItemLongClickListener;

    public CarManagerAdapter(Context context, List<CarRecode> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }


    @Override
    public void onBindViewHolder(final MyViewHolder viewHolder, int position) {
        viewHolder.textView.setText(mDatas.get(position).getCarRemark() + "(" + mDatas.get(position).getCarName() + ")");
        if (mDatas.get(position).getStatus() == 1) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
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
    public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.car_manager_item, arg0, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    public void setOnItemClickListener(MyOnItemClickListener itemClickListener) {
        this.myOnItemClickListener = itemClickListener;
    }

    public void setOnItemLongClickListener(MyOnItemLongClickListener itemLongClickListener) {
        this.myOnItemLongClickListener = itemLongClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        // Item子布局上的一个元素
        TextView textView;
        CheckBox checkBox;
        RelativeLayout carManagerItem;

        public MyViewHolder(View itemView) {
            super(itemView);
            // 关联引动该元素 ，在item.xml中findView，注意不要忘写(itemview.)
            textView = (TextView) itemView.findViewById(R.id.tv_car_info);
            checkBox = (CheckBox) itemView.findViewById(R.id.item_check);
            carManagerItem = itemView.findViewById(R.id.car_manager_item);
        }
    }
}

