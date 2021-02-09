package org.wit.fyp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userId = intent.getStringExtra("userID")
        val emailStr = intent.getStringExtra("emailStr")

        text_view_user_id_lo.text = "UserID: $userId"
        text_view_email_lo.text = "Email: $emailStr"

        btn_logout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }




    }
}