package com.example.cheat


import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*


class ChatActivity : AppCompatActivity() {

    private val viewModel: MessageViewModel by viewModels()
    private val PERMISSION_CODE = 1000;
    //private val IMAGE_CAPTURE_CODE = 1001;

    var debug = true;   // just for debugging

    lateinit var text_entry: EditText;

    lateinit var button_send: ImageButton;

    lateinit var layout: LinearLayout;

    lateinit var history: ScrollView;

    fun requestCamera(view: View) {
        val i = Intent(applicationContext, CameraActivity::class.java)
        startActivity(i)
        setContentView(R.layout.activity_camera)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
                // permission not enabled
                val permission = arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE);
                requestPermissions(permission, PERMISSION_CODE)
            } else {
                // permission granted
                CameraActivity.openCamera();
            }
        } else {
            // system os is < marshmallow
            CameraActivity.openCamera();
        }
    }

    fun openImages(view: View) {
        // TODO open and use camera
        if(debug) println("openImages");
    }

    fun loadHistory() {
        if(debug) {
            println("sendMessage");
            var i = 0;
            while(i < 15) {
                val textView = TextView(this);
                textView.text = "Message$i";
                i++;

                textView.setTextSize(25f);
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundResource(R.drawable.text_view_received);

                textView.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.LEFT
                    bottomMargin = 10;
                    topMargin = 10;
                }

                layout?.addView(textView);
            }
            history.post { history.fullScroll(View.FOCUS_DOWN) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun sendMessage(view: View) {
//         Do something in response to button click
        if(debug) println("sendMessage");

        if(!text_entry.text.isBlank()) { // not sending empty text
            val textView = TextView(this);
            textView.text = text_entry.text;
            text_entry.text = null;

            textView.setTextSize(25f);
            textView.setTextColor(Color.WHITE);
            textView.setBackgroundResource(R.drawable.text_view_sent);

            textView.layoutParams= LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.RIGHT
                bottomMargin = 10;
                topMargin = 10;
                rightMargin = 15;
            }

            layout?.addView(textView);
            history.post { history.fullScroll(View.FOCUS_DOWN) }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        btnDebug.setOnClickListener {
            startActivity(Intent(this,StartActivity::class.java))
        }

        if(debug) println("onCreate");

        history = findViewById<ScrollView>(R.id.scrollView);
        layout = findViewById(R.id.history_layout);
        text_entry = findViewById(R.id.text_entry);
        button_send = findViewById(R.id.button_send);
        loadHistory();
    }
}
