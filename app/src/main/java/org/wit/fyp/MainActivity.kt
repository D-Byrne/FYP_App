package org.wit.fyp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    var firstName: String = ""
    var lastName: String = ""
    var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userId = intent.getStringExtra("userID")
        val emailStr = intent.getStringExtra("emailStr")

        text_view_user_id_lo.text = "UserID: $userId"
        text_view_email_lo.text = "Email: $emailStr"

        database = Firebase.database.reference.child("users/$userId")


        btn_logout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        getData()
        
    }

    private fun getData(){
        database.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError){
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                firstName = dataSnapshot.child("firstName").getValue(String::class.java)!!
                lastName = dataSnapshot.child("lastName").getValue(String::class.java)!!
                phoneNumber = dataSnapshot.child("phoneNumber").getValue(String::class.java)!!

                text_view_first_name.setText(firstName)
                text_view_last_name.setText(lastName)
                text_view_phone_number.setText(phoneNumber)

            }
        })
    }
}