package com.mediclinic.onetoonechat.Profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mediclinic.onetoonechat.ProfileSetting.SettingsActivity
import com.mediclinic.onetoonechat.R
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class ProfileActivity : AppCompatActivity() {

    lateinit var mToolbar: Toolbar
    lateinit var sendFriendRequest_Button: Button
    lateinit var declineFriendRequest_Button: Button
    lateinit var profileName: TextView
    lateinit var profileStatus: TextView
    lateinit var u_work: TextView
    lateinit var go_my_profile: TextView
    lateinit var profileImage: ImageView
    lateinit var verified_icon: ImageView

    var userDatabaseReference: DatabaseReference? = null

    var friendRequestReference: DatabaseReference? = null
    var mAuth: FirebaseAuth? = null
    var CURRENT_STATE: String? = null

    lateinit var receiver_userID: String // Visited profile's id
    lateinit var senderID: String // Owner ID

    var friendsDatabaseReference: DatabaseReference? = null
    var notificationDatabaseReference: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        friendRequestReference = FirebaseDatabase.getInstance().reference.child("friend_requests")
        friendRequestReference!!.keepSynced(true) // for offline

        mAuth = FirebaseAuth.getInstance()
        senderID = mAuth!!.currentUser!!.uid // GET SENDER ID

        friendsDatabaseReference = FirebaseDatabase.getInstance().reference.child("friends")
        friendsDatabaseReference!!.keepSynced(true) // for offline

        notificationDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("notifications")
        notificationDatabaseReference!!.keepSynced(true) // for offline


        /**
         * Set Home Activity Toolbar Name
         */
        mToolbar = findViewById(R.id.single_profile_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // back on previous activity
        mToolbar!!.setNavigationOnClickListener {
            Log.d("tag", "onClick : navigating back to back activity ")
            finish()
        }

        receiver_userID = intent.extras!!.get("visitUserId")!!.toString()

        sendFriendRequest_Button = findViewById(R.id.visitUserFrndRqstSendButton)
        declineFriendRequest_Button = findViewById(R.id.visitUserFrndRqstDeclineButton)
        profileName = findViewById(R.id.visitUserProfileName)
        profileStatus = findViewById(R.id.visitUserProfileStatus)
        verified_icon = findViewById(R.id.visit_verified_icon)
        profileImage = findViewById(R.id.visit_user_profile_image)
        u_work = findViewById(R.id.visit_work)
        go_my_profile = findViewById(R.id.go_my_profile)

        verified_icon!!.visibility = View.INVISIBLE

        CURRENT_STATE = "not_friends"

        /**
         * Load every single users data
         */
        userDatabaseReference!!.child(receiver_userID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = dataSnapshot.child("user_name").value!!.toString()
                    val nickname = dataSnapshot.child("user_nickname").value!!.toString()
                    val status = dataSnapshot.child("user_status").value!!.toString()
                    val profession = dataSnapshot.child("user_profession").value!!.toString()
                    val image = dataSnapshot.child("user_image").value!!.toString()
                    val verified = dataSnapshot.child("verified").value!!.toString()

                    if (nickname.isEmpty()) {
                        profileName!!.text = name
                    } else {
                        val full_name = "$name ($nickname)"
                        profileName!!.text = full_name
                    }


                    if (profession.length > 2) {
                        u_work!!.text = "  $profession"
                    }
                    if (profession == "") {
                        u_work!!.text = "  Not provided yet"
                    }

                    profileStatus!!.text = status
                    Log.i("Image",image)
                    Picasso.get()
                        .load(image)
                        /*.placeholder(R.drawable.default_profile_image)*/
                        .into(profileImage)

                    if (verified.contains("true")) {
                        verified_icon!!.visibility = View.VISIBLE
                    }

                    // for fixing dynamic cancel / friend / unfriend / accept button
                    friendRequestReference!!.child(senderID)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // if in database has these data then, execute conditions below
                                if (dataSnapshot.hasChild(receiver_userID)) {
                                    val requestType = dataSnapshot.child(receiver_userID)
                                        .child("request_type").value!!.toString()

                                    if (requestType == "sent") {
                                        CURRENT_STATE = "request_sent"
                                        sendFriendRequest_Button!!.text = "Cancel Friend Request"

                                        declineFriendRequest_Button!!.visibility = View.INVISIBLE
                                        declineFriendRequest_Button!!.isEnabled = false

                                    } else if (requestType == "received") {
                                        CURRENT_STATE = "request_received"
                                        sendFriendRequest_Button!!.text = "Accept Friend Request"

                                        declineFriendRequest_Button!!.visibility = View.VISIBLE
                                        declineFriendRequest_Button!!.isEnabled = true


                                        declineFriendRequest_Button!!.setOnClickListener { declineFriendRequest() }

                                    }

                                } else {

                                    friendsDatabaseReference!!.child(senderID)
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    if (dataSnapshot.hasChild(receiver_userID)) {
                                                        CURRENT_STATE = "friends"
                                                        sendFriendRequest_Button!!.text =
                                                            "Unfriend This Person"

                                                        declineFriendRequest_Button!!.visibility =
                                                            View.INVISIBLE
                                                        declineFriendRequest_Button!!.isEnabled =
                                                            false

                                                    }
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {

                                            }
                                        })
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })

                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

        declineFriendRequest_Button!!.visibility = View.GONE
        declineFriendRequest_Button!!.isEnabled = false

        /** Send / Cancel / Accept / Unfriend >> request mechanism  */
        if (senderID != receiver_userID) { // condition for current owner / sender id
            sendFriendRequest_Button!!.setOnClickListener {
                sendFriendRequest_Button!!.isEnabled = false

                if (CURRENT_STATE == "not_friends") {
                    sendFriendRequest()

                } else if (CURRENT_STATE == "request_sent") {
                    cancelFriendRequest()

                } else if (CURRENT_STATE == "request_received") {
                    acceptFriendRequest()

                } else if (CURRENT_STATE == "friends") {
                    unfriendPerson()

                }
            }
        } else {
            sendFriendRequest_Button!!.visibility = View.INVISIBLE
            declineFriendRequest_Button!!.visibility = View.INVISIBLE
            go_my_profile!!.visibility = View.VISIBLE
            go_my_profile!!.setOnClickListener {
                val intent = Intent(this@ProfileActivity, SettingsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }


    } // ending OnCreate

    fun declineFriendRequest() {
        //for declination, delete data from friends_request nodes
        // delete from, sender >> receiver > values
        friendRequestReference!!.child(senderID).child(receiver_userID).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // delete from, receiver >> sender > values
                    friendRequestReference!!.child(receiver_userID).child(senderID).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // after deleting data, just set button attributes
                                sendFriendRequest_Button!!.isEnabled = true
                                CURRENT_STATE = "not_friends"
                                sendFriendRequest_Button!!.text = "Send Friend Request"

                                declineFriendRequest_Button!!.visibility = View.INVISIBLE
                                declineFriendRequest_Button!!.isEnabled = false

                            }
                        }

                }
            }
    }

    fun unfriendPerson() {
        //for unfriend, delete data from friends nodes
        // delete from, sender >> receiver > values
        friendsDatabaseReference!!.child(senderID).child(receiver_userID).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    friendsDatabaseReference!!.child(receiver_userID).child(senderID).removeValue()
                        .addOnCompleteListener {
                            sendFriendRequest_Button!!.isEnabled = true
                            CURRENT_STATE = "not_friends"
                            sendFriendRequest_Button!!.text = "Send Friend Request"

                            declineFriendRequest_Button!!.visibility = View.INVISIBLE
                            declineFriendRequest_Button!!.isEnabled = false
                        }
                }
            }
    }

    fun acceptFriendRequest() {
        //
        val myCalendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("EEEE, dd MMM, yyyy")
        val friendshipDate = currentDate.format(myCalendar.time)

        friendsDatabaseReference!!.child(senderID).child(receiver_userID).child("date")
            .setValue(friendshipDate)
            .addOnCompleteListener {
                friendsDatabaseReference!!.child(receiver_userID).child(senderID).child("date")
                    .setValue(friendshipDate)
                    .addOnCompleteListener {
                        /**
                         * because of accepting friend request,
                         * there have no more request them. So, for delete these node
                         */
                        /**
                         * because of accepting friend request,
                         * there have no more request them. So, for delete these node
                         */
                        /**
                         * because of accepting friend request,
                         * there have no more request them. So, for delete these node
                         */

                        /**
                         * because of accepting friend request,
                         * there have no more request them. So, for delete these node
                         */
                        friendRequestReference!!.child(senderID).child(receiver_userID)
                            .removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // delete from users friend_requests node, receiver >> sender > values
                                    friendRequestReference!!.child(receiver_userID).child(senderID)
                                        .removeValue()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // after deleting data, just set button attributes
                                                sendFriendRequest_Button!!.isEnabled = true
                                                CURRENT_STATE = "friends"
                                                sendFriendRequest_Button!!.text =
                                                    "Unfriend This Person"

                                                declineFriendRequest_Button!!.visibility =
                                                    View.INVISIBLE
                                                declineFriendRequest_Button!!.isEnabled = false
                                            }
                                        }

                                }
                            } //
                    }
            }
    }


    fun cancelFriendRequest() {
        //for cancellation, delete data from user nodes
        // delete from, sender >> receiver > values
        friendRequestReference!!.child(senderID).child(receiver_userID).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // delete from, receiver >> sender > values
                    friendRequestReference!!.child(receiver_userID).child(senderID).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // after deleting data, just set button attributes
                                sendFriendRequest_Button!!.isEnabled = true
                                CURRENT_STATE = "not_friends"
                                sendFriendRequest_Button!!.text = "Send Friend Request"

                                declineFriendRequest_Button!!.visibility = View.INVISIBLE
                                declineFriendRequest_Button!!.isEnabled = false

                            }
                        }

                }
            }

    }


    fun sendFriendRequest() {
        // insert or, put data to >> sender >> receiver >> request_type >> sent
        friendRequestReference!!.child(senderID).child(receiver_userID)
            .child("request_type").setValue("sent")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // change or, put data to >> receiver >> sender>> request_type >> received
                    friendRequestReference!!.child(receiver_userID).child(senderID)
                        .child("request_type").setValue("received")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                //Request notification mechanism
                                val notificationData = HashMap<String, String>()
                                notificationData["from"] = senderID
                                notificationData["type"] = "request"

                                notificationDatabaseReference!!.child(receiver_userID).push()
                                    .setValue(notificationData)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Request main mechanism
                                            sendFriendRequest_Button!!.isEnabled = true
                                            CURRENT_STATE = "request_sent"
                                            sendFriendRequest_Button!!.text =
                                                "Cancel Friend Request"

                                            declineFriendRequest_Button!!.visibility =
                                                View.INVISIBLE
                                            declineFriendRequest_Button!!.isEnabled = false
                                        }
                                    }

                            }
                        }


                }
            }


    }


}
