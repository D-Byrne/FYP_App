package org.wit.fyp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_list_ratings.*
import kotlinx.android.synthetic.main.activity_request_list.*
import kotlinx.android.synthetic.main.activity_view_offer.*
import org.wit.fyp.adapters.RatingAdapter
import org.wit.fyp.adapters.RequestAdapter
import org.wit.fyp.models.RatingModel

class ListRatingsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    var ratingList = ArrayList<RatingModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_ratings)

        database = Firebase.database.reference.child("users/VVMGq5rznrScXz1dJE08ojIouhZ2/ratings")

        val layoutManager = LinearLayoutManager(this)
        recyclerView_ratings.layoutManager = layoutManager

        toolbar_view_ratings_list.title = title
        setSupportActionBar(toolbar_view_ratings_list)

        getRatings()
    }

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
                    val adapter = RatingAdapter(ratingList)
                    recyclerView_ratings.adapter = adapter
                }


            }

        })
    }

}