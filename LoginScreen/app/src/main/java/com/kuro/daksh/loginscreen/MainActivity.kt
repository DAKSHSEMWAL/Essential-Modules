package com.kuro.daksh.loginscreen

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.analytics.FirebaseAnalytics



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the FirebaseAnalytics instance.
        val mFirebaseAnalytics: FirebaseAnalytics? = FirebaseAnalytics.getInstance(this);
        object : CountDownTimer(5000, 1000) {
            override fun onFinish() {
                //bookText.visibility = View.GONE
                loadingProgressBar.visibility = View.GONE
                rootView.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorSplashText))
                startAnimation()
            }

            override fun onTick(p0: Long) {}
        }.start()
        loginButton.setOnClickListener {
            intent = Intent(applicationContext, OtpActivity::class.java)
            startActivity(intent)
        }
        sign_up.setOnClickListener {
            intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
        }
        forget_password.setOnClickListener {
            intent = Intent(applicationContext, FogetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startAnimation() {
        bookIconImageView.animate().apply {
            x(50f)
            y(100f)
            duration = 1000
        }.setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                afterAnimationView.visibility = VISIBLE
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {

            }
        })
    }
}
