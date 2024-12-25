package com.project.job4u.ApplicantFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.project.job4u.ApplicantJobDetails
import com.project.job4u.Adapter.ApplicantJobAdapter
import com.project.job4u.Application
import com.project.job4u.Job
import com.project.job4u.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private lateinit var jobAdapter: ApplicantJobAdapter
    private lateinit var jobList: MutableList<Job>
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var jobSearchView: SearchView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        jobSearchView = view.findViewById(R.id.job_search_view)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
                intent.putExtra("jobDetails", job)
                startActivity(intent)
            })
        recyclerView.adapter = jobAdapter

        // Fetch the jobs from Firestore
        fetchApplicantJobs()

        // Set up SearchView listener
        jobSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchJobs(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchJobs(it) }
                return true
            }
        })

        return view
    }

    private fun fetchApplicantJobs() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        // Fetch all jobs with active status
        db.collection("tbl_job_listings")
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { snapshot ->
                val tempJobList = mutableListOf<Job>()
                for (jobSnapshot in snapshot) {
                    val job = jobSnapshot.toObject(Job::class.java)
                    job?.let {
                        tempJobList.add(it)
                    }
                }

                if (userId == null) {
                    // No user is signed in, show all jobs
                    jobList.clear()
                    jobList.addAll(tempJobList)

                    // Sort by postedOn date in descending order
                    jobList.sortByDescending { it.job_id }
                    jobAdapter.notifyDataSetChanged()
                } else {
                    // User is signed in, filter jobs
                    filterJobsForUser(userId, tempJobList)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load jobs", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterJobsForUser(userId: String, tempJobList: List<Job>) {
        val jobIdsToHide = mutableSetOf<String>()

        // Fetch the jobs the user has applied for from the "applications" collection
        db.collection("tbl_applications")
            .whereEqualTo("user_id", userId) // Query where userId matches the signed-in user's ID
            .get()
            .addOnSuccessListener { applicationsSnapshot ->
                if (!applicationsSnapshot.isEmpty) {
                    applicationsSnapshot.documents.forEach { applicationSnapshot ->
                        val application = applicationSnapshot.toObject(Application::class.java)
                        if (application != null) {
                            jobIdsToHide.add(application.job_id) // Add jobId to hide if user has applied
                        }
                    }
                }

                // Filter the jobs to exclude those the user has applied for
                jobList.clear()
                jobList.addAll(tempJobList.filter { it.job_id !in jobIdsToHide })

                // Sort by postedOn date in descending order
                jobList.sortByDescending { it.job_id }
                jobAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("FirebaseError", "Failed to load applied jobs: ${it.message}")
            }
    }

    private fun searchJobs(job_title: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            searchJobsNotSignedin(job_title)
            return
        }

        // If the jobTitle is empty, fetch and display all job posts
        if (job_title.isEmpty()) {
            fetchApplicantJobs() // If no search term, fetch all jobs
        } else {
            // Firestore query to fetch jobs based on jobTitle
            db.collection("tbl_job_listings")
                .get()
                .addOnSuccessListener { snapshot ->
                    val tempJobList = mutableListOf<Job>()
                    jobList.clear() // Clear the current job list

                    // Iterate over the documents in the snapshot
                    for (jobSnapshot in snapshot) {
                        val job = jobSnapshot.toObject(Job::class.java)

                        // Check if jobTitle matches part of the job's jobTitle field
                        if (job?.job_title?.lowercase()?.contains(job_title.lowercase()) == true) {
                            tempJobList.add(job) // Add matching jobs to a temporary list
                        }
                    }

                    // If there are matching jobs, filter them based on the user's applied and saved jobs
                    if (tempJobList.isNotEmpty()) {
                        // Filter the jobs to exclude those the user has applied for or saved
                        filterJobsForUser(userId, tempJobList)
                    } else {
                        tempJobList.clear()
                        filterJobsForUser(userId, tempJobList)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseError", "Error searching jobs: ${exception.message}")
                    Toast.makeText(context, "Failed to search jobs", Toast.LENGTH_SHORT).show()
                }
        }
    }



    private fun searchJobsNotSignedin(job_title: String) {
        // If the jobTitle is empty, fetch and display all job posts
        if (job_title.isEmpty()) {
            // Fetch all job posts when search is empty
            db.collection("tbl_job_listings")
                .get()
                .addOnSuccessListener { snapshot ->
                    jobList.clear() // Clear the current job list

                    // Iterate over the documents and add them to the list
                    for (jobSnapshot in snapshot) {
                        val job = jobSnapshot.toObject(Job::class.java)
                        job?.let { jobList.add(it) }
                    }

                    // Sort by postedOn date in descending order
                    jobList.sortByDescending { it.job_id }
                    jobAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseError", "Error fetching all jobs: ${exception.message}")
                    Toast.makeText(context, "Failed to load all jobs", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Firestore query with client-side filtering if a jobTitle is provided
            db.collection("tbl_job_listings")
                .get()
                .addOnSuccessListener { snapshot ->
                    jobList.clear() // Clear the current job list

                    // Iterate over the documents in the snapshot
                    for (jobSnapshot in snapshot) {
                        val job = jobSnapshot.toObject(Job::class.java)

                        // Check if jobTitle matches part of the job's jobTitle field
                        if (job?.job_title?.lowercase()?.contains(job_title.lowercase()) == true) {
                            jobList.add(job)  // Add matching jobs to the jobList
                        }
                    }

                    // If there are matching jobs, sort and update UI
                    if (jobList.isNotEmpty()) {
                        jobList.sortByDescending { it.job_id }
                        jobAdapter.notifyDataSetChanged()
                    } else {
                        // If no results found
                        Toast.makeText(context, "No jobs found for your search", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseError", "Error searching jobs: ${exception.message}")
                    Toast.makeText(context, "Failed to search jobs", Toast.LENGTH_SHORT).show()
                }
        }
    }
    companion object {
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
