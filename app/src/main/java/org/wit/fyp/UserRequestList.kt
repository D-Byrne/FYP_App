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
import kotlinx.android.synthetic.main.activity_request_list.*
import kotlinx.android.synthetic.main.activity_user_request_list.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivityForResult
import org.wit.fyp.adapters.RequestAdapter
import org.wit.fyp.models.RequestModel

class UserRequestList : AppCompatActivity(), RequestAdapter.OnItemClickListener {

    private lateinit var database : DatabaseReference
    var requestList = ArrayList<RequestModel>()
    var reqKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_request_list)

        database = Firebase.database.reference.child("requests")

        val layoutManager = LinearLayoutManager(this)
        recyclerViewPerUser.layoutManager = layoutManager

        user_request_list_nav_menu.setOnNavigationItemReselectedListener {

            when(it.itemId) {
                R.id.menu_user_view_request_user_requests -> { Toast.makeText(this, "Already viewing user requests", Toast.LENGTH_SHORT).show() }
                R.id.menu_user_view_request_add_request -> { startActivityForResult<AddRequestActivity>(0) }
                R.id.menu_user_view_request_home -> { startActivityForResult<RequestListActivity>(0) }
            }

        }

        getRequests()

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
    }

    override fun onItemClick(position: Int) {
        //Toast.makeText(this, "Request $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem: RequestModel = requestList[position]
        Toast.makeText(this, "RequestId: ${clickedItem.reqId}", Toast.LENGTH_SHORT).show()
        //startActivityForResult(intentFor<ViewRequestActivity>().putExtra("view_request_model", clickedItem), 0)

    }
}