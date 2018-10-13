package com.rowsen.serial_bluetoothusb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button set;
    Button bluetooth;
    Button usb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        set = findViewById(R.id.set);
        bluetooth = findViewById(R.id.bluetooth);
        usb = findViewById(R.id.usb);
        set.setOnClickListener(this);
        bluetooth.setOnClickListener(this);
        usb.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set:
                startActivity(new Intent(this, SetActivity.class));
                break;
            case R.id.bluetooth:
                startActivity(new Intent(this, BluetoothActivity.class));
                break;
            case R.id.usb:
                startActivity(new Intent(this, UsbActivity.class));
                break;
        }
    }
}
