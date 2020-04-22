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
        return AcceptThread(activity)
    }

    fun startConnectThread(deviceEntry: String, activity: StartActivity): ConnectThread {
        val dev : BluetoothDevice? = deviceList[deviceEntry]
        if (dev == null) {
            Log.d(TAG, "Could not connect to the Device " + deviceEntry)
            throw java.lang.Exception("Could not connect to the Device " + deviceEntry)
        }
        else {
            return ConnectThread(dev, activity)
        }
    }

    inner class AcceptThread(private val activity: StartActivity) : Thread() {
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bt?.listenUsingInsecureRfcommWithServiceRecord(serviceName, serviceUUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    Log.d(TAG, "connecting to " + it.remoteDevice.name)
                    mmServerSocket?.close()
                    shouldLoop = false
                    Log.d(TAG, "Finished connecting to " + it.remoteDevice.name)
                }
                val inputStream = socket!!.inputStream
                val outputStream = socket!!.outputStream
                // Waiting for message to arrive
                sleep(2000)

                val available = inputStream.available()
                val bytes = ByteArray(available)
                inputStream.read(bytes, 0, available)
                activity.runOnUiThread {
                    activity.updateText(bytes.toString(Charsets.UTF_8))
                }

                try {
                    outputStream.write("Connected :)".toByteArray())
                    outputStream.flush()
                } catch(e: Exception) {
                    Log.e("client", "Cannot send", e)
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
            // Cancel discovery because it otherwise slows down the connection.
            bt?.cancelDiscovery()

            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                Log.d(TAG, "Starting connecting to " + dev.name)
                socket.connect()
                Log.d(TAG, "Finished connecting to " + dev.name)
                //Toast.makeText(context, "I connected to " + dev.name, Toast.LENGTH_LONG)
                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                // manageMyConnectedSocket(socket)
                val outputStream = socket.outputStream
                val inputStream = socket.inputStream
                try {
                    outputStream.write("Connected :)".toByteArray())
                    outputStream.flush()
                } catch(e: Exception) {
                    Log.e("client", "Cannot send", e)
                } finally {
                    sleep(4000)

                    val available = inputStream.available()
                    val bytes = ByteArray(available)
                    inputStream.read(bytes, 0, available)
                    activity.runOnUiThread {
                        activity.updateText(bytes.toString(Charsets.UTF_8))
                    }

                    outputStream.close()
                    inputStream.close()
                    socket.close()
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
}