package com.mediclinic.onetoonechat.Friends

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.mediclinic.onetoonechat.Chat.ChatActivity
import com.mediclinic.onetoonechat.Model.Friends
import com.mediclinic.onetoonechat.Profile.ProfileActivity
import com.mediclinic.onetoonechat.R
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView

class FriendsActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var friend_list_RV: RecyclerView? = null

    private var friendsDatabaseReference: DatabaseReference? = null
    private var userDatabaseReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    internal lateinit var current_user_id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        toolbar = findViewById(R.id.friends_appbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Friends"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        current_user_id = mAuth!!.currentUser!!.uid

        friendsDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("friends").child(current_user_id)
        friendsDatabaseReference!!.keepSynced(true) // for offline

        userDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        userDatabaseReference!!.keepSynced(true) // for offline


        // Setup recycler view
        friend_list_RV = findViewById(R.id.friendList)
        friend_list_RV!!.setHasFixedSize(true)
        friend_list_RV!!.layoutManager = LinearLayoutManager(this)

        showPeopleList()
    }

    /**
     * FirebaseUI for Android — UI Bindings for Firebase
     *
     * Library link- https://github.com/firebase/FirebaseUI-Android
     */
    private fun showPeopleList() {
        val recyclerOptions = FirebaseRecyclerOptions.Builder<Friends>()
            .setQuery(friendsDatabaseReference!!, Friends::class.java)
            .build()

        val recyclerAdapter =
            object : FirebaseRecyclerAdapter<Friends, FriendsVH>(recyclerOptions) {
                override fun onBindViewHolder(holder: FriendsVH, position: Int, model: Friends) {
                    holder.date.text = "Friendship date -\n" + model.date!!

                    val userID = getRef(position).key

                    userDatabaseReference!!.child(userID!!)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val userName = dataSnapshot.child("user_name").value!!.toString()
                                val userThumbPhoto =
                                    dataSnapshot.child("user_thumb_image").value!!.toString()
                                val active_status =
                                    dataSnapshot.child("active_now").value!!.toString()

                                // online active status
                                holder.active_icon.visibility = View.GONE
                                if (active_status.contains("active_now")) {
                                    holder.active_icon.visibility = View.VISIBLE
                                } else {
                                    holder.active_icon.visibility = View.GONE
                                }

                                holder.name.text = userName
                                Picasso.get()
                                    .load(userThumbPhoto)
                                    .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                                    .placeholder(R.drawable.default_profile_image)
                                    .into(holder.profile_thumb)


                                //click item, 2 options in a dialogue will be appear
                                holder.itemView.setOnClickListener {
                                    val options =
                                        arrayOf<CharSequence>("Send Message", "$userName's profile")
                                    val builder = AlertDialog.Builder(this@FriendsActivity)
                                    builder.setItems(options) { dialog, which ->
                                        if (which == 0) {
                                            // user active status validation
                                            if (dataSnapshot.child("active_now").exists()) {

                                                val chatIntent = Intent(
                                                    this@FriendsActivity,
                                                    ChatActivity::class.java
                                                )
                                                chatIntent.putExtra("visitUserId", userID)
                                                chatIntent.putExtra("userName", userName)
                                                startActivity(chatIntent)

                                            } else {
                                                userDatabaseReference!!.child(userID)
                                                    .child("active_now")
                                                    .setValue(ServerValue.TIMESTAMP)
                                                    .addOnSuccessListener {
                                                        val chatIntent = Intent(
                                                            this@FriendsActivity,
                                                            ChatActivity::class.java
                                                        )
                                                        chatIntent.putExtra("visitUserId", userID)
                                                        chatIntent.putExtra("userName", userName)
                                                        startActivity(chatIntent)
                                                    }


                                            }

                                        }

                                        if (which == 1) {
                                            val profileIntent = Intent(
                                                this@FriendsActivity,
                                                ProfileActivity::class.java
                                            )
                                            profileIntent.putExtra("visitUserId", userID)
                                            startActivity(profileIntent)
                                        }
                                    }
                                    builder.show()
                                }


                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })


                }

                override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FriendsVH {
                    val view = LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.all_single_profile_display, viewGroup, false)
                    return FriendsVH(view)
                }
            }

        friend_list_RV!!.adapter = recyclerAdapter
        recyclerAdapter.startListening()
    }

    class FriendsVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        internal var date: TextView
        internal var profile_thumb: CircleImageView
        internal var active_icon: ImageView

        init {
            name = itemView.findViewById(R.id.all_user_name)
            date = itemView.findViewById(R.id.all_user_status)
            profile_thumb = itemView.findViewById(R.id.all_user_profile_img)
            active_icon = itemView.findViewById(R.id.activeIcon)
        }
    }


}
