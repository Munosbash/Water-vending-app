package com.riara.watervending

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeliveryActivity : Activity() {

    private val db = FirebaseFirestore.getInstance()
    private val deliveryList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)

        val clientInput = findViewById<EditText>(R.id.deliveryClientInput)
        val bottlesInput = findViewById<EditText>(R.id.deliveryBottlesInput)
        val logButton = findViewById<Button>(R.id.logDeliveryButton)
        val listView = findViewById<ListView>(R.id.deliveryListView)
        val sizeGroup = findViewById<RadioGroup>(R.id.sizeGroup)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deliveryList)
        listView.adapter = adapter

        loadDeliveries()

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

            // Stop here if the phone has no internet
            if (!NetworkCheck.isOnline(this)) {
                Toast.makeText(this, "No internet connection. Please connect and try again", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Disable the button so it cannot be tapped again while saving
            logButton.isEnabled = false

            // Which size was chosen, and which stock field belongs to it
            val isTwoLitre = sizeGroup.checkedRadioButtonId == R.id.size2Radio
            val sizeField = if (isTwoLitre) "twoLitre" else "oneLitre"
            val sizeLabel = if (isTwoLitre) "2L" else "1L"

            val timeText = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

            // Step 1: read the current stock
            db.collection("inventory").document("stock").get()
                .addOnSuccessListener { stockDoc ->
                    val stock = stockDoc.getLong(sizeField) ?: 0L

                    // Step 2: refuse if there are not enough bottles
                    if (bottles > stock) {
                        Toast.makeText(this, "Not enough $sizeLabel stock. Available: $stock", Toast.LENGTH_LONG).show()
                        logButton.isEnabled = true
                        return@addOnSuccessListener
                    }

                    // Step 3: save the delivery
                    val delivery = hashMapOf(
                        "client" to clientName,
                        "bottles" to bottles,
                        "size" to sizeLabel,
                        "recordedBy" to Session.username,
                        "timeText" to timeText,
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("deliveries").add(delivery)
                        .addOnSuccessListener {
                            // Step 4: take the bottles off the stock
                            db.collection("inventory").document("stock")
                                .update(sizeField, FieldValue.increment(-bottles.toLong()))

                            clientInput.text.clear()
                            bottlesInput.text.clear()
                            Toast.makeText(this, "Delivery logged", Toast.LENGTH_SHORT).show()
                            logButton.isEnabled = true
                            loadDeliveries()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Could not save. Check your internet", Toast.LENGTH_LONG).show()
                            logButton.isEnabled = true
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Could not check stock. Check your internet", Toast.LENGTH_LONG).show()
                    logButton.isEnabled = true
                }
        }
    }

    // Reads the latest 100 deliveries, newest first
    private fun loadDeliveries() {
        db.collection("deliveries")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                deliveryList.clear()
                for (doc in result) {
                    val line = "${doc.getString("client")} - ${doc.getLong("bottles")} ${doc.getString("size") ?: ""} bottles - ${doc.getString("timeText")}"
                    deliveryList.add(line + " - by " + (doc.getString("recordedBy") ?: "unknown"))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load deliveries. Check your internet", Toast.LENGTH_LONG).show()
            }
    }
}