package org.wit.fyp

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_request.*
import org.wit.fyp.models.RequestModel
import java.util.*

class AddRequestActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var database: DatabaseReference

    var day = 0
    var month = 0
    var year = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0

    var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_request)

        val countyNames = arrayOf("Request Location", "---------------------","Carlow", "Cavan", "Clare", "Cork", "Donegal", "Dublin", "Galway", "Kerry", "Kildare", "Kilkenny",
                                                                              "Laois", "Leitrim", "Limerick", "Longford", "Louth", "Mayo", "Meath", "Monaghan", "Offaly", "Roscommon",
                                                                              "Sligo", "Tipperary", "Waterford", "Westmeath", "Wexford", "Wicklow")

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countyNames)

        spinner_select_location.adapter = arrayAdapter

        spinner_select_location.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
               custom_spinner_item.text = countyNames[p2]

            }
        }

        database = Firebase.database.reference

        btn_add_request.setOnClickListener{
            if( (edit_text_add_request_title.text.toString().trim().isNotEmpty()) && (edit_text_add_request_details.text.toString().trim().isNotEmpty()) && (deadline_label.text.toString().trim() != "DD - MM - YYYY") && ((custom_spinner_item.text.toString().trim() != "Request Location") || (custom_spinner_item.text.toString().trim() != "---------------------"))){
                writeToDatabase()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                Toast.makeText(this, "All fields must be entered or selected.", Toast.LENGTH_SHORT).show()
            }
        }

        pickDate()

    }

    private fun getDateCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    private fun pickDate() {

        btn_add_request_deadline.setOnClickListener {
            getDateCalendar()

            DatePickerDialog(this, this, year, month, day).show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1
        savedYear = year

        getDateCalendar()
        deadline_label.text = "$savedDay/$savedMonth/$savedYear"

    }

    fun writeToDatabase(){

        var title = edit_text_add_request_title.text.toString().trim()
        var details = edit_text_add_request_details.text.toString().trim()
        var deadline = deadline_label.text.toString().trim()
        var location = custom_spinner_item.text.toString().trim()
        val id = database.push().key

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        var  username = intent.getStringExtra("username")

        var requestModel = RequestModel(userId, username!!,  title, details, deadline, location)

        database.child("requests").child(id!!).setValue(requestModel)
    }
}