package com.project.job4u.Authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.project.job4u.MainActivity
import com.project.job4u.R

class EmployerSignIn : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_employer_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val employer: TextView = findViewById(R.id.applicant)
        val email_input: EditText = findViewById(R.id.email_input)
        val password_input: EditText = findViewById(R.id.password_input)
        val sign_in_button: Button = findViewById(R.id.sign_in_button)
        val sign_up_link: TextView = findViewById(R.id.sign_up_link)

        employer.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent) }
        sign_up_link.setOnClickListener {  // Start SignInActivity when the button is clicked
            val intent = Intent(this, EmployerSignUp::class.java)
            startActivity(intent)
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        sign_in_button.setOnClickListener {
            // Get the email and password from input fields
            val email = email_input.text.toString().trim()
            val password = password_input.text.toString().trim()

            // Check if email or password is empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email or password cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                signInUser(email, password)
            }
        }
    }

    private fun signInUser(email: String, password: String) {
        // Sign in with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful, navigate to another activity or show success message
                    Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}