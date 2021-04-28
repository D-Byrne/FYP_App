package org.wit.fyp

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_request_list.*
import kotlinx.android.synthetic.main.activity_user_request_list.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivityForResult
import org.wit.fyp.adapters.RequestAdapter
import org.wit.fyp.models.OfferModel
import org.wit.fyp.models.RequestModel

class UserRequestList : AppCompatActivity(), RequestAdapter.OnItemClickListener {

    private lateinit var database : DatabaseReference
    var requestList = ArrayList<RequestModel>()
    var acceptedList = ArrayList<RequestModel>()
    var onlyRequestList = ArrayList<RequestModel>()
    var pendingRatingList = ArrayList<RequestModel>()
    var completedRequestsList = ArrayList<RequestModel>()

    var offerAuthorRating = false
    var requestAuthorRating = false
    var ownRating = true

    var reqKey: String = ""
    var isEdit: Boolean = false
    var isDelete: Boolean = false

    var currentlyOnMain = true
    var currentlyOnAccepted = false
    var currentlyOnAll = false
    var currentlyOnPending = false
    var currentlyOnComplete = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_request_list)

        database = Firebase.database.reference.child("requests")

        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        toolbar.title = "Active Requests"


        val layoutManager = LinearLayoutManager(this)
        recyclerViewPerUser.layoutManager = layoutManager

        //Click listener for bottom navigation bar
        user_request_list_nav_menu.setOnNavigationItemSelectedListener {

            when(it.itemId) {
                R.id.menu_user_view_request_user_requests -> { Toast.makeText(this, "Already viewing user requests", Toast.LENGTH_SHORT).show() }
                R.id.menu_user_view_request_add_request -> { startActivityForResult<AddRequestActivity>(0) }
                R.id.menu_user_view_request_home -> { startActivityForResult<RequestListActivity>(0) }
                android.R.id.home -> { Toast.makeText(this, "Back again.", Toast.LENGTH_SHORT).show() }
            }
            true
        }

        getRequests()

    }

    //Used to inflate menu with menu_top_main.
    //Conditions set to determine whether or not ot show edit, delete, and cance buttons.
    //When showing one of edit or delete buttons cancel button is shown.
    //IF showing both edit and delete buttons cancel button is not shown.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_main, menu)
        if(isEdit || isDelete) menu!!.getItem(0).setVisible(true)
        if(isEdit) menu!!.getItem(2).setVisible(false)
        if(isDelete) menu!!.getItem(1).setVisible(false)

        return super.onCreateOptionsMenu(menu)
    }

    //Click Listeners for top toolbar.
    //Booleans used to determine which list of request user is currently viewing and how that will effect what happens if they try to move to a different list.
    //Depending on list selected the recyclerview adapter will be set with a different list.
    //After pressing each option the toolbar is invalidated and refreshed to display the current list shownin the title.
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

                if(currentlyOnMain){
                    toolbar.title = "Active Requests"

                } else if(currentlyOnAccepted){
                    toolbar.title = "Accepted Offers"

                } else if(currentlyOnComplete){
                    toolbar.title = "Completed Requests"

                } else if(currentlyOnAll){
                    toolbar.title = "All Requests/Offers"

                } else if(currentlyOnPending){
                    toolbar.title = "Pending Ratings"

                }
                                    invalidateOptionsMenu()}
            R.id.menu_top_my_ratings ->{
                startActivityForResult(intentFor<ListRatingsActivity>().putExtra("offerAuthorRating", offerAuthorRating).putExtra("requestAuthorRating", requestAuthorRating).putExtra("ownRating", ownRating), 0)

            }
            R.id.menu_top_only_requests -> {
                if(currentlyOnMain){
                    Toast.makeText(this, "Already viewing your requests.", Toast.LENGTH_SHORT).show()
                } else if(!currentlyOnMain || currentlyOnAccepted || currentlyOnAll || currentlyOnPending || currentlyOnComplete){
                    val adapter = RequestAdapter(onlyRequestList, this@UserRequestList)
                    recyclerViewPerUser.adapter = adapter
                    toolbar.title = "Active Requests"

                    currentlyOnMain = true
                    currentlyOnAccepted = false
                    currentlyOnAll = false
                    currentlyOnPending = false
                    currentlyOnComplete = false
                }
            }
            R.id.menu_top_show_accepted -> {
                if(currentlyOnAccepted){
                    Toast.makeText(this, "Already viewing accepted offers.", Toast.LENGTH_SHORT).show()
                } else if(!currentlyOnAccepted || currentlyOnMain || currentlyOnAll || currentlyOnPending || currentlyOnComplete){
                    val adapter = RequestAdapter(acceptedList, this@UserRequestList)
                    recyclerViewPerUser.adapter = adapter
                    toolbar.title = "Accepted Offers"

                    currentlyOnAccepted = true
                    currentlyOnMain = false
                    currentlyOnAll = false
                    currentlyOnPending = false
                    currentlyOnComplete = false
                }
            }
            R.id.menu_top_show_all -> {
                if(currentlyOnAll){
                    Toast.makeText(this, "Already viewing requests and posts.", Toast.LENGTH_SHORT).show()
                } else if(!currentlyOnAll){
                    val adapter = RequestAdapter(requestList, this@UserRequestList)
                    recyclerViewPerUser.adapter = adapter
                    toolbar.title = "All Requests/Offers"

                    currentlyOnAll = true
                    currentlyOnMain = false
                    currentlyOnAccepted = false
                    currentlyOnPending = false
                    currentlyOnComplete = false
                }
            }
            R.id.menu_top_show_pending_rating -> {
                if(currentlyOnPending){
                    Toast.makeText(this, "Already viewing pending ratings.", Toast.LENGTH_SHORT).show()
                } else if(!currentlyOnPending || currentlyOnMain || currentlyOnAll || currentlyOnAccepted || currentlyOnComplete){
                    val adapter = RequestAdapter(pendingRatingList, this@UserRequestList)
                    recyclerViewPerUser.adapter = adapter
                    toolbar.title = "Pending Ratings"

                    currentlyOnPending = true
                    currentlyOnAll = false
                    currentlyOnMain = false
                    currentlyOnAccepted = false
                    currentlyOnComplete = false
                }
            }
            R.id.menu_top_completed_requests -> {
                if(currentlyOnComplete){
                    Toast.makeText(this, "Already Viewing complete Requests/Offers", Toast.LENGTH_SHORT).show()
                } else if (!currentlyOnComplete || currentlyOnPending || currentlyOnAccepted || currentlyOnMain || currentlyOnAll){
                    val adapter = RequestAdapter(completedRequestsList, this@UserRequestList)
                    recyclerViewPerUser.adapter = adapter
                    toolbar.title = "Completed Requests"

                    currentlyOnComplete = true
                    currentlyOnPending = false
                    currentlyOnAll = false
                    currentlyOnMain = false
                    currentlyOnAccepted = false


                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    //Gets requests and iterates through the offers inside of each request to determine which list they are put in.
    //Requests where user needs to provide a rating are added to pendingRatingList
    //Requests where a users offer has been accepted are added into the acceptedList
    //Requests which the user has completed are added to the completedRequestList
    //Requests which hve been created by the user are added to onlyRequestList
    //Requests which have been created by the user or contain an offer created by the user are added to requestList
    //If new requests are added the adapter will only change if the user is on the main view which means they are viewing the onlyRequestList and the menu item
    //they are viewing is active requests
    fun getRequests(){

        database.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                requestList.clear()
                acceptedList.clear()
                onlyRequestList.clear()
                pendingRatingList.clear()
                completedRequestsList.clear()

                for(data in snapshot.children){
                    var model = data.getValue(RequestModel::class.java)
                    reqKey = data.key!!
                    model!!.reqId = reqKey

                    data.hasChild("offers")

                    for(newData in data.child("offers").children){
                        newData.hasChild("authorName")
                        var newModel = newData.getValue(OfferModel::class.java)

                        if(newModel!!.authorId == FirebaseAuth.getInstance().currentUser!!.uid){
                            requestList.add(model as RequestModel)
                        }
                        if( (newModel!!.authorId == FirebaseAuth.getInstance().currentUser!!.uid) && (newModel!!.offerAccepted == true) && newModel.requestCompleted != true){
                            acceptedList.add(model as RequestModel)
                        }

                        Log.d("AcceptedThing", "Yes: " + acceptedList.size)
                        Log.d("TAGLLE", newData.hasChild("authorName").toString() + newModel!!.authorId)
                    }

                    if(acceptedList.size > 0){
                        toolbar.setSubtitle("Accepted Offers: ${acceptedList.size}")

                    } else if(acceptedList.size < 1){
                        toolbar.setSubtitle("Accepted Offers: 0")

                    }

                    if(model.offerAuthorRating == false && model.completedBy == FirebaseAuth.getInstance().currentUser!!.uid){
                        pendingRatingList.add(model as RequestModel)
                    } else if( (model.offerAuthorRating == true && model.completedBy == FirebaseAuth.getInstance().currentUser!!.uid) || (model.requestCompleted == true && model.authorId == FirebaseAuth.getInstance().currentUser!!.uid) ){
                        completedRequestsList.add(model  as RequestModel)
                    }

                    Log.d("PendingThing", "Yes: " + pendingRatingList.size + model.completedBy + ", " + model.offerAuthorRating)
                    Log.d("CompletedThing", "Yes: " + completedRequestsList.size)



                    if(model.authorId == FirebaseAuth.getInstance().currentUser!!.uid && model.requestCompleted != true) {
                        requestList.add(model as RequestModel)
                        onlyRequestList.add(model as RequestModel)
                    } else if(model.authorId == FirebaseAuth.getInstance().currentUser!!.uid){
                        requestList.add(model)
                    }
                }
                if(onlyRequestList.size > 0 && currentlyOnMain){
                    val adapter = RequestAdapter(onlyRequestList, this@UserRequestList)
                    recyclerViewPerUser.adapter = adapter
                }
            }

        })
    }

    //Click Listener which determines what happens when user clicks on a request.
    //If the user has pressed the edit button the user is taken to the addRequestActivity with the request they selected passsed in intent to populate the fields in
    // addRequestACtivity
    //If the user has selected has pressed the delete button an alert dialgoue asking if they want to the delete the selected request will appear. IF the user selects yes the
    //the request is deleted from the database.
    //Checks done at the beginning to determine whcih list a user is viewing to ensure that the click listener takes the request from the correct list to send in intent to either
    //view, edit, or delete.
    override fun onItemClick(position: Int) {
        var clickedItem: RequestModel = requestList[position]

        if(currentlyOnMain){
            clickedItem = onlyRequestList[position]
        } else if(currentlyOnAll){
            clickedItem = requestList[position]
        } else if(currentlyOnAccepted){
            clickedItem = acceptedList[position]
        } else if(currentlyOnPending){
            clickedItem = pendingRatingList[position]
        } else if(currentlyOnComplete){
            clickedItem = completedRequestsList[position]
        }


        if(isEdit && (clickedItem.authorId == FirebaseAuth.getInstance().currentUser!!.uid)) {

            startActivityForResult(intentFor<AddRequestActivity>().putExtra("edit_request", clickedItem), 0)
        }else if( isEdit && (clickedItem.authorId != FirebaseAuth.getInstance().currentUser!!.uid)){

            Toast.makeText(this, "Can't edit other users requests", Toast.LENGTH_SHORT).show()

        } else if(isDelete && (clickedItem.authorId == FirebaseAuth.getInstance().currentUser!!.uid && clickedItem.requestCompleted != true)){

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Are You Sure?")
            builder.setMessage("Do you want to delete this request?")
            builder.setPositiveButton("Yes", { dialogInterface: DialogInterface, i: Int ->
                database.child(clickedItem.reqId!!).removeValue()
            })
            builder.setNegativeButton("No", { dialogInterface: DialogInterface, i: Int ->

            })
            builder.show()

        }else if(isDelete && (clickedItem.authorId != FirebaseAuth.getInstance().currentUser!!.uid)){

            Toast.makeText(this, "Can't delete other users requests", Toast.LENGTH_SHORT).show()

        } else if (isDelete && (clickedItem.authorId == FirebaseAuth.getInstance().currentUser!!.uid && clickedItem.requestCompleted == true)){
            Toast.makeText(this, "Can't delete completed requests", Toast.LENGTH_SHORT).show()

        } else{
            startActivityForResult(intentFor<ViewRequestActivity>().putExtra("view_request_model", clickedItem), 0)
        }

    }
}