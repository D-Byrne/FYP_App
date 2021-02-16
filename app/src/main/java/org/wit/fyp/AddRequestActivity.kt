package org.wit.fyp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_request.*
import org.wit.fyp.models.RequestModel

class AddRequestActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_request)

        database = Firebase.database.reference

        btn_add_request.setOnClickListener{
            if(edit_text_add_request_title.text.toString().trim().isNotEmpty()){
                writeToDatabase()
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                Toast.makeText(this, "Title must be entered", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun writeToDatabase(){

        var title = edit_text_add_request_title.text.toString().trim()
        val id = database.push().key
        var requestModel = RequestModel(title)

        database.child("requests").child(id!!).setValue(requestModel)
    }
}