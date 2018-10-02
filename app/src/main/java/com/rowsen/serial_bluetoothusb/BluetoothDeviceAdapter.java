package com.rowsen.serial_bluetoothusb;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Set;

public class BluetoothDeviceAdapter extends BaseAdapter {
    Context context;
    BluetoothDevice[] list;

    BluetoothDeviceAdapter(Context context, Set<BluetoothDevice> set) {
        this.context = context;
        list = new BluetoothDevice[set.size()];
        set.toArray(list);
    }

    @Override
    public int getCount() {
        return list.length;
    }

    @Override
    public Object getItem(int i) {
        return list[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v;
        if (view == null)
            v = View.inflate(context, R.layout.layout_device_item, null);
        else v = view;
        BluetoothDevice device = list[i];
        TextView name = v.findViewById(R.id.name);
        TextView state = v.findViewById(R.id.state);
        TextView mac = v.findViewById(R.id.mac);
        name.setText(i + "、" + "设备名:" + device.getName());
        state.setText("状态:已配对");
        mac.setText("硬件MAC地址:"+device.getAddress());
        return v;
    }
}
