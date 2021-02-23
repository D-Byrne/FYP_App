package org.wit.fyp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class RequestListActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_list)

        database = Firebase.database.reference.child("requests")

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

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
                    val model = data.getValue(RequestModel::class.java)
                    list.add(model as RequestModel)
                }
                if(list.size > 0){
                    val adapter = RequestAdapter(list)
                    recyclerView.adapter = adapter
                }
            }

        })
    }
}