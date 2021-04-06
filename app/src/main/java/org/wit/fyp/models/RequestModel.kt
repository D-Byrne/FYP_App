package org.wit.fyp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RequestModel(var authorId: String = "", var authorName: String = "", var requestTitle: String = "", var requestDetails: String = "", var requestDeadline: String = "", var requestLocation:String = "", var reqId: String? = null, var requestCompleted: Boolean? = null, var completedBy: String? = null,  var offerAuthorRating: Boolean? = null) : Parcelable