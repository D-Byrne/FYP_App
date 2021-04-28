package org.wit.fyp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.graphics.toColor
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_request.*
import kotlinx.android.synthetic.main.activity_request_list.*
import kotlinx.android.synthetic.main.activity_user_request_list.*
import kotlinx.android.synthetic.main.activity_view_request.*
import kotlinx.android.synthetic.main.activity_view_request.view.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivityForResult
import org.wit.fyp.adapters.OfferAdapter
import org.wit.fyp.adapters.RequestAdapter
import org.wit.fyp.models.OfferModel
import org.wit.fyp.models.RequestModel

class ViewRequestActivity : AppCompatActivity(), OfferAdapter.OnItemClickListener {

    private lateinit var database: DatabaseReference

    var username = ""
    var firstName: String = ""
    var lastName: String = ""
    var email: String = ""
    var offerKey: String = ""
    var offerEmail: String = ""

    var request = RequestModel()
    var currentOffer = OfferModel()
    var offerList = ArrayList<OfferModel>()

    var offerExists: Boolean = false
    var ownRequest: Boolean = false

    var notComplete = true
    var requestAuthorRating = true
    var offerAuthorRating = false
    var ownRating = false

    var offerAccept: Boolean = false
    var currentlyAcceptedId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_request)

        database = Firebase.database.reference

        val layoutManager = LinearLayoutManager(this)
        recyclerView_offers.layoutManager = layoutManager

        toolbar_view_request.title = title
        setSupportActionBar(toolbar_view_request)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setRequestFields()

        getUserData()

        btn_view_request_author_ratings.setOnClickListener{
            startActivityForResult(intentFor<ListRatingsActivity>().putExtra("request", request).putExtra("requestAuthorRating", requestAuthorRating).putExtra("ownRating", ownRating).putExtra("offerAuthorRating", offerAuthorRating), 0)

        }

        btn_add_offer.setOnClickListener{
            if(edit_text_add_offer.text.toString().trim().isNotEmpty()){
                    addOffer()
                    edit_text_add_offer.setText("")
            }
            else{ Toast.makeText(this, "Must enter offer.", Toast.LENGTH_SHORT).show()}
        }

        getOffer()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_view_request, menu)
        if( offerAccept && ownRequest && notComplete){
            menu!!.getItem(0).setVisible(true)
        }
        return super.onCreateOptionsMenu(menu)
    }

    //Top toolbar click listener which sends the user to the AddReviewActivty if they press the tick on the top toolbar to complete the rating.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            android.R.id.home -> { finish() }
            R.id.menu_complete_task -> { startActivityForResult(intentFor<AddReviewActivity>().putExtra("accepted_offer", currentOffer).putExtra("request", request).putExtra("ownRequest", ownRequest), 0) }

        }
        return super.onOptionsItemSelected(item)
    }

    //Set the fields of the request such as title, auhtor name, details, deadline, location from the requests which was sent in the intent from the click listener
    // on a list of requests on RequestListActivty or UserRequestList.
    //Determines whether the person viewing the request is the person who created it or if an offer has been accepted and the tick item to complete the request can be shown.
    //Checks if the user viewing the request is the person who completed it for the request creator and hasn't created a rating yet.
    fun setRequestFields(){
        request = intent.extras?.getParcelable<RequestModel>("view_request_model")!!

        view_request_title.setText(request.requestTitle)
        view_request_author.setText("Posted by: " + request.authorName)
        view_request_details.setText(request.requestDetails)
        view_request_deadline.setText("Accepting Offers Until: " + request.requestDeadline)
        view_request_location.setText("Location: " + request.requestLocation)
        btn_view_request_author_ratings.setText("${request.authorName}'s Ratings")

        toolbar_view_request.title = request.authorName + "'s Request"


        if ( (FirebaseAuth.getInstance().currentUser!!.uid == request.authorId) && (request.requestCompleted == true) ){
            notComplete = false
        } else if(FirebaseAuth.getInstance().currentUser!!.uid == request.authorId){
            ownRequest = true
            invalidateOptionsMenu()
        }

        if(request.offerAuthorRating == false && request.completedBy == FirebaseAuth.getInstance().currentUser!!.uid){
            startActivityForResult(intentFor<AddReviewActivity>().putExtra("request", request).putExtra("accepted_offer", currentOffer).putExtra("ownRequest", ownRequest), 0)
        }else if(request.offerAuthorRating == true && request.completedBy == FirebaseAuth.getInstance().currentUser!!.uid){
            notComplete = false
        }

    }

    //Use to retrieve offers from the database and add them to a list which is used to set the recyclerview adapter to list the offer beneath the request
    private fun getOffer(){

        database = Firebase.database.reference.child("requests").child(request.reqId!!).child("offers")

        database.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                offerList.clear()

                for(data in snapshot.children){
                    val model = data.getValue(OfferModel::class.java)
                    offerKey = data.key!!
                    model!!.offerId = offerKey


                    offerList.add(model as OfferModel)
                }
                if(offerList.size > 0){
                    val adapter = OfferAdapter(offerList, this@ViewRequestActivity)
                    recyclerView_offers.adapter = adapter
                    checkOffer()

                }
            }

        })

        checkOffer()

    }

    //Used to add an offer to the database.
    //Checks if the person trying to create the offer is the creator of the post in which case they won't be allowed to create an offer fro their own request.
    //Checks if the user has already created an offer in which case they won't be allowed to created another.
    //If the user is not the creator of the request and hasn't already created an offer the offer wll be written to the database.
    private fun addOffer(){

        for(offer in offerList){
            if(offer.authorId == FirebaseAuth.getInstance().currentUser!!.uid){
                offerExists = true
            }
        }

        if(FirebaseAuth.getInstance().currentUser!!.uid == request.authorId){
            ownRequest = true
        }

        database = Firebase.database.reference

        val authorId = FirebaseAuth.getInstance().currentUser!!.uid
        val authorEmail = FirebaseAuth.getInstance().currentUser!!.email
        val authorName = username
        val offerAmount = view_request_offer_field.edit_text_add_offer.text.toString()
        val id = database.push().key

        val offerModel = OfferModel(authorId, authorName, offerAmount, null, null, authorEmail)

        if(offerExists){
            Toast.makeText(this, "You have already made an offer", Toast.LENGTH_SHORT).show()
        } else if(ownRequest){

            Toast.makeText(this, "Can't post offer on own request.", Toast.LENGTH_SHORT).show()
        } else {
            database.child("requests").child(request.reqId!!).child("offers").child(id!!).setValue(offerModel)
        }

    }

    //ed to get the first and last names and email of the current user from the database.
    private fun getUserData(){

        database.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(error: DatabaseError){
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.exists()) {

                    firstName = dataSnapshot.child("firstName").getValue(String::class.java)!!
                    lastName = dataSnapshot.child("lastName").getValue(String::class.java)!!
                    email = dataSnapshot.child("userEmail").getValue(String::class.java)!!

                    username = firstName + " " + lastName

                } else{

                }
            }
        })

    }

    //Click listener for offers
    //Before sending the user to the ViewOfferActivity chcks are adone to see who is tryin got view the offer.
    //If the request creator is clikcing an offer the request currently being viewed and the offer that was pressed will be sent with the intent as well as a
    // boolean to determine whether the offer is accepted or not.
    //If the creator of the offer presses the offer they will be sent with the request being viewed and the offer that was pressed being sent in the intent to the ViewOfferActivity
    // A boolean is also sent ott determine that the user is viewing their own request.
    override fun onItemClick(position: Int) {

       checkOffer()

        val clickedItem: OfferModel = offerList[position]

            if (FirebaseAuth.getInstance().currentUser!!.uid == request.authorId) {
                if (offerAccept && (clickedItem.offerId!! == currentlyAcceptedId)) {
                    startActivityForResult(intentFor<ViewOfferActivity>().putExtra("view_offer", clickedItem).putExtra("request", request).putExtra("user_email", offerEmail).putExtra("offer_accepted", offerAccept), 0)
                } else if (offerAccept && (clickedItem.offerId!! != currentlyAcceptedId)) {
                    Toast.makeText(this, "Only one offer can be accepted at a time.", Toast.LENGTH_SHORT).show()
                } else if (!offerAccept) {
                    startActivityForResult(intentFor<ViewOfferActivity>().putExtra("view_offer", clickedItem).putExtra("request", request).putExtra("user_email", clickedItem.authorEmail).putExtra("offer_accepted", offerAccept), 0)
                }

            } else if (clickedItem.authorId == FirebaseAuth.getInstance().currentUser!!.uid) {
                startActivityForResult(intentFor<ViewOfferActivity>().putExtra("request", request).putExtra("view_offer", clickedItem).putExtra("offer_accepted", offerAccept), 0)

            } else {
                Toast.makeText(this, "Not Allowed to proceed.", Toast.LENGTH_SHORT).show()
            }



    }

    //Used to check whether the current request contains an offer which has been accepted
    fun checkOffer(){
        for (offer in offerList){
                if(offer.offerAccepted == true) {
                    offerAccept = true
                    currentlyAcceptedId = offer.offerId!!
                    view_request_offer_status_tv.setText("Offer Accepted")
                    currentOffer = offer
                    invalidateOptionsMenu()
                }

        }
    }

}