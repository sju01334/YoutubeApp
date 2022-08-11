package com.nepplus.youtubeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        var myHandler = Handler(Looper.getMainLooper())

        myHandler.postDelayed({
            val myIntent = Intent(this, MainActivity::class.java)
            startActivity(myIntent)
            finish()


        }, 1500)
    }


}