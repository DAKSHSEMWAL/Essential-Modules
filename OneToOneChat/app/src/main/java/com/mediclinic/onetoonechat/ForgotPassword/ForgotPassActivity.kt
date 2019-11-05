package com.mediclinic.onetoonechat.ForgotPassword


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.mediclinic.onetoonechat.LoginReg.LoginActivity
import com.mediclinic.onetoonechat.R

import java.util.Timer
import java.util.TimerTask

import xyz.hasnat.sweettoast.SweetToast

class ForgotPassActivity : AppCompatActivity() {
    private var mToolbar: Toolbar? = null
    private var forgotEmail: EditText? = null
    private var resetPassButton: Button? = null

    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        mToolbar = findViewById(R.id.fp_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = "Reset Password"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        auth = FirebaseAuth.getInstance()

        forgotEmail = findViewById(R.id.forgotEmail)
        resetPassButton = findViewById(R.id.resetPassButton)
        resetPassButton!!.setOnClickListener {
            val email = forgotEmail!!.text.toString()
            if (TextUtils.isEmpty(email)) {
                SweetToast.error(this@ForgotPassActivity, "Email is required")
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                SweetToast.error(this@ForgotPassActivity, "Email format is not valid.")
            } else {
                // send email to reset password
                auth!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        emailSentSuccessPopUp()

                        // LAUNCH activity after certain time period
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                this@ForgotPassActivity.runOnUiThread {
                                    auth!!.signOut()

                                    val mainIntent =
                                        Intent(this@ForgotPassActivity, LoginActivity::class.java)
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(mainIntent)
                                    finish()

                                    SweetToast.info(
                                        this@ForgotPassActivity,
                                        "Please check your email."
                                    )
                                }
                            }
                        }, 8000)
                    }
                }.addOnFailureListener { e ->
                    SweetToast.error(
                        this@ForgotPassActivity,
                        "Oops!! " + e.message
                    )
                }
            }
        }

    }

    private fun emailSentSuccessPopUp() {
        // Custom Alert Dialog
        val builder = AlertDialog.Builder(this@ForgotPassActivity)
        val view = LayoutInflater.from(this@ForgotPassActivity)
            .inflate(R.layout.register_success_popup, null)
        val successMessage = view.findViewById<TextView>(R.id.successMessage)
        successMessage.text =
            "Password reset link has been sent successfully.\nPlease check your email. Thank You."
        builder.setCancelable(true)

        builder.setView(view)
        builder.show()
    }

}
