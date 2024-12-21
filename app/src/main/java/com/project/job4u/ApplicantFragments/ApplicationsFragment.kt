package com.project.job4u.ApplicantFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.project.job4u.Adapter.MyApplicationsAdapter
import com.project.job4u.ApplicantJobDetails
import com.project.job4u.Application
import com.project.job4u.Authentication.SignInActivity
import com.project.job4u.Job
import com.project.job4u.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ApplicationsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyApplicationsAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var not_signed_in: LinearLayout
    private lateinit var sign_in_button: MaterialButton
    private val applicationList = mutableListOf<Application>()

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
        val view = inflater.inflate(R.layout.fragment_applications, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewJobs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        not_signed_in = view.findViewById(R.id.not_signed_in)
        sign_in_button = view.findViewById(R.id.sign_in_button)

        // Initialize the adapter with applicationList and the callback for item click
        adapter = MyApplicationsAdapter(applicationList) { application ->
            // Navigate to job details activity
            val intent = Intent(requireContext(), ApplicantJobDetails::class.java)
            intent.putExtra("applicationDetails", application) // Pass the application data
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()

        sign_in_button.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
        }

        // Fetch the user's applications from Firestore
        fetchMyApplications()

        return view
    }

    private fun fetchMyApplications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            recyclerView.visibility = View.GONE
            not_signed_in.visibility = View.VISIBLE
            return
        }

        recyclerView.visibility = View.VISIBLE
        not_signed_in.visibility = View.GONE

        // Reference to the applications collection
        val applicationsRef = db.collection("applications")
            .whereEqualTo("userId", userId)  // Query to fetch only applications by the current user

        // Fetch the applications where the user is the applicant
        applicationsRef.get().addOnSuccessListener { querySnapshot ->
            applicationList.clear()
            if (!querySnapshot.isEmpty) {
                // Iterate through the job applications for the user
                for (document in querySnapshot.documents) {
                    val application = document.toObject(Application::class.java)

                    // Log the application details for debugging
                    Log.d("FirestoreDebug", "Application details: $application")

                    // Fetch job details for each application (you can modify this to match your needs)
                    val jobId = application?.jobId
                    val applicationStatus = application?.applicationStatus
                    if (jobId != null && applicationStatus != null) {
                        fetchJobDetails(jobId, applicationStatus)
                    }
                }
            } else {
                // If no applications found, notify the adapter with an empty list
                adapter.notifyDataSetChanged()
            }
        }.addOnFailureListener {
            Log.e("FirestoreError", "Failed to load applications: ${it.message}")
            Toast.makeText(context, "Failed to load applications", Toast.LENGTH_SHORT).show()
        }
    }



    private fun fetchJobDetails(jobId: String, applicationStatus: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get the current signed-in userId

        // Query the applications collection for documents that match the userId and the jobId
        db.collection("applications")
            .whereEqualTo("userId", userId)  // Filter by the userId
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    // Iterate through the applications to find the matching jobId
                    for (document in snapshot.documents) {
                        val applicationData = document.toObject(Application::class.java)
                        applicationData?.let {
                            // Check if the jobId in the application matches the current jobId
                            if (it.jobId == jobId) {
                                // Create an Application object with job details and application status
                                val application = Application(
                                    jobId = jobId,
                                    jobTitle = it.jobTitle,
                                    companyName = it.companyName,
                                    location = it.location,
                                    description = it.description,
                                    salary = it.salary,
                                    requirements = it.requirements,
                                    postedOn = it.postedOn,
                                    applicationStatus = applicationStatus,  // Use the status passed to the function
                                    date = it.date // You can replace with actual application date if available
                                )
                                // Add to the list and update the UI
                                applicationList.add(application)
                                adapter.notifyDataSetChanged() // Notify the adapter that data has changed
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "No applications found for this user", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load job details", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ApplicationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
