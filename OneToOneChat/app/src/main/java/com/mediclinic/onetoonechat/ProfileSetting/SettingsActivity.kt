package com.mediclinic.onetoonechat.ProfileSetting

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.mediclinic.onetoonechat.R
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask

import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import xyz.hasnat.sweettoast.SweetToast

class SettingsActivity : AppCompatActivity() {

    lateinit var profile_settings_image: CircleImageView
    lateinit var display_status: TextView
    lateinit var updatedMsg: TextView
    lateinit var recheckGender: TextView
    lateinit var editPhotoIcon: ImageView
    lateinit var editStatusBtn: ImageView
    lateinit var display_name: EditText
    lateinit var display_email: EditText
    lateinit var user_phone: EditText
    lateinit var user_profession: EditText
    lateinit var user_nickname: EditText
    lateinit var maleRB: RadioButton
    lateinit var femaleRB: RadioButton

    lateinit var saveInfoBtn: Button

    lateinit var getUserDatabaseReference: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var mProfileImgStorageRef: StorageReference
    lateinit var thumb_image_ref: StorageReference
    internal var thumb_Bitmap: Bitmap? = null

    var progressDialog: ProgressDialog? = null
    var selectedGender = ""
    lateinit var profile_download_url: String
    lateinit var profile_thumb_download_url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mAuth = FirebaseAuth.getInstance()
        val user_id = mAuth!!.currentUser!!.uid
        getUserDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("users").child(user_id)
        getUserDatabaseReference!!.keepSynced(true) // for offline

        mProfileImgStorageRef = FirebaseStorage.getInstance().reference.child("profile_image")
        thumb_image_ref = FirebaseStorage.getInstance().reference.child("thumb_image")

        profile_settings_image = findViewById(R.id.profile_img)
        display_name = findViewById(R.id.user_display_name)
        user_nickname = findViewById(R.id.user_nickname)
        user_profession = findViewById(R.id.profession)
        display_email = findViewById(R.id.userEmail)
        user_phone = findViewById(R.id.phone)
        display_status = findViewById(R.id.userProfileStatus)
        editPhotoIcon = findViewById(R.id.editPhotoIcon)
        saveInfoBtn = findViewById(R.id.saveInfoBtn)
        editStatusBtn = findViewById(R.id.statusEdit)
        updatedMsg = findViewById(R.id.updatedMsg)

        recheckGender = findViewById(R.id.recheckGender)
        recheckGender!!.visibility = View.VISIBLE


        maleRB = findViewById(R.id.maleRB)
        femaleRB = findViewById(R.id.femaleRB)


        val toolbar = findViewById<Toolbar>(R.id.profile_settings_appbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Profile"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        progressDialog = ProgressDialog(this)

        // Retrieve data from database
        getUserDatabaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // retrieve data from db
                val name = dataSnapshot.child("user_name").value!!.toString()
                val nickname = dataSnapshot.child("user_nickname").value!!.toString()
                val profession = dataSnapshot.child("user_profession").value!!.toString()
                val status = dataSnapshot.child("user_status").value!!.toString()
                val email = dataSnapshot.child("user_email").value!!.toString()
                val phone = dataSnapshot.child("user_mobile").value!!.toString()
                val gender = dataSnapshot.child("user_gender").value!!.toString()
                val image = dataSnapshot.child("user_image").value!!.toString()
                val thumbImage = dataSnapshot.child("user_thumb_image").value!!.toString()

                display_status!!.text = status

                display_name!!.setText(name)
                display_name!!.setSelection(display_name!!.text.length)

                user_nickname!!.setText(nickname)
                user_nickname!!.setSelection(user_nickname!!.text.length)

                user_profession!!.setText(profession)
                user_profession!!.setSelection(user_profession!!.text.length)

                user_phone!!.setText(phone)
                user_phone!!.setSelection(user_phone!!.text.length)

                display_email!!.setText(email)


                if (image != "default_image") { // default image condition for new user
                    Picasso.get()
                        .load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE) // for offline
                        .placeholder(R.drawable.default_profile_image)
                        .error(R.drawable.default_profile_image)
                        .into(profile_settings_image)
                }

                if (gender == "Male") {
                    maleRB!!.isChecked = true
                } else if (gender == "Female") {
                    femaleRB!!.isChecked = true
                } else {
                    //maleRB.setChecked(false);
                    //femaleRB.setChecked(false);
                    return
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        /** Change profile photo from GALLERY  */
        editPhotoIcon!!.setOnClickListener {
            // open gallery
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(galleryIntent, GALLERY_PICK_CODE)
        }

        /** Edit information  */
        saveInfoBtn!!.setOnClickListener {
            val uName = display_name!!.text.toString()
            val uNickname = user_nickname!!.text.toString()
            val uPhone = user_phone!!.text.toString()
            val uProfession = user_profession!!.text.toString()

            saveInformation(uName, uNickname, uPhone, uProfession, selectedGender)
        }

        /** Edit STATUS  */
        editStatusBtn!!.setOnClickListener {
            val previous_status = display_status!!.text.toString()

            val statusUpdateIntent = Intent(this@SettingsActivity, StatusUpdateActivity::class.java)
            // previous status from db
            statusUpdateIntent.putExtra("ex_status", previous_status)
            startActivity(statusUpdateIntent)
        }

        // hide soft keyboard
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
    } // Ending onCrate

    // Gender Radio Button
    fun selectedGenderRB(view: View) {
        val checked = (view as RadioButton).isChecked
        when (view.getId()) {
            R.id.maleRB -> {
                if (checked) {
                    selectedGender = "Male"
                    recheckGender!!.visibility = View.GONE
//                    break
                }
                if (checked) {
                    selectedGender = "Female"
                    recheckGender!!.visibility = View.GONE
//                    break
                }
            }
            R.id.femaleRB -> if (checked) {
                selectedGender = "Female"
                recheckGender!!.visibility = View.GONE
//                break
            }
        }
    }


    private fun saveInformation(
        uName: String,
        uNickname: String,
        uPhone: String,
        uProfession: String,
        uGender: String
    ) {
        if (uGender.length < 1) {
            recheckGender!!.setTextColor(Color.RED)
            //Toasty.info(this, "To save changes, please recheck your GENDER", 1000).show();
        } else if (TextUtils.isEmpty(uName)) {
            SweetToast.error(this, "Oops! your name can't be empty")
        } else if (uName.length < 3 || uName.length > 40) {
            SweetToast.warning(this, "Your name should be 3 to 40 numbers of characters")
        } else if (TextUtils.isEmpty(uPhone)) {
            SweetToast.error(this, "Your mobile number is required.")
        } else if (uPhone.length < 10) {
            SweetToast.warning(this, "Sorry! your mobile number is too short")
        } else {
            getUserDatabaseReference!!.child("user_name").setValue(uName)
            getUserDatabaseReference!!.child("user_nickname").setValue(uNickname)
            getUserDatabaseReference!!.child("search_name").setValue(uName.toLowerCase())
            getUserDatabaseReference!!.child("user_profession").setValue(uProfession)
            getUserDatabaseReference!!.child("user_mobile").setValue(uPhone)
            getUserDatabaseReference!!.child("user_gender").setValue(uGender)
                .addOnCompleteListener {
                    updatedMsg!!.visibility = View.VISIBLE

                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            this@SettingsActivity.runOnUiThread {
                                updatedMsg!!.visibility = View.GONE
                            }
                        }
                    }, 1500)
                }.addOnFailureListener { }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /** Cropping image functionality
         * Library Link- https://github.com/ArthurHub/Android-Image-Cropper
         */
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {
                progressDialog!!.setMessage("Please wait...")
                progressDialog!!.show()

                val resultUri = result.uri

                val thumb_filePath_Uri = File(resultUri.path!!)

                val user_id = mAuth!!.currentUser!!.uid

                /**
                 * compress image using compressor library
                 * link - https://github.com/zetbaitsu/Compressor
                 */
                try {
                    thumb_Bitmap = Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(45)
                        .compressToBitmap(thumb_filePath_Uri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }


                // firebase storage for uploading the cropped image
                val filePath = mProfileImgStorageRef!!.child("$user_id.jpg")

                val uploadTask = filePath.putFile(resultUri)
                val uriTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        SweetToast.error(
                            this@SettingsActivity,
                            "Profile Photo Error: " + task.exception!!.message
                        )
                        //throw task.getException();
                    }
                    profile_download_url = filePath.downloadUrl.toString()
                    filePath.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //Toasty.info(SettingsActivity.this, "Your profile photo is uploaded successfully.", Toast.LENGTH_SHORT).show();
                        // retrieve the stored image as profile photo
                        profile_download_url = task.result!!.toString()
                        Log.e("tag", "profile url: " + profile_download_url!!)

                        val outputStream = ByteArrayOutputStream()
                        thumb_Bitmap!!.compress(Bitmap.CompressFormat.JPEG, 45, outputStream)
                        val thumb_byte = outputStream.toByteArray()

                        // firebase storage for uploading the cropped and compressed image
                        val thumb_filePath = thumb_image_ref!!.child(user_id + "jpg")
                        val thumb_uploadTask = thumb_filePath.putBytes(thumb_byte)

                        val thumbUriTask = thumb_uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                SweetToast.error(
                                    this@SettingsActivity,
                                    "Thumb Image Error: " + task.exception!!.message
                                )
                            }
                            profile_thumb_download_url = thumb_filePath.downloadUrl.toString()
                            thumb_filePath.downloadUrl
                        }.addOnCompleteListener { task ->
                            profile_thumb_download_url = task.result!!.toString()
                            Log.e("tag", "thumb url: " + profile_thumb_download_url!!)
                            if (task.isSuccessful) {
                                Log.e("tag", "thumb profile updated")

                                val update_user_data = HashMap<String, Any>()
                                update_user_data["user_image"] = profile_download_url
                                update_user_data["user_thumb_image"] = profile_thumb_download_url

                                getUserDatabaseReference!!.updateChildren(HashMap(update_user_data))
                                    .addOnSuccessListener {
                                        Log.e("tag", "thumb profile updated")
                                        progressDialog!!.dismiss()
                                    }.addOnFailureListener { e ->
                                        Log.e("tag", "for thumb profile: " + e.message)
                                        progressDialog!!.dismiss()
                                    }
                            }
                        }


                        ////
                        /*
                            thumb_uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toasty.warning(SettingsActivity.this,"Error occurred!! "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    if (taskSnapshot != null){
                                        profile_thumb_download_url = String.valueOf(taskSnapshot.getMetadata().getReference().getDownloadUrl());
                                        Log.e("tag", "profile_thumb_download_url: "+ profile_thumb_download_url);

                                        HashMap<String, Object> update_user_data = new HashMap<>();
                                        update_user_data.put("user_image", profile_download_url);
                                        update_user_data.put("user_thumb_image", profile_thumb_download_url);

                                        getUserDatabaseReference.updateChildren(update_user_data)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("tag", "for thumb profile: "+ e.getMessage());
                                                    }
                                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.e("tag", "thumb profile updated");
                                                progressDialog.dismiss();
                                                //Toasty.success(SettingsActivity.this,"Profile photo is updated successfully.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                }
                            }); */

                        /* addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    profile_thumb_download_url = task.getResult().toString();
                                    if (task.isSuccessful()){
                                         HashMap<String, Object> update__user_data = new HashMap<>();
                                        update__user_data.put("user_image", profile_download_url);
                                        update__user_data.put("user_thumb_ima  ge", profile_thumb_download_url);


                                        getUserDatabaseReference.updateChildren(update__user_data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        progressDialog.dismiss();
                                                        //Toasty.success(SettingsActivity.this,"Profile photo is updated successfully.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            });
                            */
                    }
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //Exception error = result.getError();
                // handling more event
                SweetToast.info(this@SettingsActivity, "Image cropping failed.")
            }
        }

    }

    companion object {

        private val GALLERY_PICK_CODE = 1
    }


}
