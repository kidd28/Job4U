package com.project.job4u

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import de.hdodenhof.circleimageview.CircleImageView

class Settings : AppCompatActivity() {
    private lateinit var sign_out_button: MaterialButton
    private lateinit var edit_Profile: TextView
    private lateinit var profile_image: CircleImageView

    private val firestore = FirebaseFirestore.getInstance()

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
        edit_Profile = findViewById(R.id.edit_Profile)
        profile_image = findViewById(R.id.profile_image)
        loadProfile()

        sign_out_button.setOnClickListener {
            signOutUser()
        }
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        edit_Profile.setOnClickListener {
            // Check if the user is a company or an applicant
            checkIfCompany(userId){ isCompany ->
                if (isCompany) {
                    // Open the edit company profile activity
                    val intent = Intent(this, EditCompanyProfile::class.java)
                    startActivity(intent)
                } else {
                    // Open the edit user profile activity
                    val intent = Intent(this, EditUserProfile::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun loadProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        // First, check if the user is a company
        checkIfCompany(userId) { isCompany ->
            if (isCompany) {
                loadCompanyProfile(userId)
            } else {
                loadApplicantProfile(userId)
            }
        }
    }

    private fun checkIfCompany(userId: String, callback: (Boolean) -> Unit) {
        val companyRef = firestore.collection("companies").document(userId)
        companyRef.get()
            .addOnSuccessListener { document ->
                callback(document.exists()) // Returns true if the user is a company
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@Settings, "Failed to check company status: ${exception.message}", Toast.LENGTH_SHORT).show()
                callback(false) // Default to false if an error occurs
            }
    }

    private fun loadCompanyProfile(userId: String) {
        val companyRef = firestore.collection("companies").document(userId)
        companyRef.get()
            .addOnSuccessListener { document ->
                val companyImageUrl = document.getString("companyImage")
                Glide.with(this@Settings)
                    .load(companyImageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(profile_image)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@Settings, "Failed to load company profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadApplicantProfile(userId: String) {
        val applicantRef = firestore.collection("users").document(userId)
        applicantRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val applicantImageUrl = document.getString("profileImage")
                    Glide.with(this@Settings)
                        .load(applicantImageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(profile_image)
                } else {
                    Toast.makeText(this@Settings, "User role not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@Settings, "Failed to load applicant profile: ${exception.message}", Toast.LENGTH_SHORT).show()
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
