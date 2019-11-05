package com.mediclinic.onetoonechat.Chat

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mediclinic.onetoonechat.Adapter.MessageAdapter
import com.mediclinic.onetoonechat.Model.Message
import com.mediclinic.onetoonechat.R
import com.mediclinic.onetoonechat.Utils.UserLastSeenTime
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

import java.util.ArrayList
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask

import de.hdodenhof.circleimageview.CircleImageView
import xyz.hasnat.sweettoast.SweetToast


class ChatActivity : AppCompatActivity() {

    lateinit var messageReceiverID: String
    lateinit var messageReceiverName: String

    lateinit var chatToolbar: Toolbar
    lateinit var chatUserName: TextView
    lateinit var chatUserActiveStatus: TextView
    lateinit var ChatConnectionTV: TextView
    lateinit var chatUserImageView: CircleImageView

    lateinit var rootReference: DatabaseReference

    // sending message
    lateinit var send_message: ImageView
    lateinit var send_image: ImageView
    lateinit var input_user_message: EditText
    lateinit var mAuth: FirebaseAuth
    lateinit var messageSenderId: String
    lateinit var download_url: String

    lateinit var messageList_ReCyVw: RecyclerView
    val messageList = ArrayList<Message>()
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var messageAdapter: MessageAdapter
    lateinit var imageMessageStorageRef: StorageReference

    lateinit var connectivityReceiver: ConnectivityReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rootReference = FirebaseDatabase.getInstance().reference

        mAuth = FirebaseAuth.getInstance()
        messageSenderId = mAuth!!.currentUser!!.uid

        messageReceiverID = intent.extras!!.get("visitUserId")!!.toString()
        messageReceiverName = intent.extras!!.get("userName")!!.toString()

        imageMessageStorageRef = FirebaseStorage.getInstance().reference.child("messages_image")

        // appbar / toolbar
        chatToolbar = findViewById(R.id.chats_appbar)
        setSupportActionBar(chatToolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowCustomEnabled(true)

        val layoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.appbar_chat, null)
        actionBar.customView = view

        ChatConnectionTV = findViewById(R.id.ChatConnectionTV)
        chatUserName = findViewById(R.id.chat_user_name)
        chatUserActiveStatus = findViewById(R.id.chat_active_status)
        chatUserImageView = findViewById(R.id.chat_profile_image)

        // sending message declaration
        send_message = findViewById(R.id.c_send_message_BTN)
        send_image = findViewById(R.id.c_send_image_BTN)
        input_user_message = findViewById(R.id.c_input_message)

        // setup for showing messages
        messageAdapter = MessageAdapter(messageList)
        messageList_ReCyVw = findViewById(R.id.message_list)
        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager!!.stackFromEnd = true
        messageList_ReCyVw!!.layoutManager = linearLayoutManager
        messageList_ReCyVw!!.setHasFixedSize(true)
        //linearLayoutManager.setReverseLayout(true);
        messageList_ReCyVw!!.adapter = messageAdapter

        fetchMessages()

        chatUserName!!.text = messageReceiverName
        rootReference!!.child("users").child(messageReceiverID!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val active_status = dataSnapshot.child("active_now").value!!.toString()
                    val thumb_image = dataSnapshot.child("user_thumb_image").value!!.toString()

                    //                        // FOR TESTING
                    //                        if (currentUser != null){
                    //                            rootReference.child("active_now").setValue(ServerValue.TIMESTAMP);
                    //                        }

                    // show image on appbar
                    Picasso.get()
                        .load(thumb_image)
                        .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                        .placeholder(R.drawable.default_profile_image)
                        .into(chatUserImageView!!, object : Callback {
                            override fun onSuccess() {}
                            override fun onError(e: Exception) {
                                Picasso.get()
                                    .load(thumb_image)
                                    .placeholder(R.drawable.default_profile_image)
                                    .into(chatUserImageView)
                            }
                        })

                    //active status
                    if (active_status.contains("true")) {
                        chatUserActiveStatus!!.text = "Active now"
                    } else {
                        val lastSeenTime = UserLastSeenTime()
                        val last_seen = java.lang.Long.parseLong(active_status)

                        //String lastSeenOnScreenTime = lastSeenTime.getTimeAgo(last_seen).toString();
                        val lastSeenOnScreenTime =
                            lastSeenTime.getTimeAgo(last_seen, applicationContext).toString()
                        Log.e("lastSeenTime", lastSeenOnScreenTime)
                        if (lastSeenOnScreenTime != null) {
                            chatUserActiveStatus!!.setText(lastSeenOnScreenTime)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })


        /**
         * SEND TEXT MESSAGE BUTTON
         */
        send_message!!.setOnClickListener { sendMessage() }


        /** SEND IMAGE MESSAGE BUTTON  */
        send_image!!.setOnClickListener {
            val galleryIntent = Intent().setAction(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_PICK_CODE)
        }
    } // ending onCreate


    override fun onResume() {
        super.onResume()
        //Register Connectivity Broadcast receiver
        connectivityReceiver = ConnectivityReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        // Unregister Connectivity Broadcast receiver
        unregisterReceiver(connectivityReceiver)
    }


    override// for gallery picking
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //  For image sending
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data

            // image message sending size compressing will be placed below

            val message_sender_reference = "messages/$messageSenderId/$messageReceiverID"
            val message_receiver_reference = "messages/$messageReceiverID/$messageSenderId"

            val user_message_key = rootReference!!.child("messages").child(messageSenderId!!)
                .child(messageReceiverID!!).push()
            val message_push_id = user_message_key.key

            val file_path = imageMessageStorageRef!!.child(message_push_id!! + ".jpg")

            val uploadTask = file_path.putFile(imageUri!!)
            val uriTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    SweetToast.error(this@ChatActivity, "Error: " + task.exception!!.message)
                }
                download_url = file_path.downloadUrl.toString()
                file_path.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.isSuccessful) {
                        download_url = task.result!!.toString()
                        //Toast.makeText(ChatActivity.this, "From ChatActivity, link: " +download_url, Toast.LENGTH_SHORT).show();

                        val message_text_body = HashMap<String, Any>()
                        message_text_body["message"] = download_url
                        message_text_body["seen"] = false
                        message_text_body["type"] = "image"
                        message_text_body["time"] = ServerValue.TIMESTAMP
                        message_text_body["from"] = messageSenderId

                        val messageBodyDetails = HashMap<String, Any>()
                        messageBodyDetails["$message_sender_reference/$message_push_id"] =
                            message_text_body
                        messageBodyDetails["$message_receiver_reference/$message_push_id"] =
                            message_text_body

                        rootReference!!.updateChildren(messageBodyDetails) { databaseError, databaseReference ->
                            if (databaseError != null) {
                                Log.e("from_image_chat: ", databaseError.message)
                            }
                            input_user_message!!.setText("")
                        }
                        Log.e("tag", "Image sent successfully")
                    } else {
                        SweetToast.warning(this@ChatActivity, "Failed to send image. Try again")
                    }
                }
            }
        }
    }

    fun fetchMessages() {
        rootReference!!.child("messages").child(messageSenderId!!).child(messageReceiverID!!)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    if (dataSnapshot.exists()) {
                        val message = dataSnapshot.getValue(Message::class.java) as Message
                        messageList.add(message)
                        messageAdapter!!.notifyDataSetChanged()
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }


    fun sendMessage() {
        val message = input_user_message!!.text.toString()
        if (TextUtils.isEmpty(message)) {
            SweetToast.info(this@ChatActivity, "Please type a message")
        } else {
            val message_sender_reference = "messages/$messageSenderId/$messageReceiverID"
            val message_receiver_reference = "messages/$messageReceiverID/$messageSenderId"

            val user_message_key = rootReference!!.child("messages").child(messageSenderId!!)
                .child(messageReceiverID!!).push()
            val message_push_id = user_message_key.key

            val message_text_body = HashMap<String, Any>()
            message_text_body["message"] = message
            message_text_body["seen"] = false
            message_text_body["type"] = "text"
            message_text_body["time"] = ServerValue.TIMESTAMP
            message_text_body["from"] = messageSenderId

            val messageBodyDetails = HashMap<String, Any>()
            messageBodyDetails["$message_sender_reference/$message_push_id"] = message_text_body
            messageBodyDetails["$message_receiver_reference/$message_push_id"] = message_text_body

            rootReference!!.updateChildren(messageBodyDetails) { databaseError, databaseReference ->
                if (databaseError != null) {
                    Log.e("Sending message", databaseError.message)
                }
                input_user_message!!.setText("")
            }
        }
    }


    // Broadcast receiver for network checking
    inner class ConnectivityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            ChatConnectionTV!!.visibility = View.GONE
            if (networkInfo != null && networkInfo.isConnected) {
                ChatConnectionTV!!.text = "Internet connected"
                ChatConnectionTV!!.setTextColor(Color.WHITE)
                ChatConnectionTV!!.visibility = View.VISIBLE

                // LAUNCH activity after certain time period
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        this@ChatActivity.runOnUiThread {
                            ChatConnectionTV!!.visibility = View.GONE
                        }
                    }
                }, 1200)
            } else {
                ChatConnectionTV!!.text = "No internet connection! "
                ChatConnectionTV!!.setTextColor(Color.WHITE)
                ChatConnectionTV!!.setBackgroundColor(Color.RED)
                ChatConnectionTV!!.visibility = View.VISIBLE
            }
        }
    }

    companion object {

        private val GALLERY_PICK_CODE = 2
    }

}
