package com.example.tamas.findmacs;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.os.Handler;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 3000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private String firstFoundDeviceAddress;

    private int secondOnScanResultCounter;
    int scanCounter;
    private boolean scanRunning;

    private ScheduledExecutorService scheduledScanExecutor;

    public ArrayList<ScanResult> bleDevices;
    ArrayList<ScanResult> temp;
    ArrayAdapter mArrayAdapter;



    Button btn_startScan;
    Button btn_disconnect;
    ListView devicesListView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scheduledScanExecutor = Executors.newScheduledThreadPool(5);

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mHandler = new Handler();

        bleDevices = new ArrayList<>();
        temp = new ArrayList<>();
        scanRunning = false;

        btn_startScan = (Button) findViewById(R.id.btn_scan_start);
        btn_disconnect = (Button) findViewById(R.id.btn_scan_stop);
        devicesListView = (ListView) findViewById(R.id.listView1);


        secondOnScanResultCounter = 1;
        scanCounter = 0;



        mArrayAdapter = new ArrayAdapter(MainActivity.this, R.layout.simple_list_item_1);
        devicesListView.setAdapter(mArrayAdapter);

        scheduledScanExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i("TIMER", "Disconnecting from device");
                if(scanCounter != 0)
                {
                    disconnectFromDevice();
                }
                Log.i("TIMER", "10 seconds have passed! SCANNING!");
                myStartScan();
                scanCounter++;
                if(scanCounter > 2000000000)
                {
                    scanCounter = 1;
                }


                //Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                //Log.i("BONDED","Number: "+ bondedDevices.size());
            }
        },0,10, TimeUnit.SECONDS);

        btn_startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartScan();
                /*mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .build();
                filters = new ArrayList<ScanFilter>();
                scanLeDevice(true);
                Log.v("Scan: ", "Request start");*/
            }
        });

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectFromDevice();
            }
        });




        //Listában amelyikre kattintás történik, ahhoz kapcsolódik
        //Cserélve lesz RSSI alapon történő kapcsolódásra.
        /*devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice toConnect = bleDevices.get(position).getDevice();
                connectToDevice(toConnect);
                Log.v("ItemClick position: " + position, "Try to connect to " + toConnect.getAddress());
            }
        });*/


    }

    private void disconnectFromDevice()
    {
        int headsetProfile = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int healthProfile = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        int a2dpProfile = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        //STATE_DISCONNECTED = 0, STATE_CONNECTING = 1, STATE_CONNECTED = 2, STATE_DISCONNECTING = 3

        if(headsetProfile == 2 || headsetProfile == 1 || healthProfile == 2 || healthProfile == 1 || a2dpProfile == 2 || a2dpProfile == 1)
        {
            if(mGatt != null)
            {
                mGatt.close();
                mGatt = null;
            }
            scanLeDevice(false);
            Log.v("Scan: ", "Request stop");
            //bleDevices.clear();
            //devicesListView.setAdapter(null);
            //mArrayAdapter.clear();
        }
        else
        {
            Log.i("DISCONNECT","There are no connected devices");
        }

    }

    private void myStartScan()
    {
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        filters = new ArrayList<ScanFilter>();
        scanLeDevice(true);
        Log.v("Scan: ", "Autostart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);

        }
        else
        {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();
            filters = new ArrayList<ScanFilter>();
            //scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
        {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mGatt == null)
        {
            return;
        }
        else
        {
            mGatt.close();
            mGatt = null;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == Activity.RESULT_CANCELED)
            {
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void BroadcastScanStop()
    {
        Intent scanStoppedIntent = new Intent();
        scanStoppedIntent.setAction("BT_SCAN_STOPPED");
        scanStoppedIntent.putExtra("Logical", scanRunning);
        sendBroadcast(scanStoppedIntent);
    }

    private void scanLeDevice(final boolean enable)
    {
        if(enable)
        {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLEScanner.stopScan(mScanCallback);
                    scanRunning = false;
                    Log.i("Scan: ", "Stopped");
                   // BroadcastScanStop();
                    if(bleDevices.size() != 0) {
                        int min = -10000;
                        ScanResult bestScanResult = bleDevices.get(0);
                        for (int i = 0; i < bleDevices.size(); i++) {
                            if (bleDevices.get(i).getRssi() > min) {
                                min = bleDevices.get(i).getRssi();
                                bestScanResult = bleDevices.get(i);
                            }
                        }
                        Log.d("MIN", ": " + min);
                        connectToDevice(bestScanResult.getDevice());

                    }

                }
            },SCAN_PERIOD);
            mLEScanner.startScan(filters, settings, mScanCallback);
            scanRunning = true;
            Log.i("Scan: ", "Started");

        }
        else {
            mLEScanner.stopScan(mScanCallback);
            scanRunning = false;
            Log.i("Scan: ", "Stopped");
            //BroadcastScanStop();
        }
    }



    private void Unique(ScanResult result)
    {

        if(bleDevices.isEmpty())
        {
            temp.add(result);
        }
        else
        {

            int size = temp.size();
            for(int j = 0; j<size; j++)
            {
                int i = 0;
                while (!bleDevices.get(i).getDevice().getAddress().equals(result.getDevice().getAddress()) && i < size)
                {
                    temp.add(result);
                    i++;
                }
            }
        }
        ArrayList<ScanResult> temp2 = removeSameObjects(temp);
        bleDevices = temp2;
    }

    private ArrayList<ScanResult> removeSameObjects(ArrayList<ScanResult> scanResult)
    {
        for(int i = 0; i<scanResult.size()-1; i++)
        {
            for (int j = 1; j<scanResult.size(); j++)
            {
                if(scanResult.get(i).getDevice().getAddress().equals(scanResult.get(j).getDevice().getAddress()))
                {
                    scanResult.remove(j);
                }
            }
        }
        return scanResult;
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
            public void onScanResult(int callbackType, ScanResult result) {

                Log.i("callBackType", String.valueOf(callbackType));
                Log.i("result", result.toString());
                //BluetoothDevice btDevice = result.getDevice();
                //connectToDevice(btDevice);

                    /*if(bleDevices.isEmpty())
                    {
                        bleDevices.add(result);
                    }
                    else
                    {
                        for (int i = 0; i<bleDevices.size(); i++)
                        {
                            if(!bleDevices.get(i).getDevice().getAddress().equals(result.getDevice().getAddress()))
                            {
                                bleDevices.add(result);
                            }
                        }
                    }*/

                Unique(result);
                mArrayAdapter.clear();
                for(ScanResult device : bleDevices)
                {
                    int secs = (int) (((double) device.getTimestampNanos() / (double) 1000000000) * 100);
                    mArrayAdapter.add("Address: " + device.getDevice().getAddress() + "\n" + "RSSI: " + device.getRssi() + "\n" + "TxPowerLevel: " + device.getScanRecord().getTxPowerLevel());
                }
                //devicesListView.setAdapter(mArrayAdapter); korábbra tettem
                mArrayAdapter.notifyDataSetChanged();




        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult sr : results)
            {
                Log.i("ScanResult - Results",sr.toString());
                bleDevices.add(sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan failed", "Error Code" + errorCode);
        }
    };

    public void connectToDevice(BluetoothDevice device) {
        if(mGatt == null)
        {
            mGatt = device.connectGatt(this,false, gattCallback);
            Log.v("Connected to: ", device.getAddress());
            scanLeDevice(false);

        }

    }

    private final BluetoothGattCallback  gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState)
            {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("GattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback","STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServiceDiscovered",services.toString());
            gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead",characteristic.toString());
            gatt.disconnect();
        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
