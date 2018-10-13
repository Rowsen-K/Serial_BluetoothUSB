package com.rowsen.serial_bluetoothusb;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

//目前还未实现修改esp8266的串口波特率，实现比较简单，
// 测试时可能会遇到找不到波特率不能通讯的情况，
// 所以先完成其他部分的编写，最后来补全这一块

public class UsbActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.an.USB_PERMISSION";
    ScrollView scroll;
    LinearLayout send_select;
    LinearLayout send_manual;
    Button auto;
    Button manual;
    Button exit;
    EditText sendcontent;
    Spinner spinner;
    String[] list = {"115200","4800","9600","38400","74880"};
    SerialInputOutputManager sio;
    SerialInputOutputManager.Listener lis;
    StringBuilder sb;
    boolean flag = true;//一键透传时的AT命令正常与否的标记
    boolean mark = true;//一键透传和退出透传的标记

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
        spinner = findViewById(R.id.spinner);
        send_select = findViewById(R.id.send_select);
        send_manual = findViewById(R.id.manual_send);
        manual = findViewById(R.id.manual);
        auto = findViewById(R.id.auto);
        exit = findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sio.writeAsync("+++".getBytes());
            }
        });
        sb = new StringBuilder();
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_select.setVisibility(View.GONE);
                send_manual.setVisibility(View.VISIBLE);
            }
        });
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auto.setEnabled(false);
                    new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        //第一步测试AT是否正常，
                        command("AT\r\n", 1);
                        //第二步配置SAT模式：AT+CWMODE=1
                        if (flag)
                            command("AT+CWMODE=1\r\n", 2);
                        //第三步配置连接的AP并保存（DEF命令):AT+CWJAP_DEF="SSID","PSW"
                        if (flag)
                            command("AT+CWJAP_DEF=\"小米手机\",\"rzcs0608\"\r\n", 3);
                        //第四步连接到手机服务端：AT+CIPSTART="TCP","SERVER_IP",PORT
                        if (flag)
                            command("AT+CIPSTART=\"TCP\",\"192.168.43.1\",12345\r\n", 4);
                        //第五步开启透传：AT+CIPMODE=1
                        if (flag)
                            command("AT+CIPMODE=1\r\n", 5);
                        //第六步开启透传发送：AT+CIPSEND
                        if(flag)
                            command("AT+CIPSEND\r\n",6);
                       if(flag) {
                           System.out.println("一键透传开启完毕！");
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   test.append("\n一键透传开启完毕");
                                   scroll.smoothScrollTo(0, test.getBottom());
                                   send_select.setVisibility(View.GONE);
                                   send_manual.setVisibility(View.VISIBLE);
                               }
                           });
                       }
                    }
                }.start();
            }
        });
        scroll = findViewById(R.id.scroll);
        sendcontent = findViewById(R.id.sendcontent);

        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // read.start();
                sio.setListener(lis);
                new Thread(sio).start();
                r.setEnabled(false);
                r.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);
                spinner.setBackgroundColor(getResources().getColor(R.color.black));
                ArrayAdapter adp = new ArrayAdapter<String>(getApplication(),android.R.layout.simple_spinner_item,list);
                adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adp);
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tx = view.findViewById(android.R.id.text1);
                System.out.println("spinner============="+tx.getText());
                sio.writeAsync(("AT+UART_CUR="+tx.getText()+",8,1,0,0\r\n").getBytes());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write.start();
                //
                sio.writeAsync((sendcontent.getText() + "\r\n").getBytes());
                System.out.println("发送的数据:" + sendcontent.getText());

                // w.setEnabled(false);
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
        if (manager.hasPermission(driver.getDevice())) {
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
            sPort.setParameters(Integer.valueOf(MyApp.sp.getString("波特率", "115200")), Integer.valueOf(MyApp.sp.getString("数据位", "8")), Integer.valueOf(MyApp.sp.getString("停止位", "1")), MyApp.sp.getInt("PARITY", UsbSerialPort.PARITY_NONE));
            // sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            test.append("\n当前串口的设置：" + MyApp.sp.getAll() + "\n");
            sio = new SerialInputOutputManager(sPort);
            lis = new SerialInputOutputManager.Listener() {
                @Override
                public void onNewData(final byte[] data) {
                    sb.append(new String(data));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            test.append(new String(data));
                            scroll.smoothScrollTo(0, test.getBottom());
                        }
                    });
                }

                @Override
                public void onRunError(Exception e) {

                }
            };

        } catch (IOException e) {
            e.printStackTrace();
            test.append("\n初始化串口失败！\n");
        }

    }

    @Override
    public void onBackPressed() {
        try {
            sPort.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }


    void command(String cmd, int step) {
        System.out.println("进入第" + step + "步的操作");
        sb.setLength(0);
        sio.writeAsync((cmd + "\r\n").getBytes());
        while (flag) {
            if (sb.toString().contains("OK")) {
                flag = true;
                System.out.println("第" + step + "步拿到了ok标记");
                break;
            }
            if (sb.toString().contains("ERROR")) {
                flag = false;
                break;
            } else System.out.println("第" + step + "步什么都没获取到，等待读取！");
        }
        System.out.println("第" + step + "步的完成！");
    }
}


