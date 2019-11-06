package com.example.chatapp.activity


import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

import com.app.sample.fchat.util.CustomToast
import com.example.chatapp.R
import com.example.chatapp.data.Tools
import com.example.chatapp.fragment.ChatsFragment
import com.example.chatapp.service.NotificationService
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    lateinit var fab: FloatingActionButton
    lateinit var mJobScheduler: JobScheduler

    private var exitTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        fab = findViewById<View>(R.id.add) as FloatingActionButton

        prepareActionBar(toolbar)
        initComponent()

        fab.setOnClickListener {
            val i = Intent(this@MainActivity, SelectFriendActivity::class.java)
            startActivity(i)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // for system bar in lollipop
            Tools.systemBarLolipop(this)
            //Create the scheduler
            mJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val builder = JobInfo.Builder(1, ComponentName(packageName, NotificationService::class.java.name))
            builder.setPeriodic(900000)
            //If it needs to continue even after boot, persisted needs to be true
            //builder.setPersisted(true);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            mJobScheduler.schedule(builder.build())
        }
    }

    private fun initComponent() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val ctf = ChatsFragment()
        //icf.setRetainInstance(true);
        fragmentTransaction.add(R.id.main_container, ctf, "Chat History")
        fragmentTransaction.commit()

    }

    private fun prepareActionBar(toolbar: Toolbar?) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(false)
        actionBar.setHomeButtonEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_logout -> {
                val logoutIntent = Intent(this, SplashActivity::class.java)
                logoutIntent.putExtra("mode", "logout")
                startActivity(logoutIntent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun doExitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            CustomToast(this).showInfo(getString(R.string.press_again_exit_app))
            exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        doExitApp()
    }
}
