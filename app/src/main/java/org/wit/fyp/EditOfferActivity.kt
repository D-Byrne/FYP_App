package org.wit.fyp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit_offer.*
import kotlinx.android.synthetic.main.activity_view_offer.*
import org.jetbrains.anko.startActivityForResult
import org.wit.fyp.models.OfferModel
import org.wit.fyp.models.RequestModel

class EditOfferActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    var request = RequestModel()
    var offer = OfferModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_offer)

        database = Firebase.database.reference

        toolbar_edit_offer.title = title
        setSupportActionBar(toolbar_edit_offer)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        btn_save_offer_edit.setOnClickListener{
            if (edit_text_edit_offer.text.toString().trim().isNotEmpty()){
                saveEdit()
                startActivityForResult<RequestListActivity>(0)
            } else{
                Toast.makeText(this, "Field can't be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        setFromIntent()
    }

    //Gets request and offer from ViewOfferActivity to determine which offer needs to be updated.
    private fun setFromIntent(){
        request = intent.extras?.getParcelable<RequestModel>("request")!!
        offer = intent.extras?.getParcelable<OfferModel>("view_offer")!!

        toolbar_edit_offer.setTitle("Edit Offer")

        edit_text_edit_offer.setText(offer.amount)

    }

    //Validated on button press in onCreate value in field is taken and pushed to respective field in database
    private fun saveEdit(){
        database.child("requests/${request.reqId}/offers/${offer.offerId}/amount").setValue(edit_text_edit_offer.text.toString().trim())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            android.R.id.home -> { finish() }

        }
        return super.onOptionsItemSelected(item)
    }
}