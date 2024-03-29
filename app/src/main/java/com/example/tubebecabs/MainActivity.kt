package com.example.tubebecabs

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.PhoneBuilder
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import java.util.Arrays
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object{
        private val LOGIN_REQUEST_CODE = 7171
    }
    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var listener:FirebaseAuth.AuthStateListener

    private lateinit var database:FirebaseDatabase
    private lateinit var driverInfoRef:DatabaseReference



    override fun onStart(){
        super.onStart()
        delaySplashScreen();
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }

    private fun delaySplashScreen() {
        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe({
                firebaseAuth.addAuthStateListener(listener);
            })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_splash_screen)



        init()



    }
    private fun init(){

        database = FirebaseDatabase.getInstance()
        driverInfoRef =database.getReference(Common.DRIVER_INFO_REFERENCE)

        providers = Arrays.asList(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()

        )
        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener {  myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null)
            {
                checkUserFromFirebase()
            }
            else
                showLoginLayout()
        }

    }

    private fun checkUserFromFirebase() {
        driverInfoRef
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists())
                    {

                        Toast.makeText(this@MainActivity,"user already registered!",Toast.LENGTH_SHORT).show()

                    }
                    else{
                        showRegisterLayout()

                    }

                }

                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(this@MainActivity,p0.message,Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun showRegisterLayout() {

        
    }

    private fun showLoginLayout() { val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.layout_signin)
          .setPhoneButtonId(R.id.btn_phone_sign_in)
          .setGoogleButtonId(R.id.btn_google_sign_in)
          .build();
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build()

            ,LOGIN_REQUEST_CODE)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST_CODE)
        {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK){
                val user = FirebaseAuth.getInstance().currentUser

            }
            else
                Toast.makeText(this@MainActivity,""+response!!.error!!.message,Toast.LENGTH_SHORT).show()

    }
    }
}