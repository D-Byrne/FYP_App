package org.wit.fyp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        text_view_register.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btn_login.setOnClickListener{
            when{
                TextUtils.isEmpty(edit_text_login_email.text.toString().trim { it <= ' '}) ->{
                    Toast.makeText(this,"An email address must be entered.", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(edit_text_login_password.text.toString().trim {it <= ' '}) ->{
                    Toast.makeText(this, "A password must be entered.", Toast.LENGTH_SHORT).show()
                }

                else ->{
                    val email: String = edit_text_login_email.text.toString().trim()
                    val password: String = edit_text_login_password.text.toString().trim()

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, RequestListActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("userID", FirebaseAuth.getInstance().currentUser!!.uid)
                                intent.putExtra("emailStr", FirebaseAuth.getInstance().currentUser!!.email)
                                startActivity(intent)
                                finish()
                            } else{
                                Toast.makeText(this,task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }

        }
    }
}