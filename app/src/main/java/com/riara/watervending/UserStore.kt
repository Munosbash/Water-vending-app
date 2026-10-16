package com.riara.watervending

import com.google.firebase.firestore.FirebaseFirestore

// Each staff account is one record in the "staff" collection.
// The record name is the username, and it holds the password.
object UserStore {

    private val db = FirebaseFirestore.getInstance()

    // Checks the shape of a username. Returns an error message, or null if fine.
    fun checkUsernameFormat(username: String): String? {
        if (username.length < 4) return "Username must be at least 4 characters"
        if (!username.all { it.isLetterOrDigit() || it == '_' }) {
            return "Username can only have letters, numbers and underscore"
        }
        if (!username.any { it.isLetter() }) {
            return "Username must contain a letter (it cannot be only numbers)"
        }
        if (username == "owner") return "That username is reserved"
        return null
    }

    fun checkPassword(password: String): String? {
        if (password.length < 6) return "Password must be at least 6 characters"
        if (password.contains(" ")) return "Password cannot contain spaces"
        if (!password.any { it.isLetter() }) return "Password must contain at least one letter"
        if (!password.any { it.isDigit() }) return "Password must contain at least one number"
        return null
    }

    fun getStaffNames(onResult: (List<String>) -> Unit, onError: () -> Unit) {
        db.collection("staff").get()
            .addOnSuccessListener { result ->
                onResult(result.documents.map { it.id }.sorted())
            }
            .addOnFailureListener { onError() }
    }

    fun addStaff(username: String, password: String, onDone: () -> Unit, onError: (String) -> Unit) {
        val ref = db.collection("staff").document(username)
        ref.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onError("That username already exists")
                } else {
                    ref.set(hashMapOf("password" to password))
                        .addOnSuccessListener { onDone() }
                        .addOnFailureListener { onError("Could not save. Check your internet") }
                }
            }
            .addOnFailureListener { onError("Could not connect. Check your internet") }
    }

    fun changePassword(username: String, newPassword: String, onDone: () -> Unit, onError: (String) -> Unit) {
        db.collection("staff").document(username)
            .set(hashMapOf("password" to newPassword))
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { onError("Could not save. Check your internet") }
    }

    fun deleteStaff(username: String, onDone: () -> Unit, onError: (String) -> Unit) {
        db.collection("staff").document(username).delete()
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { onError("Could not delete. Check your internet") }
    }

    fun checkStaffLogin(username: String, password: String, onResult: (Boolean) -> Unit, onError: () -> Unit) {
        db.collection("staff").document(username).get()
            .addOnSuccessListener { doc ->
                onResult(doc.exists() && doc.getString("password") == password)
            }
            .addOnFailureListener { onError() }
    }
}
