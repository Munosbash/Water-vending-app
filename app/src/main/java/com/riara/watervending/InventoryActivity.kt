package com.riara.watervending

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class InventoryActivity : Activity() {

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

        addOneLitreButton.setOnClickListener {
            val qty = getQuantity()
            if (qty != null) {
                stockOneLitre += qty
                updateStockDisplay()
                addLog("Added $qty x 1L bottles to stock")
            }
        }

        addTwoLitreButton.setOnClickListener {
            val qty = getQuantity()
            if (qty != null) {
                stockTwoLitre += qty
                updateStockDisplay()
                addLog("Added $qty x 2L bottles to stock")
            }
        }

        removeOneLitreButton.setOnClickListener {
            val qty = getQuantity()
            if (qty != null) {
                if (qty > stockOneLitre) {
                    Toast.makeText(this, "Not enough 1L stock available", Toast.LENGTH_SHORT).show()
                } else {
                    stockOneLitre -= qty
                    updateStockDisplay()
                    addLog("Used/Spilled $qty x 1L bottles")
                }
            }
        }

        removeTwoLitreButton.setOnClickListener {
            val qty = getQuantity()
            if (qty != null) {
                if (qty > stockTwoLitre) {
                    Toast.makeText(this, "Not enough 2L stock available", Toast.LENGTH_SHORT).show()
                } else {
                    stockTwoLitre -= qty
                    updateStockDisplay()
                    addLog("Used/Spilled $qty x 2L bottles")
                }
            }
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
        logList.add(0, message)
        logAdapter.notifyDataSetChanged()
    }
}