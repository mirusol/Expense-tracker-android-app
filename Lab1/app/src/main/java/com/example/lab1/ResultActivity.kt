package com.example.lab1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val bmiResultText: TextView = findViewById(R.id.bmi_result_text)
        val tipsText: TextView = findViewById(R.id.tips_text)
        val infoButton: Button = findViewById(R.id.fab_info)
        val shareButton: Button = findViewById(R.id.fab_share)
        val backButton: Button = findViewById(R.id.fab_back)
        val bmi = intent.getStringExtra("BMI_RESULT")
        val category = intent.getStringExtra("BMI_CATEGORY")

        bmiResultText.text = "Your BMI is\n$bmi"
        tipsText.text = category

        infoButton.setOnClickListener {
            val url = "https://www.reginamaria.ro/articole-medicale/indicele-de-masa-corporala-imc-sau-bmi"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Your BMI is $bmi. $category")
            startActivity(Intent.createChooser(shareIntent, "Share your BMI result"))
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}