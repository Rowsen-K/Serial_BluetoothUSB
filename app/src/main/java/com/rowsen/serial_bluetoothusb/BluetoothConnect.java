package com.rowsen.serial_bluetoothusb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public class BluetoothConnect extends Thread {
    Handler handler;
    View v;
    Activity activity;
    BluetoothAdapter bla;
    BluetoothDevice device;
    BluetoothSocket socket;
    public BufferedInputStream in;
    public BufferedOutputStream out;
    boolean readflag = true;
    BufferedOutputStream write;

    BluetoothConnect(Activity activity, View v, Handler handler, BluetoothAdapter bla, BluetoothDevice device) {
        this.activity = activity;
        this.v = v;
        this.handler = handler;
        this.bla = bla;
        this.device = device;
    }

    public void close() {
        if (socket != null) {
            try {
                if (socket.isConnected()) {
                    readflag = false;
                    socket.close();
                    MyApp.completeFlag = false;
                }
                System.out.println("成功断开蓝牙连接!");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("断开蓝牙连接失败!!!!");
            }
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            socket = device.createRfcommSocketToServiceRecord(MyApp.SerialPortService);
            bla.cancelDiscovery();
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                handler.sendEmptyMessage(0);
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        while (!socket.isConnected()) {
        }
        System.out.println("蓝牙连接成功!");
        MyApp.connect = this;//一旦建立了蓝牙连接之后就将他保存到APP全局类中供全局使用
        handler.sendEmptyMessage(3);//断开连接按钮被激活,数据分析,数据模拟按钮被激活
        final TextView state = v.findViewById(R.id.state);
        activity.runOnUiThread(new Runnable() {
            @SuppressLint("NewApi")
            @Override
            public void run() {
                state.append("|" + "蓝牙连接成功!");
                state.setTextColor(activity.getBaseContext().getResources().getColor(R.color.colorPrimary));
            }
        });
        try {
            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            MyApp.completeFlag = true;
            readflag = true;
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void read(Handler handler) throws IOException {
        byte[] buf = new byte[128];
        int n = 0;
        write = MyApp.out;
        while (readflag) {
            if (in != null) n = in.read(buf);
            if (n != -1 && n != 0) {
                String s = new String(buf, 0, n);
                //System.out.println("接收到的字符:" + s);
                write.write(s.getBytes());
                write.flush();
                Message msg = new Message();
                msg.what = 1;
                msg.obj = s;
                handler.sendMessage(msg);
            }
        }
    }
}
