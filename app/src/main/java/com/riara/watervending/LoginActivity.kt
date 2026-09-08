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

            if (username == "owner" && password == "1234") {
                errorText.text = "Login successful (Owner) - Dashboard coming in Part 6"
            } else if (username == "staff" && password == "1234") {
                errorText.text = "Login successful (Staff) - Dashboard coming in Part 3"
            } else {
                errorText.text = "Invalid username or password"
            }
        }
    }
}
