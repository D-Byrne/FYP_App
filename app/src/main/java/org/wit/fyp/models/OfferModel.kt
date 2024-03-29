package org.wit.fyp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class OfferModel(var authorId: String ="", var authorName: String = "", var amount: String = "", var offerId: String? = null, var offerAccepted: Boolean? = null, var authorEmail: String? = null, var requestCompleted: Boolean? = null) : Parcelable