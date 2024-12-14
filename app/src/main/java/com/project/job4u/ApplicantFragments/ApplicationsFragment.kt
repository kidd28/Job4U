package com.project.job4u.ApplicantFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.job4u.Adapter.MyApplicationsAdapter
import com.project.job4u.ApplicantJobDetails
import com.project.job4u.Application
import com.project.job4u.Authentication.SignInActivity
import com.project.job4u.Job
import com.project.job4u.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ApplicationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class ApplicationsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyApplicationsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var not_signed_in: LinearLayout
    private lateinit var sign_in_button: MaterialButton
    private val applicationList = mutableListOf<Application>()
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
        database = FirebaseDatabase.getInstance().reference

        sign_in_button.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent) }


        // Fetch the user's applications from Firebase
        fetchMyApplications()
        sign_in_button.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent) }


        return view
    }

    private fun fetchMyApplications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val applicationsRef = database.child("applications").child(userId.toString())
        if (userId == null) {
            recyclerView.visibility = View.GONE
            not_signed_in.visibility = View.VISIBLE
        }else{
            recyclerView.visibility = View.VISIBLE
            not_signed_in.visibility = View.GONE
        }


        // Fetch the list of job IDs the user has applied to
        applicationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                applicationList.clear()
                if (snapshot.exists()) {
                    for (appliedJobSnapshot in snapshot.children) {
                        // Get the jobId from the applications node
                        val jobId = appliedJobSnapshot.key ?: continue
                        val applicationStatus = appliedJobSnapshot.child("applicationStatus").getValue(String::class.java) ?: "Unknown"
                        fetchJobDetails(jobId, applicationStatus)
                    }
                } else {
                    // If no applications, notify adapter with empty list
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load applications", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchJobDetails(jobId: String, applicationStatus: String) {
        database.child("jobPosts").child(jobId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get job details from the jobPosts node
                val job = snapshot.getValue(Job::class.java)
                job?.let {
                    // Create an Application object with job details and application status
                    val applicationData = Application(
                        jobId = jobId,
                        jobTitle = it.jobTitle,
                        companyName = it.companyName,
                        location = "${it.city}, ${it.state}",
                        description = it.jobDescription,
                        salary = it.salary,
                        requirements = it.requirements,
                        postedOn = it.postedOn,
                        applicationStatus = applicationStatus,  // Use the status from applications node
                        date = it.postedOn // You can replace with actual application date if available
                    )
                    applicationList.add(applicationData)
                    adapter.notifyDataSetChanged() // Notify the adapter that data has changed
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load job details", Toast.LENGTH_SHORT).show()
            }
        })
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ApplicationsFragment.
         */
        // TODO: Rename and change types and number of parameters
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