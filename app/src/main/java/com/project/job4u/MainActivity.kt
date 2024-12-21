package com.project.job4u

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.project.job4u.Authentication.SignInActivity
import com.project.job4u.ApplicantFragments.ApplicationsFragment
import com.project.job4u.ApplicantFragments.HomeFragment
import com.project.job4u.ApplicantFragments.SavedFragment
import com.project.job4u.ApplicantFragments.ProfileFragment
import com.project.job4u.EmployerFragments.Applications
import com.project.job4u.EmployerFragments.CompanyProfile
import com.project.job4u.EmployerFragments.JobPosted
import com.project.job4u.EmployerFragments.PostJob

class MainActivity : AppCompatActivity() {

    private lateinit var toolbarButton: MaterialButton
    private lateinit var toolbar: Toolbar

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersRef: CollectionReference
    private lateinit var employersRef: CollectionReference
    private lateinit var currentUserId: String
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Disable night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


        // Set up the toolbar
        toolbar = findViewById(R.id.toolbar)
        toolbarButton = findViewById(R.id.sign_in_button)
        setSupportActionBar(toolbar)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        usersRef = firestore.collection("users")
        employersRef = firestore.collection("companies")
        bottomNav = findViewById(R.id.bottom_nav)

        // Check if the user is signed in
        val currentUser = auth.currentUser

        // Get the current signed-in user's ID
        currentUserId = auth.currentUser?.uid ?: ""

        if (currentUser != null) {
            checkUserType(currentUser)
            // User is signed in, fetch their name from Firestore
            toolbarButton.setOnClickListener {
                // Start SignInActivity when the button is clicked
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
        } else {
            // User is not signed in, show "Sign In"
            showApplicantDashboard()
            loadFragment(HomeFragment())
            toolbarButton.text = "Sign In"
            toolbarButton.setOnClickListener {
                // Start SignInActivity when the button is clicked
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun checkUserType(currentUser: FirebaseUser?) {
        // Check if the user exists in the "employers" collection (company)
        employersRef.document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // The user is an employer, show employer fragments and bottom nav
                    showEmployerDashboard()
                    fetchCompanyName(currentUserId)
                    if (currentUser != null) {
                        fetchCompanyName(currentUser.uid)
                    }
                } else {
                    // If not an employer, check if the user exists in the "users" collection (applicant)
                    if (currentUser != null) {
                        fetchUserName(currentUser.uid)
                    }

                    usersRef.document(currentUserId).get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                // The user is an applicant, show applicant fragments and bottom nav
                                showApplicantDashboard()
                            } else {
                                // If no data, show applicant dashboard by default
                                Toast.makeText(this@MainActivity, "User data not found. Redirecting to applicant dashboard.", Toast.LENGTH_SHORT).show()
                                showApplicantDashboard()
                            }
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun fetchUserName(userId: String) {
        // Reference to the user's data in Firestore
        val userRef = firestore.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val fname = document.getString("firstname")
                val lname = document.getString("lastname")
                val name = "$fname $lname"
                toolbarButton.text = name
                toolbarButton.setOnClickListener {
                    val i = Intent(this@MainActivity, Settings::class.java)
                    startActivity(i)
                }
            }
        }
            .addOnFailureListener { error ->
                Toast.makeText(this@MainActivity, "Failed to fetch user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchCompanyName(companyId: String) {
        // Reference to the company's data in Firestore
        val companyRef = firestore.collection("companies").document(companyId)

        companyRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val companyName = document.getString("companyName")
                toolbarButton.text = companyName
                toolbarButton.setOnClickListener {
                    val i = Intent(this@MainActivity, Settings::class.java)
                    startActivity(i)
                }
            }
        }
            .addOnFailureListener { error ->
                Toast.makeText(this@MainActivity, "Failed to fetch company data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEmployerDashboard() {
        loadFragment(JobPosted())
        bottomNav.menu.clear()
        bottomNav.inflateMenu(R.menu.bottom_nav_menu_employer)  // Inflate employer menu
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_job_posted -> loadFragment(JobPosted())
                R.id.nav_post_job -> loadFragment(PostJob())
                R.id.nav_applications -> loadFragment(Applications())
                R.id.nav_profile -> loadFragment(CompanyProfile())
                else -> JobPosted()
            }
            true
        }
    }

    private fun showApplicantDashboard() {
        // Show applicant fragments and bottom nav
        loadFragment(HomeFragment())
        bottomNav.menu.clear()
        bottomNav.inflateMenu(R.menu.bottom_nav_menu)
        bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            val selectedFragment = when (menuItem.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_applications -> ApplicationsFragment()
                R.id.nav_saved -> SavedFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            loadFragment(selectedFragment)
            true
        }
    }
}
