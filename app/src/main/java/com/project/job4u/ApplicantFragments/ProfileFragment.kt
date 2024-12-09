package com.project.job4u.ApplicantFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.job4u.Authentication.EmployerSignUp
import com.project.job4u.Authentication.SignInActivity
import com.project.job4u.R
import de.hdodenhof.circleimageview.CircleImageView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private lateinit var profileImage: CircleImageView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var dobTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var resumeTextView: TextView
    private lateinit var signed_in: CardView
    private lateinit var not_signedin: LinearLayout
    private lateinit var sign_in_button: MaterialButton



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
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImage = view.findViewById(R.id.profile_image)
        nameTextView = view.findViewById(R.id.profile_name)
        emailTextView = view.findViewById(R.id.profile_email)
        genderTextView = view.findViewById(R.id.profile_gender)
        phoneTextView = view.findViewById(R.id.profile_phone)
        dobTextView = view.findViewById(R.id.profile_dob)
        addressTextView = view.findViewById(R.id.profile_address)
        resumeTextView = view.findViewById(R.id.profile_resume)


        signed_in = view.findViewById(R.id.signed_in)
        not_signedin = view.findViewById(R.id.not_signedin)
        sign_in_button = view.findViewById(R.id.sign_in_button)

        sign_in_button.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent) }
        loadUserProfile()
        return view
    }
    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT).show()
            signed_in.visibility = View.GONE
            not_signedin.visibility = View.VISIBLE
            return
        }else{
            signed_in.visibility = View.VISIBLE
            not_signedin.visibility = View.GONE
        }
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firstName = snapshot.child("firstname").getValue(String::class.java)
                val lastName = snapshot.child("lastname").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val gender = snapshot.child("gender").getValue(String::class.java)
                val phone = snapshot.child("phone").getValue(String::class.java)
                val dob = snapshot.child("dob").getValue(String::class.java)
                val street = snapshot.child("street").getValue(String::class.java)
                val city = snapshot.child("city").getValue(String::class.java)
                val state = snapshot.child("state").getValue(String::class.java)
                val country = snapshot.child("country").getValue(String::class.java)
                val resume = snapshot.child("fileName").getValue(String::class.java)
                val profileImageUrl = snapshot.child("profileImage").getValue(String::class.java)

                // Populate the fields
                nameTextView.text = "$firstName $lastName"
                emailTextView.text = email
                genderTextView.text = "Gender: "+" "+ gender
                phoneTextView.text = "Phone: "+" "+phone
                dobTextView.text = "Date of Birth: "+" "+dob
                addressTextView.text = "Address: "+" "+"$street, $city"+" "+"$state, $country"
                resumeTextView.text = "Resume Uploaded: "+" " + resume ?: "No resume uploaded"

                // Load profile image using Glide
                Glide.with(this@ProfileFragment)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(profileImage)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}