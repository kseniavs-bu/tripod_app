package com.sarmale.arduinobtexample_v3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FrugalLogs";
    private static final int REQUEST_ENABLE_BT = 1;
    public static Handler handler;
    private final static int ERROR_READ = 0;
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Keep a persistent connection
    private BluetoothSocket btSocket = null;
    private OutputStream btOutputStream = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        TextView btReadings = findViewById(R.id.btReadings);
        TextView btDevices = findViewById(R.id.btDevices);
        Button connectToDevice = findViewById(R.id.connectToDevice);
        Button seachDevices = findViewById(R.id.seachDevices);
        Button clearValues = findViewById(R.id.refresh);

        // New control buttons
        Button btnPan = findViewById(R.id.btnPan);
        Button btnTilt = findViewById(R.id.btnTilt);
        Button btnMoveUp = findViewById(R.id.btnMoveUp);
        Button btnMoveDown = findViewById(R.id.btnMoveDown);
        Button btnMoveUpNew = findViewById(R.id.btnMoveUpNew);
        Button btnMoveDownNew = findViewById(R.id.btnMoveDownNew);

        Log.d(TAG, "Begin Execution");

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ERROR_READ:
                        String arduinoMsg = msg.obj.toString();
                        btReadings.setText(arduinoMsg);
                        break;
                }
            }
        };

        clearValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btDevices.setText("");
                btReadings.setText("");
            }
        });

        // Observable for establishing connection
        final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            try {
                Log.d(TAG, "Calling connectThread class");
                ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
                connectThread.run();

                if (connectThread.getMmSocket().isConnected()) {
                    Log.d(TAG, "Socket connected successfully!");

                    // Store the socket and output stream for reuse
                    btSocket = connectThread.getMmSocket();
                    btOutputStream = btSocket.getOutputStream();

                    emitter.onNext("Connected successfully!");
                } else {
                    Log.e(TAG, "Socket not connected");
                    emitter.onError(new Exception("Failed to connect"));
                }

                emitter.onComplete();

            } catch (Exception e) {
                Log.e(TAG, "Exception in BT connection: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });

        connectToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btReadings.setText("Connecting...");
                if (arduinoBTModule != null) {
                    connectToBTObservable.
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribeOn(Schedulers.io()).
                            subscribe(
                                    result -> {
                                        btReadings.setText(result);
                                        // Enable control buttons after successful connection
                                        btnPan.setEnabled(true);
                                        btnTilt.setEnabled(true);
                                        btnMoveUp.setEnabled(true);
                                        btnMoveDown.setEnabled(true);
                                        btnMoveUpNew.setEnabled(true);
                                        btnMoveDownNew.setEnabled(true);
                                    },
                                    error -> {
                                        Log.e(TAG, "Connection error: " + error.getMessage(), error);
                                        btReadings.setText("Error: " + error.getMessage());
                                    }
                            );
                }
            }
        });

        // PAN button
        btnPan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("0");
            }
        });

        // TILT button
        btnTilt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("1");
            }
        });

        // MOVE UP button
        btnMoveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("2");
            }
        });

        // MOVE DOWN button
        btnMoveDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("3");
            }
        });

        // NEW MOVE UP button (sends "5")
        btnMoveUpNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("5");
            }
        });

        // NEW MOVE DOWN button (sends "6")
        btnMoveDownNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("6");
            }
        });

        seachDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter == null) {
                    Log.d(TAG, "Device doesn't support Bluetooth");
                } else {
                    Log.d(TAG, "Device support Bluetooth");
                    if (!bluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth is disabled");
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "We don't have BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        } else {
                            Log.d(TAG, "We have BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        }
                    } else {
                        Log.d(TAG, "Bluetooth is enabled");
                    }

                    String btDevicesString = "";
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress();
                            Log.d(TAG, "deviceName:" + deviceName);
                            Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);
                            btDevicesString = btDevicesString + deviceName + " || " + deviceHardwareAddress + "\n";

                            if (deviceHardwareAddress.equals("00:14:03:05:05:43")) {
                                Log.d(TAG, "Target device found");
                                arduinoUUID = device.getUuids()[0].getUuid();
                                arduinoBTModule = device;
                                connectToDevice.setEnabled(true);
                            }
                            btDevices.setText(btDevicesString);
                        }
                    }
                }
                Log.d(TAG, "Button Pressed");
            }
        });
    }

    // Method to send commands via Bluetooth
    private void sendCommand(String command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (btOutputStream != null && btSocket != null && btSocket.isConnected()) {
                        btOutputStream.write(command.getBytes());
                        btOutputStream.flush();
                        Log.d(TAG, "Sent command: " + command.trim());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView btReadings = findViewById(R.id.btReadings);
                                btReadings.setText("Sent: " + command.trim());
                            }
                        });
                    } else {
                        Log.e(TAG, "Cannot send command - not connected");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView btReadings = findViewById(R.id.btReadings);
                                btReadings.setText("Error: Not connected");
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error sending command: " + e.getMessage(), e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView btReadings = findViewById(R.id.btReadings);
                            btReadings.setText("Error sending command");
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close Bluetooth connection when app is destroyed
        try {
            if (btOutputStream != null) {
                btOutputStream.close();
            }
            if (btSocket != null) {
                btSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing BT connection", e);
        }
    }
}