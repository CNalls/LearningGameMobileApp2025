package com.example.mobileappdev2025

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileInputStream
import java.util.Random
import java.util.Scanner

data class WordDefinition(val word: String, val definition: String, var streak: Int = 0)

class MainActivity : AppCompatActivity() {
    private val ADD_WORD_CODE = 1234; // 1-65535
    private lateinit var myAdapter : ArrayAdapter<String>; // connect from data to gui
    private var dataDefList = ArrayList<String>(); // data
    private var wordDefinition = mutableListOf<WordDefinition>();
    private var score : Int = 0;
    private var streak : Int = 0;
    private var totalCorrect : Int = 0;
    private var totalWrong : Int = 0;
    private var longestStreak: Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadStats()
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadWordsFromDisk()

        pickNewWordAndLoadDataList();
        setupList();

        val defList = findViewById<ListView>(R.id.dynamic_def_list);
        defList.setOnItemClickListener { _, _, index, _ ->
            val correctDefinition = wordDefinition[0].definition
            val selectedDefinition = dataDefList[index]

            if (selectedDefinition == correctDefinition) { // Correct answer
                wordDefinition[0].streak++ // makes streak go up for correct word chosen
                streak++
                score += streak //bugs sscore starts at 5 and yesh its supposed to multiply
                if (streak > longestStreak)
                {
                    longestStreak = streak
                }
            } else { // Incorrect answer
                wordDefinition[0].streak = 0 // Reset streak for the current word
                streak = 0
            }

            saveWordsToDisk()
            updateScoreAndStreakUI()
            pickNewWordAndLoadDataList();
            myAdapter.notifyDataSetChanged();
        };
    }

    private fun updateScoreAndStreakUI()
    {
        findViewById<TextView>(R.id.scoreTextView).text = "Score: $score"
        findViewById<TextView>(R.id.streakTextView).text = "Streak: $streak"
    }

    override fun onDestroy()
    {
        saveStats()
        super.onDestroy()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_WORD_CODE && resultCode == RESULT_OK && data != null) {
            val word = data.getStringExtra("word") ?: ""
            val def = data.getStringExtra("def") ?: ""//this saves data for words so basically word and def added and then saved

            Log.d("MAD", word)
            Log.d("MAD", def)

            if (word == "" || def == "") return

            // Add the new word to the list
            wordDefinition.add(WordDefinition(word, def))//here specifically

            // Save the updated list to disk
            saveWordsToDisk()

            // Refresh the UI
            pickNewWordAndLoadDataList()
            myAdapter.notifyDataSetChanged()
        }
    }

    private fun saveStats()
    {
        val file = File(applicationContext.filesDir, "user_stats.csv")
        file.writeText("$score,$streak,$longestStreak")
    }

    private fun loadStats()
    {
        val file = File(applicationContext.filesDir, "user_stats.csv")
        if (file.exists()) {
            val scanner = Scanner(file)
            if (scanner.hasNextLine()) {
                val stats = scanner.nextLine().split(",")
                score = stats[0].toInt()
                streak = stats[1].toInt()
                longestStreak = stats[2].toInt()
            }
        }
    }

    private fun loadWordsFromDisk() //these now include streak and DONT crash upon run
    {
        val file = File(applicationContext.filesDir, "user_data.csv")
        if (file.exists()) {
            val readResult = FileInputStream(file)
            val scanner = Scanner(readResult)

            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                val wd = line.split("|")
                wordDefinition.add(WordDefinition(wd[0], wd[1], wd.getOrNull(2)?.toInt() ?: 0))
            }
        } else { // default data
            val reader = Scanner(resources.openRawResource(R.raw.default_words))
            while (reader.hasNextLine()) {
                val line = reader.nextLine()
                val wd = line.split("|")
                wordDefinition.add(WordDefinition(wd[0], wd[1]))
                file.writeText("")
                file.appendText("${wd[0]}|${wd[1]}|0\n") // Initialize streak to 0
            }
        }
    }
    //new
    private fun saveWordsToDisk()
    {
        val file = File(applicationContext.filesDir, "user_data.csv")
        file.writeText("") // Clear the file before writing
        for (wd in wordDefinition)
        {
            file.appendText("${wd.word}|${wd.definition}|${wd.streak}\n")
        }
    }
    //
    private fun pickNewWordAndLoadDataList()//have to add showing words less often based on streak
    {
        /* wordDefinition.shuffle();

         dataDefList.clear();

         for(wd in wordDefinition){
             dataDefList.add(wd.definition);
         }

         findViewById<TextView>(R.id.word).text = wordDefinition[0].word;

         dataDefList.shuffle(); */
        //buggy attempt at editing thw words to show less
        // Sort words by streak (lower streaks come first)
        wordDefinition.sortBy { it.streak }

        // Clear the existing definitions
        dataDefList.clear()

        // Add up to 4 definitions
        for (i in 0 until minOf(4, wordDefinition.size)) {
            dataDefList.add(wordDefinition[i].definition)
        }

        // Display the word with the lowest streak
        findViewById<TextView>(R.id.word).text = wordDefinition[0].word

        // Shuffle the definitions to randomize their order
        dataDefList.shuffle()
    }

    private fun setupList()
    {
        myAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataDefList);

        // connect to list
        val defList = findViewById<ListView>(R.id.dynamic_def_list);
        defList.adapter = myAdapter;
    }

    fun openStats(view : View)
    {
        val myIntent = Intent(this, StatsActivity::class.java)
        myIntent.putExtra("score", score.toString())
        myIntent.putExtra("streak", streak.toString())
        myIntent.putExtra("totalCorrect", totalCorrect.toString())
        myIntent.putExtra("totalWrong", totalWrong.toString())
        myIntent.putExtra("longestStreak", longestStreak.toString())
        startActivity(myIntent)
    }

    fun openAddWord(view : View)
    {
        var myIntent = Intent(this, AddWordActivity::class.java);
        startActivityForResult(myIntent, ADD_WORD_CODE)
    }
}