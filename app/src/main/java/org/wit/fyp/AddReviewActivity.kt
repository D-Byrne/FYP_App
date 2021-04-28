package org.wit.fyp

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_review.*
import kotlinx.android.synthetic.main.activity_view_offer.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivityForResult
import org.wit.fyp.models.OfferModel
import org.wit.fyp.models.RatingModel
import org.wit.fyp.models.RequestModel

class AddReviewActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    var currentOffer = OfferModel()
    var currentRequest = RequestModel()
    var userRating = RatingModel()

    var ownRequest: Boolean = false

    var requestAuthor: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        database = Firebase.database.reference

        toolbar_review.title = title
        setSupportActionBar(toolbar_review)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        btn_add_user_rating.setOnClickListener{

            if( (user_rating_star.rating.toDouble() != 0.0) && (edit_text_add_user_rating.text.toString().trim().isNotEmpty()) ){
                userRating.ratingDetails = edit_text_add_user_rating.text.toString().trim()
                userRating.ratingNumber = user_rating_star.rating.toDouble()

                if(ownRequest) {
                    writeToDatabase()
                    startActivityForResult(intentFor<RequestListActivity>(), 0)
                }else{
                    writeToDatabaseOfferAuthor()
                    startActivityForResult(intentFor<RequestListActivity>(), 0)

                }

            }else if( (user_rating_star.rating.toDouble() != 0.0) && (edit_text_add_user_rating.text.toString().trim().isEmpty()) ){
                userRating.ratingDetails = "No details given."
                userRating.ratingNumber = user_rating_star.rating.toDouble()

                if(ownRequest) {
                    writeToDatabase()
                    startActivityForResult(intentFor<RequestListActivity>(), 0)

                }else{
                    writeToDatabaseOfferAuthor()
                    startActivityForResult(intentFor<RequestListActivity>(), 0)
                }
            } else if(user_rating_star.rating.toDouble() == 0.0){
                Toast.makeText(this, "Star rating cannot be 0.", Toast.LENGTH_SHORT).show()
            }

        }

        getUserData()
        setFromIntent()
    }

    //When back button is pressed an alert dialogue will ask the user whether they want to complete the request.

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Are You Sure?")
        builder.setMessage("The request won't be completed.")
        builder.setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int ->
            finish()
        })
        builder.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int ->

        })
        builder.show()
    }

    //gets the request which is being completed and the offer of the person who completed it from ViewRequestActivity.
    //Used to determine which person is rating which. Uses gathered request and offer to set title of toolbar to whoever is being rated.
    fun setFromIntent(){
        currentOffer = intent.extras?.getParcelable<OfferModel>("accepted_offer")!!
        currentRequest = intent.extras?.getParcelable<RequestModel>("request")!!
        ownRequest = intent.extras?.getBoolean("ownRequest")!!

        if(ownRequest!!) {
            userRating.authorId = currentRequest.authorId
            userRating.authorName = currentRequest.authorName

            toolbar_review.title = "Rate ${currentOffer.authorName}"
           // Toast.makeText(this, currentOffer.authorName, Toast.LENGTH_SHORT).show()
        }else{
            userRating.authorId = FirebaseAuth.getInstance().currentUser!!.uid
            //userRating.authorName = currentRequest.authorName

            //Toast.makeText(this, currentRequest.authorName, Toast.LENGTH_SHORT).show()

            toolbar_review.title = "Rate ${currentRequest.authorName}"
        }
    }

    //Sets values in the request in the database when the creator of the request has marked as complete and has completed the rating of the user who completed the request.
    fun writeToDatabase(){

        val id = database.push().key

        database.child("users/${currentOffer.authorId}/ratings").child(id!!).setValue(userRating)
        database.child("requests/${currentRequest.reqId}/requestCompleted").setValue(true)
        database.child("requests/${currentRequest.reqId}/authorRating").setValue(true)
        database.child("requests/${currentRequest.reqId}/offerAuthorRating").setValue(false)
        database.child("requests/${currentRequest.reqId}/completedBy/").setValue(currentOffer.authorId)
        database.child("requests/${currentRequest.reqId}/offers/${currentOffer.offerId}/requestCompleted").setValue(true)
    }

    //sets values in the request and one of its child offers in the database when the person who created the offer and completed the request and provided a rating
    fun writeToDatabaseOfferAuthor(){

        val id = database.push().key

        database.child("users/${currentRequest.authorId}/ratings").child(id!!).setValue(userRating)
        database.child("requests/${currentRequest.reqId}/offerAuthorRating").setValue(true)
        Log.d("DumbFuck", "The Thing: " + "requests/${currentRequest.reqId}/offerAuthorRating")

    }

    //gets the first and last name of the logged in user from the database.
    private fun getUserData(){

        database.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object:
                ValueEventListener {
            override fun onCancelled(error: DatabaseError){
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.exists()) {

                    val firstName = dataSnapshot.child("firstName").getValue(String::class.java)!!
                    val lastName = dataSnapshot.child("lastName").getValue(String::class.java)!!
                    //email = dataSnapshot.child("userEmail").getValue(String::class.java)!!

                    userRating.authorName = firstName + " " + lastName

                } else{

                }
            }
        })

    }
}