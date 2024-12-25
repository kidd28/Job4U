package com.project.job4u.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job4u.Job
import com.project.job4u.R

class ApplicantJobAdapter(private val jobList: List<Job>,
                          private val onSaveJobClick: (Job) -> Unit,
                          private val onJobClick: (Job) -> Unit // Callback for job item click
) : RecyclerView.Adapter<ApplicantJobAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]
        holder.bind(job)

        holder.itemView.setOnClickListener {
            onJobClick(job)
        }

    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvPostedOn: TextView = itemView.findViewById(R.id.tvPostedOn)
        private val status: TextView = itemView.findViewById(R.id.status)

        fun bind(job: Job) {
            tvJobTitle.text = job.job_title
            tvCompanyName.text = job.company_name
            tvLocation.text = job.city+", "+ job.state
            tvPostedOn.text = job.postedOn
            status.visibility = View.GONE
        }
    }
}