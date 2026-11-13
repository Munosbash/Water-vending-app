package com.riara.watervending

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportsActivity : Activity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var reportText: TextView
    private var reportString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        reportText = findViewById(R.id.reportText)
        val refreshButton = findViewById<Button>(R.id.refreshReportButton)
        val shareButton = findViewById<Button>(R.id.shareReportButton)

        refreshButton.setOnClickListener { buildReport() }

        // Sends the report as plain text through WhatsApp, email, etc.
        shareButton.setOnClickListener {
            if (reportString.isEmpty()) {
                Toast.makeText(this, "The report is not ready yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Water Vending Station Report")
            intent.putExtra(Intent.EXTRA_TEXT, reportString)
            startActivity(Intent.createChooser(intent, "Share report"))
        }

        buildReport()
    }

    // Reads the four sets of data one after the other, then makes the report
    private fun buildReport() {
        if (!NetworkCheck.isOnline(this)) {
            reportText.text = "No internet connection. Please connect and try again"
            return
        }
        reportText.text = "Loading report..."

        db.collection("clients").get().addOnSuccessListener { clients ->
            db.collection("deliveries").get().addOnSuccessListener { deliveries ->
                db.collection("inventory_log").get().addOnSuccessListener { logs ->
                    db.collection("inventory").document("stock").get().addOnSuccessListener { stockDoc ->
                        makeReport(clients.size(), deliveries, logs, stockDoc)
                    }.addOnFailureListener { showError() }
                }.addOnFailureListener { showError() }
            }.addOnFailureListener { showError() }
        }.addOnFailureListener { showError() }
    }

    private fun showError() {
        reportText.text = "Could not load the report. Check your internet"
    }

    // List of 4 numbers: deliveries, bottles, 1L bottles, 2L bottles
    private fun addDelivery(totals: IntArray, bottles: Int, size: String?) {
        totals[0] += 1
        totals[1] += bottles
        if (size == "1L") totals[2] += bottles
        if (size == "2L") totals[3] += bottles
    }

    // List of 2 numbers: 1L bottles, 2L bottles
    private fun addToPair(pair: IntArray, qty: Int, size: String) {
        if (size == "1L") pair[0] += qty else pair[1] += qty
    }

    private fun deliveryLine(t: IntArray): String {
        return "${t[0]} deliveries, ${t[1]} bottles (1L: ${t[2]}, 2L: ${t[3]})"
    }

    private fun makeReport(clientCount: Int, deliveries: QuerySnapshot, logs: QuerySnapshot, stockDoc: DocumentSnapshot) {
        // Start of today and start of this month, in milliseconds
        val day = Calendar.getInstance()
        day.set(Calendar.HOUR_OF_DAY, 0)
        day.set(Calendar.MINUTE, 0)
        day.set(Calendar.SECOND, 0)
        day.set(Calendar.MILLISECOND, 0)
        val todayStart = day.timeInMillis
        day.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = day.timeInMillis

        val today = IntArray(4)
        val month = IntArray(4)
        val allTime = IntArray(4)

        for (doc in deliveries) {
            val created = doc.getLong("createdAt") ?: 0L
            val bottles = (doc.getLong("bottles") ?: 0L).toInt()
            val size = doc.getString("size")
            addDelivery(allTime, bottles, size)
            if (created >= monthStart) addDelivery(month, bottles, size)
            if (created >= todayStart) addDelivery(today, bottles, size)
        }

        // The stock log is text, so we pick the numbers out of each message
        val added = IntArray(2)
        val used = IntArray(2)
        val addedPattern = Regex("Added (\\d+) x (\\dL)")
        val usedPattern = Regex("Used/Spilled (\\d+) x (\\dL)")

        for (doc in logs) {
            val created = doc.getLong("createdAt") ?: 0L
            if (created < monthStart) continue
            val message = doc.getString("message") ?: ""

            val addedMatch = addedPattern.find(message)
            if (addedMatch != null) {
                addToPair(added, addedMatch.groupValues[1].toInt(), addedMatch.groupValues[2])
            }
            val usedMatch = usedPattern.find(message)
            if (usedMatch != null) {
                addToPair(used, usedMatch.groupValues[1].toInt(), usedMatch.groupValues[2])
            }
        }

        val stockOne = stockDoc.getLong("oneLitre") ?: 0L
        val stockTwo = stockDoc.getLong("twoLitre") ?: 0L
        val stamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        reportString = "WATER VENDING STATION REPORT\n" +
            "Created: $stamp\n" +
            "By: ${Session.username}\n\n" +
            "CLIENTS\n" +
            "Total clients: $clientCount\n\n" +
            "STOCK NOW\n" +
            "1L bottles: $stockOne\n" +
            "2L bottles: $stockTwo\n\n" +
            "DELIVERIES\n" +
            "Today: " + deliveryLine(today) + "\n" +
            "This month ($monthName): " + deliveryLine(month) + "\n" +
            "All time: " + deliveryLine(allTime) + "\n\n" +
            "STOCK MOVEMENT THIS MONTH\n" +
            "Bottles added: 1L ${added[0]}, 2L ${added[1]}\n" +
            "Used or spilled: 1L ${used[0]}, 2L ${used[1]}\n"

        reportText.text = reportString
    }
}
