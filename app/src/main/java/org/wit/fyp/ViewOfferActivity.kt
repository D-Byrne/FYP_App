package org.wit.fyp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_view_offer.*
import kotlinx.android.synthetic.main.activity_view_request.*
import kotlinx.android.synthetic.main.activity_view_request.view.*
import org.wit.fyp.models.OfferModel
import org.wit.fyp.models.RequestModel

class ViewOfferActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    var userEmail: String = ""
    var userName: String = ""
    var offerAccept: Boolean = false

    var request = RequestModel()
    var offer = OfferModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_offer)

        database = Firebase.database.reference

        setFromIntent()

        btn_accept_offer.setOnClickListener{

            if(offerAccept == false) {
                view_offer_author_email.isVisible = true
                btn_accept_offer.isVisible = false
                btn_cancel_offer.isVisible = true

                acceptOffer()
            }else{
                Toast.makeText(this, "Unable to accept multiple offers.", Toast.LENGTH_SHORT).show()
            }
        }

        btn_cancel_offer.setOnClickListener{

            if(offerAccept == true) {
                btn_cancel_offer.isVisible = false
                btn_accept_offer.isVisible = true
                view_offer_author_email.isVisible = false

                cancelOffer()

                val intent = Intent(this, RequestListActivity::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, "Can't " + offerAccept, Toast.LENGTH_SHORT).show()
            }

        }
    }



    private fun acceptOffer(){
        database.child("requests/${request.reqId}/offers/${offer.offerId}/offerAccepted").setValue(true)
        offerAccept = true
    }

    private fun cancelOffer(){
        database.child("requests/${request.reqId}/offers/${offer.offerId}/offerAccepted").setValue(false)
        offerAccept = false
    }

    fun setFromIntent(){
        request = intent.extras?.getParcelable<RequestModel>("request")!!
        offer = intent.extras?.getParcelable<OfferModel>("view_offer")!!
        userEmail = intent.extras?.getString("user_email")!!

        offerAccept = intent.extras?.getBoolean("offer_accepted")!!

        Toast.makeText(this, "Value: " + offerAccept, Toast.LENGTH_SHORT).show()

        if(offerAccept){
            view_offer_author_email.isVisible = true
            btn_accept_offer.isVisible = false
            btn_cancel_offer.isVisible = true
        } else{
            view_offer_author_email.isVisible = false
            btn_accept_offer.isVisible = true
            btn_cancel_offer.isVisible = false
        }

        view_offer_name.setText(offer.authorName)
        view_offer_amount.setText(offer.amount)
        view_offer_author_email.setText(userEmail)


    }
}