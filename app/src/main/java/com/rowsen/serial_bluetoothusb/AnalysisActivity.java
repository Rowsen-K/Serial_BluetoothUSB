package com.rowsen.serial_bluetoothusb;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.provider.PositionProvider;
import net.sf.marineapi.provider.event.PositionEvent;
import net.sf.marineapi.provider.event.PositionListener;
import net.sf.marineapi.provider.event.ProviderListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import androidx.appcompat.app.AppCompatActivity;

//此类用于进行各种语句的解析结果,按语句种类分
//此处先实现一个GGA的解析
public class AnalysisActivity extends AppCompatActivity {
    BluetoothConnect connect;
    InputStream in;
    Button gga;
    Button get;
    TextView content;
    Handler handler;
    File file;
    RandomAccessFile read;
    boolean flag = false;
    long filelength;
    SentenceReader reader;
    PositionProvider pos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        gga = findViewById(R.id.gga);
        get = findViewById(R.id.get);
        content = findViewById(R.id.content);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                content.append((String)msg.obj+"\n");
            }
        };
        if (MyApp.connect != null) {
            connect = MyApp.connect;
            in = connect.in;
        } else {
            Toast.makeText(this, "请先建立蓝牙连接再进行数据模拟的操作!", Toast.LENGTH_SHORT).show();
            finish();
        }
        reader = new SentenceReader(in);
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.connect.readflag = true;
                try {
                    MyApp.connect.read(handler,4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        gga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pos = new PositionProvider(reader);
                pos.addListener(new ProviderListener<PositionEvent>() {
                    @Override
                    public void providerUpdate(final PositionEvent positionEvent) {
                        Log.e("数据：",positionEvent.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                              //  Toast.makeText(AnalysisActivity.this,"数据更新",Toast.LENGTH_SHORT).show();
                                content.append("时间："+positionEvent.getDate()+"\n");
                                content.append("位置："+positionEvent.getPosition()+"\n");
                            }
                        });


                    }
                });
                reader.start();
                gga.setEnabled(false);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (MyApp.connect.socket.isConnected()) {
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
