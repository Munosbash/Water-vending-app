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

            // Stop here if the phone has no internet
            if (!NetworkCheck.isOnline(this)) {
                message.text = "No internet connection. Please connect and try again"
                return@setOnClickListener
            }

            message.text = "Saving..."
            addButton.isEnabled = false
            UserStore.addStaff(
                username,
                password,
                {
                    usernameInput.text.clear()
                    passwordInput.text.clear()
                    message.text = "Staff '$username' added"
                    addButton.isEnabled = true
                    loadStaff(message)
                },
                { error ->
                    message.text = error
                    addButton.isEnabled = true
                }
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

            // Stop here if the phone has no internet
            if (!NetworkCheck.isOnline(this)) {
                message.text = "No internet connection. Please connect and try again"
                return@setOnClickListener
            }

            changeButton.isEnabled = false
            UserStore.changePassword(
                staff,
                newPassword,
                {
                    changePasswordInput.text.clear()
                    message.text = "Password changed for '$staff'"
                    changeButton.isEnabled = true
                },
                { error ->
                    message.text = error
                    changeButton.isEnabled = true
                }
            )
        }

        deleteButton.setOnClickListener {
            val staff = selectedStaff
            if (staff == null) {
                message.text = "Tap a staff name in the list first"
                return@setOnClickListener
            }

            // Stop here if the phone has no internet
            if (!NetworkCheck.isOnline(this)) {
                message.text = "No internet connection. Please connect and try again"
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setTitle("Delete staff")
                .setMessage("Delete '$staff'? They will no longer be able to log in.")
                .setPositiveButton("Delete") { _, _ ->
                    deleteButton.isEnabled = false
                    UserStore.deleteStaff(
                        staff,
                        {
                            selectedStaff = null
                            selectedText.text = "Selected: none"
                            message.text = "Staff '$staff' deleted"
                            deleteButton.isEnabled = true
                            loadStaff(message)
                        },
                        { error ->
                            message.text = error
                            deleteButton.isEnabled = true
                        }
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
