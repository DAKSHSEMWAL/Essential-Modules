package com.mediclinic.onetoonechat.ProfileSetting

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mediclinic.onetoonechat.R

import xyz.hasnat.sweettoast.SweetToast

class StatusUpdateActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null
    private var status_from_input: EditText? = null
    private var progressDialog: ProgressDialog? = null

    private var statusDatabaseReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_update)

        mAuth = FirebaseAuth.getInstance()
        val user_id = mAuth!!.currentUser!!.uid
        statusDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("users").child(user_id)

        status_from_input = findViewById(R.id.input_status)
        progressDialog = ProgressDialog(this)

        mToolbar = findViewById(R.id.update_status_appbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "Update Status"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // back on previous activity
        mToolbar!!.setNavigationOnClickListener {
            Log.d(TAG, "onClick : navigating back to 'SettingsActivity.class' ")
            finish()
        }

        /**
         * retrieve previous profile status from SettingsActivity
         */
        val previousStatus = intent.extras!!.get("ex_status")!!.toString()
        status_from_input!!.setText(previousStatus)
        status_from_input!!.setSelection(status_from_input!!.text.length)
    } //ending onCreate

    // tool bar Status update done- menu button
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.update_status_done_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.status_update_done) {
            val new_status = status_from_input!!.text.toString()
            changeProfileStatus(new_status)
        }
        return true
    }

    private fun changeProfileStatus(new_status: String) {
        if (TextUtils.isEmpty(new_status)) {
            SweetToast.warning(applicationContext, "Please write something about status")
        } else {
            progressDialog!!.setMessage("Updating status...")
            progressDialog!!.show()
            progressDialog!!.setCanceledOnTouchOutside(false)

            statusDatabaseReference!!.child("user_status").setValue(new_status)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressDialog!!.dismiss()
                        finish()
                    } else {
                        SweetToast.warning(applicationContext, "Error occurred: failed to update.")
                    }
                }
        }
    }

    companion object {

        private val TAG = "StatusUpdateActivity"
    }

}
