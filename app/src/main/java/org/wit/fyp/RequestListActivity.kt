package org.wit.fyp

import android.content.Intent
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
import org.wit.fyp.adapters.RequestAdapter
import org.wit.fyp.models.RequestModel
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast


class RequestListActivity : AppCompatActivity(), RequestAdapter.OnItemClickListener {

    private lateinit var database: DatabaseReference

    var reqKey: String = ""

    var requestList = ArrayList<RequestModel>()
   // var adapter = RequestAdapter(requestList, this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_list)

        database = Firebase.database.reference.child("requests")

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        
        request_list_nav_menu.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.menu_add_request -> {startActivityForResult<AddRequestActivity>(0)}
                R.id.menu_home_list -> { Toast.makeText(this, "Already on home page.", Toast.LENGTH_SHORT).show() }
                R.id.menu_logout -> {
                    FirebaseAuth.getInstance().signOut()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            true
        }

        getRequest()
    }

    private fun getRequest(){

        database.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
               // var requestList = ArrayList<RequestModel>()
                for(data in snapshot.children){
                    var model = data.getValue(RequestModel::class.java)
                    reqKey = data.key!!
                    model!!.reqId = reqKey
                    requestList.add(model as RequestModel)
                }
                if(requestList.size > 0){
                    val adapter = RequestAdapter(requestList, this@RequestListActivity)
                    recyclerView.adapter = adapter
                    //Toast.makeText(applicationContext, reqId, Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this, "Request $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem: RequestModel = requestList[position]
        Toast.makeText(this, "RequestId: ${clickedItem.reqId}", Toast.LENGTH_SHORT).show()
        startActivityForResult(intentFor<ViewRequestActivity>().putExtra("view_request_model", clickedItem), 0)

    }
}