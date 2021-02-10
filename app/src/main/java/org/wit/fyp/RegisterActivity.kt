package org.wit.fyp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*
import org.wit.fyp.models.UserModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    var firstName: String = ""
    var lastName: String = ""
    var phoneNumber: String = ""
    var customKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG)

        database = Firebase.database.reference

        btn_register.setOnClickListener{
            when{
                TextUtils.isEmpty(edit_text_register_email.text.toString().trim { it <= ' '}) ->{
                    Toast.makeText(this,"An email address must be entered.", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(edit_text_register_password.text.toString().trim {it <= ' '}) ->{
                    Toast.makeText(this, "A password must be entered.", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(edit_text_register_first_name.text.toString().trim {it <= ' '}) -> {
                    Toast.makeText(this, "First Name must be entered.", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(edit_text_register_last_name.text.toString().trim {it <= ' '}) ->{
                    Toast.makeText(this, "Last Name must be entered.", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(edit_text_register_phone.text.toString().trim {it <= ' '}) ->{
                    Toast.makeText(this, "Phone Number must be entered", Toast.LENGTH_SHORT).show()
                }

                else ->{
                    val email: String = edit_text_register_email.text.toString().trim()
                    val password: String = edit_text_register_password.text.toString().trim()

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                val firebaseUser: FirebaseUser = task.result!!.user!!
                                Toast.makeText(this, "Registration Successful.", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("userID", firebaseUser.uid)
                                intent.putExtra("emailStr", email)

                                customKey = firebaseUser.uid

                                writeToDatabase()

                                startActivity(intent)
                                finish()
                            } else{
                                Toast.makeText(this,task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

        text_view_to_login.setOnClickListener{
           startActivity(Intent(this, LoginActivity::class.java))
        }

    }

    private fun writeToDatabase(){
        firstName = edit_text_register_first_name.text.toString().trim()
        lastName = edit_text_register_last_name.text.toString().trim()
        phoneNumber = edit_text_register_phone.text.toString().trim()

        var userModel = UserModel(firstName, lastName, phoneNumber)

        database.child("users").child(customKey).setValue(userModel)
    }
}

