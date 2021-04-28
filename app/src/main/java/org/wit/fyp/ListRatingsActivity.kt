package org.wit.fyp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_list_ratings.*
import kotlinx.android.synthetic.main.activity_request_list.*
import kotlinx.android.synthetic.main.activity_view_offer.*
import org.jetbrains.anko.intentFor
import org.wit.fyp.adapters.RatingAdapter
import org.wit.fyp.adapters.RequestAdapter
import org.wit.fyp.models.OfferModel
import org.wit.fyp.models.RatingModel
import org.wit.fyp.models.RequestModel

class ListRatingsActivity : AppCompatActivity(), RatingAdapter.OnItemClickListener {

    private lateinit var database: DatabaseReference

    var ratingList = ArrayList<RatingModel>()

    var offer = OfferModel()
    var request = RequestModel()

    var offerAuthorRating = false
    var requestAuthorRating = false
    var ownRating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_ratings)

        database = Firebase.database.reference

        val layoutManager = LinearLayoutManager(this)
        recyclerView_ratings.layoutManager = layoutManager

        toolbar_view_ratings_list.title = title
        setSupportActionBar(toolbar_view_ratings_list)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setFromIntent()

        getRatings()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_view_user_ratings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            android.R.id.home -> { finish() }

        }
        return super.onOptionsItemSelected(item)
    }


    //Takes offer from ViewOfferActivity to determine the id for whose ratings should be shown.
    //Shows own ratings if Accessed from UserRequestListActivity.
    fun setFromIntent(){

        offerAuthorRating = intent.extras?.getBoolean("offerAuthorRating")!!
        ownRating = intent.extras?.getBoolean("ownRating")!!
        requestAuthorRating = intent.extras?.getBoolean("requestAuthorRating")!!


        if(offerAuthorRating) {
            offer = intent.extras?.getParcelable<OfferModel>("offer")!!
            database = Firebase.database.reference.child("users/${offer.authorId}/ratings")
            toolbar_view_ratings_list.title = offer.authorName + "'s Ratings"

        } else if(ownRating){

            val myId = FirebaseAuth.getInstance().currentUser!!.uid

            database = Firebase.database.reference.child("users/$myId/ratings")
            toolbar_view_ratings_list.title = "My Ratings"

        } else if(requestAuthorRating){
            request = intent.extras?.getParcelable<RequestModel>("request")!!
            database = Firebase.database.reference.child("users/${request.authorId}/ratings")
            toolbar_view_ratings_list.title = request.authorName + "'s Ratings"

        }



    }

    //Retrieves Ratings from database and adds them to a list of rating objects. RecyclerView adapter is set with this list to show list of ratings
    private fun getRatings() {

        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("cancel", error.toString())
            }


            override fun onDataChange(snapshot: DataSnapshot) {
                ratingList.clear()

                for(data in snapshot.children){
                    var model = data.getValue(RatingModel::class.java)

                    ratingList.add(model as RatingModel)
                }
                if(ratingList.size > 0){
                    val adapter = RatingAdapter(ratingList, this@ListRatingsActivity)
                    recyclerView_ratings.adapter = adapter
                }


            }

        })
    }

    //Click listener which sends to user to the ViewRating Activity with the rating that they selected to populate fields in the next activity
    override fun onItemClick(position: Int) {
        val clickedItem: RatingModel = ratingList[position]

        startActivityForResult(intentFor<ViewRatingActivity>().putExtra("rating", clickedItem), 0)

    }

}