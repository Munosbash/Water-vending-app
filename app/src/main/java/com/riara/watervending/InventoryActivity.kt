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

class InventoryActivity : Activity() {

    private val db = FirebaseFirestore.getInstance()

    private var stockOneLitre = 0
    private var stockTwoLitre = 0

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
        val qty = getQuantity() ?: return
        val size = if (oneLitre) "1L" else "2L"
        val current = if (oneLitre) stockOneLitre else stockTwoLitre

        if (!adding && qty > current) {
            Toast.makeText(this, "Not enough $size stock available", Toast.LENGTH_SHORT).show()
            return
        }

        val newValue = if (adding) current + qty else current - qty
        if (oneLitre) stockOneLitre = newValue else stockTwoLitre = newValue

        updateStockDisplay()
        saveStock()

        if (adding) {
            addLog("Added $qty x $size bottles to stock")
        } else {
            addLog("Used/Spilled $qty x $size bottles")
        }
    }

    private fun saveStock() {
        val stock = hashMapOf(
            "oneLitre" to stockOneLitre,
            "twoLitre" to stockTwoLitre
        )
        db.collection("inventory").document("stock")
            .set(stock, SetOptions.merge())
            .addOnFailureListener {
                Toast.makeText(this, "Could not save stock. Check your internet", Toast.LENGTH_LONG).show()
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

    private fun addLog(message: String) {
        val entry = hashMapOf(
            "message" to message,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("inventory_log").add(entry)
        logList.add(0, message)
        logAdapter.notifyDataSetChanged()
    }
}
