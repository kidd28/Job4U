package com.project.job4u

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Calendar
import java.util.UUID

class EditUserProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var streetAddressEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var stateEditText: EditText
    private lateinit var countryEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var resumeNameTextView: TextView
    private lateinit var changeResumeButton: Button
    private lateinit var saveProfileButton: Button
    private val REQUEST_CODE = 1000
    private lateinit var storageReference: FirebaseStorage

    private lateinit var calendar: Calendar
    private lateinit var datePickerDialog: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_user_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.edit_first_name)
        lastNameEditText = findViewById(R.id.edit_last_name)
        emailEditText = findViewById(R.id.edit_email)
        phoneEditText = findViewById(R.id.edit_phone)
        dobEditText = findViewById(R.id.edit_dob)
        streetAddressEditText = findViewById(R.id.edit_street_address)
        cityEditText = findViewById(R.id.edit_city)
        stateEditText = findViewById(R.id.edit_state)
        countryEditText = findViewById(R.id.edit_country)
        genderRadioGroup = findViewById(R.id.radio_group_gender)
        resumeNameTextView = findViewById(R.id.resume_name)
        changeResumeButton = findViewById(R.id.btn_change_resume)
        saveProfileButton = findViewById(R.id.btn_save_profile)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Add logic for saving the profile when the save button is clicked
        saveProfileButton.setOnClickListener {
            if (validateFields()) {
                saveProfile()
            }
        }

        storageReference = FirebaseStorage.getInstance()
        loadUserProfile()

        // Add logic for changing the resume
        changeResumeButton.setOnClickListener {
            // Open the file picker for resume upload
            openFileManager()
        }

        // Initialize Calendar and DatePickerDialog
        calendar = Calendar.getInstance()
        datePickerDialog = DatePickerDialog(
            this,
            { view, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                dobEditText.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set a click listener on the Date of Birth input field
        dobEditText.setOnClickListener {
            // Show the DatePickerDialog when the user clicks the field
            datePickerDialog.show()
        }
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstname")
                    val lastName = document.getString("lastname")
                    val email = document.getString("email")
                    val phone = document.getString("phone")
                    val dob = document.getString("dob")
                    val streetAddress = document.getString("street")
                    val city = document.getString("city")
                    val state = document.getString("state")
                    val country = document.getString("country")
                    val gender = document.getString("gender")
                    val resumeUrl = document.getString("fileName")

                    // Populate the fields with data
                    firstNameEditText.setText(firstName)
                    lastNameEditText.setText(lastName)
                    emailEditText.setText(email)
                    phoneEditText.setText(phone)
                    dobEditText.setText(dob)
                    streetAddressEditText.setText(streetAddress)
                    cityEditText.setText(city)
                    stateEditText.setText(state)
                    countryEditText.setText(country)

                    // Set the gender radio button
                    when (gender) {
                        "Male" -> genderRadioGroup.check(R.id.radio_male)
                        "Female" -> genderRadioGroup.check(R.id.radio_female)
                    }

                    // Set resume file name
                    if (resumeUrl != null) {
                        resumeNameTextView.text = "Resume: ${resumeUrl?.substringAfterLast("/")}"
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@EditUserProfile, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateFields(): Boolean {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val dob = dobEditText.text.toString().trim()
        val streetAddress = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val state = stateEditText.text.toString().trim()
        val country = countryEditText.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
            dob.isEmpty() || streetAddress.isEmpty() || city.isEmpty() || state.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!phone.matches("^[0-9]{10}$".toRegex())) {
            Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show()
            return false
        }

        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveProfile() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val dob = dobEditText.text.toString().trim()
        val streetAddress = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val state = stateEditText.text.toString().trim()
        val country = countryEditText.text.toString().trim()

        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        val genderRadioButton = findViewById<RadioButton>(selectedGenderId)
        val gender = genderRadioButton.text.toString()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userProfile = mapOf(
                "firstname" to firstName,
                "lastname" to lastName,
                "email" to email,
                "phone" to phone,
                "dob" to dob,
                "street" to streetAddress,
                "city" to city,
                "state" to state,
                "country" to country,
                "gender" to gender
            )

            firestore.collection("users").document(userId).update(userProfile)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFileManager() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val fileUri = data.data
            if (fileUri != null) {
                val fileName = getFileName(fileUri)
                resumeNameTextView.text = "Resume: $fileName"

                val fileRef = storageReference.reference.child("resumes/$fileName")
                fileRef.putFile(fileUri)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Resume uploaded successfully", Toast.LENGTH_SHORT).show()

                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            val resumeUrl = fileRef.downloadUrl.toString()
                            firestore.collection("users").document(userId).update("fileName", resumeUrl)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to upload resume", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (columnIndex != -1) {
                name = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return name
    }
}
