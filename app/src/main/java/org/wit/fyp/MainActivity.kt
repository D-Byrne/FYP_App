package org.wit.fyp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
    var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val userId = intent.getStringExtra("userID")
        //val emailStr = intent.getStringExtra("emailStr")

        //text_view_user_id_lo.text = "UserID: $userId"
        //text_view_email_lo.text = "Email: $emailStr"

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val email = FirebaseAuth.getInstance().currentUser!!.email


        text_view_user_id_lo.text = "UserID: $userId"
        text_view_email_lo.text = "Email: $email"

        database = Firebase.database.reference.child("users/$userId")


        btn_logout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btn_to_add_request.setOnClickListener{
            val intent = Intent(this, AddRequestActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("username", username)

            startActivity(intent)
        }

        btn_to_view_requests.setOnClickListener{
            startActivity(Intent(this, RequestListActivity::class.java))
        }

        getData()

    }


    private fun getData(){
        database.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError){
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.exists()) {

                    firstName = dataSnapshot.child("firstName").getValue(String::class.java)!!
                    lastName = dataSnapshot.child("lastName").getValue(String::class.java)!!
                    phoneNumber = dataSnapshot.child("phoneNumber").getValue(String::class.java)!!

                    username = firstName + " " + lastName

                    text_view_first_name.setText("First Name: $firstName")
                    text_view_last_name.setText("Last Name: $lastName")
                    text_view_phone_number.setText("Phone Number: $phoneNumber")

                } else {

                    text_view_first_name.setText("First Name: Nope")
                    text_view_last_name.setText("Last Name: Nope")
                    text_view_phone_number.setText("Phone Number: Nope")

                }

                /*
                text_view_first_name.setText("First Name: $firstName")
                text_view_last_name.setText("Last Name: $lastName")
                text_view_phone_number.setText("Phone Number: $phoneNumber")
                 */

            }
        })
    }



    private fun writeDataMain(){

    }
}