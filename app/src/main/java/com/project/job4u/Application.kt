package com.project.job4u

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Application(

    val jobId: String = "",  // Make sure jobId is defined here
    val jobTitle: String = "",
    val companyName: String = "",
    val location: String = "",
    val description: String = "",
    val salary: String = "",
    val requirements: String = "",
    val postedOn: String = "",
    val applicationStatus: String = "",
    val date: String = "",
    val userId: String = ""

) : Parcelable{
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "","", "", "", "","","")
}