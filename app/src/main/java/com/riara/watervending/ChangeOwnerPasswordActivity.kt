package com.riara.watervending

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class ChangeOwnerPasswordActivity : Activity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_owner_password)

        val currentInput = findViewById<EditText>(R.id.currentPasswordInput)
        val newInput = findViewById<EditText>(R.id.newPasswordInput)
        val confirmInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val saveButton = findViewById<Button>(R.id.savePasswordButton)
        val message = findViewById<TextView>(R.id.passwordMessage)

        saveButton.setOnClickListener {
            val current = currentInput.text.toString().trim()
            val newPassword = newInput.text.toString().trim()
            val confirm = confirmInput.text.toString().trim()

            if (current.isEmpty() || newPassword.isEmpty() || confirm.isEmpty()) {
                message.text = "Please fill in all fields"
                return@setOnClickListener
            }

            // Same password rules as for staff
            val passwordError = UserStore.checkPassword(newPassword)
            if (passwordError != null) {
                message.text = passwordError
                return@setOnClickListener
            }

            if (newPassword != confirm) {
                message.text = "The new passwords do not match"
                return@setOnClickListener
            }

            if (!NetworkCheck.isOnline(this)) {
                message.text = "No internet connection. Please connect and try again"
                return@setOnClickListener
            }

            message.text = "Saving..."
            saveButton.isEnabled = false

            val ownerDoc = db.collection("settings").document("owner")

            // Step 1: check that the current password is correct
            ownerDoc.get()
                .addOnSuccessListener { doc ->
                    val stored = doc.getString("password") ?: "1234"
                    if (current != stored) {
                        message.text = "The current password is wrong"
                        saveButton.isEnabled = true
                        return@addOnSuccessListener
                    }

                    // Step 2: save the new password
                    ownerDoc.set(hashMapOf("password" to newPassword))
                        .addOnSuccessListener {
                            currentInput.text.clear()
                            newInput.text.clear()
                            confirmInput.text.clear()
                            message.text = "Password changed. Use the new one next time you log in"
                            saveButton.isEnabled = true
                        }
                        .addOnFailureListener {
                            message.text = "Could not save. Check your internet"
                            saveButton.isEnabled = true
                        }
                }
                .addOnFailureListener {
                    message.text = "Could not check the password. Check your internet"
                    saveButton.isEnabled = true
                }
        }
    }
}
