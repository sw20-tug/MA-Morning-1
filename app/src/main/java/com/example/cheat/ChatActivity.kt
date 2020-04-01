package com.example.cheat

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ChatActivity : AppCompatActivity() {

    lateinit var text_entry: EditText;

    lateinit var button_send: Button;

    lateinit var layout: LinearLayout;

    fun sendMessage(view: View) {
        // Do something in response to button click
        val textView = TextView(this);
        textView.text = text_entry.text;

        textView.setTextSize(25f);
        textView.setTextColor(getColor(R.color.white));
        textView.setBackgroundResource(R.drawable.text_view_shape);

        textView.layoutParams= LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.RIGHT
            bottomMargin = 10;
        }

        layout ?.addView(textView);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        layout = findViewById(R.id.history)
        text_entry = findViewById(R.id.text_entry);
        button_send = findViewById(R.id.button_send);

    }


}
