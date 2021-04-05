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

        btn_add_offer.setOnClickListener{
            if(edit_text_add_offer.text.toString().trim().isNotEmpty()){
                    addOffer()
                    edit_text_add_offer.setText("")
            }
            else{ Toast.makeText(this, "Must enter offer.", Toast.LENGTH_SHORT).show()}
        }

        getOffer()

    }

    /*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_main, menu)
        if(isEdit || isDelete) menu!!.getItem(0).setVisible(true)
        if(isEdit) menu!!.getItem(2).setVisible(false)
        if(isDelete) menu!!.getItem(1).setVisible(false)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_top_delete -> { isDelete = true
                toolbar.title = "Delete Request"
                invalidateOptionsMenu()}
            R.id.menu_top_edit -> { isEdit = true
                toolbar.title = "Edit Request"
                invalidateOptionsMenu()}
            R.id.menu_top_done -> { isEdit = false
                isDelete =false
                toolbar.title = "User Requests"
                invalidateOptionsMenu()}

            //android.R.id.home -> { Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show() }

        }
        return super.onOptionsItemSelected(item)
    }

     */

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_view_request, menu)
        if( offerAccept && ownRequest){
            menu!!.getItem(0).setVisible(true)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            android.R.id.home -> { finish() }
            R.id.menu_complete_task -> { startActivityForResult(intentFor<AddReviewActivity>().putExtra("accepted_offer", currentOffer), 0) }

        }
        return super.onOptionsItemSelected(item)
    }

    fun setRequestFields(){
        request = intent.extras?.getParcelable<RequestModel>("view_request_model")!!

        view_request_title.setText(request.requestTitle)
        view_request_author.setText("Posted by: " + request.authorName)
        view_request_details.setText(request.requestDetails)
        view_request_deadline.setText("Accepting Offers Until: " + request.requestDeadline)
        view_request_location.setText("Location: " + request.requestLocation)

        toolbar_view_request.title = request.authorName + "'s Request"

        if(FirebaseAuth.getInstance().currentUser!!.uid == request.authorId){
            ownRequest = true
            invalidateOptionsMenu()
        }
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

    private fun addOffer(){

        for(offer in offerList){
            if(offer.authorId == FirebaseAuth.getInstance().currentUser!!.uid){
                offerExists = true
            }
        }

        if(FirebaseAuth.getInstance().currentUser!!.uid == request.authorId){
            ownRequest = true
            //invalidateOptionsMenu()
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
            //btn_add_offer.isVisible = false
            //view_request_offer_field.isVisible = false
            Toast.makeText(this, "Can't post offer on own request.", Toast.LENGTH_SHORT).show()
        } else {
            database.child("requests").child(request.reqId!!).child("offers").child(id!!).setValue(offerModel)
        }

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
                    email = dataSnapshot.child("userEmail").getValue(String::class.java)!!

                    username = firstName + " " + lastName

                } else{

                }
            }
        })

    }

    override fun onItemClick(position: Int) {

       checkOffer()

        val clickedItem: OfferModel = offerList[position]

        if(FirebaseAuth.getInstance().currentUser!!.uid == request.authorId){
            if(offerAccept && (clickedItem.offerId!! == currentlyAcceptedId)) {
                startActivityForResult(intentFor<ViewOfferActivity>().putExtra("view_offer", clickedItem).putExtra("request", request).putExtra("user_email", offerEmail).putExtra("offer_accepted", offerAccept), 0)
            } else if(offerAccept && (clickedItem.offerId!! != currentlyAcceptedId)){
                Toast.makeText(this, "Only one offer can be accepted at a time.", Toast.LENGTH_SHORT).show()
            } else if (!offerAccept){
                startActivityForResult(intentFor<ViewOfferActivity>().putExtra("view_offer", clickedItem).putExtra("request", request).putExtra("user_email", clickedItem.authorEmail).putExtra("offer_accepted", offerAccept), 0)
            }

        } else if (clickedItem.authorId == FirebaseAuth.getInstance().currentUser!!.uid){
            startActivityForResult(intentFor<ViewOfferActivity>().putExtra("request", request).putExtra("view_offer", clickedItem).putExtra("offer_accepted", offerAccept),0)

        } else{
            Toast.makeText(this, "Not Allowed to proceed.", Toast.LENGTH_SHORT).show()
        }

    }

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