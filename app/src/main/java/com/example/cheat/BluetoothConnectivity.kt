package com.example.cheat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BluetoothConnectivity constructor(cnt : Context, blt : BluetoothAdapter) {
    private val context : Context = cnt
    private val bt : BluetoothAdapter = blt
    private var list: ArrayList<String> = ArrayList()
    private var deviceList : HashMap<String, BluetoothDevice> = HashMap()

    private val serviceName: String = "CHEAT"
    private val serviceUUID: UUID = UUID.fromString("0b538899-008d-40ed-a0dc-6e657c3be729")

    private var connectedThread : ConnectedThread? = null;

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d(TAG, action)
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                //discovery starts, we can show progress dialog or perform other tasks
                Toast.makeText(context, "Starting to discover new devices ...", Toast.LENGTH_SHORT).show()
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                //discovery finishes, dismiss progress dialog
                Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show()
            } else if (BluetoothDevice.ACTION_FOUND == action) { //bluetooth device found
                val device = intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                val listEntry = device.name + " (" + device.address + ")"
                list.add(listEntry)
                deviceList[listEntry] = device
            }
        }
    }

    fun refresh(): ArrayList<String> {
        deviceList.clear()
        list = ArrayList()
        checkForBondedDevices()
        startDiscovery()
        return list
    }

    private fun checkForBondedDevices() {
        val pairedDevices: Set<BluetoothDevice>? = bt.bondedDevices
        if (pairedDevices != null) {
            if (pairedDevices.isNotEmpty()) {
                pairedDevices?.forEach { device ->
                    val listEntry = device.name + " (" + device.address + ")"
                    list.add(listEntry)
                    deviceList[listEntry] = device
                }
            }
            else {
                Toast.makeText(context, "No paired devices found on this device...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startDiscovery() {
        bt.startDiscovery()

        val filter = IntentFilter()

        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        try {
            context.unregisterReceiver(receiver)
        } catch (e : java.lang.Exception) {
            //Todo: Do something ... ?
        }
        context.registerReceiver(receiver, filter)
    }

    fun makeDiscoverable() {
        Toast.makeText(context, "Started making ourselves discoverable", Toast.LENGTH_SHORT).show()
        try {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            context.startActivity(discoverableIntent)
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
        //Toast.makeText(context, "Finished making ourselves discoverable", Toast.LENGTH_SHORT).show()
    }

    fun startAcceptThread(activity: StartActivity): AcceptThread {
        try {
            context.unregisterReceiver(receiver)
            // Cancel discovery because it otherwise slows down the connection.
            bt?.cancelDiscovery()
        } catch (e : java.lang.Exception) {
            //Todo: Do something ... ?
        }
        return AcceptThread(activity)
    }

    fun startConnectThread(deviceEntry: String, activity: StartActivity): ConnectThread {
        try {
            context.unregisterReceiver(receiver)
            // Cancel discovery because it otherwise slows down the connection.
            bt?.cancelDiscovery()
        } catch (e : java.lang.Exception) {
            //Todo: Do something ... ?
        }
        val dev : BluetoothDevice? = deviceList[deviceEntry]
        if (dev == null) {
            Log.d(TAG, "Could not connect to the Device " + deviceEntry)
            throw java.lang.Exception("Could not connect to the Device " + deviceEntry)
        }
        else {
            return ConnectThread(dev, activity)
        }
    }

    fun manageMyConnectedThread(socket : BluetoothSocket) {
        connectedThread = ConnectedThread(socket);
        connectedThread!!.start();
    }

    inner class AcceptThread(private val activity: StartActivity) : Thread() {
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bt?.listenUsingInsecureRfcommWithServiceRecord(serviceName, serviceUUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true;
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept();
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    shouldLoop = false;
                    null;
                }
                socket?.also {
                    Log.d(TAG, "connecting to " + it.remoteDevice.name);
                    mmServerSocket?.close();
                    shouldLoop = false;
                    Log.d(TAG, "Finished connecting to " + it.remoteDevice.name);
                    manageMyConnectedThread(it);
                    while (true) {
                        writeMessage("Hello darkness my old friend.\\0");
                        sleep(500);
                        writeMessage("something positive :)\\0");
                    }
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    inner class ConnectThread(device: BluetoothDevice, activity: StartActivity) : Thread() {
        private val activity : StartActivity = activity
        private val dev : BluetoothDevice = device

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(serviceUUID)
        }

        public override fun run() {
            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                if(!socket.isConnected) {
                    Log.d(TAG, "Starting connecting to " + dev.name);
                    socket.connect();
                    manageMyConnectedThread(socket);
                    Log.d(TAG, "Finished connecting to " + dev.name);
                }
                while(true) {
                    writeMessage("This is a really really really really really really really really really really really really really really really really really really long message that we should still get on the other device.\\0");
                    sleep(500);
                    writeMessage("I've come to talk with you again.\\0")
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    public fun writeMessage (message: String) {
        val r : ConnectedThread;
        synchronized(this) {
            r = this.connectedThread!!;
        }
        r.write(message)
    }

    inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private var socket : BluetoothSocket = socket;
        private var input : InputStream = socket.inputStream;
        private var output : OutputStream = socket.outputStream;

        private var currentReceivedMessage : String = "";

        public override fun run() {
            // Todo: Remove the endless loop with an actual check
            while (true) {
                if (!socket.isConnected) {
                    Log.d(TAG, "YOU fucking lil shit!");
                }
                val available = input.available();
                if (available != 0) {
                    val bytes = ByteArray(available);
                    input.read(bytes, 0, available);
                    val stringFromBytes = String(bytes, 0, available);
                    currentReceivedMessage += stringFromBytes;
                    if (stringFromBytes.contains("\\0")) {  // Message is finished
                        // TODO: Handler do the database update
                        currentReceivedMessage = "";
                        Log.d(TAG, "Bluetooth-Read: This is what we received - " + stringFromBytes);
                        //break;
                    }
                }
            }
        }

        public fun write(message : String) {
            Log.d(TAG, "Bluetooth-Write: This should be written - " + message);
            output.write(message.toByteArray());
            output.flush();
            // TODO: Handler do the database update
        }
    }
}