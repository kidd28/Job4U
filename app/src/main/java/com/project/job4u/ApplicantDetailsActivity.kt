package com.project.job4u

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ApplicantDetailsActivity : AppCompatActivity() {

    private lateinit var tvJobTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvDateApplied: TextView
    private lateinit var tvApplicantName: TextView
    private lateinit var tvApplicantEmail: TextView
    private lateinit var tvApplicantPhone: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnResume: MaterialButton
    private lateinit var btnChangeStatus: MaterialButton

    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_applicant_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        tvJobTitle = findViewById(R.id.tvJobTitle)
        tvDescription = findViewById(R.id.tvDescription)
        tvDateApplied = findViewById(R.id.tvDateApplied)
        tvApplicantName = findViewById(R.id.tvApplicantName)
        tvApplicantEmail = findViewById(R.id.tvApplicantEmail)
        tvApplicantPhone = findViewById(R.id.tvApplicantPhone)
        btnResume = findViewById(R.id.btnResume)
        btnChangeStatus = findViewById(R.id.btnChangeStatus)
        tvStatus = findViewById(R.id.tvStatus)

        auth = FirebaseAuth.getInstance()
        val applicantDetails = intent.getParcelableExtra<Application>("applicantDetails")

        if (applicantDetails != null) {
            tvJobTitle.text = applicantDetails.jobTitle
            tvDescription.text = applicantDetails.description
            tvDateApplied.text = applicantDetails.postedOn
            tvApplicantName.text = applicantDetails.applicantName
            tvApplicantEmail.text = applicantDetails.applicantEmail
            tvApplicantPhone.text = applicantDetails.applicantPhone
            tvStatus.text = applicantDetails.applicationStatus
        }

        btnResume.setOnClickListener {
            val intent = Intent(this, ResumePreview::class.java)
            if (applicantDetails != null) {
                intent.putExtra("resume", applicantDetails.applicantResume)
            }
            startActivity(intent)
        }
        btnChangeStatus.setOnClickListener {
            if (applicantDetails != null) {
                showStatusUpdateDialog(applicantDetails.userId, applicantDetails.jobId)
            }
        }

    }

    private fun showStatusUpdateDialog(applicantId: String, jobId: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Update Application Status")
        dialogBuilder.setMessage("Choose the status for this applicant.")

        dialogBuilder.setPositiveButton("Accept") { _, _ ->
            updateApplicationStatus(applicantId, jobId, "Accepted")
        }

        dialogBuilder.setNegativeButton("Interview") { _, _ ->
            updateApplicationStatus(applicantId, jobId, "Interview")
        }

        dialogBuilder.setNeutralButton("Decline") { dialog, _ ->
            updateApplicationStatus(applicantId, jobId, "Declined")
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun updateApplicationStatus(userId: String, jobId: String, status: String) {
        // Query to find the application based on userId and jobId
        val applicationRef = firestore.collection("applications")
            .whereEqualTo("userId", userId) // Find by userId
            .whereEqualTo("jobId", jobId)   // Find by jobId
            .limit(1) // Ensure only one result is returned (since userId + jobId should be unique)

        // Fetch the application document
        applicationRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val applicationDoc = querySnapshot.documents[0]
                val applicationId = applicationDoc.id // Get the applicationId (document ID)

                // Update the application status
                firestore.collection("applications")
                    .document(applicationId) // Use the applicationId to update the correct document
                    .update("applicationStatus", status)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Application status updated to $status", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                // If no application is found for the given userId and jobId
                Toast.makeText(this, "No application found for this job", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            // If there's an error fetching the application
            Toast.makeText(this, "Failed to fetch application details: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

}
