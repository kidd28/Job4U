package com.project.job4u.ApplicantFragments

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
import com.project.job4u.Adapter.MyApplicationsAdapter
import com.project.job4u.Adapter.SavedAdapter
import com.project.job4u.ApplicantJobDetails
import com.project.job4u.Application
import com.project.job4u.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SavedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SavedFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedAdapter
    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private val savedJobsList = mutableListOf<Application>()

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
        val view = inflater.inflate(R.layout.fragment_saved, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewApplicantJobs)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the SavedAdapter with savedJobsList and job item click handler
        adapter = SavedAdapter(savedJobsList) { savedJob ->
            // Navigate to job details activity
            val intent = Intent(requireContext(), ApplicantJobDetails::class.java)
            intent.putExtra("applicationDetails", savedJob)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        database = FirebaseDatabase.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null

        fetchSavedJobs()

        return view
    }

    private fun fetchSavedJobs() {
        val savedJobsRef = database.child("savedJobs").child(userId)

        savedJobsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                savedJobsList.clear()
                if (snapshot.exists()) {
                    for (savedJobSnapshot in snapshot.children) {
                        val savedJob = savedJobSnapshot.getValue(Application::class.java)
                        savedJob?.let {
                            savedJobsList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load saved jobs", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment SavedFragment.
         */
        // TODO: Rename and change types and number of parameters
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