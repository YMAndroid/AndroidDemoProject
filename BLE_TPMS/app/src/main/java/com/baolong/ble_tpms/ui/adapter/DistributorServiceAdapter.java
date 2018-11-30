package com.baolong.ble_tpms.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baolong.ble_tpms.R;
import com.baolong.ble_tpms.ui.bean.LocalBleDevice;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class DistributorServiceAdapter extends BaseAdapter {

    private Context context;
    private List<LocalBleDevice> bleDeviceList;

    public DistributorServiceAdapter(Context context) {
        this.context = context;
        bleDeviceList = new ArrayList<>();
    }

    public void addDevice(LocalBleDevice bleDevice) {
        removeDevice(bleDevice);
        bleDeviceList.add(bleDevice);
    }

    public void removeDevice(LocalBleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            if (bleDeviceList.get(i).getBleDevice().getKey().equals(bleDevice.getBleDevice().getKey())) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearConnectedDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            LocalBleDevice device = bleDeviceList.get(i);
            if (BleManager.getInstance().isConnected(device.getBleDevice())) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearScanDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            LocalBleDevice device = bleDeviceList.get(i);
            if (!BleManager.getInstance().isConnected(device.getBleDevice())) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        if (position > bleDeviceList.size())
            return null;
        return bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("DistributorAdapter","getView!");
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.distributor_service_ble_list_item, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.img_blue = (ImageView) convertView.findViewById(R.id.img_blue);
            holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
            holder.txt_mac = (TextView) convertView.findViewById(R.id.txt_mac);
            holder.txt_rssi = (TextView) convertView.findViewById(R.id.txt_rssi);
            holder.tv_update_status = convertView.findViewById(R.id.tv_update_status);
            holder.layout_idle = (LinearLayout) convertView.findViewById(R.id.layout_idle);
            //holder.ll_ble_connect = (LinearLayout) convertView.findViewById(R.id.ll_ble_connect);
            holder.layout_connected = (LinearLayout) convertView.findViewById(R.id.layout_connected);
            holder.btn_disconnect = (Button) convertView.findViewById(R.id.btn_disconnect);
            holder.btn_connect = (Button) convertView.findViewById(R.id.btn_connect);
            holder.btn_update = convertView.findViewById(R.id.btn_update);
        }

        final LocalBleDevice bleDevice = (LocalBleDevice) getItem(position);
        if (bleDevice != null) {
            boolean isConnected = BleManager.getInstance().isConnected(bleDevice.getBleDevice());
            String name = bleDevice.getBleDevice().getName();
            String mac = bleDevice.getBleDevice().getMac();
            int rssi = bleDevice.getBleDevice().getRssi();
            String versionName = bleDevice.getDevideVersion();
            holder.txt_name.setText(name);
            holder.txt_mac.setText(mac);
            holder.txt_rssi.setText(String.valueOf(rssi));
            if(versionName != null && !versionName.isEmpty()){
                holder.tv_update_status.setText("current model: " + versionName);
            }
            if (isConnected) {
                holder.img_blue.setImageResource(R.mipmap.ic_blue_connected);
                holder.txt_name.setTextColor(0xFF1DE9B6);
                holder.txt_mac.setTextColor(0xFF1DE9B6);
                holder.tv_update_status.setTextColor(0xFF1DE9B6);
                holder.layout_idle.setVisibility(View.GONE);
                holder.layout_connected.setVisibility(View.VISIBLE);
                holder.tv_update_status.setVisibility(View.VISIBLE);
                holder.btn_update.setVisibility(View.VISIBLE);
            } else {
                holder.img_blue.setImageResource(R.mipmap.ic_blue_remote);
                holder.txt_name.setTextColor(0xFF000000);
                holder.txt_mac.setTextColor(0xFF000000);
                holder.tv_update_status.setTextColor(0xFF000000);
                holder.layout_idle.setVisibility(View.VISIBLE);
                holder.layout_connected.setVisibility(View.GONE);
                holder.tv_update_status.setVisibility(View.GONE);
                holder.btn_update.setVisibility(View.GONE);
            }
        }

        holder.btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onConnect(bleDevice.getBleDevice());
                }
            }
        });

        holder.btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onDisConnect(bleDevice.getBleDevice());
                }
            }
        });

        holder.btn_update.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.updateDevice(bleDevice.getBleDevice());
            }
        }
    });
//        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mListener != null) {
//                    mListener.onDetail(bleDevice);
//                }
//            }
//        });
        return convertView;
    }

    class ViewHolder {
        ImageView img_blue;
        TextView txt_name;
        TextView txt_mac;
        TextView txt_rssi;
        LinearLayout layout_idle;
        LinearLayout ll_ble_connect;
        LinearLayout layout_connected;
        Button btn_disconnect;
        Button btn_connect;
        Button btn_update;
        TextView tv_update_status;
    }

    public interface OnDeviceClickListener {
        void onConnect(BleDevice bleDevice);

        void onDisConnect(BleDevice bleDevice);

        void updateDevice(BleDevice bleDevice);

        void onDetail(BleDevice bleDevice);
    }

    private OnDeviceClickListener mListener;

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.mListener = listener;
    }
}
