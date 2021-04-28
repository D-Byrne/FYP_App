package org.wit.fyp

import android.content.DialogInterface
import android.content.Intent
import android.net.InetAddresses
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_view_offer.*
import kotlinx.android.synthetic.main.activity_view_request.*
import kotlinx.android.synthetic.main.activity_view_request.view.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivityForResult
import org.wit.fyp.models.OfferModel
import org.wit.fyp.models.RequestModel

class ViewOfferActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    var userEmail: String = ""
    var userName: String = ""
    var offerAccept: Boolean = false

    var request = RequestModel()
    var offer = OfferModel()

    var requestAuthorRating = false
    var offerAuthorRating = true
    var ownRating = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_offer)

        database = Firebase.database.reference

        toolbar_view_offer.title = title
        setSupportActionBar(toolbar_view_offer)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setFromIntent()

        view_offer_author_email.setOnClickListener{
            composeEmail(arrayOf(view_offer_author_email.text.toString()))
        }

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

        btn_view_ratings.setOnClickListener{
            startActivityForResult(intentFor<ListRatingsActivity>().putExtra("offer", offer).putExtra("offerAuthorRating", offerAuthorRating).putExtra("requestAuthorRating", requestAuthorRating).putExtra("ownRating", ownRating), 0)
        }

        btn_cancel_offer.setOnClickListener{

            if(offerAccept == true) {
                btn_cancel_offer.isVisible = false
                btn_accept_offer.isVisible = true
                view_offer_author_email.isVisible = false

                cancelOffer()

                val intent = Intent(this, RequestListActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Can't " + offerAccept, Toast.LENGTH_SHORT).show()
            }

        }

        //Edit offer button. If pressed user is sent to page to edit offer. Request and offer details are sent to determine which offer in which request is being edited
        btn_edit_offer.setOnClickListener{
            startActivityForResult(intentFor<EditOfferActivity>().putExtra("request", request).putExtra("view_offer", offer), 0)
        }

        //Delete button will create alert dialogue asking user if they are sure they want to delete and offer. If they say yes the offer is deleted from the database
        // and the user is sent to the RequestListActivity. OTherwise i they press no the dialogue is removed.
        btn_delete_offer.setOnClickListener{

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Are You Sure?")
            builder.setMessage("Do you want to delete this offer?")
            builder.setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int ->
                database.child("requests/${request.reqId}").child("offers/${offer.offerId}").removeValue()
                startActivityForResult<RequestListActivity>(0)
            })
            builder.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int ->

            })
            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_view_offer, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            android.R.id.home -> { finish() }

        }
        return super.onOptionsItemSelected(item)
    }

    //Function used when request creator accepts an offer.
    //OfferAccepted value is written to the database and set to true.
    private fun acceptOffer(){
        database.child("requests/${request.reqId}/offers/${offer.offerId}/offerAccepted").setValue(true)
        offerAccept = true
    }

    //Function used when request creator cancels an offer.
    //OfferAccepted value is written to the database and set to false.
    private fun cancelOffer(){
        database.child("requests/${request.reqId}/offers/${offer.offerId}/offerAccepted").setValue(false)
        offerAccept = false
    }

    //Used to get the offer information and request information from the request and offer which the user came from
    //to set fields When viewing an offer. Checks done to determine who is viewing an offers and what buttons and textViews they are allowed to see.
    //Only creator of request is shown buttons to accept or cancel and email if an offer is accepted.
    //Only creator of offer is allowed to see edit and delete buttons for offer.
    fun setFromIntent(){
        request = intent.extras?.getParcelable<RequestModel>("request")!!
        offer = intent.extras?.getParcelable<OfferModel>("view_offer")!!
        //userEmail = intent.extras?.getString("user_email")!!
        userEmail = offer.authorEmail!!

        offerAccept = intent.extras?.getBoolean("offer_accepted")!!

        toolbar_view_offer.title = offer.authorName + "'s Offer"

        view_offer_name.setText(offer.authorName)
        view_offer_amount.setText("€" + offer.amount)
        view_offer_author_email.setText(userEmail)

        if(offerAccept && (request.authorId == FirebaseAuth.getInstance().currentUser!!.uid) && request.requestCompleted != true){
            view_offer_author_email.isVisible = true
            view_offer_click_email.isVisible = true
            btn_accept_offer.isVisible = false
            btn_edit_offer.isVisible = false
            btn_delete_offer.isVisible = false
            btn_cancel_offer.isVisible = true
        } else if(offerAccept && (offer.authorId == FirebaseAuth.getInstance().currentUser!!.uid)){
            view_offer_author_email.isVisible = false
            btn_accept_offer.isVisible = false
            btn_edit_offer.isVisible = false
            btn_delete_offer.isVisible = false
            btn_cancel_offer.isVisible = false
        } else if(!offerAccept && (offer.authorId == FirebaseAuth.getInstance().currentUser!!.uid)){
            view_offer_author_email.isVisible = false
            btn_accept_offer.isVisible = false
            btn_edit_offer.isVisible = true
            btn_delete_offer.isVisible = true
            btn_cancel_offer.isVisible = false
        } else if(request.requestCompleted == true){
            btn_accept_offer.isVisible = false
            btn_cancel_offer.isVisible = false
            view_offer_author_email.isVisible = false
        } else{
            view_offer_author_email.isVisible = false
            btn_accept_offer.isVisible = true
            btn_cancel_offer.isVisible = false
        }


    }

    //Function used to prompt user to select email app and then populate the 'to' field in that email app.
    fun composeEmail(addresses: Array<String>){
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, addresses)
        }
        if(intent.resolveActivity(packageManager) != null){
            startActivity(intent)
        }
    }
}