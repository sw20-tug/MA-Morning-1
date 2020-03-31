package com.example.cheat

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*
import org.jetbrains.anko.toast

class StartActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1

    private var bt = BluetoothAdapter.getDefaultAdapter()

    private var list: ArrayList<String> = ArrayList()

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                //discovery starts, we can show progress dialog or perform other tasks
                toast("Starting discovering new devices")
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                //discovery finishes, dismiss progress dialog
                toast("Discovery finished")
            } else if (BluetoothDevice.ACTION_FOUND == action) { //bluetooth device found
                val device = intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                list.add(device.name + " (" + device.address + ")")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        btnRefresh.setOnClickListener {
            refresh()
        }

        btnMakeDiscoverable.setOnClickListener {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            startActivity(discoverableIntent)
            //setContentView(R.layout.activity_second)
        }

        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT)

        refresh()
    }

    private fun refresh() {
        list = ArrayList()
        checkForBondedDevices()
        updateSpinner()
        startDiscovery()
        updateSpinner()
    }

    private fun updateSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        spinnerFoundBTDevices.adapter = adapter
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
                toast("No paired devices found on this device...")
            }
        }
    }

    fun startDiscovery() {
        bt.startDiscovery()

        val filter = IntentFilter()

        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        registerReceiver(receiver, filter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (bt != null) {
                    if (bt.isEnabled) {
                        toast("Bluetooth has been enabled")
                    }
                    else {
                        toast("Bluetooth has been disabled")
                    }
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                toast("Bluetooth enabling has been canceled")
            }
        }
    }
}
