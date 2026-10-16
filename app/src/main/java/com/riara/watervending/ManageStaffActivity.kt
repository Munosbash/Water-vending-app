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
    private val staffNames = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

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

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, staffNames)
        listView.adapter = adapter

        loadStaff(message)

        addButton.setOnClickListener {
            val username = usernameInput.text.toString().trim().lowercase()
            val password = passwordInput.text.toString()

            val usernameError = UserStore.checkUsernameFormat(username)
            if (usernameError != null) {
                message.text = usernameError
                return@setOnClickListener
            }

            val passwordError = UserStore.checkPassword(password)
            if (passwordError != null) {
                message.text = passwordError
                return@setOnClickListener
            }

            message.text = "Saving..."
            UserStore.addStaff(
                username,
                password,
                {
                    usernameInput.text.clear()
                    passwordInput.text.clear()
                    message.text = "Staff '$username' added"
                    loadStaff(message)
                },
                { error -> message.text = error }
            )
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

            UserStore.changePassword(
                staff,
                newPassword,
                {
                    changePasswordInput.text.clear()
                    message.text = "Password changed for '$staff'"
                },
                { error -> message.text = error }
            )
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
                    UserStore.deleteStaff(
                        staff,
                        {
                            selectedStaff = null
                            selectedText.text = "Selected: none"
                            message.text = "Staff '$staff' deleted"
                            loadStaff(message)
                        },
                        { error -> message.text = error }
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Reloads the list of staff names from the database
    private fun loadStaff(message: TextView) {
        UserStore.getStaffNames(
            { names ->
                staffNames.clear()
                staffNames.addAll(names)
                adapter.notifyDataSetChanged()
            },
            { message.text = "Could not load staff. Check your internet" }
        )
    }
}
