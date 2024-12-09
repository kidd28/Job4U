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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.job4u.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PostJob.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostJob : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var companyRef: DatabaseReference

    // TODO: Rename and change types of parameters
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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_post_job, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("jobPosts")
        companyRef = FirebaseDatabase.getInstance().getReference("companies")

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
            companyRef.child(userId).child("companyName").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val companyName = snapshot.getValue(String::class.java)
                    if (companyName != null) {
                        etCompanyName.setText(companyName)
                        etCompanyName.isEnabled = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Toast.makeText(requireContext(), "Failed to fetch company name: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
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
                requirements.isEmpty() || state.isEmpty()|| city.isEmpty() || salary.isEmpty() || jobType.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                // Use currentTimeMillis for unique jobId
                val jobId = System.currentTimeMillis().toString()
                // Get current date and time
                // Get current date in MM-dd-yyyy format
                val currentDate = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Date())

                val jobData = mapOf(
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

                database.child(jobId).setValue(jobData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Job posted successfully!", Toast.LENGTH_SHORT).show()
                        clearFields(etJobTitle, etCompanyName, etJobDescription, etRequirements, etCity,etState, etSalary, etJobType)
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PostJob.
         */
        // TODO: Rename and change types and number of parameters
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