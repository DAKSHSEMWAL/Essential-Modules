package com.example.chatapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*


class RegisterActivty : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var user: FirebaseUser? = null
    private var storeDefaultDatabaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.chatapp.R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()

        sign_up.setOnClickListener {
            registerUser()
        }

        loginButton.setOnClickListener{
            intent= Intent(this,LoginActivty::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser() {

        //getting email and password from edit texts
        val email = email.getText().toString().trim()
        val password = password.getText().toString().trim()
        val name = name.getText().toString().trim()

        //checking if email and passwords are empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show()
            return
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show()
            return
        }

        //if the email and password are not empty
        //displaying a progress dialog

        //creating a new user
        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        finish()
                        val deviceToken = FirebaseInstanceId.getInstance().token
                        val current_userID = mAuth!!.currentUser!!.uid
                        val isDoctor=false
                        val gender="male"
                        storeDefaultDatabaseReference = FirebaseDatabase.getInstance().reference.child("users").child(current_userID)
                        storeDefaultDatabaseReference!!.child("user_id").setValue(current_userID)
                        storeDefaultDatabaseReference!!.child("user_name").setValue(name)
                        storeDefaultDatabaseReference!!.child("user_email").setValue(email)
                        storeDefaultDatabaseReference!!.child("gender").setValue(gender)
                        storeDefaultDatabaseReference!!.child("is_doctor").setValue(isDoctor)

                        if(isDoctor)
                        {
                            if(gender.equals("male"))
                            storeDefaultDatabaseReference!!.child("profile_photo").setValue("https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcQg-x3c8WirYZA3xWe8Zp4VR-scvr9MkjfbTJpubfYX1PURkr-S")
                            else
                            storeDefaultDatabaseReference!!.child("profile_photo").setValue("https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcRntOBQyukx0aDZLCzF7EwflLsycV-Z3jtJZV91rI8svlJsjftR")
                        }
                        else{
                            if(gender.equals("male"))
                                storeDefaultDatabaseReference!!.child("profile_photo").setValue("https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcQg-x3c8WirYZA3xWe8Zp4VR-scvr9MkjfbTJpubfYX1PURkr-S")
                            else
                                storeDefaultDatabaseReference!!.child("profile_photo").setValue("https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcRntOBQyukx0aDZLCzF7EwflLsycV-Z3jtJZV91rI8svlJsjftR")

                        }
                        storeDefaultDatabaseReference!!.child("device_token").setValue(deviceToken).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // SENDING VERIFICATION EMAIL TO THE REGISTERED USER'S EMAIL
                                user = mAuth!!.currentUser
                                if (user != null) {
                                    user!!.sendEmailVerification()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {

                                                    Timer().schedule(object : TimerTask() {
                                                        override fun run() {
                                                            this@RegisterActivty.runOnUiThread {
                                                                mAuth!!.signOut()

                                                                val mainIntent = Intent(
                                                                        this@RegisterActivty,
                                                                        LoginActivty::class.java
                                                                )
                                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                                startActivity(mainIntent)
                                                                finish()

                                                                Toast.makeText(
                                                                        this@RegisterActivty,
                                                                        "Please check your email & verify.", Toast.LENGTH_LONG).show()

                                                            }
                                                        }
                                                    }, 8000)


                                                } else {
                                                    mAuth!!.signOut()
                                                }
                                            }
                                }

                            }
                        }
                        startActivity(Intent(applicationContext, LoginActivty::class.java))
                    } else {
                        Toast.makeText(this, "Registration Error", Toast.LENGTH_LONG).show()
                    }
                }

    }
}

