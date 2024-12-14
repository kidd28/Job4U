package com.project.job4u

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Job(
    val jobTitle: String = "",
    val jobId: String = "",
    val companyName: String = "",
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