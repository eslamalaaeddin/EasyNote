package com.example.easynote.activity

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.easynote.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream

private const val TAG = "ProfileActivity"
private const val REQUEST_CODE = 159

class ProfileActivity : AppCompatActivity() {
    private val user = FirebaseAuth.getInstance().currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user?.let {
            displayNameEditText.apply {
                setText(it.displayName)
                setSelection(it.displayName?.length!!)
            }

            if (it.photoUrl != null){
                Glide
                    .with(this)
                    .load(it.photoUrl)
                    .into(profileImageView)
            }

        }

        progressBar.visibility = View.GONE

        updateProfileButton.setOnClickListener { updateButton ->

            updateButton.isEnabled = false
            progressBar.visibility = View.VISIBLE

            //1 create the request
            val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(displayNameEditText.text.toString())
                .build()

            //2 fire the request
            user?.updateProfile(userProfileChangeRequest)?.addOnSuccessListener {
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                updateButton.isEnabled = true
                progressBar.visibility = View.GONE

            }?.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                updateButton.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }

        profileImageView.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            val bitmap = data?.extras?.get("data") as Bitmap
            profileImageView.setImageBitmap(bitmap)

            uploadImageToCloudStorage(bitmap)

        }
    }

    private fun uploadImageToCloudStorage(bitmap: Bitmap) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val firebaseStorageRef =
            FirebaseStorage.getInstance().reference.child("Profile images").child("${userId}.jpeg")

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        firebaseStorageRef.putBytes(byteArrayOutputStream.toByteArray())
            .addOnSuccessListener {
                Toast.makeText(this, "Image uploaded successfully ", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}