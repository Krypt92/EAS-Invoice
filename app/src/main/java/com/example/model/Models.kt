package com.example.model

import java.util.UUID

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val name: String
)

val CURRENCIES = listOf(
    CurrencyItem("ZAR", "R", "South African Rand (R)"),
    CurrencyItem("USD", "$", "US Dollar ($)"),
    CurrencyItem("EUR", "€", "Euro (€)"),
    CurrencyItem("GBP", "£", "British Pound (£)"),
    CurrencyItem("NGN", "₦", "Nigerian Naira (₦)"),
    CurrencyItem("KES", "KSh", "Kenyan Shilling (KSh)"),
    CurrencyItem("GHS", "GH₵", "Ghanaian Cedi (GH₵)"),
    CurrencyItem("BWP", "P", "Botswana Pula (P)"),
    CurrencyItem("NAD", "N$", "Namibian Dollar (N$)"),
    CurrencyItem("MZN", "MT", "Mozambican Metical (MT)"),
    CurrencyItem("ZMW", "ZK", "Zambian Kwacha (ZK)"),
    CurrencyItem("MWK", "MK", "Malawian Kwacha (MK)"),
    CurrencyItem("TZS", "TSh", "Tanzanian Shilling (TSh)"),
    CurrencyItem("UGX", "USh", "Ugandan Shilling (USh)"),
    CurrencyItem("INR", "₹", "Indian Rupee (₹)"),
    CurrencyItem("PKR", "₨", "Pakistani Rupee (₨)"),
    CurrencyItem("AED", "AED", "UAE Dirham (AED)"),
    CurrencyItem("AUD", "A$", "Australian Dollar (A$)"),
    CurrencyItem("CAD", "C$", "Canadian Dollar (C$)"),
    CurrencyItem("NZD", "NZ$", "New Zealand Dollar (NZ$)"),
    CurrencyItem("JPY", "¥", "Japanese Yen (¥)"),
    CurrencyItem("CNY", "CN¥", "Chinese Yuan (CN¥)"),
    CurrencyItem("CHF", "CHF", "Swiss Franc (CHF)")
)

data class LineItem(
    val id: String = UUID.randomUUID().toString(),
    val desc: String = "",
    val price: Double = 0.0,
    val qty: Int = 1,
    val discountPercent: Double = 0.0
) {
    val subtotal: Double
        get() = price * qty

    val discountAmount: Double
        get() = subtotal * (discountPercent / 100.0)

    val afterDiscount: Double
        get() = subtotal - discountAmount

    fun vatAmount(vatRate: Double, vatEnabled: Boolean): Double {
        return if (vatEnabled) afterDiscount * (vatRate / 100.0) else 0.0
    }

    fun totalAmount(vatRate: Double, vatEnabled: Boolean): Double {
        return afterDiscount + vatAmount(vatRate, vatEnabled)
    }
}

data class DocumentTotals(
    val subtotal: Double,
    val totalDiscount: Double,
    val totalVat: Double,
    val grandTotal: Double
)

enum class DocumentType(val title: String, val prefix: String) {
    TAX_INVOICE("Tax Invoice", "INV"),
    QUOTATION("Quotation", "Q"),
    PROFORMA_INVOICE("Proforma Invoice", "PI");

    companion object {
        fun fromString(str: String): DocumentType {
            return entries.firstOrNull { it.title.equals(str, ignoreCase = true) } ?: TAX_INVOICE
        }
    }
}

enum class DocumentStamp(val label: String, val badgeText: String, val colorHex: Long) {
    NONE("None", "", 0x00000000),
    PAID_IN_FULL("Paid in full", "🟢 Paid in full", 0xFF27AE60),
    PAYMENT_DUE("Payment due", "🟢 Payment due", 0xFF27AE60),
    OVERDUE("Overdue", "🟠 Overdue", 0xFFF39C12),
    URGENTLY_DUE("Urgently due", "🔴 Urgently due", 0xFFE74C3C),
    DRAFT("Draft", "🔵 Draft", 0xFF3498DB),
    QUOTE("Quote", "🔵 Quote", 0xFF3498DB);

    companion object {
        fun fromString(str: String): DocumentStamp {
            return entries.firstOrNull { it.label.equals(str, ignoreCase = true) } ?: NONE
        }
    }
}

data class CompanySettings(
    val name: String = "",
    val address: String = "",
    val contact: String = "",
    val email: String = "",
    val website: String = "",
    val vat: String = "",
    val reg: String = "",
    val logoBase64: String? = null
)

data class BankSettings(
    val holder: String = "",
    val number: String = "",
    val name: String = "",
    val branch: String = "",
    val ref: String = ""
)

data class AppPreferences(
    val currencyCode: String = "ZAR",
    val vatRate: Double = 15.0,
    val nextInvoiceNumber: Int = 1,
    val nextQuoteNumber: Int = 1,
    val nextProformaNumber: Int = 1,
    val isDarkMode: Boolean = false
) {
    val currencySymbol: String
        get() = CURRENCIES.firstOrNull { it.code == currencyCode }?.symbol ?: "R"
}
