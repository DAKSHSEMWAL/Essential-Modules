package com.mediclinic.onetoonechat.LoginReg

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.mediclinic.onetoonechat.ForgotPassword.ForgotPassActivity
import com.mediclinic.onetoonechat.Home.MainActivity
import com.mediclinic.onetoonechat.R

import java.util.Calendar

import xyz.hasnat.sweettoast.SweetToast

class LoginActivity : AppCompatActivity() {


    private var userEmail: EditText? = null
    private var userPassword: EditText? = null
    private var loginButton: Button? = null
    private var linkSingUp: TextView? = null
    private var linkForgotPassword: TextView? = null
    private var copyrightTV: TextView? = null


    private var progressDialog: ProgressDialog? = null

    //Firebase Auth
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null

    private var userDatabaseReference: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser

        userDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

        userEmail = findViewById(R.id.inputEmail)
        userPassword = findViewById(R.id.inputPassword)
        loginButton = findViewById(R.id.loginButton)
        linkSingUp = findViewById(R.id.linkSingUp)
        linkForgotPassword = findViewById(R.id.linkForgotPassword)
        progressDialog = ProgressDialog(this)

        //Copyright text
        copyrightTV = findViewById(R.id.copyrightTV)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        copyrightTV!!.text = "uMe © $year"

        //redirect to FORGOT PASS activity
        linkForgotPassword!!.setOnClickListener {
            Log.d(TAG, "onClick: go to FORGOT Activity")
            val intent = Intent(this@LoginActivity, ForgotPassActivity::class.java)
            startActivity(intent)
        }

        //redirect to register activity
        linkSingUp!!.setOnClickListener {
            Log.d(TAG, "onClick: go to Register Activity")
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }


        /**
         * Login Button with Firebase
         */
        loginButton!!.setOnClickListener {
            val email = userEmail!!.text.toString()
            val password = userPassword!!.text.toString()

            loginUserAccount(email, password)
        }
    }

    private fun loginUserAccount(email: String, password: String) {
        //just validation
        if (TextUtils.isEmpty(email)) {
            SweetToast.error(this, "Email is required")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SweetToast.error(this, "Your email is not valid.")
        } else if (TextUtils.isEmpty(password)) {
            SweetToast.error(this, "Password is required")
        } else if (password.length < 6) {
            SweetToast.error(this, "May be your password had minimum 6 numbers of character.")
        } else {
            //progress bar
            progressDialog!!.setMessage("Please wait...")
            progressDialog!!.show()
            progressDialog!!.setCanceledOnTouchOutside(false)

            // after validation checking, log in user a/c
            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // these lines for taking DEVICE TOKEN for sending device to device notification
                        val userUID = mAuth!!.currentUser!!.uid
                        val userDeiceToken = FirebaseInstanceId.getInstance().token
                        userDatabaseReference!!.child(userUID).child("device_token")
                            .setValue(userDeiceToken)
                            .addOnSuccessListener { checkVerifiedEmail() }

                    } else {
                        SweetToast.error(
                            this@LoginActivity,
                            "Your email and password may be incorrect. Please check & try again."
                        )
                    }

                    progressDialog!!.dismiss()
                }
        }
    }

    /** checking email verified or NOT  */
    private fun checkVerifiedEmail() {
        user = mAuth!!.currentUser
        var isVerified = false
        if (user != null) {
            isVerified = user!!.isEmailVerified
        }
        if (isVerified) {
            val UID = mAuth!!.currentUser!!.uid
            userDatabaseReference!!.child(UID).child("verified").setValue("true")

            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        } else {
            SweetToast.info(this@LoginActivity, "Email is not verified. Please verify first")
            mAuth!!.signOut()
        }
    }

    companion object {

        private val TAG = "LoginActivity"
    }


}