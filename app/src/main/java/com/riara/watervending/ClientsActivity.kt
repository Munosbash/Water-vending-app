package com.riara.watervending

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ClientsActivity : Activity() {

    private val clientList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private val db = FirebaseFirestore.getInstance()

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

        // Show the clients already saved in the database
        loadClients()

        addButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val type = typeInput.text.toString().trim()
            val contact = contactInput.text.toString().trim()

            if (name.isEmpty() || location.isEmpty() || type.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // One client = one record with these fields
            val client = hashMapOf(
                "name" to name,
                "location" to location,
                "type" to type,
                "contact" to contact,
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("clients").add(client)
                .addOnSuccessListener {
                    nameInput.text.clear()
                    locationInput.text.clear()
                    typeInput.text.clear()
                    contactInput.text.clear()
                    Toast.makeText(this, "Client saved", Toast.LENGTH_SHORT).show()
                    loadClients()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Could not save. Check your internet", Toast.LENGTH_LONG).show()
                }
        }

        // The "?" means: only do this if the button is on the screen
        val logoutButton: Button? = findViewById(R.id.logoutButton)
        logoutButton?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    // Reads all clients from the database, newest first
    private fun loadClients() {
        db.collection("clients")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                clientList.clear()
                for (doc in result) {
                    val line = "${doc.getString("name")} | ${doc.getString("type")} | " +
                        "${doc.getString("location")} | ${doc.getString("contact")}"
                    clientList.add(line)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load clients. Check your internet", Toast.LENGTH_LONG).show()
            }
    }
}