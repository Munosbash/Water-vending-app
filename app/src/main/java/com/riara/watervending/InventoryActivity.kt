package com.riara.watervending

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryActivity : Activity() {

    private val db = FirebaseFirestore.getInstance()

    private var stockOneLitre = 0
    private var stockTwoLitre = 0

    // True while a save is in progress, so extra taps are ignored
    private var saving = false

    private val logList = mutableListOf<String>()
    private lateinit var logAdapter: ArrayAdapter<String>

    private lateinit var stockOneLitreText: TextView
    private lateinit var stockTwoLitreText: TextView
    private lateinit var quantityInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        stockOneLitreText = findViewById(R.id.stockOneLitreText)
        stockTwoLitreText = findViewById(R.id.stockTwoLitreText)
        quantityInput = findViewById(R.id.quantityInput)

        val addOneLitreButton = findViewById<Button>(R.id.addOneLitreButton)
        val addTwoLitreButton = findViewById<Button>(R.id.addTwoLitreButton)
        val removeOneLitreButton = findViewById<Button>(R.id.removeOneLitreButton)
        val removeTwoLitreButton = findViewById<Button>(R.id.removeTwoLitreButton)
        val logView = findViewById<ListView>(R.id.inventoryLogView)

        logAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logList)
        logView.adapter = logAdapter

        loadStock()
        loadLog()

        addOneLitreButton.setOnClickListener { changeStock(true, true) }
        addTwoLitreButton.setOnClickListener { changeStock(false, true) }
        removeOneLitreButton.setOnClickListener { changeStock(true, false) }
        removeTwoLitreButton.setOnClickListener { changeStock(false, false) }
    }

    // Reads the saved stock counts from the database
    private fun loadStock() {
        db.collection("inventory").document("stock").get()
            .addOnSuccessListener { doc ->
                stockOneLitre = (doc.getLong("oneLitre") ?: 0L).toInt()
                stockTwoLitre = (doc.getLong("twoLitre") ?: 0L).toInt()
                updateStockDisplay()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load stock. Check your internet", Toast.LENGTH_LONG).show()
            }
    }

    // Reads the latest 50 log entries, newest first
    private fun loadLog() {
        db.collection("inventory_log")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { result ->
                logList.clear()
                for (doc in result) {
                    logList.add(doc.getString("message") ?: "")
                }
                logAdapter.notifyDataSetChanged()
            }
    }

    // One function handles all four buttons
    private fun changeStock(oneLitre: Boolean, adding: Boolean) {
        // Ignore taps while the last save is still running
        if (saving) return

        // Stop here if the phone has no internet
        if (!NetworkCheck.isOnline(this)) {
            Toast.makeText(this, "No internet connection. Please connect and try again", Toast.LENGTH_LONG).show()
            return
        }

        val qty = getQuantity() ?: return
        val size = if (oneLitre) "1L" else "2L"
        val current = if (oneLitre) stockOneLitre else stockTwoLitre

        if (!adding && qty > current) {
            Toast.makeText(this, "Not enough $size stock available", Toast.LENGTH_SHORT).show()
            return
        }

        val newValue = if (adding) current + qty else current - qty
        val field = if (oneLitre) "oneLitre" else "twoLitre"

        saving = true

        // Save first. The screen and the log change only if the save works.
        db.collection("inventory").document("stock")
            .set(hashMapOf(field to newValue), SetOptions.merge())
            .addOnSuccessListener {
                if (oneLitre) stockOneLitre = newValue else stockTwoLitre = newValue
                updateStockDisplay()

                if (adding) {
                    addLog("Added $qty x $size bottles to stock")
                } else {
                    addLog("Used/Spilled $qty x $size bottles")
                }
                saving = false
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not save stock. Check your internet", Toast.LENGTH_LONG).show()
                saving = false
            }
    }

    private fun getQuantity(): Int? {
        val text = quantityInput.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter a quantity", Toast.LENGTH_SHORT).show()
            return null
        }
        val qty = text.toIntOrNull()
        if (qty == null || qty <= 0) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
            return null
        }
        quantityInput.text.clear()
        return qty
    }

    private fun updateStockDisplay() {
        stockOneLitreText.text = "1L Bottles in stock: $stockOneLitre"
        stockTwoLitreText.text = "2L Bottles in stock: $stockTwoLitre"
    }

    private fun addLog(text: String) {
        val time = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        val message = "$time - $text by ${Session.username}"
        val entry = hashMapOf(
            "message" to message,
            "recordedBy" to Session.username,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("inventory_log").add(entry)
        logList.add(0, message)
        logAdapter.notifyDataSetChanged()
    }
}
