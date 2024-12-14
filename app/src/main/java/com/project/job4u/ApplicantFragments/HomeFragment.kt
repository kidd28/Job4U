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
    private lateinit var jobSearchView:SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        jobSearchView = view.findViewById(R.id.job_search_view)

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
                intent.putExtra("jobDetails", job)
                startActivity(intent)
            })
        recyclerView.adapter = jobAdapter

        // Fetch the jobs from Firebase
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
        database.orderByChild("status").equalTo("active")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempJobList = mutableListOf<Job>()

                    for (jobSnapshot in snapshot.children) {
                        val job = jobSnapshot.getValue(Job::class.java)
                        if (job != null) {
                            tempJobList.add(job)
                        }
                    }
                    if (userId == null) {
                        // No user is signed in, show all jobs
                        jobList.clear()
                        jobList.addAll(tempJobList)

                        // Sort by postedOn date in descending order
                        jobList.sortByDescending { it.postedOn }
                        jobAdapter.notifyDataSetChanged()
                    } else {
                        // User is signed in, filter jobs
                        filterJobsForUser(userId, tempJobList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load jobs", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterJobsForUser(userId: String, tempJobList: List<Job>) {
        val jobAppliedRef = FirebaseDatabase.getInstance()
            .getReference("applications")
            .child(userId)

        val savedJobsRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("savedJobs")

        val jobIdsToHide = mutableSetOf<String>()

        // Fetch the jobs the user has applied for
        jobAppliedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(appliedSnapshot: DataSnapshot) {
                if (appliedSnapshot.exists()) {
                    for (appliedJobSnapshot in appliedSnapshot.children) {
                        appliedJobSnapshot.key?.let { jobIdsToHide.add(it) }
                    }
                }

                // Fetch the jobs the user has saved
                savedJobsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(savedSnapshot: DataSnapshot) {
                        if (savedSnapshot.exists()) {
                            for (savedJobSnapshot in savedSnapshot.children) {
                                savedJobSnapshot.key?.let { jobIdsToHide.add(it) }
                            }
                        }

                        // Filter jobs to exclude those the user has applied for or saved
                        jobList.clear()
                        jobList.addAll(tempJobList.filter { it.jobId !in jobIdsToHide })

                        // Sort by postedOn date in descending order
                        jobList.sortByDescending { it.postedOn }
                        jobAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Failed to load saved jobs: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to load applied jobs: ${error.message}")
            }
        })
    }

    private fun searchJobs(jobTitle: String) {
        val jobRef = FirebaseDatabase.getInstance().getReference("jobPosts")
        jobRef.orderByChild("jobTitle")
            .startAt(jobTitle.lowercase())
            .endAt(jobTitle.lowercase() + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempJobList = mutableListOf<Job>()
                    for (jobSnapshot in snapshot.children) {
                        val job = jobSnapshot.getValue(Job::class.java)
                        if (job != null) {
                            tempJobList.add(job)
                        }
                    }
                    // Update job list with search results
                    jobList.clear()
                    jobList.addAll(tempJobList)

                    // Sort by postedOn date in descending order
                    jobList.sortByDescending { it.postedOn }
                    jobAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error searching jobs: ${error.message}")
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