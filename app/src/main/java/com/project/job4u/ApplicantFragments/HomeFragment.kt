package com.project.job4u.ApplicantFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.job4u.ApplicantJobDetails
import com.project.job4u.Adapter.ApplicantJobAdapter
import com.project.job4u.Job
import com.project.job4u.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private lateinit var jobAdapter: ApplicantJobAdapter
    private lateinit var jobList: MutableList<Job>
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

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
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("jobPosts")

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerViewApplicantJobs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        jobList = mutableListOf()
        jobAdapter = ApplicantJobAdapter(jobList,
            onSaveJobClick = { job ->
                // Handle saving the job to favorites
            },
            onJobClick = { job ->
                // Navigate to the JobDetailsActivity when job item is clicked
                val intent = Intent(requireContext(), ApplicantJobDetails::class.java)
                intent.putExtra("jobDetails", job) // Pass job data to the new activity
                startActivity(intent)
            })
        recyclerView.adapter = jobAdapter

        // Fetch the jobs from the Firebase database
        fetchApplicantJobs()


        return view
    }
    private fun fetchApplicantJobs() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val jobAppliedRef = FirebaseDatabase.getInstance()
            .getReference("applications")
            .child(userId)

        // First, fetch all jobs from the database with active status
        database.orderByChild("status")
            .equalTo("active")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempJobList = mutableListOf<Job>()
                    val jobIdsToHide = mutableSetOf<String>()
                    for (jobSnapshot in snapshot.children) {
                        val job = jobSnapshot.getValue(Job::class.java)
                        if (job != null) {
                            tempJobList.add(job)
                        }
                    }

                    // Now, fetch the user's jobApplied list to filter the job posts
                    jobAppliedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(appliedSnapshot: DataSnapshot) {
                            if (appliedSnapshot.exists()) {
                                for (appliedJobSnapshot in appliedSnapshot.children) {
                                    appliedJobSnapshot.key?.let { jobIdsToHide.add(it) }
                                }
                            }
                            // Filter out jobs the user has already applied for
                            jobList.clear()
                            jobList.addAll(tempJobList.filter { it.jobId !in jobIdsToHide })

                            // Sort the job list by postedOn date in ascending order
                            jobList.sortByDescending { it.postedOn }
                            // Notify adapter of the updated data
                            jobAdapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("FirebaseError", "Failed to load applied jobs: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load jobs", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}