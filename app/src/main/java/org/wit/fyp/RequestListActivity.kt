package org.wit.fyp

import android.content.DialogInterface
import android.content.Intent
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
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_request_list.*
import kotlinx.android.synthetic.main.activity_user_request_list.*
import kotlinx.android.synthetic.main.activity_view_offer.*
import org.wit.fyp.adapters.RequestAdapter
import org.wit.fyp.models.RequestModel
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.wit.fyp.models.UserModel


class RequestListActivity : AppCompatActivity(), RequestAdapter.OnItemClickListener {

    private lateinit var database: DatabaseReference

    var reqKey: String = ""

    var requestList = ArrayList<RequestModel>()
    var filteredList = ArrayList<RequestModel>()

    var currentUser = UserModel()

    var filtering = false
    var selectedCounty = ""
    val countyNames = arrayOf("Carlow", "Cavan", "Clare", "Cork", "Donegal", "Dublin", "Galway", "Kerry", "Kildare", "Kilkenny",
            "Laois", "Leitrim", "Limerick", "Longford", "Louth", "Mayo", "Meath", "Monaghan", "Offaly", "Roscommon",
            "Sligo", "Tipperary", "Waterford", "Westmeath", "Wexford", "Wicklow")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_list)

        database = Firebase.database.reference.child("requests")

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        toolbar_view_request_list.title = title
        setSupportActionBar(toolbar_view_request_list)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        toolbar_view_request_list.title = "Home"

        //Bottom navigation button click listener
        request_list_nav_menu.setOnNavigationItemSelectedListener{
            when(it.itemId){

                R.id.menu_add_request -> {startActivityForResult<AddRequestActivity>(0)}
                R.id.menu_home_list -> { Toast.makeText(this, "Already on home page.", Toast.LENGTH_SHORT).show() }
                R.id.menu_view_user_requests -> {startActivityForResult<UserRequestList>(0)}

            }
            true
        }

        getRequest()
    }

    //USed to inflate top toolbar with menu_top_toolbar_list_requests.
    //Check done to see whether user is currently viewing a filtered list of requests.
    //IF true then the 'Show All' menu is made visible on the toolbar. Otherwise its hidden
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_view_list_requests, menu)
        if(filtering)menu!!.getItem(0).setVisible(true)
        return super.onCreateOptionsMenu(menu)
    }

    //Used to determine the effect of pressing menu items on toolbar.
    //Logout button logs user out.
    //Filter button contains code to create multiple choice alert dialogue allowing user to select county to filter by
    //Selecting filter will set adapter with new list of requests whose location is the county which was selected on filter.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_filter_list -> {


                val arrayChecked = booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Filter by County")

                builder.setMultiChoiceItems(countyNames, arrayChecked, {dialog, which, isChecked ->
                    arrayChecked[which] = isChecked
                    val county = countyNames[which]

                })

                builder.setPositiveButton("Ok"){_, _ ->
                    selectedCounty = ""
                    filteredList.clear()
                    var count: Int = 0

                    for(i in 0 until countyNames.size){
                        if (arrayChecked[i] == true){
                            count++
                        }
                    }

                    for (i in 0 until countyNames.size){
                        val checked = arrayChecked[i]

                        if(checked && count == 1){
                            selectedCounty = countyNames[i].toString()
                            for (request in requestList){
                                if(request.requestLocation == countyNames[i]){
                                    filteredList.add(request)
                                }
                            }

                        } else if(checked && count > 1) {
                            Toast.makeText(this, "Can only select one county.", Toast.LENGTH_SHORT).show()
                        } else{

                        }
                    }

                    if(filteredList.size > 0){
                        val adapter = RequestAdapter(filteredList, this@RequestListActivity)
                        recyclerView.adapter = adapter

                        filtering = true
                        invalidateOptionsMenu()
                    }

                }

                builder.show()

            }

            R.id.menu_logout -> {

                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            R.id.menu_top_undo_filter -> {
                filtering = false
                invalidateOptionsMenu()

                val adapter = RequestAdapter(requestList, this@RequestListActivity)
                recyclerView.adapter = adapter

            }

        }
        return super.onOptionsItemSelected(item)
    }

    //Gets Requests from database to populate recyclerview.
    //IF user is filtering adapter will not be set to avoid users filter being replaced with original list
    private fun getRequest(){

        database.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                requestList.clear()

                for(data in snapshot.children){
                    var model = data.getValue(RequestModel::class.java)
                    reqKey = data.key!!
                    model!!.reqId = reqKey


                    if(model.requestCompleted != true) {
                        requestList.add(model as RequestModel)
                    }

                }
                if(requestList.size > 0 && !filtering){
                    val adapter = RequestAdapter(requestList, this@RequestListActivity)
                    recyclerView.adapter = adapter
                }
            }

        })
    }

    //Handles Clicks on requests.
    //Clicking on request will bring user to viewRequestActivity and request clicked on will be sent in intent to populate fields in ViewRequestActivity
    override fun onItemClick(position: Int) {

        var clickedItem: RequestModel = requestList[position]

        if(filtering){
            clickedItem = filteredList[position]
        }else {
            clickedItem = requestList[position]
        }

        startActivityForResult(intentFor<ViewRequestActivity>().putExtra("view_request_model", clickedItem), 0)

    }
}