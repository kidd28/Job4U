package com.project.job4u.Authentication

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.project.job4u.MainActivity
import com.project.job4u.R
import java.util.Calendar
import java.util.UUID

class ApplicantInfo : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Input fields
    private lateinit var fnameInput: TextInputEditText
    private lateinit var lnameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var dobInput: TextInputEditText


    private lateinit var calendar: Calendar
    private lateinit var datePickerDialog: DatePickerDialog

    private lateinit var submitButton: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_applicant_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        fnameInput = findViewById(R.id.fname_input)
        lnameInput = findViewById(R.id.lname_input)
        emailInput = findViewById(R.id.email_input)
        dobInput = findViewById(R.id.dob_input)

        submitButton = findViewById(R.id.Save)


        // Set submit button click listener
        submitButton.setOnClickListener {
            if (validateInputs()) {
                uploadUserData()
            }
        }

        // Initialize Calendar and DatePickerDialog
        calendar = Calendar.getInstance()
        datePickerDialog = DatePickerDialog(
            this,
            { view, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                dobInput.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set a click listener on the Date of Birth input field
        dobInput.setOnClickListener {
            // Show the DatePickerDialog when the user clicks the field
            datePickerDialog.show()
        }

        auth.currentUser?.let {
            emailInput.setText(it.email)
        }


    }
    private fun validateInputs(): Boolean {
        // Check if the fields are empty
        return when {
            fnameInput.text.isNullOrEmpty() -> {
                showToast("Name is required")
                false
            }
            lnameInput.text.isNullOrEmpty() -> {
                showToast("Name is required")
                false
            }
            emailInput.text.isNullOrEmpty() -> {
                showToast("Email is required")
                false
            }
            dobInput.text.isNullOrEmpty() -> {
                showToast("Date of Birth is required")
                false
            }
            else -> true
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun uploadUserData() {
        // Get user data from the input fields
        val fname = fnameInput.text.toString()
        val lname = lnameInput.text.toString()
        val email = emailInput.text.toString()
        val dob = dobInput.text.toString()


        // Prepare the data to upload
        val userData = mapOf(
            "firstname" to fname,
            "lastname" to lname,
            "email" to email,
            "dob" to dob,
        )

        // Get the current user from Firebase Authentication
        val userId = auth.currentUser?.uid ?: return showToast("User is not signed in")

        // Reference to the Firebase Database location
        database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(userId)

        // Upload user data to Firebase
        userRef.updateChildren(userData)
            .addOnSuccessListener {
                startActivity(Intent(this, ApplicantContact::class.java))
            }
            .addOnFailureListener {
                showToast("Failed: ${it.message}")
            }
    }


}