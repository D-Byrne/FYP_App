package org.wit.fyp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
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



class RequestListActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    var reqKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_list)

        database = Firebase.database.reference.child("requests")

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        request_list_nav_menu.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.menu_add_request -> {startActivityForResult<AddRequestActivity>(0)}
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
                var list = ArrayList<RequestModel>()
                for(data in snapshot.children){
                    var model = data.getValue(RequestModel::class.java)
                    reqKey = data.key!!
                    model!!.reqId = reqKey
                    list.add(model as RequestModel)
                }
                if(list.size > 0){
                    val adapter = RequestAdapter(list)
                    recyclerView.adapter = adapter
                    //Toast.makeText(applicationContext, reqId, Toast.LENGTH_SHORT).show()
                }
            }

        })
    }
}