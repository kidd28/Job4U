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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.job4u.Authentication.EmployerSignUp
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

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var jobId: String
    private lateinit var status: String

    private lateinit var withdrawButton: MaterialButton
    private lateinit var applyButton: MaterialButton
    private lateinit var saveButton: MaterialButton
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
        database = FirebaseDatabase.getInstance().reference

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
        if (applicationDetails != null) {
            // Set the data in the TextViews
            jobTitleText.text = applicationDetails.jobTitle
            companyNameText.text = applicationDetails.companyName
            jobDescriptionText.text = applicationDetails.description
            jobLocationText.text = applicationDetails.location
            salaryText.text = applicationDetails.salary
            requirementsText.text = applicationDetails.requirements
            postedOnText.text = applicationDetails.postedOn

            jobId = applicationDetails.jobId
            status = applicationDetails.applicationStatus
        }

        // Get references to buttons
        withdrawButton = findViewById(R.id.withdraw_button)
        applyButton = findViewById(R.id.apply_button)
        saveButton = findViewById(R.id.save_button)
        sign_in = findViewById(R.id.sign_in)

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            withdrawButton.visibility = View.GONE
            applyButton.visibility = View.GONE
            saveButton.visibility = View.GONE
            sign_in.visibility = View.VISIBLE
        }else{
            withdrawButton.visibility = View.VISIBLE
            applyButton.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
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
            if(applicationDetails != null){
                saveJob(applicationDetails)
            }
        }
        sign_in.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent) }
    }

    private fun checkJobStatus() {
        val userId = auth.currentUser?.uid ?: return

        // Check if the jobId is in the user's jobApplied node
        database.child("users").child(userId).child("jobApplied").child(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Job is already applied for, show Withdraw button
                        applyButton.visibility = View.GONE
                        withdrawButton.visibility = View.VISIBLE
                    } else {
                        // Job is not yet applied, show Apply button
                        applyButton.visibility = View.VISIBLE
                        withdrawButton.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ApplicantJobDetails, "Failed to check application status", Toast.LENGTH_SHORT).show()
                }
            })

        // Check if the job is saved
        database.child("savedJobs").child(userId).child(jobId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Job is saved, show "Saved" on button
                        saveButton.text = "Saved"
                        saveButton.isEnabled = false
                    } else {
                        // Job is not saved, show "Save" button
                        saveButton.text = "Save Job"
                        saveButton.isEnabled = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ApplicantJobDetails, "Failed to check saved job status", Toast.LENGTH_SHORT).show()
                }
            })
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
        val userId = auth.currentUser?.uid ?: return // Ensure the user is logged in
        val jobId = job.jobId // Ensure that job has a jobId property

        // Get the current date in the format "MM-dd-yyyy"
        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Fetch the user's name and email from the "users" node
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firstname = snapshot.child("firstname").getValue(String::class.java) ?: "Unknown"
                val lastname = snapshot.child("lastname").getValue(String::class.java) ?: "Unknown"
                val applicantEmail = snapshot.child("email").getValue(String::class.java) ?: "Unknown"
                val applicantResume = snapshot.child("resume").getValue(String::class.java) ?: "Unknown"
                val applicantPhone = snapshot.child("phone").getValue(String::class.java) ?: "Unknown"

                // Create an Application object with the necessary job details and user information
                val applicationData = Application(
                    jobId = jobId,
                    jobTitle = job.jobTitle,
                    companyName = job.companyName,
                    location = "${job.city}, ${job.state}",
                    description = job.jobDescription,
                    salary = job.salary,
                    requirements = job.requirements,
                    postedOn = job.postedOn,
                    applicationStatus = "applied", // Initial status is "applied"
                    date = currentDate,
                    userId = userId,
                    postedBy = job.postedBy,
                    applicantName = firstname+" "+lastname,
                    applicantEmail = applicantEmail,
                    applicantResume = applicantResume,
                    applicantPhone = applicantPhone
                )

                // Step 1: Add the application to the "applications" node under the userId
                val applicationRef = FirebaseDatabase.getInstance().getReference("applications")
                    .child(userId) // Add under the user's node
                    .child(jobId)  // Add under the specific jobId node
                applicationRef.setValue(applicationData)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Step 2: Add jobId under the "jobApplied" node in the user's node
                            val userJobAppliedRef = FirebaseDatabase.getInstance().getReference("users")
                                .child(userId) // Under the user node
                                .child("jobApplied") // Inside jobApplied node
                                .child(jobId) // Add jobId under jobApplied
                            userJobAppliedRef.setValue(true) // Indicating that the user has applied for this job
                            val intent = Intent(this@ApplicantJobDetails, MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this@ApplicantJobDetails, "Application submitted successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ApplicantJobDetails, "Failed to apply for the job", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ApplicantJobDetails, "Failed to retrieve user details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveJob(job: Job?) {
        val userId = auth.currentUser?.uid ?: return
        val jobId = job?.jobId ?: return

        // Save job details under the savedJobs node
        val savedJob = Application(
            jobId = jobId,
            jobTitle = job?.jobTitle ?: "",
            companyName = job?.companyName ?: "",
            location = "${job?.city}, ${job?.state}",
            description = job?.jobDescription ?: "",
            salary = job?.salary ?: "",
            requirements = job?.requirements ?: "",
            postedOn = job?.postedOn ?: "",
            applicationStatus = "saved", // Mark as saved
            date = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date())
        )

        // Save the job in the savedJobs node
        database.child("savedJobs").child(userId).child(jobId).setValue(savedJob)
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
        val jobId = application?.jobId ?: return

        // Save job details under the savedJobs node
        val savedJob = Application(
            jobId = jobId,
            jobTitle = application?.jobTitle ?: "",
            companyName = application?.companyName ?: "",
            location = application.location,
            description = application?.description ?: "",
            salary = application?.salary ?: "",
            requirements = application?.requirements ?: "",
            postedOn = application?.postedOn ?: "",
            applicationStatus = "saved", // Mark as saved
            date = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date())
        )

        // Save the job in the savedJobs node
        database.child("savedJobs").child(userId).child(jobId).setValue(savedJob)
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
        builder.setTitle("Confirm Withdrawal")
        builder.setMessage("Are you sure you want to withdraw your application for this job?")

        builder.setPositiveButton("Withdraw") { dialog, _ ->
            withdrawApplication()
            dialog.dismiss() // Close the dialog after withdrawing
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss() // Simply close the dialog if canceled
        }

        builder.show()
    }

    private fun withdrawApplication() {
        val userId = auth.currentUser?.uid ?: return

        // Remove the application from the "applications" node (delete the specific job's application)
        val applicationRef = database.child("applications").child(userId).child(jobId)

        applicationRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Remove the jobId from user's "jobApplied" list
                val userJobAppliedRef = database.child("users").child(userId).child("jobApplied").child(jobId)
                userJobAppliedRef.removeValue().addOnCompleteListener { innerTask ->
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
    }
}
