package com.riara.watervending

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast

class ClientsActivity : Activity() {

    private val clientList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clients)

        val nameInput = findViewById<EditText>(R.id.clientNameInput)
        val locationInput = findViewById<EditText>(R.id.clientLocationInput)
        val typeInput = findViewById<EditText>(R.id.clientTypeInput)
        val contactInput = findViewById<EditText>(R.id.clientContactInput)
        val addButton = findViewById<Button>(R.id.addClientButton)
        val listView = findViewById<ListView>(R.id.clientListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clientList)
        listView.adapter = adapter

        addButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val type = typeInput.text.toString().trim()
            val contact = contactInput.text.toString().trim()

            if (name.isEmpty() || location.isEmpty() || type.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val entry = "$name | $type | $location | $contact"
            clientList.add(0, entry)
            adapter.notifyDataSetChanged()

            nameInput.text.clear()
            locationInput.text.clear()
            typeInput.text.clear()
            contactInput.text.clear()

            Toast.makeText(this, "Client added", Toast.LENGTH_SHORT).show()
        }
    }
}