package com.project.job4u

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.project.job4u.Authentication.SignInActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplicantJobDetails : AppCompatActivity() {
    private lateinit var jobTitleText: TextView
    private lateinit var companyNameText: TextView
    private lateinit var jobDescriptionText: TextView
    private lateinit var jobLocationText: TextView
    private lateinit var salaryText: TextView
    private lateinit var jobTypeText: TextView
    private lateinit var requirementsText: TextView
    private lateinit var postedOnText: TextView

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var job_id: String
    private lateinit var status: String

    private lateinit var withdrawButton: MaterialButton
    private lateinit var applyButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var unsaveButton: MaterialButton
    private lateinit var sign_in: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_applicant_job_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
        val applicationDetails = intent.getParcelableExtra<Application>("applicationDetails")

        if (job != null) {
            // Set the data in the TextViews
            jobTitleText.text = job.job_title
            companyNameText.text = job.company_name
            jobDescriptionText.text = job.jobDescription
            jobLocationText.text = "${job.city}, ${job.state}"
            salaryText.text = job.salary
            jobTypeText.text = job.jobType
            requirementsText.text = job.requirements
            postedOnText.text = job.postedOn
            job_id = job.job_id
            status = job.status
        }
        if (applicationDetails != null) {
            // Set the data in the TextViews
            jobTitleText.text = applicationDetails.job_title
            companyNameText.text = applicationDetails.company_name
            jobDescriptionText.text = applicationDetails.description
            jobLocationText.text = applicationDetails.location
            salaryText.text = applicationDetails.salary
            requirementsText.text = applicationDetails.requirements
            postedOnText.text = applicationDetails.postedOn

            job_id = applicationDetails.job_id
            status = applicationDetails.application_status
        }

        // Get references to buttons
        withdrawButton = findViewById(R.id.withdraw_button)
        applyButton = findViewById(R.id.apply_button)
        saveButton = findViewById(R.id.save_button)
        unsaveButton = findViewById(R.id.unsave_button)
        sign_in = findViewById(R.id.sign_in)

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            withdrawButton.visibility = View.GONE
            applyButton.visibility = View.GONE
            saveButton.visibility = View.GONE
            unsaveButton.visibility = View.GONE
            sign_in.visibility = View.VISIBLE
        } else {
            withdrawButton.visibility = View.VISIBLE
            applyButton.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            unsaveButton.visibility = View.VISIBLE
            sign_in.visibility = View.GONE
        }

        // Check if the applicant has already applied or saved this job
        checkJobStatus()


        // Apply button click listener
        applyButton.setOnClickListener {
            showApplyConfirmationDialog(job)
        }

        // Withdraw button click listener
        withdrawButton.setOnClickListener {
            showWithdrawConfirmationDialog()
        }

        // Save button click listener
        saveButton.setOnClickListener {
            if (job != null) {
                saveJob(job)
            }
            if (applicationDetails != null) {
                saveJob(applicationDetails)
            }
        }
        sign_in.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        unsaveButton.setOnClickListener {
            Unsave()
        }
    }

    private fun Unsave() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("savedJobs").document(userId).collection("jobs").document(job_id).delete().addOnSuccessListener {
                // Job is saved, show "Saved" on button
                saveButton.visibility = View.VISIBLE
                unsaveButton.visibility = View.GONE
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Job was removed from saved jobs successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkJobStatus() {
        val userId = auth.currentUser?.uid ?: return

        // Check if the jobId is in the user's jobApplied node
        db.collection("tbl_users").document(userId).collection("jobApplied").document(job_id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Job is already applied for, show Withdraw button
                    applyButton.visibility = View.GONE
                    withdrawButton.visibility = View.VISIBLE
                } else {
                    // Job is not yet applied, show Apply button
                    applyButton.visibility = View.VISIBLE
                    withdrawButton.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@ApplicantJobDetails, "Failed to check application status", Toast.LENGTH_SHORT).show()
            }

        // Check if the job is saved
        db.collection("savedJobs").document(userId).collection("jobs").document(job_id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Job is saved, show "Saved" on button
                    saveButton.visibility = View.GONE
                    unsaveButton.visibility = View.VISIBLE
                } else {
                    // Job is not saved, show "Save" button
                    saveButton.visibility = View.VISIBLE
                    unsaveButton.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@ApplicantJobDetails, "Failed to check saved job status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showApplyConfirmationDialog(job: Job?) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Confirm Application")
        builder.setMessage("Your resume will be submitted to the recruiter for review. Do you want to proceed?")

        builder.setPositiveButton("Apply") { dialog, _ ->
            if (job != null) {
                applyForJob(job)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun applyForJob(job: Job) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return // Ensure the user is logged in
        val job_id = job.job_id // Ensure that job has a jobId property

        // Get the current date in the format "MM-dd-yyyy"
        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Fetch the user's name and email from the "users" node
        val userRef = FirebaseFirestore.getInstance().collection("tbl_users").document(userId)
        userRef.get()
            .addOnSuccessListener { document ->
                val firstname = document.getString("firstname") ?: "Unknown"
                val lastname = document.getString("lastname") ?: "Unknown"
                val applicantEmail = document.getString("email") ?: "Unknown"
                val applicantResume = document.getString("resume_url") ?: "Unknown"
                val applicantPhone = document.getString("phone") ?: "Unknown"
// Generate applicationId using current timestamp
                val applicationId = System.currentTimeMillis().toString()
                // Create an Application map with the necessary job details and user information
                val applicationData = hashMapOf(
                    "job_id" to job_id,
                    "job_title" to job.job_title,
                    "company_name" to job.company_name,
                    "location" to "${job.city}, ${job.state}",
                    "description" to job.jobDescription,
                    "salary" to job.salary,
                    "requirements" to job.requirements,
                    "postedOn" to job.postedOn,
                    "application_status" to "applied", // Initial status is "applied"
                    "applied_on" to currentDate,
                    "user_id" to userId,
                    "postedBy" to job.postedBy,
                    "applicantName" to "$firstname $lastname",
                    "applicantEmail" to applicantEmail,
                    "applicantResume" to applicantResume,
                    "applicantPhone" to applicantPhone,
                    "application_id" to applicationId
                )



                // Save the application to Firestore under the path: applications > applicationId
                FirebaseFirestore.getInstance().collection("tbl_applications")
                    .document(applicationId) // Use generated timestamp as document ID
                    .set(applicationData)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Optionally, add jobId under the "jobApplied" collection in the user's node
                            FirebaseFirestore.getInstance().collection("tbl_users")
                                .document(userId)
                                .collection("jobApplied") // Subcollection to track applied jobs
                                .document(job_id) // Save jobId as document ID
                                .set(mapOf("applied" to true))

                            val intent = Intent(this@ApplicantJobDetails, MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this@ApplicantJobDetails, "Application submitted successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ApplicantJobDetails, "Failed to apply for the job", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this@ApplicantJobDetails, "Failed to retrieve user details", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveJob(job: Job?) {
        val userId = auth.currentUser?.uid ?: return
        val job_id = job?.job_id ?: return

        // Save job details under the savedJobs collection
        val savedJob = Application(
            job_id = job_id,
            job_title = job?.job_title ?: "",
            location = "${job?.city}, ${job?.state}",
            description = job?.jobDescription ?: "",
            salary = job?.salary ?: "",
            requirements = job?.requirements ?: "",
            postedOn = job?.postedOn ?: "",
            application_status = "saved", // Mark as saved
            applied_on = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date())
        )

        // Save the job in the savedJobs collection
        db.collection("savedJobs").document(userId).collection("jobs").document(job_id).set(savedJob)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveButton.text = "Saved"
                    saveButton.isEnabled = false
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Job saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save job", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveJob(application: Application?) {
        val userId = auth.currentUser?.uid ?: return
        val job_id = application?.job_id ?: return

        // Save the application as saved
        db.collection("savedJobs").document(userId).collection("jobs").document(job_id)
            .set(application!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveButton.text = "Saved"
                    saveButton.isEnabled = false
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Job saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save job", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showWithdrawConfirmationDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Withdraw Application")
        builder.setMessage("Are you sure you want to withdraw your application for this job?")

        builder.setPositiveButton("Withdraw") { dialog, _ ->
            withdrawApplication()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun withdrawApplication() {
        val userId = auth.currentUser?.uid ?: return

        // Reference to the application in the "applications" collection using applicationId (timestamp)
        val applicationRef = db.collection("tbl_applications")
            .whereEqualTo("user_id", userId) // Find the application by userId
            .whereEqualTo("job_id", job_id) // and jobId to ensure it's the correct application
            .limit(1) // To ensure we get only one application

        // Fetch the application document
        applicationRef.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val applicationDoc = querySnapshot.documents[0]
                val applicationId = applicationDoc.id // Get the applicationId (document ID)

                // Delete the application document using the applicationId
                db.collection("tbl_applications")
                    .document(applicationId)
                    .delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Reference to the user's jobApplied document
                            val userJobAppliedRef = db.collection("tbl_users")
                                .document(userId)
                                .collection("jobApplied")
                                .document(job_id)

                            // Remove the jobId from the "jobApplied" collection
                            userJobAppliedRef.delete().addOnCompleteListener { innerTask ->
                                if (innerTask.isSuccessful) {
                                    // Both application and jobApplied data have been deleted
                                    Toast.makeText(this, "Application withdrawn successfully", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)

                                    // Hide the withdraw button and show the apply button again
                                    applyButton.visibility = View.VISIBLE
                                    withdrawButton.visibility = View.GONE
                                } else {
                                    // Failed to remove from jobApplied list
                                    Toast.makeText(this, "Failed to update application status", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Failed to remove application from "applications"
                            Toast.makeText(this, "Failed to withdraw application", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // No application found for this user and job
                Toast.makeText(this, "No application found to withdraw", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // Failed to fetch the application
            Toast.makeText(this, "Failed to fetch application details", Toast.LENGTH_SHORT).show()
        }
    }


}
