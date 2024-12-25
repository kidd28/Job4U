package com.project.job4u.ApplicantFragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.job4u.Authentication.SignInActivity
import com.project.job4u.MainActivity
import com.project.job4u.R
import com.project.job4u.ResumePreview
import de.hdodenhof.circleimageview.CircleImageView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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
    private lateinit var resume: MaterialButton
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 71

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
        resume = view.findViewById(R.id.resume)

        signed_in = view.findViewById(R.id.signed_in)
        not_signedin = view.findViewById(R.id.not_signedin)
        sign_in_button = view.findViewById(R.id.sign_in_button)

        sign_in_button.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
        }

        profileImage.setOnClickListener {
            openImagePicker()
        }
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        resume.setOnClickListener {
            val i= Intent(requireContext(), ResumePreview::class.java)
                i.putExtra("resume", "")
                i.putExtra("applicantuserId",userId)

            startActivity(i)
        }
        loadUserProfile()
        return view
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            signed_in.visibility = View.GONE
            not_signedin.visibility = View.VISIBLE
            return
        } else {
            signed_in.visibility = View.VISIBLE
            not_signedin.visibility = View.GONE
        }

        val userRef = FirebaseFirestore.getInstance().collection("tbl_users").document(userId)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstname")
                    val lastName = document.getString("lastname")
                    val email = document.getString("email")
                    val gender = document.getString("gender")
                    val phone = document.getString("phone")
                    val dob = document.getString("dob")
                    val street = document.getString("street")
                    val city = document.getString("city")
                    val state = document.getString("state")
                    val country = document.getString("country")
                    val resume = document.getString("fileName")
                    val profileImageUrl = document.getString("profileImage")

                    // Populate the fields
                    nameTextView.text = "$firstName $lastName"
                    emailTextView.text = email
                    genderTextView.text = "Gender: $gender"
                    phoneTextView.text = "Phone: $phone"
                    dobTextView.text = "Date of Birth: $dob"
                    addressTextView.text = "Address: $street, $city, $state, $country"
                    resumeTextView.text = "Resume Uploaded: ${resume ?: "No resume uploaded"}"

                    // Load profile image using Glide
                    Glide.with(requireContext())
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(profileImage)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to load profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Open image picker to choose a picture
    private fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    // Handle the result of image picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this@ProfileFragment)
                .load(imageUri)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(profileImage) // Preview the selected image
            uploadImageToFirebase()
        }
    }

    // Upload image to Firebase Storage
    private fun uploadImageToFirebase() {
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            val profileImageRef = storageRef.child("users/$currentUserId/profile.jpg")

            profileImageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    // Get the download URL and save it to Firestore
                    profileImageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveImageLinkToFirestore(downloadUrl.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Save image URL in Firestore
    private fun saveImageLinkToFirestore(imageUrl: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId)

        userRef.update("profileImage", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to save image link: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
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
