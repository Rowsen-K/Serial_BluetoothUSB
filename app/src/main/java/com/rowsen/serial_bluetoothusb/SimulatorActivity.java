package com.rowsen.serial_bluetoothusb;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import static java.lang.Thread.sleep;

//此处应该实现模拟多种设备的语句,预置进去一些设备
//当前先实现GPS和自编辑语句的发送

public class SimulatorActivity extends AppCompatActivity {
    Button GPS;
    Button edit;
    EditText data;
    Button send;
    BluetoothConnect connect;
    BufferedOutputStream out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulator);
        GPS = findViewById(R.id.GPS);
        edit = findViewById(R.id.Edit);
        data = findViewById(R.id.data);
        send = findViewById(R.id.send);
        if (MyApp.connect != null) {
            connect = MyApp.connect;
            out = connect.out;
        } else {
            Toast.makeText(this, "请先建立蓝牙连接再进行数据模拟的操作!", Toast.LENGTH_SHORT).show();
            finish();
        }
        GPS.setOnClickListener(new View.OnClickListener() {
            boolean flag = false;
            String s = MyApp.GPS_DATA;
            @Override
            public void onClick(View v) {
                flag = !flag;
               Thread sim = new Thread(){
                   @Override
                   public void run() {
                       super.run();
                       while (flag) {
                           try {
                               out.write(s.getBytes());
                               out.flush();
                               sleep(1000);
                           } catch (IOException e) {
                               e.printStackTrace();
                               System.out.println("蓝牙数据写出失败!");
                               Toast.makeText(SimulatorActivity.this, "蓝牙数据写出失败!", Toast.LENGTH_SHORT).show();
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                           }
                       }
                   }
               } ;
               if(flag) {
                   Toast.makeText(SimulatorActivity.this,"GPS数据模拟开始!" ,Toast.LENGTH_SHORT ).show();
                   sim.start();
               }
               else Toast.makeText(SimulatorActivity.this,"GPS数据模拟关闭!" , Toast.LENGTH_SHORT).show();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.setVisibility(View.VISIBLE);
                send.setVisibility(View.VISIBLE);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    out.write(edit.getText().toString().getBytes());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("蓝牙数据写出失败!");
                    Toast.makeText(SimulatorActivity.this, "蓝牙数据写出失败!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
