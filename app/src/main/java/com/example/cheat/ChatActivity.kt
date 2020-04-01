package com.example.cheat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ChatActivity : AppCompatActivity() {

    lateinit var text_entry: EditText;

    lateinit var button_send: ImageButton;

    lateinit var layout: LinearLayout;

    lateinit var history: ScrollView;

    fun openCamera(view: View) {
        // TODO open and use camera
    }

    fun openImages(view: View) {
        // TODO open and use camera
    }

    fun sendMessage(view: View) {
//         Do something in response to button click
        if(!text_entry.text.isBlank()) { // not sending empty text
            val textView = TextView(this);
            textView.text = text_entry.text;
            text_entry.text = null;

            textView.setTextSize(25f);
            textView.setTextColor(getColor(R.color.white));
            textView.setBackgroundResource(R.drawable.text_view_shape);

            textView.layoutParams= LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.RIGHT
                bottomMargin = 10;
                topMargin = 10;
            }

            layout?.addView(textView);
            history.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        history = findViewById(R.id.scrollView);
        layout = findViewById(R.id.history);
        text_entry = findViewById(R.id.text_entry);
        button_send = findViewById(R.id.button_send);

    }


}
