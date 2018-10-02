package com.rowsen.serial_bluetoothusb;

import android.app.Application;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MyApp extends Application {
    public static UUID SerialPortService = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static boolean completeFlag = false;
    public static String GPS_DATA =
            "$GPGGA,073141.00,2237.56087,N,11401.59486,E,1,09,0.098,123.7,M,-2.5,M,,*42\n" +
                    "$GPGSV,3,1,12,09,25,041,31,12,39,084,40,14,44,313,33,15,11,085,*7D\n" +
                    "$GPGSV,3,2,12,18,65,139,30,21,15,189,,22,68,314,42,25,40,143,35*73\n" +
                    "$GPGSV,3,3,12,27,11,044,,31,24,232,30,42,45,137,,44,27,246,*7C\n" +
                    "$GPGGA,020255.00,3037.9173189,N,11417.5550436,E,1,7,1.1,41.562,M,-11.727,M,,*4D\n" +
                    "$GPGLL,2237.56087,N,11401.59486,E,073141.00,A,A*62\n" +
                    "$GPGSV,3,1,12,09,25,041,30,12,39,084,40,14,44,313,33,15,11,085,*7C\n" +
                    "$GPGSV,3,2,12,18,65,139,30,21,15,189,,22,68,314,42,25,40,143,35*73\n" +
                    "$GPGSV,3,3,12,27,11,044,,31,24,232,30,42,45,137,,44,27,246,*7C\n" +
                    "$GPGGA,020256.00,3037.9173509,N,11417.5550690,E,1,7,1.1,41.689,M,-11.727,M,,*4A\n" +
                    "$GPGSA,A,3,02,04,05,29,08,15,09,10,26,,,,1.68,0.98,1.37*01\n" +
                    "$GPGSV,3,1,12,09,25,041,29,12,39,084,39,14,44,313,33,15,11,085,*7A\n" +
                    "$GPGSV,3,2,12,18,65,139,30,21,15,189,,22,68,314,42,25,40,143,35*73\n" +
                    "$GPGSV,3,3,12,27,11,044,,31,24,232,30,42,45,137,,44,27,246,*7C\n" +
                    "$GPGGA,020257.00,3037.9173806,N,11417.5550931,E,1,7,1.1,41.814,M,-11.727,M,,*47\n" +
                    "$GPRMC,020254.00,A,3037.9173254,N,11417.5550334,E,0.0,0.0,270512,4.5,W,*2F\n" +
                    "$GPVTG,,T,,M,0.16,N,0.029,K,A*2F\n";
    public static BluetoothConnect connect;
    public static File file;
    public static BufferedOutputStream out;

    @Override
    public void onCreate() {
        super.onCreate();
        file = new File(getCacheDir(), "GGAtemp.txt");
        System.out.println("文件的保存路径:" + file.getAbsolutePath());
        try {
            file.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(file, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
