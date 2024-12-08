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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.job4u.EmployerDashboard
import com.project.job4u.R

class CompanyMoreInfo : AppCompatActivity() {
    private lateinit var streetAddressInput: TextInputEditText
    private lateinit var cityInput: TextInputEditText
    private lateinit var stateProvinceInput: TextInputEditText
    private lateinit var companySizeInput: TextInputEditText
    private lateinit var businessTypeInput: TextInputEditText

    private lateinit var database: FirebaseDatabase
    private lateinit var companyRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_company_more_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }// Initialize Firebase Database and Auth
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Reference to the companies node under the user ID
        companyRef = database.reference.child("companies")

        // Initialize text fields
        streetAddressInput = findViewById(R.id.street_address_input)
        cityInput = findViewById(R.id.city_input)
        stateProvinceInput = findViewById(R.id.state_province_input)
        companySizeInput = findViewById(R.id.company_size_input)
        businessTypeInput = findViewById(R.id.business_type_input)

        // Handle submit button click
        findViewById<View>(R.id.Save).setOnClickListener {
            submitCompanyData()
        }
    }
    private fun submitCompanyData() {
        // Get values from the text fields
        val streetAddress = streetAddressInput.text.toString().trim()
        val city = cityInput.text.toString().trim()
        val stateProvince = stateProvinceInput.text.toString().trim()
        val companySize = companySizeInput.text.toString().trim()
        val businessType = businessTypeInput.text.toString().trim()

        // Validate required fields
        if (streetAddress.isEmpty() || city.isEmpty() || stateProvince.isEmpty() ||
            companySize.isEmpty() || businessType.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the user ID from Firebase Authentication
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a simple data class for the company
        val companyData = mapOf(
            "streetAddress" to streetAddress,
            "city" to city,
            "stateProvince" to stateProvince,
            "companySize" to companySize,
            "businessType" to businessType
        )

        // Upload company data under the user's ID
        companyRef.child(userId).updateChildren(companyData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, EmployerDashboard::class.java))
                } else {
                    Toast.makeText(this, "Failed to save company data. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}