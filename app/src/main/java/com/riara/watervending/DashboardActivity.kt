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

                val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        
        viewClientsButton.setOnClickListener {
            startActivity(Intent(this, ClientsActivity::class.java))
        }

        viewInventoryButton.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        viewDeliveriesButton.setOnClickListener {
            startActivity(Intent(this, DeliveryActivity::class.java))
        }
    }
}