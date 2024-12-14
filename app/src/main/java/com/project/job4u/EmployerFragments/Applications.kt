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
import com.project.job4u.Adapter.CompanyApplicationsAdapter
import com.project.job4u.ApplicantDetailsActivity
import com.project.job4u.Application
import com.project.job4u.JobDetailsActivity
import com.project.job4u.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Applications.newInstance] factory method to
 * create an instance of this fragment.
 */
class Applications : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CompanyApplicationsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var companyId: String
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
        val view =  inflater.inflate(R.layout.fragment_applications2, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCompanyApplications)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize adapter
        adapter = CompanyApplicationsAdapter(applicationList,
            onApplicantClick = {
                application ->
                // Navigate to the JobDetailsActivity when job item is clicked
                val intent = Intent(requireContext(), ApplicantDetailsActivity::class.java)
                intent.putExtra("applicantDetails", application) // Pass applicant data to the new activity

                startActivity(intent)
            })
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().reference
        companyId = FirebaseAuth.getInstance().currentUser?.uid ?: return null

        fetchApplications()
        return view
    }
    private fun fetchApplications() {
        val applicationsRef = database.child("applications")

        applicationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                applicationList.clear()
                if (snapshot.exists()) {
                    for (applicantSnapshot in snapshot.children) {
                        for (jobSnapshot in applicantSnapshot.children) {
                            val application = jobSnapshot.getValue(Application::class.java)
                            if (application?.postedBy == companyId) {
                                applicationList.add(application)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load applications", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment Applications.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Applications().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}