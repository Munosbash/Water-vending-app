package com.riara.watervending

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val errorText = findViewById<TextView>(R.id.errorText)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                errorText.text = "Please enter both username and password"
                return@setOnClickListener
            }

            // The owner password is kept in the cloud database.
            // Until the owner changes it, the starting password 1234 is used.
            if (username == "owner") {
                errorText.text = "Checking..."
                FirebaseFirestore.getInstance().collection("settings").document("owner").get()
                    .addOnSuccessListener { doc ->
                        val ownerPassword = doc.getString("password") ?: "1234"
                        if (password == ownerPassword) {
                            errorText.text = ""
                            Session.username = "owner"
                            startActivity(Intent(this, DashboardActivity::class.java))
                        } else {
                            errorText.text = "Invalid username or password"
                        }
                    }
                    .addOnFailureListener {
                        errorText.text = "Could not connect. Check your internet"
                    }
                return@setOnClickListener
            }

            // Staff accounts are checked in the cloud database
            errorText.text = "Checking..."
            UserStore.checkStaffLogin(
                username.lowercase(),
                password,
                { ok ->
                    if (ok) {
                        errorText.text = ""
                        Session.username = username.lowercase()
                        startActivity(Intent(this, StaffHomeActivity::class.java))
                    } else {
                        errorText.text = "Invalid username or password"
                    }
                },
                { errorText.text = "Could not connect. Check your internet" }
            )
        }
    }
}
