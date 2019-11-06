package com.example.chatapp.activity


import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity

import com.app.sample.fchat.adapter.ChatDetailsListAdapter
import com.app.sample.fchat.util.CustomToast
import com.example.chatapp.R
import com.example.chatapp.data.ParseFirebaseData
import com.example.chatapp.data.SettingsAPI
import com.example.chatapp.data.Tools
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.model.Friend
import com.example.chatapp.util.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

import java.util.ArrayList
import java.util.HashMap

import com.example.chatapp.util.Constants.NODE_IS_READ
import com.example.chatapp.util.Constants.NODE_RECEIVER_ID
import com.example.chatapp.util.Constants.NODE_RECEIVER_NAME
import com.example.chatapp.util.Constants.NODE_RECEIVER_PHOTO
import com.example.chatapp.util.Constants.NODE_SENDER_ID
import com.example.chatapp.util.Constants.NODE_SENDER_NAME
import com.example.chatapp.util.Constants.NODE_SENDER_PHOTO
import com.example.chatapp.util.Constants.NODE_TEXT
import com.example.chatapp.util.Constants.NODE_TIMESTAMP

class ChatDetailsActivity : AppCompatActivity() {

    private var btn_send: Button? = null
    private var et_content: EditText? = null
    lateinit var mAdapter: ChatDetailsListAdapter

    private var listview: ListView? = null
    private var actionBar: ActionBar? = null
    private var friend: Friend? = null
    private val items = ArrayList<ChatMessage>()
    private var parent_view: View? = null
    lateinit var  pfbd: ParseFirebaseData
    lateinit var set: SettingsAPI

    lateinit var chatNode: String
    lateinit var chatNode_1: String
    lateinit var chatNode_2: String

    lateinit var ref: DatabaseReference
    lateinit var valueEventListener: ValueEventListener

    private val contentWatcher = object : TextWatcher {
        override fun afterTextChanged(etd: Editable) {
            if (etd.toString().trim { it <= ' ' }.length == 0) {
                btn_send!!.visibility = View.GONE
            } else {
                btn_send!!.visibility = View.VISIBLE
            }
            //draft.setContent(etd.toString());
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}

        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_details)
        parent_view = findViewById(android.R.id.content)
        pfbd = ParseFirebaseData(this)
        set = SettingsAPI(this)

        // animation transition
        ViewCompat.setTransitionName(parent_view!!, KEY_FRIEND)

        // initialize conversation data
        val intent = intent
        friend = intent.extras!!.getSerializable(KEY_FRIEND) as Friend?
        initToolbar()

        iniComponen()
        chatNode_1 = set.readSetting(Constants.PREF_MY_ID) + "-" + friend!!.id
        chatNode_2 = friend!!.id + "-" + set.readSetting(Constants.PREF_MY_ID)

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(Constants.LOG_TAG, "Data changed from activity")
                if (dataSnapshot.hasChild(chatNode_1)) {
                    chatNode = chatNode_1
                } else if (dataSnapshot.hasChild(chatNode_2)) {
                    chatNode = chatNode_2
                } else {
                    chatNode = chatNode_1
                }
                items.clear()
                items.addAll(pfbd.getMessagesForSingleUser(dataSnapshot.child(chatNode)))

                //Here we are traversing all the messages and mark all received messages read

                for (data in dataSnapshot.child(chatNode).children) {
                    if (data.child(NODE_RECEIVER_ID).value!!.toString() == set.readSetting(Constants.PREF_MY_ID)) {
                        data.child(NODE_IS_READ).ref.runTransaction(object : Transaction.Handler {
                            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                mutableData.value = true
                                return Transaction.success(mutableData)
                            }

                            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {

                            }
                        })
                    }
                }

                // TODO: 12/09/18 Change it to recyclerview
                mAdapter = ChatDetailsListAdapter(this@ChatDetailsActivity, items)
                listview!!.adapter = mAdapter
                listview!!.requestFocus()
                registerForContextMenu(listview)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                CustomToast(this@ChatDetailsActivity).showError(getString(R.string.error_could_not_connect))
            }
        }

        ref = FirebaseDatabase.getInstance().getReference(Constants.MESSAGE_CHILD)
        ref.addValueEventListener(valueEventListener)

        // for system bar in lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Tools.systemBarLolipop(this)
        }
    }

    fun initToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        actionBar!!.title = friend!!.name
    }

    fun iniComponen() {
        listview = findViewById<View>(R.id.listview) as ListView
        btn_send = findViewById<View>(R.id.btn_send) as Button
        et_content = findViewById<View>(R.id.text_content) as EditText
        btn_send!!.setOnClickListener {
            //                ChatMessage im=new ChatMessage(et_content.getText().toString(), String.valueOf(System.currentTimeMillis()),friend.getId(),friend.getName(),friend.getPhoto());

            val hm = HashMap<String,String>()
            hm.put(NODE_TEXT, et_content!!.text.toString())
            hm.put(NODE_TIMESTAMP, System.currentTimeMillis().toString())
            hm.put(NODE_RECEIVER_ID, friend!!.id)
            hm.put(NODE_RECEIVER_NAME, friend!!.name)
            hm.put(NODE_RECEIVER_PHOTO, friend!!.photo)
            hm.put(NODE_SENDER_ID, set.readSetting(Constants.PREF_MY_ID))
            hm.put(NODE_SENDER_NAME, set.readSetting(Constants.PREF_MY_NAME))
            hm.put(NODE_SENDER_PHOTO, set.readSetting(Constants.PREF_MY_DP))
            hm.put(NODE_IS_READ, false.toString())

            ref.child(chatNode).push().setValue(hm)
            et_content!!.setText("")
            hideKeyboard()
        }
        et_content!!.addTextChangedListener(contentWatcher)
        if (et_content!!.length() == 0) {
            btn_send!!.visibility = View.GONE
        }
        hideKeyboard()
    }


    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        //Remove the listener, otherwise it will continue listening in the background
        //We have service to run in the background
        ref.removeEventListener(valueEventListener)
        super.onDestroy()
    }

    companion object {
        var KEY_FRIEND = "FRIEND"

        // give preparation animation activity transition
        fun navigate(activity: FragmentActivity?, transitionImage: View, obj: Friend) {
            val intent = Intent(activity, ChatDetailsActivity::class.java)
            intent.putExtra(KEY_FRIEND, obj)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, transitionImage, KEY_FRIEND)
            ActivityCompat.startActivity(activity, intent, options.toBundle())
        }

    }
}
