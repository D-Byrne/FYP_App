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
    var userEmail: String = ""
    var customKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        database = Firebase.database.reference

        //Register button containing which first checks fields. Once fields are checked it moves on to creating the user with FirebaseAuth.
        //Once the user is successfully created the users additionalinformation is written to firebase database.
        //User is then sent to RequestListActivy page.
        btn_register.setOnClickListener{
            when{
                TextUtils.isEmpty(edit_text_register_email.text.toString().trim { it <= ' '}) ->{
                    Toast.makeText(this,"An email address must be entered.", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(edit_text_register_password.text.toString().trim {it <= ' '}) ->{
                    Toast.makeText(this, "A password must be entered.", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(edit_text_register_confirm_password.text.toString().trim {it <= ' '}) ->{
                    Toast.makeText(this, "A confirmation password must be entered.", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(edit_text_register_first_name.text.toString().trim {it <= ' '}) || !isLetters(edit_text_register_first_name.text.toString()) -> {
                    if(!isLetters(edit_text_register_first_name.text.toString())){
                        Toast.makeText(this, "First Name can only contain letters.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "First Name must be entered.", Toast.LENGTH_SHORT).show()
                    }
                }

                TextUtils.isEmpty(edit_text_register_last_name.text.toString().trim {it <= ' '}) || !isLetters(edit_text_register_last_name.text.toString()) ->{
                    if(!isLetters(edit_text_register_last_name.text.toString())){
                        Toast.makeText(this, "Last Name can only contain letters.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Last Name must be entered.", Toast.LENGTH_SHORT).show()
                    }
                }

                TextUtils.isEmpty(edit_text_register_phone.text.toString().trim {it <= ' '}) ->{
                    Toast.makeText(this, "Phone Number must be entered", Toast.LENGTH_SHORT).show()
                }

                !TextUtils.equals(edit_text_register_confirm_password.text, edit_text_register_password.text) ->{
                    Toast.makeText(this, "Password Must be the Same", Toast.LENGTH_SHORT).show()
                }

                else ->{
                    val email: String = edit_text_register_email.text.toString().trim()
                    val password: String = edit_text_register_password.text.toString().trim()

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                val firebaseUser: FirebaseUser = task.result!!.user!!
                                Toast.makeText(this, "Registration Successful.", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, RequestListActivity::class.java)
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

    //Used to validate first and last name fields.
    fun isLetters(string: String): Boolean {
        return string.all { it.isLetter() }
    }

    //Once fields have been validated and the account is successfully registered a user object with additionl fields is written to the database
    //using the id of the newly created user as the reference
    private fun writeToDatabase(){
        firstName = edit_text_register_first_name.text.toString().trim()
        lastName = edit_text_register_last_name.text.toString().trim()
        phoneNumber = edit_text_register_phone.text.toString().trim()
        userEmail = edit_text_register_email.text.toString().trim()

        var userModel = UserModel(firstName, lastName, phoneNumber, userEmail)

        database.child("users").child(customKey).setValue(userModel)
    }
}

