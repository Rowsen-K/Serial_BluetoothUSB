package com.rowsen.serial_bluetoothusb;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;

public class UsbActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.an.USB_PERMISSION";
    TextView test;
    Button r;
    Button w;
    UsbDeviceConnection connection;
    UsbSerialPort port;
    SerialInputOutputManager sio;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        test = findViewById(R.id.test);
        r = findViewById(R.id.read);
        w = findViewById(R.id.write);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                sio.writeAsync("Hello!\n".getBytes());
                sendEmptyMessageDelayed(0,3000 );
            }
        };

        r.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                //   read.start();
                SerialInputOutputManager.Listener lis = new SerialInputOutputManager.Listener() {
                    @Override
                    public void onNewData(final byte[] data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                test.append(new String(data));
                            }
                        });

                    }

                    @Override
                    public void onRunError(final Exception e) {
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               test.append("发生了读取异常!\n");
                               test.append(e.getMessage());
                           }
                       });
                    }
                };
                sio = new SerialInputOutputManager(port, lis);
                test.append("数据读取已开启!\n");
                new Thread(sio).start();
                r.setEnabled(false);
            }
        });
        w.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                //   write.start();
                handler.sendEmptyMessageDelayed(0, 3000);
                test.append("数据写出已开启!\n");
                w.setEnabled(false);
            }
        });


        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty())
        {
            test.append("没有可用的串口设备!");
            return;
        }
        // Open a connection to the first available driver.
        //这里的可用驱动集合包含了所有已带驱动所支持的usb串口,如果你的硬件只有一个usb口那么默认可以选一,否则应该查看清单匹配
        UsbSerialDriver driver = availableDrivers.get(0);
        connection = manager.openDevice(driver.getDevice());
        if (connection == null)
        {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            test.append("\n没有USB使用权限!!");
            return;
        }

// Read some data! Most have just one port (port 0).
        //这里应该要加入串口设置模式.借用蓝牙那边的界面和sharedPreferences数据
        port = driver.getPorts().get(0);
        try
        {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            test.append("串口已打开!\n");
        } catch (IOException e)
        {
            test.append("打开串口失败!请检查连接!\n");
            // Deal with error.
            try {
                port.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
