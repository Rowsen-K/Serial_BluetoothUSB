package com.rowsen.serial_bluetoothusb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

//此类用于进行各种语句的解析结果,按语句种类分
//此处先实现一个GGA的解析
public class AnalysisActivity extends AppCompatActivity {
    BluetoothConnect connect;
    BufferedInputStream in;
    Button gga;
    Handler handler;
    File file;
    RandomAccessFile read;
    boolean flag = false;
    long filelength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        gga = findViewById(R.id.gga);
        if (MyApp.connect != null) {
            connect = MyApp.connect;
            in = connect.in;
        } else {
            Toast.makeText(this, "请先建立蓝牙连接再进行数据模拟的操作!", Toast.LENGTH_SHORT).show();
            finish();
        }
        file = MyApp.file;
        try {
            read = new RandomAccessFile(file,"r" );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        filelength = file.length();
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    long newlength = file.length();
                    synchronized (file) {
                        if (newlength > filelength) {
                            byte[] buf = new byte[(int) (newlength - filelength)];
                            try {
                                read.seek(filelength);
                                read.read(buf);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String s = new String(buf);
                            System.out.println("这是解析时拿到的字符:" + s);
                            int start = s.lastIndexOf("$GPGGA");
                            if (start != -1) {
                                int end = s.indexOf("\n", start);
                                if (end != -1) s = s.substring(start, end);
                                if (s.contains("*")) flag = true;
                                System.out.println("=============flag:" + flag + "------------" + s);
                            }
                            if (flag) {
                                String[] result = s.split(",");
                                gga.setText("时间:" + result[1] + "\n" + "纬度:" + result[2] + "\n" + "经度:" + result[4] + "\n");
                                filelength = newlength;
                            }
                        }
                    }
                }
            }
        };
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //先简单的提取数据,不做校验验证数据的完整性和正确性,是否需要舍弃


            }
        };
        gga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    connect.readflag = true;
                    connect.read(handler);
                } catch (IOException e) {
                    e.printStackTrace();
                    connect.close();
                }
            }
        });
    }
}
