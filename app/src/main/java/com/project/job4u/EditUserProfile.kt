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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Calendar
import java.util.UUID

class EditUserProfile : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
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
    private lateinit var databaseReference: FirebaseDatabase

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
        // Add logic for saving the profile when the save button is clicked
        saveProfileButton.setOnClickListener {
            if (validateFields()) {
                saveProfile()
            }
        }

        storageReference = FirebaseStorage.getInstance()
        databaseReference = FirebaseDatabase.getInstance()
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

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstname").getValue(String::class.java)
                    val lastName = snapshot.child("lastname").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val phone = snapshot.child("phone").getValue(String::class.java)
                    val dob = snapshot.child("dob").getValue(String::class.java)
                    val streetAddress = snapshot.child("street").getValue(String::class.java)
                    val city = snapshot.child("city").getValue(String::class.java)
                    val state = snapshot.child("state").getValue(String::class.java)
                    val country = snapshot.child("country").getValue(String::class.java)
                    val gender = snapshot.child("gender").getValue(String::class.java)
                    val resumeUrl = snapshot.child("fileName").getValue(String::class.java)

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

                    // Load the profile image using Glide
                    val profileImageUrl = snapshot.child("profileImage").getValue(String::class.java)

                    // Set resume file name
                    if (resumeUrl != null) {
                        resumeNameTextView.text = "Resume: ${resumeUrl?.substringAfterLast("/")}"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditUserProfile, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun validateFields(): Boolean {
        // Check if all required fields are not empty
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val dob = dobEditText.text.toString().trim()
        val streetAddress = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val state = stateEditText.text.toString().trim()
        val country = countryEditText.text.toString().trim()

        // Validate that none of the fields are empty
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
            dob.isEmpty() || streetAddress.isEmpty() || city.isEmpty() || state.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate phone number format (just a basic check)
        if (!phone.matches("^[0-9]{10}$".toRegex())) {
            Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check gender selection
        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show()
            return false
        }



        return true
    }

    private fun saveProfile() {
        // Retrieve the values from the fields
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val dob = dobEditText.text.toString().trim()
        val streetAddress = streetAddressEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val state = stateEditText.text.toString().trim()
        val country = countryEditText.text.toString().trim()

        // Retrieve gender from the selected radio button
        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        val genderRadioButton = findViewById<RadioButton>(selectedGenderId)
        val gender = genderRadioButton.text.toString()

        // If the user is authenticated, save to Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            // Create a map with the updated profile information
            val userUpdates = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "phone" to phone,
                "dob" to dob,
                "street" to streetAddress,
                "city" to city,
                "state" to state,
                "country" to country,
                "gender" to gender
            )

            // Update the user profile data in Firebase
            userRef.updateChildren(userUpdates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()  // Close the activity after saving
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
        intent.type = "application/pdf"  // For PDF files
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        startActivityForResult(intent, REQUEST_CODE)
    }
    // Handle the result after file is selected
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val fileUri: Uri = data.data!!
            val fileName = getFileName(fileUri)

            // Update the TextView with the file name
            resumeNameTextView.text = "  "+fileName
            resumeNameTextView.visibility = View.VISIBLE
            // Upload the file to Firebase
            uploadFileToFirebase(fileUri, fileName)
        }
    }

    // Upload the selected file to Firebase Storage
    private fun uploadFileToFirebase(fileUri: Uri, fileName: String) {
        val userId = auth.currentUser?.uid ?: return
        val filePath = "resumes/$userId/${UUID.randomUUID()}_$fileName"
        val fileReference = storageReference.reference.child(filePath)

        fileReference.putFile(fileUri)
            .addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveFileLinkToDatabase(userId, downloadUrl.toString(),fileName)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload resume: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Save the file download link to Firebase Database
    private fun saveFileLinkToDatabase(userId: String, fileUrl: String, fileName: String) {
        val userResumeRef = databaseReference.reference.child("users").child(userId)
        val userData = mapOf(
            "resume" to fileUrl,
            "fileName" to fileName)
        userResumeRef.updateChildren(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Resume link saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save resume link: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Get the file name from the Uri
    private fun getFileName(uri: Uri): String {
        var fileName = "Unknown"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
    }
}