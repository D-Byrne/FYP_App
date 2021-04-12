package org.wit.fyp

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_request.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_request_list.*
import kotlinx.android.synthetic.main.activity_user_request_list.*
import org.jetbrains.anko.startActivityForResult
import org.wit.fyp.models.RequestModel
import java.util.*

class AddRequestActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var database: DatabaseReference
    var editRequest = RequestModel()

    var day = 0
    var month = 0
    var year = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0

    var username = ""

    var firstName: String = ""
    var lastName: String = ""

    var edit = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_request)

        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        toolbar_add.title = "Create Request"
        setSupportActionBar(toolbar_add)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

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

        if(intent.hasExtra("edit_request")) {
            edit = true

            toolbar_add.title = "Edit Request"

            btn_add_request.isVisible = false
            btn_edit_request.isVisible = true

            editRequest = intent.extras?.getParcelable<RequestModel>("edit_request")!!

            edit_text_add_request_title.setText(editRequest.requestTitle)
            edit_text_add_request_details.setText(editRequest.requestDetails)
            deadline_label.setText(editRequest.requestDeadline)
            spinner_select_location.setSelection(arrayAdapter.getPosition(editRequest.requestLocation))


        }

        btn_edit_request.setOnClickListener{
            val locationSpin = spinner_select_location.selectedItem.toString().trim()
            val checkList = countyNames.copyOfRange(2, 28)

            if( (edit_text_add_request_title.text.toString().trim().isNotEmpty()) && (edit_text_add_request_details.text.toString().trim().isNotEmpty()) && (deadline_label.text.toString().trim() != "DD - MM - YYYY") && ((checkList.contains(locationSpin) ) || (checkList.contains(locationSpin) ))){
                if(editRequest.requestCompleted != true) {
                    updateRequest()
                    startActivity(Intent(this, UserRequestList::class.java))
                    finish()
                }else{
                    Toast.makeText(this, "Can't edit completed requests.", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "All fields must be entered or selected.", Toast.LENGTH_SHORT).show()
            }
        }

        btn_add_request.setOnClickListener{
            val locationSpin = spinner_select_location.selectedItem.toString().trim()
            val checkList = countyNames.copyOfRange(2, 28)

            if( (edit_text_add_request_title.text.toString().trim().isNotEmpty()) && (edit_text_add_request_details.text.toString().trim().isNotEmpty()) && (deadline_label.text.toString().trim() != "DD - MM - YYYY") && ((checkList.contains(locationSpin) ) || (checkList.contains(locationSpin) ))){
                writeToDatabase()
                startActivity(Intent(this, RequestListActivity::class.java))
                finish()
            }else{
                Toast.makeText(this, "All fields must be entered or selected.", Toast.LENGTH_SHORT).show()
            }
        }

        pickDate()


        getData()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_top_add_request, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            android.R.id.home -> { finish() }

        }
        return super.onOptionsItemSelected(item)
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

    fun updateRequest(){

        var newTitle = edit_text_add_request_title.text.toString().trim()
        var newDetails = edit_text_add_request_details.text.toString().trim()
        var newDeadline = deadline_label.text.toString().trim()
        var newLocation = spinner_select_location.selectedItem.toString().trim()

        database.child("requests").child(editRequest.reqId!!).child("requestTitle").setValue(newTitle)
        database.child("requests").child(editRequest.reqId!!).child("requestDetails").setValue(newDetails)
        database.child("requests").child(editRequest.reqId!!).child("requestDeadline").setValue(newDeadline)
        database.child("requests").child(editRequest.reqId!!).child("requestLocation").setValue(newLocation)




    }

    fun writeToDatabase(){

        var title = edit_text_add_request_title.text.toString().trim()
        var details = edit_text_add_request_details.text.toString().trim()
        var deadline = deadline_label.text.toString().trim()
        var location = custom_spinner_item.text.toString().trim()
        val id = database.push().key

        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        var requestModel = RequestModel(userId, username!!,  title, details, deadline, location)

        database.child("requests").child(id!!).setValue(requestModel)
    }

    private fun getData(){
        database.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError){
                Log.e("cancel", error.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.exists()) {

                    firstName = dataSnapshot.child("firstName").getValue(String::class.java)!!
                    lastName = dataSnapshot.child("lastName").getValue(String::class.java)!!

                    username = firstName + " " + lastName

                } else{

                }
            }
        })
    }
}