package com.project.job4u.Authentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.job4u.R

class CompanyInfo : AppCompatActivity() {

    private lateinit var companyNameInput: TextInputEditText
    private lateinit var companyEmailInput: TextInputEditText
    private lateinit var companyPhoneInput: TextInputEditText
    private lateinit var companyWebsiteInput: TextInputEditText
    private lateinit var companyDescriptionInput: TextInputEditText

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_company_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firestore and Auth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize text fields
        companyNameInput = findViewById(R.id.company_name_input)
        companyEmailInput = findViewById(R.id.company_email_input)
        companyPhoneInput = findViewById(R.id.company_phone_input)
        companyWebsiteInput = findViewById(R.id.company_website_input)
        companyDescriptionInput = findViewById(R.id.company_description_input)

        auth.currentUser?.let {
            companyEmailInput.setText(it.email)
        }

        // Handle submit button click
        findViewById<View>(R.id.Save).setOnClickListener {
            submitCompanyData()
        }
    }

    private fun submitCompanyData() {
        // Get values from the text fields
        val companyName = companyNameInput.text.toString().trim()
        val companyEmail = companyEmailInput.text.toString().trim()
        val companyPhone = companyPhoneInput.text.toString().trim()
        val companyWebsite = companyWebsiteInput.text.toString().trim()  // Optional field
        val companyDescription = companyDescriptionInput.text.toString().trim()

        // Validate required fields
        if (companyName.isEmpty() || companyEmail.isEmpty() || companyPhone.isEmpty() ||
            companyDescription.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the user ID from Firebase Authentication
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a map for the company data
        val companyData = mapOf(
            "companyName" to companyName,
            "companyEmail" to companyEmail,
            "companyPhone" to companyPhone,
            "companyWebsite" to companyWebsite,
            "companyDescription" to companyDescription,
        )

        // Upload company data to Firestore under the user's ID in the "companies" collection
        firestore.collection("companies").document(userId).set(companyData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, CompanyMoreInfo::class.java))
                } else {
                    Toast.makeText(this, "Failed to register company. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
