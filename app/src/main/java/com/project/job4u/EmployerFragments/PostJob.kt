package com.project.job4u.EmployerFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.project.job4u.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PostJob : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var companyRef: DocumentReference

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_job, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etJobTitle: TextInputEditText = view.findViewById(R.id.etJobTitle)
        val etCompanyName: TextInputEditText = view.findViewById(R.id.etCompanyName)
        val etJobDescription: TextInputEditText = view.findViewById(R.id.etJobDescription)
        val etRequirements: TextInputEditText = view.findViewById(R.id.etRequirements)
        val etCity: TextInputEditText = view.findViewById(R.id.etCity)
        val etState: TextInputEditText = view.findViewById(R.id.etState)
        val etSalary: TextInputEditText = view.findViewById(R.id.etSalary)
        val etJobType: TextInputEditText = view.findViewById(R.id.etJobType)
        val btnSubmit: MaterialButton = view.findViewById(R.id.btnSubmitJobPost)

        // Fetch and set company name if user is an employer
        val userId = auth.currentUser?.uid
        if (userId != null) {
            companyRef = db.collection("companies").document(userId)

            companyRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val companyName = document.getString("companyName")
                    if (companyName != null) {
                        etCompanyName.setText(companyName)
                        etCompanyName.isEnabled = false
                    }
                } else {
                    Toast.makeText(requireContext(), "No company data found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch company name", Toast.LENGTH_SHORT).show()
            }
        }

        btnSubmit.setOnClickListener {
            val jobTitle = etJobTitle.text.toString().trim()
            val companyName = etCompanyName.text.toString().trim()
            val jobDescription = etJobDescription.text.toString().trim()
            val requirements = etRequirements.text.toString().trim()
            val city = etCity.text.toString().trim()
            val state = etState.text.toString().trim()
            val salary = etSalary.text.toString().trim()
            val jobType = etJobType.text.toString().trim()

            if (jobTitle.isEmpty() || companyName.isEmpty() || jobDescription.isEmpty() ||
                requirements.isEmpty() || state.isEmpty() || city.isEmpty() || salary.isEmpty() || jobType.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                // Use currentTimeMillis for unique jobId
                val jobId = System.currentTimeMillis().toString()
                val currentDate = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date())

                val jobData = hashMapOf(
                    "jobId" to jobId,
                    "jobTitle" to jobTitle,
                    "companyName" to companyName,
                    "jobDescription" to jobDescription,
                    "requirements" to requirements,
                    "city" to city,
                    "state" to state,
                    "salary" to salary,
                    "jobType" to jobType,
                    "postedBy" to auth.currentUser?.uid,
                    "postedOn" to currentDate,
                    "status" to "active"
                )

                // Add job data to Firestore collection "jobPosts"
                db.collection("jobPosts").document(jobId)
                    .set(jobData)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Job posted successfully!", Toast.LENGTH_SHORT).show()
                            clearFields(etJobTitle, etCompanyName, etJobDescription, etRequirements, etCity, etState, etSalary, etJobType)
                        } else {
                            Toast.makeText(requireContext(), "Failed to post job: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        return view
    }

    private fun clearFields(vararg fields: TextInputEditText) {
        fields.forEach { it.text?.clear() }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PostJob().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
