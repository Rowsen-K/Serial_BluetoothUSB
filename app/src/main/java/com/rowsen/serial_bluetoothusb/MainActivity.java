package com.rowsen.serial_bluetoothusb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button bluetooth;
    Button usb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        bluetooth = findViewById(R.id.bluetooth);
        usb = findViewById(R.id.usb);
        bluetooth.setOnClickListener(this);
        usb.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bluetooth:
                startActivity(new Intent(this, BluetoothActivity.class));
                break;
            case R.id.usb:
                startActivity(new Intent(this, UsbActivity.class));
                break;
        }
    }
}
