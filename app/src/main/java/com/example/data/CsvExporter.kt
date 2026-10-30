package com.example.data

import android.content.Context
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private fun escapeCsv(value: String?): String {
        if (value == null) return "\"\""
        var clean = value.trim()
        // Prevent CSV Formula Injection (DDE / spreadsheet formula execution in Excel & Sheets)
        if (clean.isNotEmpty()) {
            val firstChar = clean[0]
            val isFormulaTrigger = firstChar == '=' || firstChar == '+' || firstChar == '@' ||
                firstChar == '\t' || firstChar == '\r' ||
                (firstChar == '-' && clean.toDoubleOrNull() == null)
            if (isFormulaTrigger) {
                clean = "'$clean"
            }
        }
        // Escape existing double quotes and wrap in quotes
        clean = clean.replace("\"", "\"\"")
        return "\"$clean\""
    }

    private fun formatLineItems(itemsJson: String): Pair<Int, String> {
        if (itemsJson.isBlank() || itemsJson == "[]") return Pair(0, "")
        return try {
            val array = JSONArray(itemsJson)
            val itemsCount = array.length()
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val desc = obj.optString("desc", "Item")
                val qty = obj.optInt("qty", 1)
                val price = obj.optDouble("price", 0.0)
                list.add("${qty}x $desc (R${String.format(Locale.US, "%.2f", price)})")
            }
            Pair(itemsCount, list.joinToString("; "))
        } catch (e: Exception) {
            Pair(0, "")
        }
    }

    fun generateCsv(documents: List<DocumentEntity>): String {
        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel & Google Sheets compatibility
        sb.append("\uFEFF")

        // CSV Header
        val headers = listOf(
            "Document Type",
            "Document Number",
            "Status",
            "Issue Date",
            "Client Name",
            "Client Phone",
            "Client Email",
            "Client Account",
            "Client Address",
            "Currency",
            "Subtotal",
            "Discount Amount",
            "VAT Rate (%)",
            "VAT Amount",
            "Grand Total",
            "Items Count",
            "Items Summary",
            "Terms & Notes",
            "Created Date"
        )
        sb.append(headers.joinToString(","))
        sb.append("\r\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        for (doc in documents) {
            val (itemsCount, itemsDetail) = formatLineItems(doc.itemsJson)
            val createdStr = dateFormat.format(Date(doc.createdAt))

            val row = listOf(
                escapeCsv(doc.docType),
                escapeCsv(doc.docNumber),
                escapeCsv(if (doc.stamp.isNotBlank()) doc.stamp else "ISSUED"),
                escapeCsv(doc.docDate),
                escapeCsv(doc.clientName),
                escapeCsv(doc.clientCell),
                escapeCsv(doc.clientEmail),
                escapeCsv(doc.accountNo),
                escapeCsv(doc.clientAddress),
                escapeCsv(doc.currency),
                escapeCsv(String.format(Locale.US, "%.2f", doc.subtotal)),
                escapeCsv(String.format(Locale.US, "%.2f", doc.totalDiscount)),
                escapeCsv(if (doc.vatEnabled) String.format(Locale.US, "%.1f", doc.vatRate) else "0.0"),
                escapeCsv(String.format(Locale.US, "%.2f", doc.totalVat)),
                escapeCsv(String.format(Locale.US, "%.2f", doc.grandTotal)),
                escapeCsv(itemsCount.toString()),
                escapeCsv(itemsDetail),
                escapeCsv(doc.terms),
                escapeCsv(createdStr)
            )
            sb.append(row.joinToString(","))
            sb.append("\r\n")
        }

        return sb.toString()
    }

    fun exportCsvToFile(context: Context, documents: List<DocumentEntity>): File {
        val csvContent = generateCsv(documents)
        val fileDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "EAS_Jobs_Report_$fileDate.csv"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { fos ->
            fos.write(csvContent.toByteArray(Charsets.UTF_8))
            fos.flush()
        }
        return file
    }
}
