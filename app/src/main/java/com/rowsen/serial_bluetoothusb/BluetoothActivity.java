package com.rowsen.serial_bluetoothusb;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

public class BluetoothActivity extends AppCompatActivity {
  //  TextView receive;
    Button analysis;
    Button stop;
    Button send;
    ListView list;
    BluetoothAdapter bla;
    Set<BluetoothDevice> devices;
    BluetoothDeviceAdapter bda;
    Handler handler;
    BluetoothConnect connect;

    AutoScrollTextView receive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        //receive = findViewById(R.id.receive);
        analysis = findViewById(R.id.analysis);
        stop = findViewById(R.id.stop);
        send = findViewById(R.id.send);
        list = findViewById(R.id.list);

        receive = findViewById(R.id.scroll);
        receive.setAutoSplitEnabled(true);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect.close();
                stop.setEnabled(false);
            }
        });
        analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect.readflag = false;
                startActivity(new Intent(BluetoothActivity.this,AnalysisActivity.class));
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BluetoothActivity.this,SimulatorActivity.class));
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        Toast.makeText(BluetoothActivity.this, "请确认对方蓝牙串口已开启后,再次点击设备进行连接!", Toast.LENGTH_LONG).show();
                        break;
                    case 1:

                        synchronized (this) {
                            //添加完文本后需要滚动TextView到最后一行
                            int height = receive.getLineCount() * receive.getLineHeight();
                            int scale = receive.getHeight() - receive.getLineHeight() - 20;
                            if (height > scale)
                                receive.scrollTo(0, height - scale);
                            //receive.clearComposingText();
                            receive.append(receive.autoSplitText(receive,(String) msg.obj));
                        }

                        // scroll.appendText((String)msg.obj);
                        break;
                    case 3://连接成功
                        stop.setEnabled(true);
                        analysis.setEnabled(true);
                        send.setEnabled(true);
                        break;
                }
            }
        };
        bla = BluetoothAdapter.getDefaultAdapter();
        if (bla.isEnabled()) {
            devices = bla.getBondedDevices();
            bda = new BluetoothDeviceAdapter(this, devices);
            list.setAdapter(bda);
        } else {
            Toast.makeText(this, "蓝牙正在开启请稍等!", Toast.LENGTH_SHORT);
            bla.enable();
            bla.startDiscovery();
            finish();
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = bda.list[i];
                connect = new BluetoothConnect(BluetoothActivity.this, view, handler, bla, device);
                connect.start();
                final TextView state= view.findViewById(R.id.state);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        while(!MyApp.completeFlag){}
                        try {
                            connect.read(handler);
                        } catch (IOException e) {
                            e.printStackTrace();
                                connect.close();
                                state.setText("已配对|" + "蓝牙已断开!");
                                state.setTextColor(getBaseContext().getResources().getColor(R.color.colorAccent));
                        }
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connect != null) connect.close();
    }

    //TextView可能由于全半角字符的问题导致换行参差不齐,这里是全半角字符的转换函数
    public String transport(String inputStr) {
        char arr[] = inputStr.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == ' ') {
                arr[i] = '\u3000';
            } else if (arr[i] < '\177') {
                arr[i] = (char) (arr[i] + 65248);
            }
        }
        return new String(arr);
    }
}
