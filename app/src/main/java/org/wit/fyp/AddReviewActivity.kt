package org.wit.fyp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_review.*
import kotlinx.android.synthetic.main.activity_view_offer.*
import org.wit.fyp.models.OfferModel

class AddReviewActivity : AppCompatActivity() {

    var currentOffer = OfferModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        toolbar_review.title = title
        setSupportActionBar(toolbar_review)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setFromIntent()
    }

    fun setFromIntent(){
        currentOffer = intent.extras?.getParcelable<OfferModel>("accepted_offer")!!

        toolbar_review.title = "Rate ${currentOffer.authorName}"
        Toast.makeText(this, currentOffer.authorName, Toast.LENGTH_SHORT).show()
    }
}