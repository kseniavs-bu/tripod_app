package com.sarmale.arduinobtexample_v3;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectThread extends Thread {
    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private static final String TAG = "FrugalLogs";
    public static Handler handler;
    private final static int ERROR_READ = 0;
    private final UUID MY_UUID;

    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothDevice device, UUID MY_UUID, Handler handler) {
        this.mmDevice = device;
        this.MY_UUID = MY_UUID;
        this.handler = handler;
        BluetoothSocket tmp = null;

        try {
            // Try standard method first
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        try {
            // Try standard connection
            Log.d(TAG, "Attempting standard connection...");
            mmSocket.connect();
            Log.d(TAG, "Standard connection successful!");
        } catch (IOException connectException) {
            Log.e(TAG, "Standard connection failed: " + connectException);

            // Try fallback method
            try {
                Log.d(TAG, "Trying fallback connection method...");
                mmSocket.close();

                // Use reflection to create insecure socket
                Method m = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                mmSocket = (BluetoothSocket) m.invoke(mmDevice, 1);
                mmSocket.connect();

                Log.d(TAG, "Fallback connection successful!");
            } catch (Exception fallbackException) {
                Log.e(TAG, "Fallback connection also failed: " + fallbackException);
                handler.obtainMessage(ERROR_READ, "Unable to connect to the BT device").sendToTarget();
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public BluetoothSocket getMmSocket() {
        return mmSocket;
    }
}