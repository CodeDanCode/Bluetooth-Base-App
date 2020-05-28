package com.example.bluetoothtest1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView mBluetoothStatus;
    private static final String TAG = "MainActivity";
    private BluetoothAdapter BlueAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private Button BTconnect,BTdisconnect,Send;
    private ListView listView;
    private ArrayAdapter<String> mBTArrayAdapter;
    private Handler handler;
    private ConnectedThread connectedThread;
    private BluetoothSocket BTsocket;
    private static UUID uuid = UUID.randomUUID();
    private String input;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int MESSAGE_READ = 2;
    private final static int CONNECTING_STATUS =3;
    private final static int MESSAGE_WRITE = 4;
    private final static int MESSAGE_TOAST =5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        BTconnect = findViewById(R.id.button3);
        BTdisconnect = findViewById(R.id.button4);
        Send = findViewById(R.id.sendSignal);
        listView = findViewById(R.id.listview);

        mBTArrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        BlueAdapter = BluetoothAdapter.getDefaultAdapter();
        listView.setAdapter(mBTArrayAdapter);
        listView.setOnItemClickListener(mDeviceClickListener);

        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mBluetoothStatus.setText(readMessage);
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + (String)(msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {

            Send.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(connectedThread != null)
                        input = "1";
                        byte[] bytes = input.getBytes();
                        connectedThread.write(bytes);
                }
            });

            BTconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                    listPairedDevices(v);
                }
            });

            BTdisconnect.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    bluetoothOff(v);
                }
            });
        }
    }


    private void bluetoothOn(View view){
        if (!BlueAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                mBluetoothStatus.setText("Enabled");
            } else
                mBluetoothStatus.setText("Disabled");
        }
    }

    private void bluetoothOff(View view){
//        input = "q";
//        byte[] bytes = input.getBytes();
//        connectedThread.write(bytes);

        BlueAdapter.disable();
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    private void listPairedDevices(View view){
        mBTArrayAdapter.clear();
        pairedDevices = BlueAdapter.getBondedDevices();
        if(BlueAdapter.isEnabled()) {
            for (BluetoothDevice device : pairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if(!BlueAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            new Thread()
            {
                public void run() {
                    boolean fail = false;
                    BluetoothDevice device = BlueAdapter.getRemoteDevice(address);
                    Log.d(TAG,"device:::" + device);
                    try {
                        BTsocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    try {
                        BTsocket.connect();
                        Log.d(TAG,"In the BT CONNECTION TRY");
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            fail = true;
                            BTsocket.close();
                            handler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();

                        } catch (IOException e2) {
                            e2.printStackTrace();
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        connectedThread = new ConnectedThread(BTsocket);
                        connectedThread.start();
                        handler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        try {
//            ////////////// this part helped fix the socket connection issue //////////
//            Method method = device.getClass().getMethod("getUuids"); /// get all services
//            ParcelUuid[] parcelUuids = (ParcelUuid[]) method.invoke(device); /// get all services
//            ///// using parcelUuids 1 or 2 ] is what connected to the raspberry pi
//            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(parcelUuids[1].getUuid());

            Method method;
            method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
            BluetoothSocket socket = (BluetoothSocket) method.invoke(device, 1);

            Log.d(TAG,""+socket);
            Log.d(TAG, "SOCKET Created Successfully");
            return socket;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(uuid);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] buffer;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                Log.d(TAG,"In The Socket input try section");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream",e);
                e.printStackTrace();
            }
            try {
                tmpOut = socket.getOutputStream();
                Log.d(TAG,"In the Socket Output try section");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    Message readMsg = handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer);
                    readMsg.sendToTarget();
                    Log.d(TAG,"In the message read try section");
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                Message writtenMsg = handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer);
                writtenMsg.sendToTarget();
                Log.d(TAG,"In the message out try section");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                Message writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}

