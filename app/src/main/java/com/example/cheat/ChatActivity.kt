package com.example.cheat


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_chat.*
import androidx.lifecycle.Observer
import com.example.cheat.CameraActivity
import com.example.cheat.model.Message
import java.util.*


class ChatActivity : AppCompatActivity() {

    private val viewModel: MessageViewModel by viewModels()

    var debug = true;   // just for debugging

    lateinit var text_entry: EditText;

    lateinit var button_send: ImageButton;

    lateinit var layout: LinearLayout;

    lateinit var history: ScrollView;

    var nextUid: Int = 0;

    fun requestCamera(view: View) {
        if(debug) println("requestCamera");

        val i = Intent(getApplicationContext(), CameraActivity::class.java)
        startActivity(i)
    }

    fun openImages(view: View) {
        if(debug) println("openImages");
        val intent = Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
        // save image to local variable
        // encode it.
        // send to BT for transmission
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
        if(!text_entry.text.isBlank()){
            var message = Message(nextUid, text_entry.text.toString(), Date(), true)
            viewModel.insertMessage(message)
            text_entry.text = null;
            nextUid++
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // called when img captured from camera intent
        if (resultCode == Activity.RESULT_OK && data != null) {
            // set image captured to image view
            val imageUri: Uri? = data!!.data
            val bitmap =  MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri);

            val imageView = ImageView(this);

            imageView.setImageURI(imageUri);
            imageView.maxHeight = 400;
            imageView.minimumHeight = 400;

            layout?.addView(imageView);
            history.post { history.fullScroll(View.FOCUS_DOWN)}
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        viewModel.deleteAllMessage()

        btnDebug.setOnClickListener {
            startActivity(Intent(this,StartActivity::class.java))
        }

        if(debug) println("onCreate");

        viewModel.getAllMessages().observe(this, Observer<List<Message>> {
            layout.removeAllViews()
            for (message in it) {
                val textView = TextView(this);
                textView.text = message.text;
                textView.setTextSize(25f);
                if (message.belongsToCurrentUser){
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
                } else {
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
                }
                layout?.addView(textView);
            }
            history.post { history.fullScroll(View.FOCUS_DOWN) }
        })

        history = findViewById<ScrollView>(R.id.scrollView);
        layout = findViewById(R.id.history_layout);
        text_entry = findViewById(R.id.text_entry);
        button_send = findViewById(R.id.button_send);
    }
}
