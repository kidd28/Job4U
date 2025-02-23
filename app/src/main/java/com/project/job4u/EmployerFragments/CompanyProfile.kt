package com.project.job4u.EmployerFragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.job4u.MainActivity
import com.project.job4u.R
import de.hdodenhof.circleimageview.CircleImageView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CompanyProfile.newInstance] factory method to
 * create an instance of this fragment.
 */
class CompanyProfile : Fragment() {

    private lateinit var companyImage: CircleImageView
    private lateinit var companyNameTextView: TextView
    private lateinit var companyEmailTextView: TextView
    private lateinit var companyPhoneTextView: TextView
    private lateinit var companyWebsiteTextView: TextView
    private lateinit var companyAddressTextView: TextView
    private lateinit var businessTypeTextView: TextView
    private lateinit var companyDescriptionTextView: TextView
    private lateinit var companySizeTextView: TextView
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 71



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
        val view = inflater.inflate(R.layout.fragment_company_profile, container, false)

        companyImage = view.findViewById(R.id.company_image)
        companyNameTextView = view.findViewById(R.id.company_name_text_view)
        companyEmailTextView = view.findViewById(R.id.company_email_text_view)
        companyPhoneTextView = view.findViewById(R.id.company_phone_text_view)
        companyWebsiteTextView = view.findViewById(R.id.company_website_text_view)
        companyAddressTextView = view.findViewById(R.id.company_address_text_view)
        businessTypeTextView = view.findViewById(R.id.business_type_text_view)
        companyDescriptionTextView = view.findViewById(R.id.company_description_text_view)
        companySizeTextView = view.findViewById(R.id.company_size_text_view)

        loadCompanyProfile()
        companyImage.setOnClickListener{
            openImagePicker()
        }
        return view
    }
    private fun loadCompanyProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val companyRef = firestore.collection("companies").document(userId)

        companyRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val company_name = snapshot.getString("company_name")
                val companyEmail = snapshot.getString("companyEmail")
                val companyPhone = snapshot.getString("companyPhone")
                val companyWebsite = snapshot.getString("companyWebsite")
                val streetAddress = snapshot.getString("streetAddress")
                val city = snapshot.getString("city")
                val stateProvince = snapshot.getString("stateProvince")
                val companyAddress = "$streetAddress, $city, $stateProvince"
                val businessType = snapshot.getString("businessType")
                val companyDescription = snapshot.getString("companyDescription")
                val companySize = snapshot.getString("companySize")
                val companyImageUrl = snapshot.getString("companyImage")

                // Populate the fields
                companyNameTextView.text = company_name
                companyEmailTextView.text = companyEmail
                companyPhoneTextView.text = "Phone: $companyPhone"
                companyWebsiteTextView.text = "Website: $companyWebsite"
                companyAddressTextView.text = "Address: $companyAddress"
                businessTypeTextView.text = "Business Type: $businessType"
                companyDescriptionTextView.text = "Description: $companyDescription"
                companySizeTextView.text = "Size: $companySize"

                // Load company image using Glide
                Glide.with(this@CompanyProfile)
                    .load(companyImageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(companyImage)
            } else {
                Toast.makeText(context, "No company profile found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load company profile", Toast.LENGTH_SHORT).show()
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
            Glide.with(this@CompanyProfile)
                .load(imageUri)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(companyImage)// Preview the selected image
            uploadImageToFirebase()
        }
    }

    // Upload image to Firebase Storage
    private fun uploadImageToFirebase() {
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            val profileImageRef = storageRef.child("companies/$currentUserId/profile.jpg")

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
        val firestore = FirebaseFirestore.getInstance()
        val companyRef = firestore.collection("companies").document(currentUserId)

        companyRef.update("companyImage", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to save image link: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CompanyProfile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CompanyProfile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}