package com.riara.watervending

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeliveryActivity : Activity() {

    private val deliveryList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)

        val clientInput = findViewById<EditText>(R.id.deliveryClientInput)
        val bottlesInput = findViewById<EditText>(R.id.deliveryBottlesInput)
        val logButton = findViewById<Button>(R.id.logDeliveryButton)
        val listView = findViewById<ListView>(R.id.deliveryListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deliveryList)
        listView.adapter = adapter

        logButton.setOnClickListener {
            val clientName = clientInput.text.toString().trim()
            val bottlesText = bottlesInput.text.toString().trim()

            if (clientName.isEmpty() || bottlesText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bottles = bottlesText.toIntOrNull()
            if (bottles == null || bottles <= 0) {
                Toast.makeText(this, "Please enter a valid number of bottles", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timestamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
            val entry = "$clientName - $bottles bottles - $timestamp"
            deliveryList.add(0, entry)
            adapter.notifyDataSetChanged()

            clientInput.text.clear()
            bottlesInput.text.clear()

            Toast.makeText(this, "Delivery logged", Toast.LENGTH_SHORT).show()
        }
    }
}