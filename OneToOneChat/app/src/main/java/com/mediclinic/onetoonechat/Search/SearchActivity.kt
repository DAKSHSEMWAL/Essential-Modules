package com.mediclinic.onetoonechat.Search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.mediclinic.onetoonechat.Model.ProfileInfo
import com.mediclinic.onetoonechat.Profile.ProfileActivity
import com.mediclinic.onetoonechat.R
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

import cn.zhaiyifan.rememberedittext.RememberEditText
import de.hdodenhof.circleimageview.CircleImageView
import xyz.hasnat.sweettoast.SweetToast

class SearchActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var searchInput: EditText? = null
    private var backButton: ImageView? = null
    private var notFoundTV: TextView? = null

    private var peoples_list: RecyclerView? = null
    private var peoplesDatabaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // appbar / toolbar
        toolbar = findViewById(R.id.search_appbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowCustomEnabled(true)

        val layoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.appbar_search, null)
        actionBar.customView = view

        searchInput = findViewById(R.id.serachInput)
        notFoundTV = findViewById(R.id.notFoundTV)
        backButton = findViewById(R.id.backButton)
        searchInput!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchPeopleProfile(searchInput!!.text.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        backButton!!.setOnClickListener { finish() }


        // Setup recycler view
        peoples_list = findViewById(R.id.SearchList)
        peoples_list!!.setHasFixedSize(true)
        peoples_list!!.layoutManager = LinearLayoutManager(this)

        peoplesDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        peoplesDatabaseReference!!.keepSynced(true) // for offline

    }


    /**
     * FirebaseUI for Android — UI Bindings for Firebase
     * Library link- https://github.com/firebase/FirebaseUI-Android
     */
    private fun searchPeopleProfile(searchString: String) {
        val searchQuery = peoplesDatabaseReference!!.orderByChild("search_name")
            .startAt(searchString).endAt(searchString + "\uf8ff")
        //final Query searchQuery = peoplesDatabaseReference.orderByChild("search_name").equalTo(searchString);

        val recyclerOptions = FirebaseRecyclerOptions.Builder<ProfileInfo>()
            .setQuery(searchQuery, ProfileInfo::class.java)
            .build()

        val adapter =
            object : FirebaseRecyclerAdapter<ProfileInfo, SearchPeopleVH>(recyclerOptions) {
                override fun onBindViewHolder(
                    holder: SearchPeopleVH,
                    position: Int,
                    model: ProfileInfo
                ) {
                    holder.name.text = model.user_name
                    holder.status.text = model.user_status

                    Picasso.get()
                        .load(model.user_image)
                        .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                        .placeholder(R.drawable.default_profile_image)
                        .into(holder.profile_pic)

                    holder.verified_icon.visibility = View.GONE
                    if (model.verified!!.contains("true")) {
                        holder.verified_icon.visibility = View.VISIBLE
                    } else {
                        holder.verified_icon.visibility = View.GONE
                    }

                    /**on list >> clicking item, then, go to single user profile */
                    holder.itemView.setOnClickListener {
                        val visit_user_id = getRef(position).key
                        val intent = Intent(this@SearchActivity, ProfileActivity::class.java)
                        intent.putExtra("visitUserId", visit_user_id)
                        startActivity(intent)
                    }


                }

                override fun onCreateViewHolder(
                    viewGroup: ViewGroup,
                    viewType: Int
                ): SearchPeopleVH {
                    val view = LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.all_single_profile_display, viewGroup, false)
                    return SearchPeopleVH(view)
                }
            }
        peoples_list!!.adapter = adapter
        adapter.startListening()
    }

    class SearchPeopleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView
        internal var status: TextView
        internal var profile_pic: CircleImageView
        internal var verified_icon: ImageView

        init {
            name = itemView.findViewById(R.id.all_user_name)
            status = itemView.findViewById(R.id.all_user_status)
            profile_pic = itemView.findViewById(R.id.all_user_profile_img)
            verified_icon = itemView.findViewById(R.id.verifiedIcon)
        }
    }


    // Toolbar menu for clearing search history
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.search_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.menu_clear_search) {
            RememberEditText.clearCache(this@SearchActivity)
            SweetToast.info(this, "Search history cleared successfully.")
            this.finish()
        }
        return true
    }
}
