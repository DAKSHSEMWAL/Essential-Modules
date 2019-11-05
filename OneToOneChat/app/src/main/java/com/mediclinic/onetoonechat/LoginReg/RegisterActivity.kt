package com.mediclinic.onetoonechat.LoginReg

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.iid.FirebaseInstanceId
import com.mediclinic.onetoonechat.R

import java.util.Timer
import java.util.TimerTask

import xyz.hasnat.sweettoast.SweetToast

class RegisterActivity : AppCompatActivity() {
    private val myContext = this@RegisterActivity


    private var registerUserFullName: EditText? = null
    private var registerUserEmail: EditText? = null
    private var registerUserMobileNo: EditText? = null
    private var registerUserPassword: EditText? = null
    private var confirmRegisterUserPassword: EditText? = null

    private var registerUserButton: Button? = null
    private var progressDialog: ProgressDialog? = null

    //Firebase
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null

    private var storeDefaultDatabaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        Log.d(TAG, "on Create : started")

        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser

        registerUserFullName = findViewById<View>(R.id.registerFullName) as EditText
        registerUserEmail = findViewById<View>(R.id.registerEmail) as EditText
        registerUserMobileNo = findViewById<View>(R.id.registerMobileNo) as EditText
        registerUserPassword = findViewById<View>(R.id.registerPassword) as EditText
        confirmRegisterUserPassword = findViewById<View>(R.id.confirm_registerPassword) as EditText

        //Working with Create A/C Button Or, Register a/c
        registerUserButton = findViewById<View>(R.id.resisterButton) as Button
        registerUserButton!!.setOnClickListener {
            val name = registerUserFullName!!.text.toString()
            val email = registerUserEmail!!.text.toString()
            val mobile = registerUserMobileNo!!.text.toString()
            val password = registerUserPassword!!.text.toString()
            val confirmPassword = confirmRegisterUserPassword!!.text.toString()

            // pass input parameter through this Method
            registerAccount(name, email, mobile, password, confirmPassword)
        }
        progressDialog = ProgressDialog(myContext)
    }// ending onCreate


    private fun registerAccount(
        name: String,
        email: String,
        mobile: String,
        password: String,
        confirmPassword: String
    ) {

        //Validation for empty fields
        if (TextUtils.isEmpty(name)) {
            SweetToast.error(myContext, "Your name is required.")
        } else if (name.length < 3 || name.length > 40) {
            SweetToast.error(myContext, "Your name should be 3 to 40 numbers of characters.")

        } else if (TextUtils.isEmpty(email)) {
            SweetToast.error(myContext, "Your email is required.")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SweetToast.error(myContext, "Your email is not valid.")

        } else if (TextUtils.isEmpty(mobile)) {
            SweetToast.error(myContext, "Your mobile number is required.")
        } else if (mobile.length < 10) {
            SweetToast.error(myContext, "Mobile number should be min 10 characters.")

        } else if (TextUtils.isEmpty(password)) {
            SweetToast.error(myContext, "Please fill this password field")
        } else if (password.length < 6) {
            SweetToast.error(myContext, "Create a password at least 6 characters long.")
        } else if (TextUtils.isEmpty(confirmPassword)) {
            SweetToast.warning(myContext, "Please retype in password field")
        } else if (password != confirmPassword) {
            SweetToast.error(myContext, "Your password don't match with your confirm password")

        } else {
            //NOw ready to create a user a/c
            mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val deviceToken = FirebaseInstanceId.getInstance().token

                        // get and link storage
                        val current_userID = mAuth!!.currentUser!!.uid
                        storeDefaultDatabaseReference =
                            FirebaseDatabase.getInstance().reference.child("users")
                                .child(current_userID)

                        storeDefaultDatabaseReference!!.child("user_name").setValue(name)
                        storeDefaultDatabaseReference!!.child("verified").setValue("false")
                        storeDefaultDatabaseReference!!.child("search_name")
                            .setValue(name.toLowerCase())
                        storeDefaultDatabaseReference!!.child("user_mobile").setValue(mobile)
                        storeDefaultDatabaseReference!!.child("user_email").setValue(email)
                        storeDefaultDatabaseReference!!.child("user_nickname").setValue("")
                        storeDefaultDatabaseReference!!.child("user_gender").setValue("")
                        storeDefaultDatabaseReference!!.child("user_profession").setValue("")
                        storeDefaultDatabaseReference!!.child("created_at")
                            .setValue(ServerValue.TIMESTAMP)
                        storeDefaultDatabaseReference!!.child("user_status")
                            .setValue("Hi, I'm new uMe user")
                        storeDefaultDatabaseReference!!.child("user_image")
                            .setValue("default_image") // Original image
                        storeDefaultDatabaseReference!!.child("device_token").setValue(deviceToken)
                        storeDefaultDatabaseReference!!.child("user_thumb_image")
                            .setValue("default_image")
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // SENDING VERIFICATION EMAIL TO THE REGISTERED USER'S EMAIL
                                    user = mAuth!!.currentUser
                                    if (user != null) {
                                        user!!.sendEmailVerification()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {

                                                    registerSuccessPopUp()

                                                    // LAUNCH activity after certain time period
                                                    Timer().schedule(object : TimerTask() {
                                                        override fun run() {
                                                            this@RegisterActivity.runOnUiThread {
                                                                mAuth!!.signOut()

                                                                val mainIntent = Intent(
                                                                    myContext,
                                                                    LoginActivity::class.java
                                                                )
                                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                                startActivity(mainIntent)
                                                                finish()

                                                                SweetToast.info(
                                                                    myContext,
                                                                    "Please check your email & verify."
                                                                )
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

                    } else {
                        val message = task.exception!!.message
                        SweetToast.error(myContext, "Error occurred : " + message!!)
                    }

                    progressDialog!!.dismiss()
                }


            //config progressbar
            progressDialog!!.setTitle("Creating new account")
            progressDialog!!.setMessage("Please wait a moment....")
            progressDialog!!.show()
            progressDialog!!.setCanceledOnTouchOutside(false)
        }

    }

    private fun registerSuccessPopUp() {
        // Custom Alert Dialog
        val builder = AlertDialog.Builder(this@RegisterActivity)
        val view = LayoutInflater.from(this@RegisterActivity)
            .inflate(R.layout.register_success_popup, null)

        //ImageButton imageButton = view.findViewById(R.id.successIcon);
        //imageButton.setImageResource(R.drawable.logout);
        builder.setCancelable(false)

        builder.setView(view)
        builder.show()
    }

    companion object {

        private val TAG = "RegisterActivity"
    }


}
