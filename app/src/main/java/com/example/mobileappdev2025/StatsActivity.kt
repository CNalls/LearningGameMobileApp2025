package com.example.mobileappdev2025

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        prefs = getSharedPreferences("game_data", MODE_PRIVATE)

        // Load saved stats from SharedPreferences
        val score = prefs.getInt("score", 0)
        val totalCorrect = prefs.getInt("totalCorrect", 0)
        val totalWrong = prefs.getInt("totalWrong", 0)
        val streak = prefs.getInt("streak", 0)

        // Update UI with loaded values
        findViewById<TextView>(R.id.score_text).text = "Score: $score"
        findViewById<TextView>(R.id.correct_text).text = "Total Correct: $totalCorrect"
        findViewById<TextView>(R.id.wrong_text).text = "Total Wrong: $totalWrong"
        findViewById<TextView>(R.id.streak_text).text = "Current Streak: $streak"
    }
}
