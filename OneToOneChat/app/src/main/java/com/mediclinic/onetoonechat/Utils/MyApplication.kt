package com.mediclinic.onetoonechat.Utils

import android.app.Application

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

class MyApplication : Application() {

    private var userDatabaseReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var currentOnlineUser: FirebaseUser? = null

    override fun onCreate() {

        super.onCreate()

        //  all strings >> load offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        //  all images >> load offline
        val builder = Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this, Integer.MAX_VALUE.toLong()))
        val builtPicasso = builder.build()
        builtPicasso.setIndicatorsEnabled(true)
        builtPicasso.isLoggingEnabled = true

        Picasso.setSingletonInstance(builtPicasso)


        // ONLINE STATUS
        mAuth = FirebaseAuth.getInstance()
        currentOnlineUser = mAuth!!.currentUser

        if (currentOnlineUser != null) {
            val user_u_id = mAuth!!.currentUser!!.uid

            userDatabaseReference =
                FirebaseDatabase.getInstance().reference.child("users").child(user_u_id)

            userDatabaseReference!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    userDatabaseReference!!.child("active_now").onDisconnect()
                        .setValue(ServerValue.TIMESTAMP)

                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })


        }


    }
}
