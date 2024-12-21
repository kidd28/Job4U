package com.project.job4u

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JobDetailsActivity : AppCompatActivity() {

    private lateinit var jobTitleText: TextView
    private lateinit var companyNameText: TextView
    private lateinit var jobDescriptionText: TextView
    private lateinit var jobLocationText: TextView
    private lateinit var salaryText: TextView
    private lateinit var jobTypeText: TextView
    private lateinit var requirementsText: TextView
    private lateinit var postedOnText: TextView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var jobId: String // Job ID passed via intent
    private lateinit var status: String // Job ID passed via intent

    private lateinit var deleteButton: MaterialButton
    private lateinit var closeButton: MaterialButton
    private lateinit var open_button: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        jobTitleText = findViewById(R.id.jobTitleText)
        companyNameText = findViewById(R.id.companyNameText)
        jobDescriptionText = findViewById(R.id.jobDescriptionText)
        jobLocationText = findViewById(R.id.jobLocationText)
        salaryText = findViewById(R.id.salaryText)
        jobTypeText = findViewById(R.id.jobTypeText)
        requirementsText = findViewById(R.id.requirementsText)
        postedOnText = findViewById(R.id.postedOnText)

        // Get the job details from the Intent
        val job = intent.getParcelableExtra<Job>("jobDetails")

        if (job != null) {
            // Set the data in the TextViews
            jobTitleText.text = job.jobTitle
            companyNameText.text = job.companyName
            jobDescriptionText.text = job.jobDescription
            jobLocationText.text = "${job.city}, ${job.state}"
            salaryText.text = job.salary
            jobTypeText.text = job.jobType
            requirementsText.text = job.requirements
            postedOnText.text = job.postedOn

            jobId = job.jobId
            status = job.status
        }

        // Initialize buttons
        deleteButton = findViewById(R.id.delete_button)
        closeButton = findViewById(R.id.close_button)
        open_button = findViewById(R.id.open_button)

        if (status == "active") {
            closeButton.visibility = View.VISIBLE
            open_button.visibility = View.GONE
        } else {
            closeButton.visibility = View.GONE
            open_button.visibility = View.VISIBLE
        }

        // Open Job (Activate Job)
        open_button.setOnClickListener {
            showOpenConfirmationDialog(jobId)
        }
        // Delete Job
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(jobId)
        }

        // Close Job
        closeButton.setOnClickListener {
            showCloseConfirmationDialog(jobId)
        }
    }

    private fun showDeleteConfirmationDialog(jobId: String) {
        // Create the Delete confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Job")
            .setMessage("Are you sure you want to delete this job? This action cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                deleteJobPost(jobId)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showCloseConfirmationDialog(jobId: String) {
        // Create the Close confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Close Job")
            .setMessage("Are you sure you want to close this job? The job will no longer be open for applicants.")
            .setPositiveButton("Yes") { _, _ ->
                closeJobPost(jobId)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showOpenConfirmationDialog(jobId: String) {
        // Create the Open confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Open Job")
            .setMessage("Are you sure you want to open this job? The job will be active and available for applicants.")
            .setPositiveButton("Yes") { _, _ ->
                openJobPost(jobId)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteJobPost(jobId: String) {
        // Reference to the job in the Firestore database
        val jobRef = firestore.collection("jobPosts").document(jobId)

        // Delete the job post entirely
        jobRef.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Job deleted successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Close the activity after deletion
            } else {
                Toast.makeText(this, "Failed to delete job: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun closeJobPost(jobId: String) {
        // Reference to the job in the Firestore database
        val jobRef = firestore.collection("jobPosts").document(jobId)

        // Update the status of the job to "closed"
        jobRef.update("status", "closed").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Job marked as closed", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Close the activity after deletion
            } else {
                Toast.makeText(this, "Failed to close job: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openJobPost(jobId: String) {
        // Reference to the job in the Firestore database
        val jobRef = firestore.collection("jobPosts").document(jobId)

        // Update the status of the job to "active"
        jobRef.update("status", "active").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Job marked as active", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Optionally, close the activity after marking as active
            } else {
                Toast.makeText(this, "Failed to open job: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
