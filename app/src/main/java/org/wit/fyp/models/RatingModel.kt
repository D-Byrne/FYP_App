package org.wit.fyp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RatingModel(var authorId: String = "", var authorName: String = "", var ratingDetails: String = "", var ratingNumber: Double = 0.0) : Parcelable