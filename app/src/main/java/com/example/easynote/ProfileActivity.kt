package com.example.easynote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_profile.*

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

        }

        progressBar.visibility = View.GONE

        updateProfileButton.setOnClickListener {updateButton ->

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

        profileImageView.setOnClickListener{

        }
    }
}