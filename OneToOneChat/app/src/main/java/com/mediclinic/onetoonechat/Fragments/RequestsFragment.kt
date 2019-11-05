package com.mediclinic.onetoonechat.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mediclinic.onetoonechat.Model.Requests
import com.mediclinic.onetoonechat.Profile.ProfileActivity
import com.mediclinic.onetoonechat.R
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

import java.text.SimpleDateFormat
import java.util.Calendar

import de.hdodenhof.circleimageview.CircleImageView


/**
 * A simple [Fragment] subclass.
 */
class RequestsFragment : Fragment() {

    private var request_list: RecyclerView? = null

    private var databaseReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    lateinit internal var user_UId: String
    private var userDatabaseReference: DatabaseReference? = null

    // for accept and cancel mechanism
    private var friendsDatabaseReference: DatabaseReference? = null
    private var friendReqDatabaseReference: DatabaseReference? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_requests, container, false)

        request_list = view!!.findViewById(R.id.requestList)
        request_list!!.setHasFixedSize(true)

        mAuth = FirebaseAuth.getInstance()
        user_UId = mAuth!!.currentUser!!.uid
        userDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        databaseReference =
            FirebaseDatabase.getInstance().reference.child("friend_requests").child(user_UId)

        friendsDatabaseReference = FirebaseDatabase.getInstance().reference.child("friends")
        friendReqDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("friend_requests")


        val linearLayoutManager = LinearLayoutManager(getContext())
        //linearLayoutManager.setStackFromEnd(true);
        request_list!!.setHasFixedSize(true)
        request_list!!.layoutManager = linearLayoutManager


        return view
    }


    override fun onStart() {
        super.onStart()

        val recyclerOptions = FirebaseRecyclerOptions.Builder<Requests>()
            .setQuery(databaseReference!!, Requests::class.java)
            .build()

        val adapter = object : FirebaseRecyclerAdapter<Requests, RequestsVH>(recyclerOptions) {
            override fun onBindViewHolder(holder: RequestsVH, position: Int, model: Requests) {
                val userID = getRef(position).key
                // handling accept/cancel button
                val getTypeReference = getRef(position).child("request_type").ref
                getTypeReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val requestType = dataSnapshot.value!!.toString()
                            holder.verified_icon.visibility = View.GONE

                            if (requestType == "received") {
                                holder.re_icon.visibility = View.VISIBLE
                                holder.se_icon.visibility = View.GONE
                                userDatabaseReference!!.child(userID!!)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            val userName =
                                                dataSnapshot.child("user_name").value!!.toString()
                                            val userVerified =
                                                dataSnapshot.child("verified").value!!.toString()
                                            val userThumbPhoto =
                                                dataSnapshot.child("user_thumb_image").value!!.toString()
                                            val user_status =
                                                dataSnapshot.child("user_status").value!!.toString()

                                            holder.name.text = userName
                                            holder.status.text = user_status

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

                                            if (userVerified.contains("true")) {
                                                holder.verified_icon.visibility = View.VISIBLE
                                            }

                                            holder.itemView.setOnClickListener {
                                                val options = arrayOf<CharSequence>(
                                                    "Accept Request",
                                                    "Cancel Request",
                                                    "$userName's profile"
                                                )

                                                val builder = AlertDialog.Builder(getContext())

                                                builder.setItems(options) { dialog, which ->
                                                    if (which == 0) {
                                                        val myCalendar = Calendar.getInstance()
                                                        val currentDate =
                                                            SimpleDateFormat("EEEE, dd MMM, yyyy")
                                                        val friendshipDate =
                                                            currentDate.format(myCalendar.time)

                                                        friendsDatabaseReference!!.child(user_UId)
                                                            .child(userID).child("date")
                                                            .setValue(friendshipDate)
                                                            .addOnCompleteListener {
                                                                friendsDatabaseReference!!.child(
                                                                    userID
                                                                ).child(user_UId).child("date")
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
                                                                        friendReqDatabaseReference!!.child(
                                                                            user_UId
                                                                        ).child(userID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener { task ->
                                                                                if (task.isSuccessful) {
                                                                                    // delete from users friend_requests node, receiver >> sender > values
                                                                                    friendReqDatabaseReference!!.child(
                                                                                        userID
                                                                                    ).child(
                                                                                        user_UId
                                                                                    ).removeValue()
                                                                                        .addOnCompleteListener { task ->
                                                                                            if (task.isSuccessful) {
                                                                                                // after deleting data
                                                                                                val snackbar =
                                                                                                    Snackbar
                                                                                                        .make(
                                                                                                            view!!,
                                                                                                            "This person is now your friend",
                                                                                                            Snackbar.LENGTH_LONG
                                                                                                        )
                                                                                                // Changing message text color
                                                                                                val sView =
                                                                                                    snackbar.view
                                                                                                sView.setBackgroundColor(
                                                                                                    ContextCompat.getColor(
                                                                                                        getContext()!!,
                                                                                                        R.color.colorPrimary
                                                                                                    )
                                                                                                )
                                                                                                val textView =
                                                                                                    sView.findViewById<TextView>(
                                                                                                        R.id.snackbar_text
                                                                                                    )
                                                                                                textView.setTextColor(
                                                                                                    Color.WHITE
                                                                                                )
                                                                                                snackbar.show()
                                                                                            }
                                                                                        }

                                                                                }
                                                                            } //
                                                                    }
                                                            }
                                                    }


                                                    if (which == 1) {
                                                        //for cancellation, delete data from user nodes
                                                        // delete from, sender >> receiver > values
                                                        friendReqDatabaseReference!!.child(user_UId)
                                                            .child(userID).removeValue()
                                                            .addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    // delete from, receiver >> sender > values
                                                                    friendReqDatabaseReference!!.child(
                                                                        userID
                                                                    ).child(user_UId).removeValue()
                                                                        .addOnCompleteListener { task ->
                                                                            if (task.isSuccessful) {
                                                                                //Toast.makeText(getActivity(), "Cancel Request", Toast.LENGTH_SHORT).show();
                                                                                val snackbar =
                                                                                    Snackbar
                                                                                        .make(
                                                                                            view!!,
                                                                                            "Canceled Request",
                                                                                            Snackbar.LENGTH_LONG
                                                                                        )
                                                                                // Changing message text color
                                                                                val sView =
                                                                                    snackbar.view
                                                                                sView.setBackgroundColor(
                                                                                    ContextCompat.getColor(
                                                                                        getContext()!!,
                                                                                        R.color.colorPrimary
                                                                                    )
                                                                                )
                                                                                val textView =
                                                                                    sView.findViewById<TextView>(
                                                                                        R.id.snackbar_text
                                                                                    )
                                                                                textView.setTextColor(
                                                                                    Color.WHITE
                                                                                )
                                                                                snackbar.show()

                                                                            }
                                                                        }

                                                                }
                                                            }
                                                    }
                                                    if (which == 2) {
                                                        val profileIntent = Intent(
                                                            getContext(),
                                                            ProfileActivity::class.java
                                                        )
                                                        profileIntent.putExtra(
                                                            "visitUserId",
                                                            userID
                                                        )
                                                        startActivity(profileIntent)
                                                    }
                                                }
                                                builder.show()
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {

                                        }
                                    })
                            }
                            if (requestType == "sent") {
                                holder.re_icon.visibility = View.GONE
                                holder.se_icon.visibility = View.VISIBLE
                                userDatabaseReference!!.child(userID!!)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            val userName =
                                                dataSnapshot.child("user_name").value!!.toString()
                                            val userVerified =
                                                dataSnapshot.child("verified").value!!.toString()
                                            val userThumbPhoto =
                                                dataSnapshot.child("user_thumb_image").value!!.toString()
                                            val user_status =
                                                dataSnapshot.child("user_status").value!!.toString()

                                            holder.name.text = userName
                                            holder.status.text = user_status

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

                                            if (userVerified.contains("true")) {
                                                holder.verified_icon.visibility = View.VISIBLE
                                            }

                                            holder.itemView.setOnClickListener {
                                                val options = arrayOf<CharSequence>(
                                                    "Cancel Sent Request",
                                                    "$userName's profile"
                                                )

                                                val builder = AlertDialog.Builder(getContext())

                                                builder.setItems(options) { dialog, which ->
                                                    if (which == 0) {
                                                        //for cancellation, delete data from user nodes
                                                        // delete from, sender >> receiver > values
                                                        friendReqDatabaseReference!!.child(user_UId)
                                                            .child(userID).removeValue()
                                                            .addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    // delete from, receiver >> sender > values
                                                                    friendReqDatabaseReference!!.child(
                                                                        userID
                                                                    ).child(user_UId).removeValue()
                                                                        .addOnCompleteListener { task ->
                                                                            if (task.isSuccessful) {
                                                                                val snackbar =
                                                                                    Snackbar
                                                                                        .make(
                                                                                            view!!,
                                                                                            "Cancel Sent Request",
                                                                                            Snackbar.LENGTH_LONG
                                                                                        )
                                                                                // Changing message text color
                                                                                val sView =
                                                                                    snackbar.view
                                                                                sView.setBackgroundColor(
                                                                                    ContextCompat.getColor(
                                                                                        getContext()!!,
                                                                                        R.color.colorPrimary
                                                                                    )
                                                                                )
                                                                                val textView =
                                                                                    sView.findViewById<TextView>(
                                                                                        R.id.snackbar_text
                                                                                    )
                                                                                textView.setTextColor(
                                                                                    Color.WHITE
                                                                                )
                                                                                snackbar.show()
                                                                            }
                                                                        }

                                                                }
                                                            }
                                                    }
                                                    if (which == 1) {
                                                        val profileIntent = Intent(
                                                            getContext(),
                                                            ProfileActivity::class.java
                                                        )
                                                        profileIntent.putExtra(
                                                            "visitUserId",
                                                            userID
                                                        )
                                                        startActivity(profileIntent)
                                                    }
                                                }
                                                builder.show()
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {}
                                    })
                            }

                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RequestsVH {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.request_single, viewGroup, false)
                return RequestsVH(view)
            }
        }
        request_list!!.adapter = adapter
        adapter.startListening()
    }

    class RequestsVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView
        internal var status: TextView
        internal var user_photo: CircleImageView
        internal var re_icon: ImageView
        internal var se_icon: ImageView
        internal var verified_icon: ImageView

        init {
            name = itemView.findViewById(R.id.r_profileName)
            user_photo = itemView.findViewById(R.id.r_profileImage)
            status = itemView.findViewById(R.id.r_profileStatus)
            re_icon = itemView.findViewById(R.id.receivedIcon)
            se_icon = itemView.findViewById(R.id.sentIcon)
            verified_icon = itemView.findViewById(R.id.verifiedIcon)
        }
    }

}// Required empty public constructor
