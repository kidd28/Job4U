package com.project.job4u.Authentication

import android.app.Activity
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.job4u.MainActivity
import com.project.job4u.R
import java.util.UUID

class ApplicantContact : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: FirebaseStorage
    private lateinit var phoneInput: TextInputEditText
    private lateinit var streetInput: TextInputEditText
    private lateinit var cityInput: TextInputEditText
    private lateinit var stateInput: TextInputEditText
    private lateinit var countryInput: TextInputEditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var resumeButton: MaterialButton
    private lateinit var resume_uploaded_text: TextView
    private lateinit var save: MaterialButton
    private val REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_applicant_contact)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance()

        // Initialize UI components
        phoneInput = findViewById(R.id.phone_input)
        streetInput = findViewById(R.id.street_address_input)
        cityInput = findViewById(R.id.city_input)
        stateInput = findViewById(R.id.state_input)
        countryInput = findViewById(R.id.country_input)
        genderRadioGroup = findViewById(R.id.gender_radio_group)
        resumeButton = findViewById(R.id.resume_button)
        save = findViewById(R.id.Save)
        resume_uploaded_text = findViewById(R.id.resume_uploaded_text)

        resumeButton.setOnClickListener { openFileManager() }

        // Set save button click listener
        save.setOnClickListener {
            if (validateInputs()) {
                uploadUserData()
            }
        }
    }

    private fun validateInputs(): Boolean {
        return when {
            phoneInput.text.isNullOrEmpty() -> {
                showToast("Phone number is required")
                false
            }
            streetInput.text.isNullOrEmpty() -> {
                showToast("Street address is required")
                false
            }
            cityInput.text.isNullOrEmpty() -> {
                showToast("City is required")
                false
            }
            stateInput.text.isNullOrEmpty() -> {
                showToast("State/Province is required")
                false
            }
            countryInput.text.isNullOrEmpty() -> {
                showToast("Country is required")
                false
            }
            genderRadioGroup.checkedRadioButtonId == -1 -> {
                showToast("Gender is required")
                false
            }
            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun uploadUserData() {
        val phone = phoneInput.text.toString()
        val street = streetInput.text.toString()
        val city = cityInput.text.toString()
        val state = stateInput.text.toString()
        val country = countryInput.text.toString()

        // Get the selected gender
        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        val genderRadioButton: MaterialRadioButton = findViewById(selectedGenderId)
        val gender = genderRadioButton.text.toString()

        // Prepare the data to upload
        val userData = mapOf(
            "phone" to phone,
            "street" to street,
            "city" to city,
            "state" to state,
            "country" to country,
            "gender" to gender,
        )

        // Get the current user from Firebase Authentication
        val userId = auth.currentUser?.uid ?: return showToast("User is not signed in")

        // Upload user data to Firestore
        firestore.collection("tbl_users").document(userId).update(userData)
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
            }
            .addOnFailureListener {
                showToast("Failed: ${it.message}")
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
            val fileUri: Uri = data.data!!
            val fileName = getFileName(fileUri)
            resume_uploaded_text.text = "  $fileName"
            resume_uploaded_text.visibility = View.VISIBLE
            uploadFileToFirebase(fileUri, fileName)
        }
    }

    private fun uploadFileToFirebase(fileUri: Uri, fileName: String) {
        val userId = auth.currentUser?.uid ?: return
        val filePath = "resumes/$userId/${UUID.randomUUID()}_$fileName"
        val fileReference = storageReference.reference.child(filePath)

        fileReference.putFile(fileUri)
            .addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveFileLinkToFirestore(userId, downloadUrl.toString(), fileName)
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to upload resume: ${e.message}")
            }
    }

    private fun saveFileLinkToFirestore(userId: String, fileUrl: String, fileName: String) {
        val userData = mapOf(
            "resume_url" to fileUrl,
            "fileName" to fileName
        )
        firestore.collection("tbl_users").document(userId).update(userData)
            .addOnSuccessListener {
                showToast("Resume link saved successfully!")
            }
            .addOnFailureListener { e ->
                showToast("Failed to save resume link: ${e.message}")
            }
    }

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
