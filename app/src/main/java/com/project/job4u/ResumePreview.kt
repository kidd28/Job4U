package com.project.job4u

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ResumePreview : AppCompatActivity() {
    private lateinit var pdfImageView: ImageView
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_resume_preview)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pdfImageView = findViewById(R.id.pdfImageView)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val resumeId = intent.getStringExtra("resumeId")

        if (userId != null && resumeId != null) {
            fetchResumeUrl(userId, resumeId)
        } else {
            Toast.makeText(this, "No resume data available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchResumeUrl(userId: String, resumeId: String) {
        // Fetch the resume document from Firestore
        val resumeRef = firestore.collection("users").document(userId).collection("resumes").document(resumeId)

        resumeRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val pdfUrl = document.getString("resumeUrl")
                    if (pdfUrl != null) {
                        downloadAndRenderPdf(pdfUrl)
                    } else {
                        Toast.makeText(this, "PDF URL not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Resume not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch resume: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadAndRenderPdf(pdfUrl: String) {
        val client = OkHttpClient()

        // Create a request to fetch the PDF file
        val request = Request.Builder()
            .url(pdfUrl)
            .build()

        // Download the file in a background thread
        Thread {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Failed to download file: ${response.message}")

                // Create a temporary file to store the PDF
                val tempFile = File.createTempFile("tempPdf", ".pdf", cacheDir)
                val outputStream = FileOutputStream(tempFile)
                outputStream.write(response.body?.bytes())
                outputStream.close()

                // Render the PDF's first page
                runOnUiThread { renderPdf(tempFile) }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to load PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun renderPdf(file: File) {
        try {
            val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)

            if (pdfRenderer.pageCount > 0) {
                val page = pdfRenderer.openPage(0) // Open the first page

                // Get screen dimensions to calculate scaling
                val displayMetrics = resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels

                // Calculate aspect ratio of the PDF page
                val aspectRatio = page.width.toFloat() / page.height.toFloat()
                val bitmapWidth: Int
                val bitmapHeight: Int

                if (screenWidth / aspectRatio <= screenHeight) {
                    // Scale based on width
                    bitmapWidth = screenWidth
                    bitmapHeight = (screenWidth / aspectRatio).toInt()
                } else {
                    // Scale based on height
                    bitmapHeight = screenHeight
                    bitmapWidth = (screenHeight * aspectRatio).toInt()
                }

                // Create a scaled bitmap
                val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Set the bitmap to the ImageView
                pdfImageView.setImageBitmap(bitmap)

                page.close()
            }

            pdfRenderer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to render PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
