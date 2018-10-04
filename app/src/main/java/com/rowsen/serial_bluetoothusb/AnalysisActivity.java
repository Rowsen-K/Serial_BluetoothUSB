package com.rowsen.serial_bluetoothusb;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import androidx.appcompat.app.AppCompatActivity;

//此类用于进行各种语句的解析结果,按语句种类分
//此处先实现一个GGA的解析
public class AnalysisActivity extends AppCompatActivity {
    BluetoothConnect connect;
    BufferedInputStream in;
    Button gga;
    TextView content;
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
        content = findViewById(R.id.content);
        if (MyApp.connect != null) {
            connect = MyApp.connect;
            in = connect.in;
        } else {
            Toast.makeText(this, "请先建立蓝牙连接再进行数据模拟的操作!", Toast.LENGTH_SHORT).show();
            finish();
        }
        file = MyApp.file;
        try {
            read = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        filelength = file.length();
        final Thread one = new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    long newlength = file.length();
                    if (newlength > filelength) {
                        byte[] buf = new byte[(int) (newlength - filelength)];
                        try {
                            read.seek(filelength);
                            read.read(buf);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String s = new String(buf);
                        // System.out.println("这是解析时拿到的字符:" + s);
                        int start = s.lastIndexOf("$GPGGA");
                        if (start != -1) {
                            int end = s.indexOf("\n", start);
                            if (end != -1) s = s.substring(start, end);
                            if (s.contains("*")) flag = true;
                            //  System.out.println("=============flag:" + flag + "------------" + s);
                        }
                        if (flag) {
                            final String[] result = s.split(",");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    content.setText("时间:" + result[1] + "\n" + "纬度:" + result[2] + "\n" + "经度:" + result[4] + "\n");
                                }
                            });
                            filelength = newlength;
                            flag = false;
                        }
                    }
                }
            }
        };

        gga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                one.start();
                gga.setEnabled(false);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (connect.readflag) {
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                content.append("蓝牙连接已断开！");
                                Toast.makeText(AnalysisActivity.this, "蓝牙连接已断开！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }.start();
            }
        });
    }
}
