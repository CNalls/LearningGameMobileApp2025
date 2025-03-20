package com.example.mobileappdev2025

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileInputStream
import java.util.Scanner

data class WordDefinition(val word: String, val definition: String)

class MainActivity : AppCompatActivity() {
    private val ADD_WORD_CODE = 1234
    private lateinit var myAdapter: ArrayAdapter<String>
    private var dataDefList = ArrayList<String>()
    private var wordDefinition = mutableListOf<WordDefinition>()

    private var score: Int = 0
    private var totalCorrect: Int = 0
    private var totalWrong: Int = 0
    private var streak: Int = 0

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefs = getSharedPreferences("game_data", MODE_PRIVATE)
        loadScore()

        findViewById<Button>(R.id.stats_button).setOnClickListener { openStats(it) }
        findViewById<Button>(R.id.add_word_button).setOnClickListener { openAddWord(it) }

        loadWordsFromDisk()
        pickNewWordAndLoadDataList()
        setupList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_WORD_CODE && resultCode == RESULT_OK && data != null) {
            val word = data.getStringExtra("word") ?: ""
            val def = data.getStringExtra("def") ?: ""

            if (word.isNotEmpty() && def.isNotEmpty()) {
                wordDefinition.add(WordDefinition(word, def))
                pickNewWordAndLoadDataList()
                myAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadWordsFromDisk() {
        val file = File(applicationContext.filesDir, "user_data.csv")

        if (file.exists()) {
            val readResult = FileInputStream(file)
            val scanner = Scanner(readResult)

            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                val wd = line.split("|")
                if (wd.size == 2) {
                    wordDefinition.add(WordDefinition(wd[0], wd[1]))
                }
            }
        } else {
            file.createNewFile()
            val reader = Scanner(resources.openRawResource(R.raw.default_words))
            while (reader.hasNextLine()) {
                val line = reader.nextLine()
                val wd = line.split("|")
                if (wd.size == 2) {
                    wordDefinition.add(WordDefinition(wd[0], wd[1]))
                    file.appendText("${wd[0]}|${wd[1]}\n")
                }
            }
        }
    }

    private fun pickNewWordAndLoadDataList() {
        wordDefinition.shuffle()
        dataDefList.clear()

        for (wd in wordDefinition) {
            dataDefList.add(wd.definition)
        }

        findViewById<TextView>(R.id.word).text = wordDefinition[0].word
        dataDefList.shuffle()
    }

    private fun setupList() {
        myAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dataDefList)
        val defList = findViewById<ListView>(R.id.dynamic_def_list)
        defList.adapter = myAdapter

        // Handle answer selection + streak tracking
        defList.setOnItemClickListener { _, _, index, _ ->
            if (wordDefinition.isNotEmpty() && wordDefinition[0].definition == dataDefList[index]) {
                score++
                totalCorrect++
                streak++  //
            } else {
                totalWrong++
                streak = 0  //
            }

            updateScoreAndSave()  //
            pickNewWordAndLoadDataList()  //
            myAdapter.notifyDataSetChanged()
        }

    }

    fun openStats(view: View) {
        updateScoreAndSave() // Ensure latest values are saved before opening StatsActivity
        val myIntent = Intent(this, StatsActivity::class.java)
        startActivity(myIntent)
    }

    fun openAddWord(view: View) {
        val myIntent = Intent(this, AddWordActivity::class.java)
        startActivityForResult(myIntent, ADD_WORD_CODE)
    }

    private fun updateScoreAndSave() {
        val editor = prefs.edit()
        editor.putInt("score", score)
        editor.putInt("totalCorrect", totalCorrect)
        editor.putInt("totalWrong", totalWrong)
        editor.putInt("streak", streak)  //
        editor.apply()
    }

    private fun loadScore() {
        score = prefs.getInt("score", 0)
        totalCorrect = prefs.getInt("totalCorrect", 0)
        totalWrong = prefs.getInt("totalWrong", 0)
        streak = prefs.getInt("streak", 0)  //
    }

}
