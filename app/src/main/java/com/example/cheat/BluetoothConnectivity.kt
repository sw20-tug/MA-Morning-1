package com.example.cheat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Parcelable
import android.widget.Toast

class BluetoothConnectivity constructor(cnt : Context, blt : BluetoothAdapter) {
    private val context : Context = cnt
    private val bt : BluetoothAdapter = blt
    private var list: ArrayList<String> = ArrayList()

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                //discovery starts, we can show progress dialog or perform other tasks
                Toast.makeText(context, "Starting to discover new devices ...", Toast.LENGTH_SHORT).show()
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                //discovery finishes, dismiss progress dialog
                Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show()
            } else if (BluetoothDevice.ACTION_FOUND == action) { //bluetooth device found
                val device = intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                list.add(device.name + " (" + device.address + ")")
            }
        }
    }

    fun refresh(): ArrayList<String> {
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
                    list.add(device.name + " (" + device.address + ")")
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

        context.registerReceiver(receiver, filter)
    }

    fun makeDiscoverable() {
        Toast.makeText(context, "Started making ourselves discoverable", Toast.LENGTH_SHORT).show()
        try {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30)
            context.startActivity(discoverableIntent)
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
        //Toast.makeText(context, "Finished making ourselves discoverable", Toast.LENGTH_SHORT).show()
    }
}