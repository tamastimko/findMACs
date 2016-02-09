package com.example.tamas.findmacs;

import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Tamas on 2016.02.07..
 */
public class ScanStoppedBroadcastReceiver extends BroadcastReceiver {

    MainActivity mainActivity;


    @Override
    public void onReceive(Context context, Intent intent)
    {
        /*int min = -10000;
        ArrayList<ScanResult> devices = mainActivity.bleDevices;
        for(int i = 0; i<devices.size();i++)
        {
            if(devices.get(i).getRssi() > min)
            {
                min = devices.get(i).getRssi();
            }
        }
        Bundle extra = intent.getExtras();


        Toast.makeText(context,"Scan stopped. Best RSSI: " + min, Toast.LENGTH_LONG).show();*/
    }
}
