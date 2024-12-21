package com.project.job4u.ApplicantFragments

import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.firestore.QuerySnapshot
import com.project.job4u.Adapter.SavedAdapter
import com.project.job4u.ApplicantJobDetails
import com.project.job4u.Application
import com.project.job4u.Authentication.SignInActivity
import com.project.job4u.R

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SavedFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedAdapter
    private val savedJobsList = mutableListOf<Application>()
    private lateinit var not_signed_in: LinearLayout
    private lateinit var sign_in_button: MaterialButton
    private var param1: String? = null
    private var param2: String? = null
    private val firestore = FirebaseFirestore.getInstance()

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
        val view = inflater.inflate(R.layout.fragment_saved, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewApplicantJobs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        not_signed_in = view.findViewById(R.id.not_signed_in)
        sign_in_button = view.findViewById(R.id.sign_in_button)

        adapter = SavedAdapter(savedJobsList) { savedJob ->
            // Navigate to job details activity
            val intent = Intent(requireContext(), ApplicantJobDetails::class.java)
            intent.putExtra("applicationDetails", savedJob)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        fetchSavedJobs()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            recyclerView.visibility = View.GONE
            not_signed_in.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            not_signed_in.visibility = View.GONE
        }

        sign_in_button.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    private fun fetchSavedJobs() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val savedJobsRef = firestore.collection("savedJobs").document(userId.toString()).collection("jobs")

        if (userId == null) {
            recyclerView.visibility = View.GONE
            not_signed_in.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            not_signed_in.visibility = View.GONE
        }

        // Fetch all the saved jobs under "jobs" subcollection
        savedJobsRef.get()
            .addOnSuccessListener { result: QuerySnapshot ->
                savedJobsList.clear()
                if (!result.isEmpty) {
                    for (document in result.documents) {
                        val savedJob = document.toObject(Application::class.java)  // Assuming Job is the class for the job details
                        savedJob?.let {
                            savedJobsList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    // If no saved jobs, notify the adapter with empty list
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to load saved jobs: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SavedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
