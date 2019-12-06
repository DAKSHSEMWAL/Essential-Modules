package com.example.chatapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.example.chatapp.data.SettingsAPI
import com.example.chatapp.util.Constants
import com.example.chatapp.util.Constants.NODE_EMAIl
import com.example.chatapp.util.Constants.NODE_GENDER
import com.example.chatapp.util.Constants.NODE_ID
import com.example.chatapp.util.Constants.NODE_NAME
import com.example.chatapp.util.Constants.NODE_PHOTO
import com.example.chatapp.util.Constants.is_docotor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivty : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    lateinit var set: SettingsAPI

    private var userDatabaseReference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        set = SettingsAPI(this)
        user = mAuth!!.currentUser

        userDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        loginButton.setOnClickListener {
            val email = email!!.text.toString()
            val password = password!!.text.toString()

            loginUserAccount(email, password)
        }
    }

    private fun loginUserAccount(email1: String, password1: String) {
        //just validation
        if (TextUtils.isEmpty(email1)) {
            email!!.error = "Email is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email1).matches()) {
            email!!.error = "Your email is not valid."
        } else if (TextUtils.isEmpty(password1)) {
            password!!.error = "Password is required"
        } else if (password1.length < 6) {
            password!!.error = "May be your password had minimum 6 numbers of character."
        } else {

            // after validation checking, log in user a/c
            mAuth!!.signInWithEmailAndPassword(email1, password1)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // these lines for taking DEVICE TOKEN for sending device to device notification
                            val userUID = mAuth!!.currentUser!!.uid
                            val userDeiceToken = FirebaseInstanceId.getInstance().token
                            userDatabaseReference!!.child(userUID).child("device_token")
                                    .setValue(userDeiceToken)
                                    .addOnSuccessListener { checkVerifiedEmail() }

                        } else {
                            Toast.makeText(this,
                                    "Your email and password may be incorrect. Please check & try again.", Toast.LENGTH_LONG).show()
                        }

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
            userDatabaseReference!!.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (data in dataSnapshot.children) {
                        val usrNm = data.child(NODE_NAME).value!!.toString()
                        val usrEm = data.child(NODE_EMAIl).value!!.toString()
                        val usrId = data.child(NODE_ID).value!!.toString()
                        val usrDp = data.child(NODE_PHOTO).value!!.toString()
                        val usrGender = data.child(NODE_GENDER).value!!.toString()
                        val isDocotr = data.child(is_docotor).value!!.toString()
                        if (usrId == UID) {
                            set.addUpdateSettings(Constants.PREF_MY_ID, usrId)
                            set.addUpdateSettings(Constants.PREF_MY_NAME, usrNm)
                            set.addUpdateSettings(Constants.PREF_MY_EMAIL, usrEm)
                            set.addUpdateSettings(Constants.PREF_MY_DP, usrDp)
                            set.addUpdateSettings(Constants.PREF_MY_IS_DOCTOR, isDocotr)
                            set.addUpdateSettings(Constants.PREF_MY_GENDER, usrGender)
                        }
                    }
                }
            })
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Email is not verified. Please verify first", Toast.LENGTH_LONG).show()
            mAuth!!.signOut()
        }
    }

}

