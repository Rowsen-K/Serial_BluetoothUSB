package com.rowsen.serial_bluetoothusb;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class UsbActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.an.USB_PERMISSION";
    TextView test;
    Button r;
    Button w;
    Thread read;
    Thread write;
    UsbDeviceConnection connection;
    UsbSerialPort sPort;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        test = findViewById(R.id.test);
        r = findViewById(R.id.read);
        w = findViewById(R.id.write);
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                read.start();
                r.setEnabled(false);
            }
        });
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write.start();
                w.setEnabled(false);
            }
        });
        // 获取系统服务得到UsbManager实例
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //查找所有插入的设备
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            test.setText("没有可用的USB设备！！！");
            System.out.println("====没有可用的USB设备！！！");
            return;
        } else {
            test.setText("可用的USB个数：" + availableDrivers.size() + "\n" + availableDrivers);
            System.out.println("======可用的USB个数：" + availableDrivers.size());
            System.out.println(availableDrivers);
        }
        // 打开设备，建立通信连接
        UsbSerialDriver driver = availableDrivers.get(0);
        sPort = driver.getPorts().get(0);
        manager.requestPermission(driver.getDevice(), PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
       if(manager.hasPermission(driver.getDevice())){
           //connection = manager.openDevice(driver.getDevice());
           connection = manager.openDevice(sPort.getDriver().getDevice());
           test.append("获取了USB权限！！！");
       }
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            test.append("连接USB设备失败！\n");
            return;
        }

        //打开端口，设置端口参数，读取数据
       // final UsbSerialPort port = driver.getPorts().get(0);
        try {
            sPort.open(connection);
            //四个参数分别是：波特率，数据位，停止位，校验位
            sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            final byte buffer[] = new byte[16];
            read = new Thread() {
                @Override
                public void run() {
                    super.run();
                    int numBytesRead = 0;
                    while (true) {
                        try {
                            numBytesRead = sPort.read(buffer, 1000);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("====记录：", "Read " + numBytesRead + " bytes.");
                        if (numBytesRead != -1)
                            test.append("=============收到的字符：" + new String(buffer, 0, numBytesRead));
                    }
                }
            };
            write = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (true) {
                        try {
                            sPort.write("Hello Rowsen".getBytes(), 1000);
                            sleep(5000);
                        } catch (IOException e) {
                            e.printStackTrace();
                            test.append("写出失败");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                sPort.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
