package com.mediclinic.onetoonechat.WelcomeSlide

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this@WelcomeActivity, IntroActivity::class.java))
        finish()

    }

}
