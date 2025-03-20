package com.example.mobileappdev2025

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class AddWordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Correct way to enable edge-to-edge in AppCompatActivity
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_add_word)
    }

    fun addWord(view: View) {
        val word = findViewById<EditText>(R.id.word_edit_text).text.toString()
        val def = findViewById<EditText>(R.id.def_edit_text).text.toString()

        if (word.isBlank() || def.isBlank()) {
            // Error handling (optional)
            return
        }

        val myIntent = Intent().apply {
            putExtra("word", word)
            putExtra("def", def)
        }
        setResult(RESULT_OK, myIntent)
        finish()
    }
}
