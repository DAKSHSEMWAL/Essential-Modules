package com.example.chatapp.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.app.sample.fchat.adapter.ChatsListAdapter
import com.example.chatapp.R
import com.example.chatapp.activity.ChatDetailsActivity
import com.example.chatapp.data.ParseFirebaseData
import com.example.chatapp.data.SettingsAPI
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.util.Constants
import com.example.chatapp.widget.DividerItemDecoration
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatsFragment : Fragment() {

    lateinit var recyclerView: RecyclerView

    private var mLayoutManager: LinearLayoutManager? = null
    lateinit var mAdapter: ChatsListAdapter
    private var progressBar: ProgressBar? = null

    internal lateinit var valueEventListener: ValueEventListener
    internal lateinit var ref: DatabaseReference

    internal lateinit var view: View

    internal lateinit var pfbd: ParseFirebaseData
    internal lateinit var set: SettingsAPI

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        view = inflater.inflate(R.layout.fragment_chat, container, false)
        pfbd = ParseFirebaseData(context)
        set = SettingsAPI(context)

        // activate fragment menu
        setHasOptionsMenu(true)

        recyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView
        progressBar = view.findViewById<View>(R.id.progressBar) as ProgressBar

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL_LIST))


        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(Constants.LOG_TAG, "Data changed from fragment")
                if (dataSnapshot.value != null) {
                    mAdapter = ChatsListAdapter(context!!, pfbd.getAllLastMessages(dataSnapshot))
                    recyclerView.adapter = mAdapter
                    recyclerView.visibility=View.VISIBLE
                    mAdapter.setOnItemClickListener(object : ChatsListAdapter.OnItemClickListener {
                        override fun onItemClick(v: View, obj: ChatMessage, position: Int) {
                            if (obj.receiver.id == set.readSetting(Constants.PREF_MY_ID))
                                ChatDetailsActivity.navigate(activity, v.findViewById(R.id.lyt_parent), obj.sender)
                            else if (obj.sender.id == set.readSetting(Constants.PREF_MY_ID))
                                ChatDetailsActivity.navigate(activity, v.findViewById(R.id.lyt_parent), obj.receiver)
                        }
                    })
                }
                else
                {
                    progressBar!!.visibility = View.GONE
                    recyclerView.visibility=View.GONE
                    nomessage.visibility=View.VISIBLE
                }
                bindView()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        ref = FirebaseDatabase.getInstance().getReference(Constants.MESSAGE_CHILD)
        ref.addValueEventListener(valueEventListener)

        return view
    }

    fun bindView() {
        try {
            mAdapter.notifyDataSetChanged()
            progressBar!!.visibility = View.GONE
        } catch (e: Exception) {
        }

    }

    override fun onDestroy() {
        //Remove the listener, otherwise it will continue listening in the background
        //We have service to run in the background
        ref.removeEventListener(valueEventListener)
        super.onDestroy()
    }
}
