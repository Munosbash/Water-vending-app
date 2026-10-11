package com.riara.watervending

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView

class ManageStaffActivity : Activity() {

    private var selectedStaff: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_staff)

        val usernameInput = findViewById<EditText>(R.id.newStaffUsername)
        val passwordInput = findViewById<EditText>(R.id.newStaffPassword)
        val addButton = findViewById<Button>(R.id.addStaffButton)
        val message = findViewById<TextView>(R.id.staffMessage)
        val listView = findViewById<ListView>(R.id.staffListView)
        val selectedText = findViewById<TextView>(R.id.selectedStaffText)
        val changePasswordInput = findViewById<EditText>(R.id.changePasswordInput)
        val changeButton = findViewById<Button>(R.id.changePasswordButton)
        val deleteButton = findViewById<Button>(R.id.deleteStaffButton)

        val staffNames = UserStore.getStaffNames(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, staffNames)
        listView.adapter = adapter

        addButton.setOnClickListener {
            val username = usernameInput.text.toString().trim().lowercase()
            val password = passwordInput.text.toString()

            val usernameError = UserStore.checkUsername(this, username)
            if (usernameError != null) {
                message.text = usernameError
                return@setOnClickListener
            }

            val passwordError = UserStore.checkPassword(password)
            if (passwordError != null) {
                message.text = passwordError
                return@setOnClickListener
            }

            UserStore.addStaff(this, username, password)
            staffNames.add(username)
            adapter.notifyDataSetChanged()

            usernameInput.text.clear()
            passwordInput.text.clear()
            message.text = "Staff '$username' added"
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            selectedStaff = staffNames[position]
            selectedText.text = "Selected: ${staffNames[position]}"
        }

        changeButton.setOnClickListener {
            val staff = selectedStaff
            if (staff == null) {
                message.text = "Tap a staff name in the list first"
                return@setOnClickListener
            }

            val newPassword = changePasswordInput.text.toString()
            val passwordError = UserStore.checkPassword(newPassword)
            if (passwordError != null) {
                message.text = passwordError
                return@setOnClickListener
            }

            UserStore.changePassword(this, staff, newPassword)
            changePasswordInput.text.clear()
            message.text = "Password changed for '$staff'"
        }

        deleteButton.setOnClickListener {
            val staff = selectedStaff
            if (staff == null) {
                message.text = "Tap a staff name in the list first"
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Delete staff")
                .setMessage("Delete '$staff'? They will no longer be able to log in.")
                .setPositiveButton("Delete") { _, _ ->
                    UserStore.deleteStaff(this, staff)
                    staffNames.remove(staff)
                    adapter.notifyDataSetChanged()
                    selectedStaff = null
                    selectedText.text = "Selected: none"
                    message.text = "Staff '$staff' deleted"
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}