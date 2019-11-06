package com.example.chatapp.activity


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

import com.app.sample.fchat.util.CustomToast
import com.example.chatapp.R
import com.example.chatapp.data.SettingsAPI
import com.example.chatapp.data.Tools
import com.example.chatapp.util.Constants
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.example.chatapp.util.Constants.NODE_ID
import com.example.chatapp.util.Constants.NODE_NAME
import com.example.chatapp.util.Constants.NODE_PHOTO

class SplashActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private var signInButton: SignInButton? = null
    private var loginProgress: ProgressBar? = null

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mFirebaseAuth: FirebaseAuth? = null
    lateinit var ref: DatabaseReference
    lateinit var set: SettingsAPI

    lateinit var customToast: CustomToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        bindLogo()

        customToast = CustomToast(this)

        // Assign fields
        signInButton = findViewById<View>(R.id.sign_in_button) as SignInButton
        loginProgress = findViewById<View>(R.id.login_progress) as ProgressBar

        // Set click listeners
        signInButton!!.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance()
        set = SettingsAPI(this)

        if (intent.getStringExtra("mode") != null) {
            if (intent.getStringExtra("mode") == "logout") {
                mGoogleApiClient!!.connect()
                mGoogleApiClient!!.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) {
                        mFirebaseAuth!!.signOut()
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                        set.deleteAllSettings()
                    }

                    override fun onConnectionSuspended(i: Int) {

                    }
                })
            }
        }
        if (!mGoogleApiClient!!.isConnecting) {
            if (set.readSetting(Constants.PREF_MY_ID) != "na") {
                signInButton!!.visibility = View.GONE
                val handler = Handler()
                handler.postDelayed({
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }, 3000)
            }
        }
        // for system bar in lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Tools.systemBarLolipop(this)
        }
    }

    private fun bindLogo() {
        // Start animating the image
        val splash = findViewById<View>(R.id.splash) as ImageView
        val animation1 = AlphaAnimation(0.2f, 1.0f)
        animation1.duration = 700
        val animation2 = AlphaAnimation(1.0f, 0.2f)
        animation2.duration = 700
        //animation1 AnimationListener
        animation1.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(arg0: Animation) {
                // start animation2 when animation1 ends (continue)
                splash.startAnimation(animation2)
            }

            override fun onAnimationRepeat(arg0: Animation) {}

            override fun onAnimationStart(arg0: Animation) {}
        })

        //animation2 AnimationListener
        animation2.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(arg0: Animation) {
                // start animation1 when animation2 ends (repeat)
                splash.startAnimation(animation1)
            }

            override fun onAnimationRepeat(arg0: Animation) {}

            override fun onAnimationStart(arg0: Animation) {}
        })

        splash.startAnimation(animation1)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> signIn()
            else -> return
        }
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                signInButton!!.visibility = View.GONE
                loginProgress!!.visibility = View.VISIBLE
                // Google Sign In was successful, authenticate with Firebase
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                customToast.showError(getString(R.string.error_login_failed))
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mFirebaseAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        customToast.showError(getString(R.string.error_authetication_failed))
                    } else {
                        ref = FirebaseDatabase.getInstance().getReference(USERS_CHILD)
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val usrNm = acct.displayName.toString()
                                val usrId = acct.id.toString()
                                val usrDp = acct.photoUrl!!.toString()

                                set.addUpdateSettings(Constants.PREF_MY_ID, usrId)
                                set.addUpdateSettings(Constants.PREF_MY_NAME, usrNm)
                                set.addUpdateSettings(Constants.PREF_MY_DP, usrDp)

                                if (!snapshot.hasChild(usrId)) {
                                    ref.child("$usrId/$NODE_NAME").setValue(usrNm)
                                    ref.child("$usrId/$NODE_PHOTO").setValue(usrDp)
                                    ref.child("$usrId/$NODE_ID").setValue(usrId)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })

                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        customToast.showError("Google Play Services error.")
    }

    companion object {

        private val RC_SIGN_IN = 100

        val USERS_CHILD = "users"
    }
}
