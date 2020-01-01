package com.android.contactnumberprefixtchanger

import android.content.ContentValues
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {


    lateinit var progressBar: ProgressBar
    lateinit var updateButton: Button
    val permissions = arrayOf(android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar = findViewById<ProgressBar>(R.id.progressUpdate)
        updateButton = findViewById(R.id.updateButton)
        updateButton.setOnClickListener {  if (!checkPermissions()) askForPermissions() else  getContacts() }

    }


    fun getContacts() {
        var progress = 0
        var oneProgressValue: Int
        progressBar.setProgress(progress, true)
       val cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, arrayOf(
           ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME), "data1 like ?", arrayOf("8%"), null)
        if (cursor?.count!! == 0){
            Toast.makeText(this, "NO NUMBERS TO UPDATE", Toast.LENGTH_LONG).show()
            return
        }
        oneProgressValue = 100 / cursor?.count!!
        while (cursor?.moveToNext()!!) {
            val name = cursor.getString(1)
            val number = cursor.getString(0)
//            var newNumber = number.apply {
//                replace("(", "")
//                replace(")", "")
//                replace("-", "")
//               trimStart('8')
//          }.let { "+370$it" }

            val newNumber = number.replace("(", "").replace(")", "").replace("-", "").replace(" ", "").trimStart('8').let {
                "+370${it}"
            }



            updateContacts(number, newNumber)
            progress += oneProgressValue
            progressBar.setProgress(progress, true)
        }
//        contacts.text = cursor?.moveToFirst().let { cursor?.getString(0) }
        cursor.close()
        Toast.makeText(this, "UPDATED!!", Toast.LENGTH_LONG).show()

    }

    fun updateContacts(number: String, newNumber: String) {
        val contentValues = ContentValues().also { it.put(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber) }
        contentResolver.update(ContactsContract.Data.CONTENT_URI, contentValues, "data1 == ?", arrayOf(number))
    }

    fun askForPermissions() {
        ActivityCompat.requestPermissions(this, permissions, 101)
    }

    fun checkPermissions() : Boolean{
        val readPerm = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
        val writePerm = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS)
        return readPerm == PackageManager.PERMISSION_GRANTED || writePerm == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && checkPermissions()) getContacts()
    }
}
