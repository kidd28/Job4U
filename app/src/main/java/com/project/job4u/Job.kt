package com.project.job4u

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Job(
    val job_title: String = "",
    val job_id: String = "",
    val company_name: String = "",
    val jobDescription: String = "",
    val city: String = "",
    val state: String = "",
    val salary: String = "",
    val jobType: String = "",
    val requirements: String = "",
    val postedOn: String = "",
    val postedBy: String = "",
    val status: String = ""
) : Parcelable {
    // No-argument constructor for Firebase
    constructor() : this("","","","", "", "", "", "","", "", "", "")
}