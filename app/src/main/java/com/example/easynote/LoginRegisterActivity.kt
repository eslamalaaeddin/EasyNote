package com.example.easynote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login_register.*
import java.util.*

private const val AUTH_UI_REQUEST_CODE = 159

class LoginRegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        loginRegisterButton.setOnClickListener {
            handleLoginAndRegister()
        }
    }

    private fun handleLoginAndRegister() {
        //1
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build()
        )
        //2
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.notes)
            .build()

        //3
        startActivityForResult(intent, AUTH_UI_REQUEST_CODE)
    }

    //4
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //5 --> if registering is OK
        if (requestCode == AUTH_UI_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val currentUser = auth.currentUser
                //6 --> new user?
                if (currentUser?.metadata?.creationTimestamp == currentUser?.metadata?.lastSignInTimestamp) {
                    Toast.makeText(this, "Welcome new user", Toast.LENGTH_SHORT).show()
                }
                //7 --> existing user?
                else {
                    Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show()
                }
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        //8 --> if a problem occured during registering
        else {
            val response = IdpResponse.fromResultIntent(data)
            //9 --> user left the app without sign in or up
            if (response == null){
                Toast.makeText(this, "User has canceled its registering", Toast.LENGTH_SHORT).show()
            }
            //10 --> error in firebase
            else{
                Toast.makeText(this, response.error?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}