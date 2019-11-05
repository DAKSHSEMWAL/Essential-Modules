package com.mediclinic.onetoonechat.About


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.mediclinic.onetoonechat.R

class AboutAppActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null
    private var gitBtn: Button? = null
    private var InstaBtn: Button? = null
    private var TwBtn: Button? = null
    private var LinBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_app)

        // Set Home Activity Toolbar Name
        mToolbar = findViewById(R.id.about_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "About"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        gitBtn = findViewById(R.id.git_btn)
        InstaBtn = findViewById(R.id.insta_btn)
        LinBtn = findViewById(R.id.lin_btn)
        TwBtn = findViewById(R.id.tw_btn)

        // 4 buttons
        // git button
        gitBtn!!.setOnClickListener {
            val uri = Uri.parse("https://github.com/TheHasnatBD")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // instagram button
        InstaBtn!!.setOnClickListener {
            val uri = Uri.parse("https://instagram.com/TheHasnatBD")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // Linkedin button
        LinBtn!!.setOnClickListener {
            val uri = Uri.parse("https://linkedin.com/in/TheHasnatBD")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        //twitter button
        TwBtn!!.setOnClickListener {
            val uri = Uri.parse("https://twitter.com/TheHasnatBD")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }


    }

}
