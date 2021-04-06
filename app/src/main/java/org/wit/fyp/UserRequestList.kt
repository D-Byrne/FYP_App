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
    var reqKey: String = ""
    var isEdit: Boolean = false
    var isDelete: Boolean = false

    var numberChildren: Long = 0

    var offerList = ArrayList<OfferModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_request_list)

        database = Firebase.database.reference.child("requests")

        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        toolbar.title = "User Requests"


        val layoutManager = LinearLayoutManager(this)
        recyclerViewPerUser.layoutManager = layoutManager

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

    fun getRequests(){

        database.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                requestList.clear()

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


                        Log.d("TAGLLE", newData.hasChild("authorName").toString() + newModel!!.authorId)
                    }

                   // Log.d("TAGLLE", data.child("offers").children)

                   // Log.d("TAGLLE", data.child("offers").children.toString())

                    if(model.authorId == FirebaseAuth.getInstance().currentUser!!.uid) {
                        requestList.add(model as RequestModel)
                    }
                }
                if(requestList.size > 0){
                    val adapter = RequestAdapter(requestList, this@UserRequestList)
                    recyclerViewPerUser.adapter = adapter
                    //Toast.makeText(applicationContext, reqId, Toast.LENGTH_SHORT).show()
                }
            }

        })
        //Toast.makeText(this, numberChildren, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(position: Int) {
        //Toast.makeText(this, "Request $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem: RequestModel = requestList[position]

        if(isEdit && (clickedItem.authorId == FirebaseAuth.getInstance().currentUser!!.uid)) {
            //Toast.makeText(this, "Editing", Toast.LENGTH_SHORT).show()

            startActivityForResult(intentFor<AddRequestActivity>().putExtra("edit_request", clickedItem), 0)
        }else if( isEdit && (clickedItem.authorId != FirebaseAuth.getInstance().currentUser!!.uid)){

            Toast.makeText(this, "Can't edit other users requests", Toast.LENGTH_SHORT).show()

        } else if(isDelete && (clickedItem.authorId == FirebaseAuth.getInstance().currentUser!!.uid)){
            //Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show()

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

        } else{
            startActivityForResult(intentFor<ViewRequestActivity>().putExtra("view_request_model", clickedItem), 0)
        }
        //Toast.makeText(this, "RequestId: ${clickedItem.reqId}", Toast.LENGTH_SHORT).show()
        //startActivityForResult(intentFor<ViewRequestActivity>().putExtra("view_request_model", clickedItem), 0)

    }
}