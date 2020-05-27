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
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.exitProcess

class BluetoothConnectivity constructor(cnt : Context, blt : BluetoothAdapter){
    private var context : Context = cnt
    private val bt : BluetoothAdapter = blt
    private var list: ArrayList<String> = ArrayList()
    private var deviceList : HashMap<String, BluetoothDevice> = HashMap()

    private val serviceName: String = "CHEAT"
    private val serviceUUID: UUID = UUID.fromString("0b538899-008d-40ed-a0dc-6e657c3be729")

    private var connectedThread : ConnectedThread? = null;

    private var chatActivity : ChatActivity? = null;

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
                if(!list.contains(listEntry)) {
                    list.add(listEntry)
                    deviceList[listEntry] = device
                }
            }
        }
    }

    companion object {
        private var connectivity : BluetoothConnectivity? = null;

        fun instance(ctx : Context, bt : BluetoothAdapter) : BluetoothConnectivity {
            if (connectivity == null) {
                connectivity = BluetoothConnectivity(ctx, bt)
            }
            return connectivity!!
        }
    }

    fun updateContext(new_context : Context) {
        context = new_context
    }

    fun setChatActivity(chatActivity_: ChatActivity) {
        chatActivity = chatActivity_
    }

    fun refresh(): ArrayList<String> {
        deviceList.clear()
        list = ArrayList()
        checkForBondedDevices()
        startDiscovery()
        list = list.distinct() as ArrayList<String>
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

    fun manageMyConnectedThread(socket : BluetoothSocket, cp : String) {
        connectedThread = ConnectedThread(socket, cp);
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
                    activity.runOnUiThread(java.lang.Runnable {
                        Toast.makeText(context, "Connected to " + it.remoteDevice.name, Toast.LENGTH_SHORT).show()
                    })
                    manageMyConnectedThread(it, it.remoteDevice.name);
                    activity.changeToChatActivity(it.remoteDevice.name)
                    while (true) {
                        // Needs to be here as the thread is killed otherwise and we can't receive or send messages then
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
                    manageMyConnectedThread(socket, dev.name);
                    Log.d(TAG, "Finished connecting to " + dev.name);
                    activity.runOnUiThread(java.lang.Runnable {
                        Toast.makeText(context, "Connected to " + dev.name, Toast.LENGTH_SHORT).show()
                    })
                }
                activity.changeToChatActivity(dev.name)
                while (true) {
                    // Needs to be here as the thread is killed otherwise and we can't receive or send messages then
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

    public fun writeMessage (message: String, id: Int) {
        val r : ConnectedThread;
        synchronized(this) {
            r = this.connectedThread!!;
        }
        var messageToSend = "/write " + id + " " + message;
        r.write(messageToSend)
    }

    inner class ConnectedThread(socket: BluetoothSocket, cp : String) : Thread() {
        private var socket : BluetoothSocket = socket;
        private var input : InputStream = socket.inputStream;
        private var output : OutputStream = socket.outputStream;

        private var currentReceivedMessage : String = "";

        var cheatingPartner : String = cp

        public override fun run() {
            // Todo: Remove the endless loop with an actual check
            while (true) {
                val available = input.available();
                if (available != 0) {
                    val bytes = ByteArray(available);
                    input.read(bytes, 0, available);
                    val stringFromBytes = String(bytes, 0, available);
                    currentReceivedMessage += stringFromBytes;
                    if (stringFromBytes.contains("\\0")) {  // Message is finished
                        currentReceivedMessage = currentReceivedMessage.replace("\\0", "")
                        var strings = currentReceivedMessage.split(" ");
                        val operation = currentReceivedMessage.takeWhile { it != ' ' };
                        // val operation = strings[0];
                        val id = strings[1].toInt();
                        currentReceivedMessage = strings.drop(2).joinToString(separator = " ");
                        if(currentReceivedMessage.toLowerCase() == "/disconnect") {
                            chatActivity?.runOnUiThread(java.lang.Runnable {
                                Toast.makeText(chatActivity, "Disconnected from " + cheatingPartner, Toast.LENGTH_LONG).show()
                            })
                            //Why postDelayed? because otherwise we will never see the toast message above ...
                            // Restarts the whole application - HOW CONVINIENT!!!
                            Handler(Looper.getMainLooper()).postDelayed({exitProcess(0)}, 2000)
                            break;
                        }
                        else if(operation == "/write") {
                            chatActivity!!.receiveMessage(currentReceivedMessage, id)
                        }
                        else {
                        }
                        currentReceivedMessage = "";
                        Log.d(TAG, "Bluetooth-Read: This is what we received - " + stringFromBytes);
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