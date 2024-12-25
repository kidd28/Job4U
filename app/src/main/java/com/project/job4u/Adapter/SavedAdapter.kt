package com.project.job4u.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job4u.Application
import com.project.job4u.R

class SavedAdapter(
    private val savedJobList: List<Application>, // List of saved jobs
    private val onJobClick: (Application) -> Unit // Callback for job item click
) : RecyclerView.Adapter<SavedAdapter.SavedJobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedJobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return SavedJobViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedJobViewHolder, position: Int) {
        val savedJob = savedJobList[position]
        holder.bind(savedJob)

        holder.itemView.setOnClickListener {
            onJobClick(savedJob)
        }
    }

    override fun getItemCount(): Int {
        return savedJobList.size
    }

    class SavedJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvPostedOn: TextView = itemView.findViewById(R.id.tvPostedOn)
        private val tvApplicationStatus: TextView = itemView.findViewById(R.id.status)

        fun bind(savedJob: Application) {
            tvJobTitle.text = savedJob.job_title
            tvCompanyName.text = savedJob.company_name
            tvLocation.text = savedJob.location
            tvPostedOn.text = savedJob.postedOn
            tvApplicationStatus.text = savedJob.application_status // Show saved status
        }
    }
}
