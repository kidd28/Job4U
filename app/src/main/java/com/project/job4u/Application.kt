package com.project.job4u

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Application(

    val job_id: String = "",  // Make sure jobId is defined here
    val job_title: String = "",
    val company_name: String = "",
    val location: String = "",
    val description: String = "",
    val salary: String = "",
    val requirements: String = "",
    val postedOn: String = "",
    val application_status: String = "",
    val applied_on: String = "",
    val user_id: String = "",
    val postedBy: String = "",
    val applicantName : String = "",
    val applicantEmail : String = "",
    val applicantResume : String = "",
    val applicantPhone : String = "",

) : Parcelable{
    // No-argument constructor for Firebase
    constructor() : this("","","","","","", "", "", "", "","", "", "", "","","")
}