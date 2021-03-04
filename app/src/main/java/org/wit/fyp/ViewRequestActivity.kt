package org.wit.fyp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_view_request.*
import kotlinx.android.synthetic.main.activity_view_request.view.*
import org.wit.fyp.adapters.OfferAdapter
import org.wit.fyp.adapters.RequestAdapter
import org.wit.fyp.models.OfferModel
import org.wit.fyp.models.RequestModel

class ViewRequestActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    var username = ""
    var firstName: String = ""
    var lastName: String = ""

    var request = RequestModel()

    var offerList = ArrayList<OfferModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_request)

        database = Firebase.database.reference

        val layoutManager = LinearLayoutManager(this)
        recyclerView_offers.layoutManager = layoutManager


        setRequestFields()

        getUserData()

        btn_add_offer.setOnClickListener{
            if(edit_text_add_offer.text.toString().trim().isNotEmpty()){
                    addOffer()
                    edit_text_add_offer.setText("")
            }
            else{ Toast.makeText(this, "Must enter offer.", Toast.LENGTH_SHORT).show()}
        }

        getOffer()



    }

    fun setRequestFields(){
        request = intent.extras?.getParcelable<RequestModel>("view_request_model")!!

        view_request_title.setText(request.requestTitle)
        view_request_author.setText("Posted by: " + request.authorName)
        view_request_details.setText(request.requestDetails)
        view_request_deadline.setText("Accepting Offers Until: " + request.requestDeadline)
        view_request_location.setText("Location: " + request.requestLocation)

        //database = Firebase.database.reference.child("requests").child(request.reqId!!).child("offers")
    }

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

                    offerList.add(model as OfferModel)
                }
                if(offerList.size > 0){
                    val adapter = OfferAdapter(offerList)
                    recyclerView_offers.adapter = adapter
                    //Toast.makeText(applicationContext, reqId, Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun addOffer(){

        database = Firebase.database.reference

        val authorId = FirebaseAuth.getInstance().currentUser!!.uid
        val authorName = username
        val offerAmount = view_request_offer_field.edit_text_add_offer.text.toString()
        val id = database.push().key

        val offerModel = OfferModel(authorId, authorName, offerAmount)

        database.child("requests").child(request.reqId!!).child("offers").child(id!!).setValue(offerModel)

    }

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

                    username = firstName + " " + lastName

                } else{

                }
            }
        })
    }
}