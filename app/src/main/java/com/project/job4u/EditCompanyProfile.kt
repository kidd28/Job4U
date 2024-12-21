package com.project.job4u

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions

class EditCompanyProfile : AppCompatActivity() {
    private lateinit var companyNameEditText: EditText
    private lateinit var companyEmailEditText: EditText
    private lateinit var companyPhoneEditText: EditText
    private lateinit var companyDescriptionEditText: EditText
    private lateinit var companyWebsiteEditText: EditText
    private lateinit var companySizeEditText: EditText
    private lateinit var streetAddressEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var businessTypeEditText: EditText
    private lateinit var stateProvinceEditText: EditText
    private lateinit var saveProfileButton: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_company_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        companyNameEditText = findViewById(R.id.edit_company_name)
        companyEmailEditText = findViewById(R.id.edit_company_email)
        companyPhoneEditText = findViewById(R.id.edit_company_phone)
        companyDescriptionEditText = findViewById(R.id.edit_company_description)
        companyWebsiteEditText = findViewById(R.id.edit_company_website)
        companySizeEditText = findViewById(R.id.edit_company_size)
        streetAddressEditText = findViewById(R.id.edit_street_address)
        cityEditText = findViewById(R.id.edit_city)
        businessTypeEditText = findViewById(R.id.edit_business_type)
        stateProvinceEditText = findViewById(R.id.edit_state_province)
        saveProfileButton = findViewById(R.id.btn_save_profile)

        // Load existing company profile data
        loadCompanyProfile()

        // Add logic for saving the profile when the save button is clicked
        saveProfileButton.setOnClickListener {
            if (validateFields()) {
                saveProfile()
            }
        }
    }

    private fun loadCompanyProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val companyRef = db.collection("companies").document(userId)
        companyRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve current company data
                    val companyName = document.getString("companyName")
                    val companyEmail = document.getString("companyEmail")
                    val companyPhone = document.getString("companyPhone")
                    val companyDescription = document.getString("companyDescription")
                    val companyWebsite = document.getString("companyWebsite")
                    val companySize = document.getString("companySize")
                    val streetAddress = document.getString("streetAddress")
                    val city = document.getString("city")
                    val businessType = document.getString("businessType")
                    val stateProvince = document.getString("stateProvince")

                    // Populate the fields with the current data
                    companyName?.let { companyNameEditText.setText(it) }
                    companyEmail?.let { companyEmailEditText.setText(it) }
                    companyPhone?.let { companyPhoneEditText.setText(it) }
                    companyDescription?.let { companyDescriptionEditText.setText(it) }
                    companyWebsite?.let { companyWebsiteEditText.setText(it) }
                    companySize?.let { companySizeEditText.setText(it) }
                    streetAddress?.let { streetAddressEditText.setText(it) }
                    city?.let { cityEditText.setText(it) }
                    businessType?.let { businessTypeEditText.setText(it) }
                    stateProvince?.let { stateProvinceEditText.setText(it) }
                } else {
                    Toast.makeText(this, "No company profile found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load company profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateFields(): Boolean {
        val companyName = companyNameEditText.text.toString().trim()
        val companyEmail = companyEmailEditText.text.toString().trim()
        val companyPhone = companyPhoneEditText.text.toString().trim()
        val companyDescription = companyDescriptionEditText.text.toString().trim()
        val companySize = companySizeEditText.text.toString().trim()
        val streetAddress = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val businessType = businessTypeEditText.text.toString().trim()
        val stateProvince = stateProvinceEditText.text.toString().trim()

        if (companyName.isEmpty() || companyEmail.isEmpty() || companyPhone.isEmpty() ||
            companyDescription.isEmpty() || companySize.isEmpty() ||
            streetAddress.isEmpty() || city.isEmpty() || businessType.isEmpty() || stateProvince.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(companyEmail).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveProfile() {
        val companyName = companyNameEditText.text.toString().trim()
        val companyEmail = companyEmailEditText.text.toString().trim()
        val companyPhone = companyPhoneEditText.text.toString().trim()
        val companyDescription = companyDescriptionEditText.text.toString().trim()
        val companyWebsite = companyWebsiteEditText.text.toString().trim()
        val companySize = companySizeEditText.text.toString().trim()
        val streetAddress = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val businessType = businessTypeEditText.text.toString().trim()
        val stateProvince = stateProvinceEditText.text.toString().trim()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val companyRef = db.collection("companies").document(userId)

            val companyUpdates = mapOf(
                "companyName" to companyName,
                "companyEmail" to companyEmail,
                "companyPhone" to companyPhone,
                "companyDescription" to companyDescription,
                "companyWebsite" to companyWebsite,
                "companySize" to companySize,
                "streetAddress" to streetAddress,
                "city" to city,
                "businessType" to businessType,
                "stateProvince" to stateProvince
            )

            companyRef.update(companyUpdates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Company profile updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()  // Close the activity after saving
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update company profile", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
        }
    }
}
