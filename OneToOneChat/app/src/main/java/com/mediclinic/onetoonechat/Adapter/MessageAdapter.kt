package com.mediclinic.onetoonechat.Adapter

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.makeramen.roundedimageview.RoundedImageView
import com.mediclinic.onetoonechat.Model.Message
import com.mediclinic.onetoonechat.R
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(private val messageList: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var mAuth: FirebaseAuth? = null
    private var databaseReference: DatabaseReference? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_message_layout, parent, false)
        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(view)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val sender_UID = mAuth!!.currentUser!!.uid
        val message = messageList[position]

        val from_user_ID = message.from
        val from_message_TYPE = message.type

        databaseReference =
            FirebaseDatabase.getInstance().reference.child("users").child(from_user_ID!!)
        databaseReference!!.keepSynced(true) // for offline
        databaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userName = dataSnapshot.child("user_name").value!!.toString()
                    val userProfileImage = dataSnapshot.child("user_thumb_image").value!!.toString()
                    //
                    Picasso.get()
                        .load(userProfileImage)
                        .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                        .placeholder(R.drawable.default_profile_image)
                        .into(holder.user_profile_image)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })


        // if message type is TEXT
        if (from_message_TYPE == "text") {
            holder.receiver_text_message.visibility = INVISIBLE
            holder.user_profile_image.visibility = INVISIBLE

            // when msg is TEXT, image views are gone
            holder.senderImageMsg.visibility = GONE
            holder.receiverImageMsg.visibility = GONE

            if (from_user_ID == sender_UID) {
                holder.sender_text_message.setBackgroundResource(R.drawable.single_message_text_another_background)
                holder.sender_text_message.setTextColor(Color.BLACK)
                holder.sender_text_message.gravity = Gravity.START
                holder.sender_text_message.text = message.message
            } else {
                holder.sender_text_message.visibility = INVISIBLE
                holder.receiver_text_message.visibility = VISIBLE
                holder.user_profile_image.visibility = VISIBLE

                holder.receiver_text_message.setBackgroundResource(R.drawable.single_message_text_background)
                holder.receiver_text_message.setTextColor(Color.WHITE)
                holder.receiver_text_message.gravity = Gravity.START
                holder.receiver_text_message.text = message.message
            }
        }
        if (from_message_TYPE == "image") { // if message type is NON TEXT
            // when msg has IMAGE, text views are GONE
            holder.sender_text_message.visibility = GONE
            holder.receiver_text_message.visibility = GONE

            if (from_user_ID == sender_UID) {
                holder.user_profile_image.visibility = GONE
                holder.receiverImageMsg.visibility = GONE
                /*holder.senderImageMsg.visibility = VISIBLE*/
                Picasso.get()
                    .load(message.message)
                    .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                    //.placeholder(R.drawable.default_profile_image)
                    .into(holder.senderImageMsg)
                Log.e("tag", "from adapter, link : " + message.message)
            } else {
                holder.user_profile_image.visibility = VISIBLE
                holder.senderImageMsg.visibility = GONE
                /*holder.receiverImageMsg.visibility = VISIBLE*/
                Picasso.get()
                    .load(message.message)
                    .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                    //.placeholder(R.drawable.default_profile_image)
                    .into(holder.receiverImageMsg)
                Log.e("tag", "from adapter, link : " + message.message)

            }
        }

    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class MessageViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal var sender_text_message: TextView
        internal var receiver_text_message: TextView
        internal var user_profile_image: CircleImageView
        internal var senderImageMsg: RoundedImageView
        internal var receiverImageMsg: RoundedImageView

        init {
            sender_text_message = view.findViewById(R.id.senderMessageText)
            receiver_text_message = view.findViewById(R.id.receiverMessageText)
            user_profile_image = view.findViewById(R.id.messageUserImage)

            senderImageMsg = view.findViewById(R.id.messageImageVsender)
            receiverImageMsg = view.findViewById(R.id.messageImageVreceiver)
        }

    }
}
