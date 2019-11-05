package com.mediclinic.onetoonechat.Home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager

import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.mediclinic.onetoonechat.About.AboutAppActivity
import com.mediclinic.onetoonechat.Adapter.TabsPagerAdapter
import com.mediclinic.onetoonechat.Friends.FriendsActivity
import com.mediclinic.onetoonechat.LoginReg.LoginActivity
import com.mediclinic.onetoonechat.ProfileSetting.SettingsActivity
import com.mediclinic.onetoonechat.R
import com.mediclinic.onetoonechat.Search.SearchActivity

class MainActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null

    private var mViewPager: ViewPager? = null
    private var mTabLayout: TabLayout? = null
    private lateinit var mTabsPagerAdapter: TabsPagerAdapter

    //Firebase
    private var mAuth: FirebaseAuth? = null
    private var userDatabaseReference: DatabaseReference? = null
    var currentUser: FirebaseUser? = null

    private var connectivityReceiver: ConnectivityReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            val user_uID = mAuth!!.currentUser!!.uid

            userDatabaseReference = FirebaseDatabase.getInstance().reference
                .child("users").child(user_uID)
        }


        /**
         * Tabs >> Viewpager for MainActivity
         */
        mViewPager = findViewById(R.id.tabs_pager)
        mTabsPagerAdapter = TabsPagerAdapter(supportFragmentManager)
        mViewPager!!.adapter = mTabsPagerAdapter

        mTabLayout = findViewById(R.id.main_tabs)
        mTabLayout!!.setupWithViewPager(mViewPager)
        //setupTabIcons();

        /**
         * Set Home Activity Toolbar Name
         */
        mToolbar = findViewById(R.id.main_page_toolbar)
        setSupportActionBar(mToolbar)
        //getSupportActionBar().setTitle("uMe");

    } // ending onCreate

    private fun setupTabIcons() {
        //mTabLayout.getTabAt(0).setText("CHATS");
        //mTabLayout.getTabAt(1).setText("REQUESTS");
        //mTabLayout.getTabAt(2).setText("FRIENDS");
    }

    override fun onStart() {
        super.onStart()
        currentUser = mAuth!!.currentUser
        //checking logging, if not login redirect to Login ACTIVITY
        if (currentUser == null) {
            logOutUser() // Return to Login activity
        }
        if (currentUser != null) {
            userDatabaseReference!!.child("active_now").setValue("true")
        }
    }

    override fun onResume() {
        super.onResume()
        //Register Connectivity Broadcast receiver
        connectivityReceiver = ConnectivityReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        // from onStop
        if (currentUser != null) {
            userDatabaseReference!!.child("active_now").setValue(ServerValue.TIMESTAMP)
        }
    }

    private fun logOutUser() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }


    // tool bar action menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.menu_search) {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        if (item.itemId == R.id.profile_settings) {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }

        if (item.itemId == R.id.all_friends) {
            val intent = Intent(this@MainActivity, FriendsActivity::class.java)
            startActivity(intent)
        }

        /*if (item.itemId == R.id.about_app) {
            val intent = Intent(this@MainActivity, AboutAppActivity::class.java)
            startActivity(intent)
        }*/

        if (item.itemId == R.id.main_logout) {
            // Custom Alert Dialog
            val builder = AlertDialog.Builder(this@MainActivity)
            val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.logout_dailog, null)

            val imageButton = view.findViewById<ImageButton>(R.id.logoutImg)
            imageButton.setImageResource(R.drawable.logout)
            builder.setCancelable(true)

            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

            builder.setPositiveButton("YES, Log out") { dialog, which ->
                if (currentUser != null) {
                    userDatabaseReference!!.child("active_now").setValue(ServerValue.TIMESTAMP)
                }
                mAuth!!.signOut()
                logOutUser()
            }
            builder.setView(view)
            builder.show()
        }
        return true
    }

    // Broadcast receiver for network checking
    inner class ConnectivityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {

            } else {
                val snackbar = Snackbar
                    .make(mViewPager!!, "No internet connection! ", Snackbar.LENGTH_LONG)
                    .setAction("Go settings") {
                        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(intent)
                    }
                // Changing action button text color
                snackbar.setActionTextColor(Color.BLACK)
                // Changing message text color
                val view = snackbar.view
                view.setBackgroundColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.colorPrimary
                    )
                )
                val textView = view.findViewById<TextView>(R.id.snackbar_text)
                textView.setTextColor(Color.WHITE)
                snackbar.show()
            }
        }
    }

    // This method is used to detect back button
    override fun onBackPressed() {
        if (TIME_LIMIT + backPressed > System.currentTimeMillis()) {
            super.onBackPressed()
            //Toast.makeText(getApplicationContext(), "Exited", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(applicationContext, "Press back again to exit", Toast.LENGTH_SHORT)
                .show()
        }
        backPressed = System.currentTimeMillis()
    } //End Back button press for exit...

    companion object {

        private val TIME_LIMIT = 1500
        private var backPressed: Long = 0
    }


}
