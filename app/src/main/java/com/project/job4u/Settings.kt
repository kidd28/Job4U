package com.project.job4u

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class Settings : AppCompatActivity() {
    private lateinit var sign_out_button: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sign_out_button = findViewById(R.id.sign_out_button)
        sign_out_button.setOnClickListener {
            signOutUser()
        }

    }
    private fun signOutUser() {
        // Get the FirebaseAuth instance
        FirebaseAuth.getInstance().signOut()

        // Show a Toast to confirm that the user has signed out
        Toast.makeText(this, "You have successfully signed out", Toast.LENGTH_SHORT).show()

        // Redirect to the SignIn Activity (or any activity you want)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // Clear the activity stack
        startActivity(intent)
        finish()  // Finish the current activity
    }
}