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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.project.job4u.MainActivity
import com.project.job4u.R

class ApplicantSignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001 // Request code for sign-in
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_applicant_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Configure Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Use your web client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signIn: TextView = findViewById(R.id.sign_in_link)
        val sign_up_button: MaterialButton = findViewById(R.id.sign_up_button)
        val email_input: EditText = findViewById(R.id.email_input)
        val password_input: EditText = findViewById(R.id.password_input)
        val confirm_password_input: EditText = findViewById(R.id.confirm_password_input)
        val google_signin: MaterialButton = findViewById(R.id.google_signin)

        signIn.setOnClickListener {  // Start SignInActivity when the button is clicked
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent) }

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
            google_signin.setOnClickListener {
                signIn()
            }
            // Proceed with sign-up if passwords match
            signUpUser(email, password)
        }
    }
    // Google Sign-In function
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // Handle the result of the Google Sign-In Intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign-In failed, update UI appropriately
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Firebase authentication with Google Sign-In token
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success, check if the user is new or existing
                    val user = auth.currentUser
                    val isNewUser = isNewUser(user)
                    if (isNewUser) {
                        startActivity(Intent(this, ApplicantInfo::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to check if the user is new or existing
    private fun isNewUser(user: FirebaseUser?): Boolean {
        return user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp
    }





    private fun signUpUser(email: String, password: String) {
        // Sign up with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-up successful, navigate to another activity or show success message
                    Toast.makeText(this, "Sign Up successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ApplicantInfo::class.java))
                } else {
                    // If sign up fails, display a message to the user
                    Toast.makeText(this, "Sign Up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}