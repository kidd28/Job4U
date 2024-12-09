package com.project.job4u.EmployerFragments

import android.content.Intent
import android.os.Bundle
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
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var jobAdapter: JobAdapter
    private lateinit var jobList: MutableList<Job>

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
        val view = inflater.inflate(R.layout.fragment_job_posted, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewJobs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Initialize the job list and adapter
        jobList = mutableListOf() // Fetch this list from Firebase
        jobAdapter = JobAdapter(jobList,
            onSaveJobClick = { job ->
                // Handle saving the job to favorites

            },
            onJobClick = { job ->
                // Navigate to the JobDetailsActivity when job item is clicked
                val intent = Intent(requireContext(), JobDetailsActivity::class.java)
                intent.putExtra("jobDetails", job) // Pass job data to the new activity

                startActivity(intent)
            })
        recyclerView.adapter = jobAdapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("jobPosts")

        fetchCompanyJobs()

        return view
    }
    private fun fetchCompanyJobs() {
        val companyId = auth.currentUser?.uid ?: return  // Ensure the user is logged in

        // Query Firebase to fetch jobs posted by the current company (where postedBy == companyId)
        // and order them by postedOn (ascending)
        database.orderByChild("postedBy")
            .equalTo(companyId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    jobList.clear()  // Clear existing list

                    // Loop through each job post in Firebase and add it to the list
                    for (jobSnapshot in snapshot.children) {
                        val job = jobSnapshot.getValue(Job::class.java)
                        job?.let { jobList.add(it) }
                    }

                    // Sort the job list by postedOn date in ascending order
                    jobList.sortByDescending { it.jobId }

                    // Notify adapter that data has changed
                    jobAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch jobs: ${error.message}", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment JobPosted.
         */
        // TODO: Rename and change types and number of parameters
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