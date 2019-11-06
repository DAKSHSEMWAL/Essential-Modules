package com.example.chatapp.activity


import android.os.Build
import android.os.Bundle
import android.view.View

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.app.sample.fchat.adapter.FriendsListAdapter
import com.app.sample.fchat.util.CustomToast
import com.example.chatapp.R
import com.example.chatapp.data.ParseFirebaseData
import com.example.chatapp.data.Tools
import com.example.chatapp.model.Friend
import com.example.chatapp.widget.DividerItemDecoration
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.ArrayList

class SelectFriendActivity : AppCompatActivity() {

    private var actionBar: ActionBar? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: FriendsListAdapter? = null
    lateinit var friendList: List<Friend>
    lateinit var pfbd: ParseFirebaseData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_chat)
        initToolbar()
        initComponent()
        friendList = ArrayList()
        pfbd = ParseFirebaseData(this)

        val ref = FirebaseDatabase.getInstance().getReference(USERS_CHILD)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // TODO: 25-05-2017 if number of items is 0 then show something else
                mAdapter = FriendsListAdapter(this@SelectFriendActivity, pfbd.getAllUser(dataSnapshot))
                recyclerView!!.adapter = mAdapter

                mAdapter!!.setOnItemClickListener(object : FriendsListAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, obj: Friend, position: Int) {
                        ChatDetailsActivity.navigate(this@SelectFriendActivity, findViewById(R.id.lyt_parent), obj)
                    }
                })

                bindView()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                CustomToast(this@SelectFriendActivity).showError(getString(R.string.error_could_not_connect))
            }
        })

        // for system bar in lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Tools.systemBarLolipop(this)
        }
    }

    private fun initComponent() {
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView

        // use a linear layout manager
        val mLayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST))
    }

    fun initToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        //        actionBar.setSubtitle(Constant.getFriendsData(this).size()+" friends");
    }

    fun bindView() {
        try {
            mAdapter!!.notifyDataSetChanged()
        } catch (e: Exception) {
        }

    }

    companion object {

        val USERS_CHILD = "users"
    }
}
