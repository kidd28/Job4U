package com.project.job4u.EmployerFragments

import android.content.Intent
import android.os.Bundle
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
import com.project.job4u.JobDetailsActivity
import com.project.job4u.Adapter.JobAdapter
import com.project.job4u.R
import com.project.job4u.Job
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [JobPosted.newInstance] factory method to
 * create an instance of this fragment.
 */
class JobPosted : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var jobAdapter: JobAdapter
    private lateinit var jobList: MutableList<Job>
    private lateinit var jobSearchView: SearchView

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
        val view = inflater.inflate(R.layout.fragment_job_posted, container, false)

        // Initialize the SearchView
        jobSearchView = view.findViewById(R.id.job_search_view)
        jobSearchView.setQuery("", false)

        // Initialize RecyclerView and adapter
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewJobs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        jobList = mutableListOf() // List to hold job posts
        jobAdapter = JobAdapter(jobList,
            onSaveJobClick = { job ->
                // Handle saving the job to favorites (you can implement this logic)
            },
            onJobClick = { job ->
                // Navigate to JobDetailsActivity when job item is clicked
                val intent = Intent(requireContext(), JobDetailsActivity::class.java)
                intent.putExtra("jobDetails", job) // Pass job data
                startActivity(intent)
            })
        recyclerView.adapter = jobAdapter

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Fetch the jobs posted by the company
        fetchCompanyJobs()

        // Add listener to the SearchView for real-time search functionality
        jobSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { fetchCompanyJobs(it) } // Submit the search query and filter jobs
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { fetchCompanyJobs(it) } // Update the list as the user types
                return true
            }
        })

        return view
    }

    private fun fetchCompanyJobs(searchQuery: String = "") {
        val companyId = auth.currentUser?.uid ?: return  // Ensure the user is logged in

        // Query Firestore to fetch jobs posted by the current company (where postedBy == companyId)
        db.collection("tbl_job_listings")
            .whereEqualTo("postedBy", companyId)
            .get()
            .addOnSuccessListener { snapshot ->
                jobList.clear()  // Clear existing job list

                // Loop through each job post in Firestore and add it to the list
                for (jobSnapshot in snapshot) {
                    val job = jobSnapshot.toObject(Job::class.java)
                    job?.let {
                        jobList.add(it)
                    }
                }

                // Filter job list based on the search query (case-insensitive)
                if (searchQuery.isNotEmpty()) {
                    val filteredJobList = jobList.filter { job ->
                        job.job_title.lowercase().contains(searchQuery.lowercase())  // Case-insensitive comparison
                    }
                    jobList.clear()
                    jobList.addAll(filteredJobList)
                }

                // Sort the job list by jobId or postedOn in descending order
                jobList.sortByDescending { it.job_id }

                // Notify the adapter that data has changed
                jobAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to fetch jobs", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JobPosted().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
