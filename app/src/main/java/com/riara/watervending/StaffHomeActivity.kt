package com.riara.watervending

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class StaffHomeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_home)

        val clientsButton = findViewById<Button>(R.id.staffClientsButton)
        val inventoryButton = findViewById<Button>(R.id.staffInventoryButton)
        val deliveryButton = findViewById<Button>(R.id.staffDeliveryButton)
        val logoutButton = findViewById<Button>(R.id.staffLogoutButton)

        clientsButton.setOnClickListener {
            startActivity(Intent(this, ClientsActivity::class.java))
        }

        inventoryButton.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        deliveryButton.setOnClickListener {
            startActivity(Intent(this, DeliveryActivity::class.java))
        }

        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}