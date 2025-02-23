package com.project.job4u.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job4u.Application
import com.project.job4u.Job
import com.project.job4u.R

class CompanyApplicationsAdapter(
    private val applicationList: List<Application>,
    private val onApplicantClick: (Application) -> Unit
) : RecyclerView.Adapter<CompanyApplicationsAdapter.ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_application, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = applicationList[position]
        holder.bind(application)

        holder.itemView.setOnClickListener {
            onApplicantClick(application)
        }
    }

    override fun getItemCount(): Int {
        return applicationList.size
    }

    class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvApplicantName: TextView = itemView.findViewById(R.id.tvApplicantName)
        private val tvApplicantEmail: TextView = itemView.findViewById(R.id.tvApplicantEmail)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(application: Application) {
            tvJobTitle.text = application.job_title
            tvApplicantName.text = application.applicantName
            tvApplicantEmail.text = application.applicantEmail
            tvStatus.text = application.application_status
        }
    }
}