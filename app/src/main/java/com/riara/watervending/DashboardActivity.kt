package com.riara.watervending

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class DashboardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val viewClientsButton = findViewById<Button>(R.id.viewClientsButton)
        val viewInventoryButton = findViewById<Button>(R.id.viewInventoryButton)
        val viewDeliveriesButton = findViewById<Button>(R.id.viewDeliveriesButton)
        val reportsButton = findViewById<Button>(R.id.reportsButton)
        val manageStaffButton = findViewById<Button>(R.id.manageStaffButton)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        viewClientsButton.setOnClickListener {
            startActivity(Intent(this, ClientsActivity::class.java))
        }

        viewInventoryButton.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        viewDeliveriesButton.setOnClickListener {
            startActivity(Intent(this, DeliveryActivity::class.java))
        }

        reportsButton.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        manageStaffButton.setOnClickListener {
            startActivity(Intent(this, ManageStaffActivity::class.java))
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ChangeOwnerPasswordActivity::class.java))
        }

        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
