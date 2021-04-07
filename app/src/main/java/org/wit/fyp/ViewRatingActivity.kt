package org.wit.fyp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_list_ratings.*
import kotlinx.android.synthetic.main.activity_view_rating.*
import org.wit.fyp.models.RatingModel

class ViewRatingActivity : AppCompatActivity() {

    var rating = RatingModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_rating)

        toolbar_view_rating.title = title
        setSupportActionBar(toolbar_view_rating)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setFromIntent()

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

    private fun setFromIntent(){
        rating = intent.extras?.getParcelable<RatingModel>("rating")!!

        view_rating_author_name.text = "Rated By: " + rating.authorName
        view_rating_bar_small.rating = rating.ratingNumber.toFloat()
        view_rating_details.text = rating.ratingDetails

        toolbar_view_rating.title = "${rating.authorName}'s Rating"

    }

}