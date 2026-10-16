package com.riara.watervending

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

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

            // The owner account is fixed
            if (username == "owner" && password == "1234") {
                errorText.text = ""
                startActivity(Intent(this, DashboardActivity::class.java))
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
