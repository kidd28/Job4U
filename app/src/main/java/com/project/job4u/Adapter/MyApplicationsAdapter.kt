package com.project.job4u.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job4u.Application
import com.project.job4u.Job
import com.project.job4u.R

class MyApplicationsAdapter(
    private val applicationList: List<Application>,
    private val onJobClick: (Application) -> Unit // Callback for job item click
) : RecyclerView.Adapter<MyApplicationsAdapter.ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = applicationList[position]
        holder.bind(application)

        holder.itemView.setOnClickListener {
            onJobClick(application)
        }
    }

    override fun getItemCount(): Int {
        return applicationList.size
    }

    class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvPostedOn: TextView = itemView.findViewById(R.id.tvPostedOn)
        private val tvApplicationStatus: TextView = itemView.findViewById(R.id.status)

        fun bind(application: Application) {
            tvJobTitle.text = application.job_title
            tvCompanyName.text = application.company_name
            tvLocation.text = application.location
            tvPostedOn.text = application.postedOn
            tvApplicationStatus.text = application.application_status // Show application status
        }
    }
}
