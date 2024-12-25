package com.project.job4u.Authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.job4u.MainActivity
import com.project.job4u.R

class EmployerSignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_employer_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val signIn: TextView = findViewById(R.id.sign_in_link)
        val sign_up_button: Button = findViewById(R.id.sign_up_button)
        val email_input: EditText = findViewById(R.id.email_input)
        val password_input: EditText = findViewById(R.id.password_input)
        val confirm_password_input: EditText = findViewById(R.id.confirm_password_input)


        signIn.setOnClickListener {  // Start SignInActivity when the button is clicked
            val intent = Intent(this, EmployerSignIn::class.java)
            startActivity(intent)
        }


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        sign_up_button.setOnClickListener {
            // Get the email, password, and confirm password from input fields
            val email = email_input.text.toString().trim()
            val password = password_input.text.toString().trim()
            val confirmPassword = confirm_password_input.text.toString().trim()

            // Check if email or password is empty
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if password and confirm password match
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proceed with sign-up if passwords match
            signUpUser(email, password)
        }
    }

    private fun signUpUser(email: String, password: String) {
        // Sign up with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-up successful, navigate to another activity or show success message
                    Toast.makeText(this, "Sign Up successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CompanyInfo::class.java))
                } else {
                    // If sign up fails, display a message to the user
                    Toast.makeText(this, "Sign Up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}