package com.mediclinic.onetoonechat.Fragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.mediclinic.onetoonechat.Chat.ChatActivity
import com.mediclinic.onetoonechat.Model.Friends
import com.mediclinic.onetoonechat.R
import com.mediclinic.onetoonechat.Utils.UserLastSeenTime
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView


/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment() {

    private var chat_list: RecyclerView? = null

    private var friendsDatabaseReference: DatabaseReference? = null
    private var userDatabaseReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    internal lateinit var current_user_id: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view= inflater.inflate(R.layout.fragment_chats, container, false)

        chat_list = view!!.findViewById(R.id.chatList)

        mAuth = FirebaseAuth.getInstance()
        current_user_id = mAuth!!.currentUser!!.uid

        friendsDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("friends").child(current_user_id)
        userDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

        chat_list!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)

        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        chat_list!!.layoutManager = linearLayoutManager



        return view
    }

    override fun onStart() {
        super.onStart()

        val recyclerOptions = FirebaseRecyclerOptions.Builder<Friends>()
            .setQuery(friendsDatabaseReference!!, Friends::class.java)
            .build()

        val adapter = object : FirebaseRecyclerAdapter<Friends, ChatsVH>(recyclerOptions) {
            override fun onBindViewHolder(holder: ChatsVH, position: Int, model: Friends) {
                val userID = getRef(position).key
                userDatabaseReference!!.child(userID!!)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val userName = dataSnapshot.child("user_name").value!!.toString()
                                val userPresence =
                                    dataSnapshot.child("active_now").value!!.toString()
                                val userThumbPhoto =
                                    dataSnapshot.child("user_thumb_image").value!!.toString()

                                if (userThumbPhoto != "default_image") { // default image condition for new user
                                    Picasso.get()
                                        .load(userThumbPhoto)
                                        .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(holder.user_photo, object : Callback {
                                            override fun onSuccess() {}

                                            override fun onError(e: Exception) {
                                                Picasso.get()
                                                    .load(userThumbPhoto)
                                                    .placeholder(R.drawable.default_profile_image)
                                                    .into(holder.user_photo)
                                            }
                                        })
                                }
                                holder.user_name.text = userName

                                //active status
                                holder.active_icon.visibility = View.GONE
                                if (userPresence.contains("true")) {
                                    holder.user_presence.text = "Active now"
                                    holder.active_icon.visibility = View.VISIBLE
                                } else {
                                    holder.active_icon.visibility = View.GONE
                                    val lastSeenTime = UserLastSeenTime()
                                    val last_seen = java.lang.Long.parseLong(userPresence)
                                    val lastSeenOnScreenTime =
                                        lastSeenTime.getTimeAgo(last_seen, context)
                                    Log.e("lastSeenTime", lastSeenOnScreenTime)
                                    if (lastSeenOnScreenTime != null) {
                                        holder.user_presence.setText(lastSeenOnScreenTime)
                                    }
                                }


                                holder.itemView.setOnClickListener {
                                    // user active status validation
                                    if (dataSnapshot.child("active_now").exists()) {

                                        val chatIntent = Intent(context, ChatActivity::class.java)
                                        chatIntent.putExtra("visitUserId", userID)
                                        chatIntent.putExtra("userName", userName)
                                        startActivity(chatIntent)

                                    } else {
                                        userDatabaseReference!!.child(userID).child("active_now")
                                            .setValue(ServerValue.TIMESTAMP).addOnSuccessListener {
                                                val chatIntent =
                                                    Intent(context, ChatActivity::class.java)
                                                chatIntent.putExtra("visitUserId", userID)
                                                chatIntent.putExtra("userName", userName)
                                                startActivity(chatIntent)
                                            }


                                    }
                                }
                            }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })

            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ChatsVH {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.all_single_profile_display, viewGroup, false)
                return ChatsVH(view)
            }
        }

        chat_list!!.adapter = adapter
        adapter.startListening()
    }

    class ChatsVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var user_name: TextView
        internal var user_presence: TextView
        internal var user_photo: CircleImageView
        internal var active_icon: ImageView

        init {
            user_name = itemView.findViewById(R.id.all_user_name)
            user_photo = itemView.findViewById(R.id.all_user_profile_img)
            user_presence = itemView.findViewById(R.id.all_user_status)
            active_icon = itemView.findViewById(R.id.activeIcon)
        }
    }


}// Required empty public constructor
