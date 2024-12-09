package com.project.job4u

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var employersRef: DatabaseReference
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

        // Set up the toolbar
        toolbar= findViewById(R.id.toolbar)
        toolbarButton = findViewById(R.id.sign_in_button)
        setSupportActionBar(toolbar)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.reference.child("users")
        employersRef = database.reference.child("companies")
        bottomNav = findViewById(R.id.bottom_nav)
        // Check if the user is signed in
        val currentUser = auth.currentUser

        // Get the current signed-in user's ID
        currentUserId = auth.currentUser?.uid ?: ""

        if (currentUser != null) {
            checkUserType(currentUser)
            // User is signed in, fetch their name from Firebase
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
        // Check if the user exists in the "employers" node (company)
        employersRef.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // The user is an employer, show employer fragments and bottom nav
                    showEmployerDashboard()
                    if (currentUser != null) {
                        fetchCompanyName(currentUser.uid)
                    }
                } else {
                    if (currentUser != null) {
                        fetchUserName(currentUser.uid)
                    }
                    // Check if the user exists in the "users" node (applicant)
                    usersRef.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // The user is an applicant, show applicant fragments and bottom nav
                                showApplicantDashboard()
                            } else {
                                // If no data, show applicant dashboard by default
                                Toast.makeText(this@MainActivity, "User data not found. Redirecting to applicant dashboard.", Toast.LENGTH_SHORT).show()
                                showApplicantDashboard()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    private fun fetchUserName(userId: String) {
        // Reference to the user's data in Firebase Database
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fname = snapshot.child("firstname").getValue(String::class.java)
                val lname = snapshot.child("lastname").getValue(String::class.java)
                val name = fname+" "+lname
                if (name != null) {
                    toolbarButton.text = name
                    toolbarButton.setOnClickListener {
                        val i = Intent(this@MainActivity,Settings::class.java)
                        startActivity(i)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to fetch user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchCompanyName(companyId: String) {
        // Reference to the company's data in Firebase Database
        val companyRef = FirebaseDatabase.getInstance().getReference("companies").child(companyId)

        companyRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val companyName = snapshot.child("companyName").getValue(String::class.java)
                if (companyName != null) {
                    toolbarButton.text = companyName
                    toolbarButton.setOnClickListener {
                        val i = Intent(this@MainActivity,Settings::class.java)
                        startActivity(i)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to fetch company data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
                else ->JobPosted()
            }
            true
        }
    }

    private fun showApplicantDashboard() {
        // Show applicant fragments and bottom nav
        loadFragment(HomeFragment())
        bottomNav.menu.clear()
        bottomNav.inflateMenu(R.menu.bottom_nav_menu)
        bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
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