package com.riara.watervending

import android.content.Context
import android.content.SharedPreferences

object UserStore {

    private const val PREFS = "users_prefs"
    private const val KEY_STAFF_LIST = "staff_list"

    // Opens the phone's saved data. First time only, it creates one starter staff account.
    private fun prefs(context: Context): SharedPreferences {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!p.contains(KEY_STAFF_LIST)) {
            p.edit()
                .putString(KEY_STAFF_LIST, "staff")
                .putString("pass_staff", "1234")
                .apply()
        }
        return p
    }

    fun getStaffNames(context: Context): MutableList<String> {
        val saved = prefs(context).getString(KEY_STAFF_LIST, "") ?: ""
        if (saved.isEmpty()) return mutableListOf()
        return saved.split(",").toMutableList()
    }

    fun checkUsername(context: Context, username: String): String? {
        if (username.length < 4) return "Username must be at least 4 characters"
        if (!username.all { it.isLetterOrDigit() || it == '_' }) {
            return "Username can only have letters, numbers and underscore"
        }
        if (!username.any { it.isLetter() }) {
            return "Username must contain a letter (it cannot be only numbers)"
        }
        if (username == "owner") return "That username is reserved"
        if (getStaffNames(context).contains(username)) return "That username already exists"
        return null
    }

    fun checkPassword(password: String): String? {
        if (password.length < 6) return "Password must be at least 6 characters"
        if (password.contains(" ")) return "Password cannot contain spaces"
        if (!password.any { it.isLetter() }) return "Password must contain at least one letter"
        if (!password.any { it.isDigit() }) return "Password must contain at least one number"
        return null
    }

    fun addStaff(context: Context, username: String, password: String) {
        val names = getStaffNames(context)
        names.add(username)
        prefs(context).edit()
            .putString(KEY_STAFF_LIST, names.joinToString(","))
            .putString("pass_$username", password)
            .apply()
    }

    fun changePassword(context: Context, username: String, newPassword: String) {
        prefs(context).edit().putString("pass_$username", newPassword).apply()
    }

        fun deleteStaff(context: Context, username: String) {
        val names = getStaffNames(context)
        names.remove(username)
        prefs(context).edit()
            .putString(KEY_STAFF_LIST, names.joinToString(","))
            .remove("pass_$username")
            .apply()
    }

    fun checkStaffLogin(context: Context, username: String, password: String): Boolean {
        val saved = prefs(context).getString("pass_$username", null)
        return saved != null && saved == password
    }
}