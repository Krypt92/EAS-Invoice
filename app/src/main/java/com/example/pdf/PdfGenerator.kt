package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.model.BankSettings
import com.example.model.CompanySettings
import com.example.model.DocumentStamp
import com.example.model.DocumentTotals
import com.example.model.DocumentType
import com.example.model.LineItem
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object PdfGenerator {

    private val numberFormat = DecimalFormat("#,##0.00")

    fun formatMoney(symbol: String, amount: Double): String {
        val sign = if (amount < 0) "-" else ""
        return "$symbol $sign${numberFormat.format(Math.abs(amount))}"
    }

    data class InvoicePdfData(
        val docType: DocumentType,
        val docNumber: String,
        val clientName: String,
        val clientAddress: String,
        val clientCell: String,
        val clientEmail: String,
        val accountNo: String,
        val docDate: String,
        val dueDate: String,
        val stamp: DocumentStamp,
        val vatEnabled: Boolean,
        val vatRate: Double,
        val currencySymbol: String,
        val terms: String,
        val items: List<LineItem>,
        val totals: DocumentTotals,
        val company: CompanySettings,
        val bank: BankSettings
    )

    // Standard ISO A4 dimensions in typographic points (72 pt / inch)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2 // 523f
    private const val CONTENT_RIGHT = PAGE_WIDTH - MARGIN     // 559f
    private const val FOOTER_Y = 820f
    private const val FOOTER_LINE_Y = 810f

    // Corporate Color Palette
    private val COLOR_NAVY_PRIMARY = Color.parseColor("#0F2439")
    private val COLOR_NAVY_ACCENT = Color.parseColor("#1E3A8A")
    private val COLOR_AMBER_ACCENT = Color.parseColor("#D97706")
    private val COLOR_TEXT_PRIMARY = Color.parseColor("#0F172A")
    private val COLOR_TEXT_SECONDARY = Color.parseColor("#334155")
    private val COLOR_TEXT_MUTED = Color.parseColor("#64748B")
    private val COLOR_BORDER_SUBTLE = Color.parseColor("#E2E8F0")
    private val COLOR_BORDER_MEDIUM = Color.parseColor("#CBD5E1")
    private val COLOR_CARD_BG = Color.parseColor("#F8FAFC")
    private val COLOR_TABLE_HEAD_BG = Color.parseColor("#F1F5F9")
    private val COLOR_ROW_ALT_BG = Color.parseColor("#FAFBFD")

    fun generatePdf(context: Context, data: InvoicePdfData): File {
        val pdfDocument = PdfDocument()

        try {
            // Compute layout pagination
            val pages = paginateDocument(data)
            val totalPages = pages.size
            var runningItemIndex = 0

            for ((pageIndex, pageItems) in pages.withIndex()) {
                val pageNumber = pageIndex + 1
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Pure white background
                canvas.drawColor(Color.WHITE)

                // Top luxury stationery accent bar
                drawTopAccentBar(canvas)

                var y: Float

                if (pageNumber == 1) {
                    // First page: Full Corporate Header, Title, Status & Metadata Cards
                    y = drawHeader(canvas, data)
                    y = drawDocumentTitleAndStatus(canvas, y, data)
                    y = drawMetadataCards(canvas, y, data)
                } else {
                    // Subsequent pages: Crisp continuation header
                    y = drawContinuationHeader(canvas, data)
                }

                // Line Items Table for this page
                y = drawItemsTable(canvas, y, pageItems, pageIndex == 0, runningItemIndex, data)
                runningItemIndex += pageItems.size

                // If this is the last page, render the Totals, Bank, Acceptance & Terms
                if (pageNumber == totalPages) {
                    drawClosingSection(canvas, y, data)
                }

                // Uniform Corporate Footer on every page
                drawFooter(canvas, pageNumber, totalPages, data)

                pdfDocument.finishPage(page)
            }

            // Write to cache
            val cleanName = "${data.docNumber}_${data.clientName}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(80)
            val file = File(context.cacheDir, "${cleanName}.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            return file
        } finally {
            pdfDocument.close()
        }
    }

    // --- Pagination Calculation ---
    private fun paginateDocument(data: InvoicePdfData): List<List<LineItem>> {
        val paintDesc = Paint().apply {
            isAntiAlias = true
            textSize = 8.5f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val descWidth = if (data.vatEnabled) 205f else 240f

        val itemHeights = data.items.map { item ->
            val lines = wrapText(item.desc.ifBlank { "—" }, paintDesc, descWidth)
            val count = Math.max(1, lines.size)
            if (count == 1) 19f else 12f + (count * 9.5f)
        }

        // Estimate vertical space required on page 1
        // Header ~75f, Title & Status ~28f, Metadata Cards ~78f, Table Header ~22f
        val page1UsedAboveTable = 220f
        // Bottom closing section (Totals + Bank ~115f, Acceptance ~45f, Terms ~ dynamic)
        val termsPaintForMeasure = Paint().apply { textSize = 7f }
        val termsBoxTextWidth = CONTENT_WIDTH - 20f
        val wrappedTermsLines = if (data.terms.isNotBlank()) wrapText(data.terms, termsPaintForMeasure, termsBoxTextWidth) else emptyList()
        val dynamicTermsHeight = if (wrappedTermsLines.isNotEmpty()) (24f + (wrappedTermsLines.size * 9.5f)) else 0f
        val closingHeight = 120f + (if (data.docType == DocumentType.QUOTATION) 45f else 0f) + dynamicTermsHeight
        val maxAvailablePage1 = FOOTER_LINE_Y - page1UsedAboveTable - 15f

        // Can everything fit on a single page?
        val totalItemsHeight = itemHeights.sum()
        if (totalItemsHeight + closingHeight <= maxAvailablePage1) {
            return listOf(data.items)
        }

        // Multi-page distribution
        val pages = mutableListOf<List<LineItem>>()
        var currentPageItems = mutableListOf<LineItem>()
        var currentHeight = 0f
        var isFirstPage = true

        for (i in data.items.indices) {
            val item = data.items[i]
            val itemH = itemHeights[i]
            val maxAllowed = if (isFirstPage) {
                // On page 1 before overflow
                maxAvailablePage1 - 20f
            } else {
                // Subsequent page with continuation header (~50f)
                FOOTER_LINE_Y - 70f
            }

            if (currentHeight + itemH > maxAllowed && currentPageItems.isNotEmpty()) {
                pages.add(currentPageItems)
                currentPageItems = mutableListOf(item)
                currentHeight = itemH
                isFirstPage = false
            } else {
                currentPageItems.add(item)
                currentHeight += itemH
            }
        }

        if (currentPageItems.isNotEmpty()) {
            // Check if closing section fits on this final page
            val maxAllowedFinal = FOOTER_LINE_Y - (if (isFirstPage) page1UsedAboveTable else 70f)
            if (currentHeight + closingHeight > maxAllowedFinal) {
                if (currentPageItems.size > 1) {
                    // Push last item to its own final page with closing section
                    val lastItem = currentPageItems.removeAt(currentPageItems.lastIndex)
                    val lastItemH = itemHeights.lastOrNull() ?: 19f
                    pages.add(currentPageItems)
                    if (lastItemH + closingHeight > (FOOTER_LINE_Y - 70f)) {
                        pages.add(listOf(lastItem))
                        pages.add(emptyList())
                    } else {
                        pages.add(listOf(lastItem))
                    }
                } else {
                    pages.add(currentPageItems)
                    pages.add(emptyList())
                }
            } else {
                pages.add(currentPageItems)
            }
        }

        return if (pages.isEmpty()) listOf(emptyList()) else pages
    }

    // --- 1. Top Luxury Stationery Accent Bar ---
    private fun drawTopAccentBar(canvas: Canvas) {
        val paintBar = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = COLOR_NAVY_PRIMARY
        }
        // Deep navy band
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 3.5f, paintBar)

        // Warm amber accent segment on left (40% width)
        paintBar.color = COLOR_AMBER_ACCENT
        canvas.drawRect(MARGIN, 0f, MARGIN + 120f, 3.5f, paintBar)
    }

    // --- 2. Corporate Header (Logo / Monogram + Company Details) ---
    private fun drawHeader(canvas: Canvas, data: InvoicePdfData): Float {
        val topY = 24f
        var logoBottomY = topY

        // Left side: Company Logo or Elegant Corporate Monogram
        val logoBitmap = decodeLogo(data.company.logoBase64)
        if (logoBitmap != null) {
            val maxLogoW = 140f
            val maxLogoH = 50f
            val ratio = Math.min(maxLogoW / logoBitmap.width, maxLogoH / logoBitmap.height)
            val logoW = logoBitmap.width * ratio
            val logoH = logoBitmap.height * ratio
            val destRect = RectF(MARGIN, topY, MARGIN + logoW, topY + logoH)
            val paintBmp = Paint().apply { isFilterBitmap = true }
            canvas.drawBitmap(logoBitmap, null, destRect, paintBmp)
            logoBottomY = topY + logoH
        } else {
            // Clean Corporate Monogram & Company Title
            val monogramBox = RectF(MARGIN, topY, MARGIN + 34f, topY + 34f)
            val monoBgPaint = Paint().apply {
                isAntiAlias = true
                color = COLOR_NAVY_PRIMARY
            }
            canvas.drawRoundRect(monogramBox, 6f, 6f, monoBgPaint)

            val initial = if (data.company.name.isNotBlank()) data.company.name.trim().take(1).uppercase() else "E"
            val monoTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(initial, MARGIN + 17f, topY + 24f, monoTextPaint)

            // Company Name beside monogram
            val compTitlePaint = Paint().apply {
                isAntiAlias = true
                color = COLOR_NAVY_PRIMARY
                textSize = 15f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            val displayName = if (data.company.name.isNotBlank()) data.company.name else "Company Name"
            canvas.drawText(displayName, MARGIN + 42f, topY + 20f, compTitlePaint)

            val compSubPaint = Paint().apply {
                isAntiAlias = true
                color = COLOR_TEXT_MUTED
                textSize = 7.5f
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            canvas.drawText("Professional Services & Solutions", MARGIN + 42f, topY + 31f, compSubPaint)
            logoBottomY = topY + 38f
        }

        // Right side: Company Contact & Registration Information (Right Aligned)
        val paintCompRight = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        var compY = topY + 12f

        // If a logo was used, draw Company Name in bold on right
        if (logoBitmap != null && data.company.name.isNotBlank()) {
            paintCompRight.textSize = 13f
            paintCompRight.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            paintCompRight.color = COLOR_NAVY_PRIMARY
            canvas.drawText(data.company.name, CONTENT_RIGHT, compY, paintCompRight)
            compY += 14f
        }

        paintCompRight.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        paintCompRight.textSize = 7.5f
        paintCompRight.color = COLOR_TEXT_SECONDARY

        if (data.company.address.isNotBlank()) {
            canvas.drawText(data.company.address, CONTENT_RIGHT, compY, paintCompRight)
            compY += 11f
        }

        val telEmail = listOfNotNull(
            data.company.contact.takeIf { it.isNotBlank() }?.let { "Tel: $it" },
            data.company.email.takeIf { it.isNotBlank() }?.let { "Email: $it" }
        ).joinToString("  •  ")
        if (telEmail.isNotBlank()) {
            canvas.drawText(telEmail, CONTENT_RIGHT, compY, paintCompRight)
            compY += 11f
        }

        if (data.company.website.isNotBlank()) {
            canvas.drawText(data.company.website, CONTENT_RIGHT, compY, paintCompRight)
            compY += 11f
        }

        val regVat = listOfNotNull(
            data.company.vat.takeIf { it.isNotBlank() }?.let { "VAT No: $it" },
            data.company.reg.takeIf { it.isNotBlank() }?.let { "Reg No: $it" }
        ).joinToString("  |  ")
        if (regVat.isNotBlank()) {
            paintCompRight.color = COLOR_TEXT_MUTED
            canvas.drawText(regVat, CONTENT_RIGHT, compY, paintCompRight)
            compY += 11f
        }

        val headerBottomY = Math.max(logoBottomY, compY) + 8f

        // Subtle hairline separator rule
        val paintRule = Paint().apply {
            isAntiAlias = true
            color = COLOR_BORDER_MEDIUM
            strokeWidth = 0.75f
        }
        canvas.drawLine(MARGIN, headerBottomY, CONTENT_RIGHT, headerBottomY, paintRule)

        return headerBottomY + 12f
    }

    // --- 3. Document Title & Status Ribbon ---
    private fun drawDocumentTitleAndStatus(canvas: Canvas, startY: Float, data: InvoicePdfData): Float {
        // Document Title on Left
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_NAVY_PRIMARY
            textSize = 17f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        canvas.drawText(data.docType.title.uppercase(), MARGIN, startY + 15f, titlePaint)

        val subtitlePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_TEXT_MUTED
            textSize = 7f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        canvas.drawText("OFFICIAL ACCOUNTING DOCUMENT", MARGIN, startY + 25f, subtitlePaint)

        // Status Badge on Right (if stamp is active)
        if (data.stamp != DocumentStamp.NONE) {
            val (bgColor, borderColor, textColor, badgeText) = when (data.stamp) {
                DocumentStamp.PAID_IN_FULL -> Quad(
                    Color.parseColor("#DCFCE7"),
                    Color.parseColor("#16A34A"),
                    Color.parseColor("#15803D"),
                    "● PAID IN FULL"
                )
                DocumentStamp.PAYMENT_DUE -> Quad(
                    Color.parseColor("#EFF6FF"),
                    Color.parseColor("#2563EB"),
                    Color.parseColor("#1D4ED8"),
                    "● PAYMENT DUE"
                )
                DocumentStamp.OVERDUE -> Quad(
                    Color.parseColor("#FEF2F2"),
                    Color.parseColor("#DC2626"),
                    Color.parseColor("#B91C1C"),
                    "● OVERDUE"
                )
                DocumentStamp.URGENTLY_DUE -> Quad(
                    Color.parseColor("#FFF1F2"),
                    Color.parseColor("#E11D48"),
                    Color.parseColor("#BE123C"),
                    "● URGENTLY DUE"
                )
                DocumentStamp.QUOTE -> Quad(
                    Color.parseColor("#F0FDF4"),
                    Color.parseColor("#059669"),
                    Color.parseColor("#047857"),
                    "● QUOTATION"
                )
                DocumentStamp.DRAFT -> Quad(
                    Color.parseColor("#F1F5F9"),
                    Color.parseColor("#64748B"),
                    Color.parseColor("#475569"),
                    "● DRAFT"
                )
                else -> Quad(
                    Color.parseColor("#F1F5F9"),
                    COLOR_NAVY_PRIMARY,
                    COLOR_NAVY_PRIMARY,
                    data.stamp.label.uppercase()
                )
            }

            val badgeTextPaint = Paint().apply {
                isAntiAlias = true
                color = textColor
                textSize = 7.5f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val textWidth = badgeTextPaint.measureText(badgeText)
            val badgeW = textWidth + 18f
            val badgeH = 18f
            val badgeRect = RectF(CONTENT_RIGHT - badgeW, startY + 6f, CONTENT_RIGHT, startY + 6f + badgeH)

            val badgeBgPaint = Paint().apply {
                isAntiAlias = true
                color = bgColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(badgeRect, 9f, 9f, badgeBgPaint)

            val badgeBorderPaint = Paint().apply {
                isAntiAlias = true
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
            canvas.drawRoundRect(badgeRect, 9f, 9f, badgeBorderPaint)
            canvas.drawText(badgeText, badgeRect.centerX(), badgeRect.centerY() + 2.8f, badgeTextPaint)
        }

        return startY + 34f
    }

    // Helper quad for stamp styling
    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    // --- 4. Client & Document Metadata Cards ---
    private fun drawMetadataCards(canvas: Canvas, startY: Float, data: InvoicePdfData): Float {
        val cardGap = 12f
        val cardWidth = (CONTENT_WIDTH - cardGap) / 2f // 255.5f
        val cardHeight = 72f

        val bgPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_CARD_BG
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_BORDER_SUBTLE
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
        }

        // Left Card: "BILLED TO" / "INVOICE TO"
        val leftCardRect = RectF(MARGIN, startY, MARGIN + cardWidth, startY + cardHeight)
        canvas.drawRoundRect(leftCardRect, 5f, 5f, bgPaint)
        canvas.drawRoundRect(leftCardRect, 5f, 5f, borderPaint)

        val cardLabelPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_TEXT_MUTED
            textSize = 6.8f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        val cardHeaderTitle = if (data.docType == DocumentType.QUOTATION) "QUOTATION FOR" else "INVOICE TO"
        canvas.drawText(cardHeaderTitle, MARGIN + 10f, startY + 14f, cardLabelPaint)

        val clientNamePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_TEXT_PRIMARY
            textSize = 10.5f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        val cName = if (data.clientName.isNotBlank()) data.clientName else "Valued Client"
        canvas.drawText(cName, MARGIN + 10f, startY + 28f, clientNamePaint)

        val clientInfoPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_TEXT_SECONDARY
            textSize = 7.5f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        var cY = startY + 40f
        if (data.clientAddress.isNotBlank()) {
            val addrLine = if (data.clientAddress.length > 40) data.clientAddress.take(38) + "…" else data.clientAddress
            canvas.drawText(addrLine, MARGIN + 10f, cY, clientInfoPaint)
            cY += 10f
        }

        val contactLine = listOfNotNull(
            data.clientCell.takeIf { it.isNotBlank() }?.let { "Cell: $it" },
            data.clientEmail.takeIf { it.isNotBlank() }?.let { "Email: $it" }
        ).joinToString("  •  ")
        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine, MARGIN + 10f, cY, clientInfoPaint)
            cY += 10f
        }

        if (data.accountNo.isNotBlank()) {
            val accPaint = Paint().apply {
                isAntiAlias = true
                color = COLOR_NAVY_PRIMARY
                textSize = 7.2f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            canvas.drawText("Account No: #${data.accountNo}", MARGIN + 10f, cY, accPaint)
        }

        // Right Card: "DOCUMENT DETAILS"
        val rightCardLeft = MARGIN + cardWidth + cardGap
        val rightCardRect = RectF(rightCardLeft, startY, CONTENT_RIGHT, startY + cardHeight)
        canvas.drawRoundRect(rightCardRect, 5f, 5f, bgPaint)
        canvas.drawRoundRect(rightCardRect, 5f, 5f, borderPaint)

        canvas.drawText("DOCUMENT DETAILS", rightCardLeft + 10f, startY + 14f, cardLabelPaint)

        // Key-Value Rows
        val keyX = rightCardLeft + 10f
        val valX = CONTENT_RIGHT - 10f
        val valAlignRight = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
            textSize = 7.5f
            color = COLOR_TEXT_SECONDARY
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }

        var rY = startY + 30f
        fun drawMetaRow(label: String, value: String, isBold: Boolean = false, isPrimary: Boolean = false) {
            canvas.drawText(label, keyX, rY, clientInfoPaint)
            valAlignRight.color = if (isPrimary) COLOR_NAVY_PRIMARY else COLOR_TEXT_PRIMARY
            valAlignRight.textSize = if (isPrimary) 9.5f else 8f
            valAlignRight.typeface = if (isBold) Typeface.create("sans-serif", Typeface.BOLD) else Typeface.create("sans-serif", Typeface.NORMAL)
            canvas.drawText(value, valX, rY, valAlignRight)
            rY += 15f
        }

        drawMetaRow("Document #:", data.docNumber, isBold = true, isPrimary = true)
        drawMetaRow("Date Issued:", data.docDate, isBold = true)
        if (data.dueDate.isNotBlank()) {
            drawMetaRow("Due Date:", data.dueDate.trim(), isBold = true)
        }

        return startY + cardHeight + 14f
    }

    // --- Continuation Header (for Page 2+) ---
    private fun drawContinuationHeader(canvas: Canvas, data: InvoicePdfData): Float {
        val topY = 24f
        val contTitlePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_NAVY_PRIMARY
            textSize = 12f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        canvas.drawText("${data.docType.title.uppercase()} (CONTINUED)", MARGIN, topY + 14f, contTitlePaint)

        val metaPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_TEXT_MUTED
            textSize = 8f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Doc: ${data.docNumber}  •  Client: ${data.clientName}", CONTENT_RIGHT, topY + 14f, metaPaint)

        val rulePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_BORDER_MEDIUM
            strokeWidth = 0.75f
        }
        canvas.drawLine(MARGIN, topY + 22f, CONTENT_RIGHT, topY + 22f, rulePaint)

        return topY + 30f
    }

    // --- 5. Line Items Table ---
    private fun drawItemsTable(
        canvas: Canvas,
        startY: Float,
        items: List<LineItem>,
        isFirstPage: Boolean,
        startIndex: Int,
        data: InvoicePdfData
    ): Float {
        if (items.isEmpty() && !isFirstPage) {
            return startY + 4f
        }
        val headerH = 20f

        // Define column geometries based on whether VAT is shown
        // Printable width = 523f (from MARGIN=36f to CONTENT_RIGHT=559f)
        data class ColDef(val name: String, val left: Float, val right: Float, val alignRight: Boolean)

        val columns: List<ColDef> = if (data.vatEnabled) {
            listOf(
                ColDef("#", 36f, 52f, false),
                ColDef("DESCRIPTION", 54f, 260f, false),
                ColDef("QTY", 260f, 292f, true),
                ColDef("PRICE", 292f, 350f, true),
                ColDef("SUBTOTAL", 350f, 410f, true),
                ColDef("DISC", 410f, 448f, true),
                ColDef("VAT", 448f, 492f, true),
                ColDef("TOTAL", 492f, 559f, true)
            )
        } else {
            listOf(
                ColDef("#", 36f, 54f, false),
                ColDef("DESCRIPTION", 56f, 295f, false),
                ColDef("QTY", 295f, 332f, true),
                ColDef("PRICE", 332f, 396f, true),
                ColDef("SUBTOTAL", 396f, 460f, true),
                ColDef("DISC", 460f, 500f, true),
                ColDef("TOTAL", 500f, 559f, true)
            )
        }

        // Draw Table Header Background
        val headBgPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_TABLE_HEAD_BG
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, startY, CONTENT_RIGHT, startY + headerH, headBgPaint)

        // Header Top & Bottom Borders
        val headBorderPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_BORDER_MEDIUM
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN, startY, CONTENT_RIGHT, startY, headBorderPaint)
        canvas.drawLine(MARGIN, startY + headerH, CONTENT_RIGHT, startY + headerH, headBorderPaint)

        // Header Typography
        val headTextPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_NAVY_PRIMARY
            textSize = 7f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }

        for (col in columns) {
            if (col.alignRight) {
                headTextPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(col.name, col.right - 4f, startY + 13f, headTextPaint)
            } else {
                headTextPaint.textAlign = Paint.Align.LEFT
                val x = if (col.name == "#") col.left + 4f else col.left + 2f
                canvas.drawText(col.name, x, startY + 13f, headTextPaint)
            }
        }

        var currentY = startY + headerH

        // Row Typography & Drawing
        val rowTextPaint = Paint().apply {
            isAntiAlias = true
            textSize = 8f
            color = COLOR_TEXT_PRIMARY
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val rowMutedPaint = Paint().apply {
            isAntiAlias = true
            textSize = 7.5f
            color = COLOR_TEXT_MUTED
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val rowTotalBoldPaint = Paint().apply {
            isAntiAlias = true
            textSize = 8f
            color = COLOR_NAVY_PRIMARY
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val rowDividerPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_BORDER_SUBTLE
            strokeWidth = 0.5f
        }
        val altBgPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_ROW_ALT_BG
            style = Paint.Style.FILL
        }

        val descCol = columns.first { it.name == "DESCRIPTION" }
        val descMaxWidth = descCol.right - descCol.left - 6f

        for ((index, item) in items.withIndex()) {
            val descLines = wrapText(item.desc.ifBlank { "—" }, rowTextPaint, descMaxWidth)
            val lineCount = Math.max(1, descLines.size)
            val rowHeight = if (lineCount == 1) 18f else 10f + (lineCount * 10f)

            // Alternating row background
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, currentY, CONTENT_RIGHT, currentY + rowHeight, altBgPaint)
            }

            val textBaselineY = currentY + 12f

            // 1. Line Index
            rowMutedPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("${startIndex + index + 1}", columns[0].left + 4f, textBaselineY, rowMutedPaint)

            // 2. Description (wrapped if multi-line)
            rowTextPaint.textAlign = Paint.Align.LEFT
            var lineY = textBaselineY
            for (line in descLines) {
                canvas.drawText(line, descCol.left + 2f, lineY, rowTextPaint)
                lineY += 9.5f
            }

            // 3. Numeric Columns (Right Aligned to column right edge - 4f)
            val qtyCol = columns.first { it.name == "QTY" }
            rowTextPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${item.qty}", qtyCol.right - 4f, textBaselineY, rowTextPaint)

            val priceCol = columns.first { it.name == "PRICE" }
            canvas.drawText(formatMoney(data.currencySymbol, item.price), priceCol.right - 4f, textBaselineY, rowTextPaint)

            val subCol = columns.first { it.name == "SUBTOTAL" }
            canvas.drawText(formatMoney(data.currencySymbol, item.subtotal), subCol.right - 4f, textBaselineY, rowTextPaint)

            val discCol = columns.first { it.name == "DISC" }
            val discText = if (item.discountPercent > 0) "${item.discountPercent}%" else "—"
            canvas.drawText(discText, discCol.right - 4f, textBaselineY, rowTextPaint)

            if (data.vatEnabled) {
                val vatCol = columns.first { it.name == "VAT" }
                val vatAmt = item.vatAmount(data.vatRate, data.vatEnabled)
                canvas.drawText(formatMoney(data.currencySymbol, vatAmt), vatCol.right - 4f, textBaselineY, rowTextPaint)
            }

            val totalCol = columns.first { it.name == "TOTAL" }
            val totalAmt = item.totalAmount(data.vatRate, data.vatEnabled)
            canvas.drawText(formatMoney(data.currencySymbol, totalAmt), totalCol.right - 4f, textBaselineY, rowTotalBoldPaint)

            currentY += rowHeight

            // Hairline separator below row
            canvas.drawLine(MARGIN, currentY, CONTENT_RIGHT, currentY, rowDividerPaint)
        }

        return currentY + 12f
    }

    // --- 6. Closing Section (Banking + Totals + Acceptance + Terms) ---
    private fun drawClosingSection(canvas: Canvas, startY: Float, data: InvoicePdfData) {
        val bottomWidth = CONTENT_WIDTH
        val totalsWidth = 220f
        val bankWidth = bottomWidth - totalsWidth - 14f // 289f
        val cardHeight = 88f

        // --- A. Totals Summary Card (Right Aligned) ---
        val totalsLeft = CONTENT_RIGHT - totalsWidth
        val totalsRect = RectF(totalsLeft, startY, CONTENT_RIGHT, startY + cardHeight)

        val cardBgPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_CARD_BG
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_BORDER_SUBTLE
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
        }
        canvas.drawRoundRect(totalsRect, 5f, 5f, cardBgPaint)
        canvas.drawRoundRect(totalsRect, 5f, 5f, borderPaint)

        val totLabelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 7.5f
            color = COLOR_TEXT_MUTED
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val totValPaint = Paint().apply {
            isAntiAlias = true
            textSize = 7.5f
            color = COLOR_TEXT_PRIMARY
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }

        val totKeyX = totalsLeft + 10f
        val totValX = CONTENT_RIGHT - 10f
        var ty = startY + 15f

        // Subtotal
        canvas.drawText("Subtotal", totKeyX, ty, totLabelPaint)
        canvas.drawText(formatMoney(data.currencySymbol, data.totals.subtotal), totValX, ty, totValPaint)
        ty += 13f

        // Total Discount
        if (data.totals.totalDiscount > 0) {
            totLabelPaint.color = COLOR_AMBER_ACCENT
            totValPaint.color = COLOR_AMBER_ACCENT
            canvas.drawText("Discount Savings", totKeyX, ty, totLabelPaint)
            canvas.drawText("- ${formatMoney(data.currencySymbol, data.totals.totalDiscount)}", totValX, ty, totValPaint)
            totLabelPaint.color = COLOR_TEXT_MUTED
            totValPaint.color = COLOR_TEXT_PRIMARY
            ty += 13f
        }

        // VAT
        val vatLabel = if (data.vatEnabled) "VAT (${data.vatRate}%)" else "VAT (Zero-Rated)"
        canvas.drawText(vatLabel, totKeyX, ty, totLabelPaint)
        canvas.drawText(formatMoney(data.currencySymbol, data.totals.totalVat), totValX, ty, totValPaint)
        ty += 13f

        // Grand Total Banner Block
        val grandRect = RectF(totalsLeft, startY + cardHeight - 24f, CONTENT_RIGHT, startY + cardHeight)
        val grandBgPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_NAVY_PRIMARY
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(grandRect, 5f, 5f, grandBgPaint)

        val grandLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 7.5f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        canvas.drawText("TOTAL AMOUNT DUE", totalsLeft + 10f, grandRect.centerY() + 2.8f, grandLabelPaint)

        val grandValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(formatMoney(data.currencySymbol, data.totals.grandTotal), CONTENT_RIGHT - 10f, grandRect.centerY() + 3.2f, grandValPaint)

        // --- B. Payment & Banking Instructions Card (Left Aligned) ---
        val bankRect = RectF(MARGIN, startY, MARGIN + bankWidth, startY + cardHeight)
        canvas.drawRoundRect(bankRect, 5f, 5f, cardBgPaint)
        canvas.drawRoundRect(bankRect, 5f, 5f, borderPaint)

        val bankHeadPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_NAVY_PRIMARY
            textSize = 7f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        canvas.drawText("PAYMENT & BANKING DETAILS", MARGIN + 10f, startY + 14f, bankHeadPaint)

        val bankLabelPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_TEXT_MUTED
            textSize = 7f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val bankValPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_TEXT_PRIMARY
            textSize = 7.2f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }

        var by = startY + 27f
        fun drawBankRow(lbl: String, value: String, isBold: Boolean = false) {
            canvas.drawText(lbl, MARGIN + 10f, by, bankLabelPaint)
            bankValPaint.typeface = if (isBold) Typeface.create("sans-serif", Typeface.BOLD) else Typeface.create("sans-serif", Typeface.NORMAL)
            canvas.drawText(value.ifBlank { "—" }, MARGIN + 85f, by, bankValPaint)
            by += 11f
        }

        drawBankRow("Bank Name:", data.bank.name)
        drawBankRow("Account Name:", data.bank.holder)
        drawBankRow("Account Number:", data.bank.number, isBold = true)
        drawBankRow("Branch Code:", data.bank.branch)
        val depositRef = data.bank.ref.ifBlank { data.docNumber }
        drawBankRow("Reference:", depositRef, isBold = true)

        var nextY = startY + cardHeight + 12f

        // --- C. Quotation Acceptance & Sign-off Block ---
        if (data.docType == DocumentType.QUOTATION) {
            val acceptRect = RectF(MARGIN, nextY, CONTENT_RIGHT, nextY + 38f)
            canvas.drawRoundRect(acceptRect, 5f, 5f, cardBgPaint)
            canvas.drawRoundRect(acceptRect, 5f, 5f, borderPaint)

            canvas.drawText("QUOTATION ACCEPTANCE & AUTHORIZATION", MARGIN + 10f, nextY + 12f, bankHeadPaint)

            val statementPaint = Paint().apply {
                isAntiAlias = true
                color = COLOR_TEXT_MUTED
                textSize = 6.5f
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            canvas.drawText("I confirm acceptance of the goods/services as quoted above and agree to terms.", MARGIN + 10f, nextY + 22f, statementPaint)

            val sigLinePaint = Paint().apply {
                isAntiAlias = true
                color = COLOR_BORDER_MEDIUM
                strokeWidth = 0.8f
            }
            // Signature line
            canvas.drawText("Authorized Signature:", MARGIN + 10f, nextY + 32f, bankLabelPaint)
            canvas.drawLine(MARGIN + 95f, nextY + 32f, MARGIN + 260f, nextY + 32f, sigLinePaint)

            // Date line
            canvas.drawText("Date:", MARGIN + 280f, nextY + 32f, bankLabelPaint)
            canvas.drawLine(MARGIN + 310f, nextY + 32f, MARGIN + 420f, nextY + 32f, sigLinePaint)

            nextY += 46f
        }

        // --- D. Terms & Conditions Box (Full Page Width) ---
        if (data.terms.isNotBlank()) {
            val termsHeadPaint = Paint().apply {
                isAntiAlias = true
                color = COLOR_NAVY_PRIMARY
                textSize = 7f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            val termsBodyPaint = Paint().apply {
                isAntiAlias = true
                color = COLOR_TEXT_SECONDARY
                textSize = 7f
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val termsTextWidth = CONTENT_WIDTH - 20f
            val wrapped = wrapText(data.terms, termsBodyPaint, termsTextWidth)
            val maxAllowedLines = ((FOOTER_LINE_Y - nextY - 26f) / 9.5f).toInt().coerceAtLeast(1)
            val linesToDraw = wrapped.take(maxAllowedLines)
            val termsBoxHeight = 22f + (linesToDraw.size * 9.5f)

            val termsRect = RectF(MARGIN, nextY, CONTENT_RIGHT, nextY + termsBoxHeight)
            canvas.drawRoundRect(termsRect, 5f, 5f, cardBgPaint)
            canvas.drawRoundRect(termsRect, 5f, 5f, borderPaint)

            canvas.drawText("TERMS & CONDITIONS", MARGIN + 10f, nextY + 12f, termsHeadPaint)

            var ty2 = nextY + 23f
            for (line in linesToDraw) {
                canvas.drawText(line, MARGIN + 10f, ty2, termsBodyPaint)
                ty2 += 9.5f
            }
        }
    }

    // --- 7. Uniform Corporate Footer ---
    private fun drawFooter(canvas: Canvas, pageNumber: Int, totalPages: Int, data: InvoicePdfData) {
        val linePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_BORDER_MEDIUM
            strokeWidth = 0.5f
        }
        canvas.drawLine(MARGIN, FOOTER_LINE_Y, CONTENT_RIGHT, FOOTER_LINE_Y, linePaint)

        val footerTextPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_TEXT_MUTED
            textSize = 7f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }

        // Left: Company identifier
        val compLeft = if (data.company.name.isNotBlank()) data.company.name else "Eas-invoice & Quote Generator"
        footerTextPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(compLeft, MARGIN, FOOTER_Y, footerTextPaint)

        // Center: Customer appreciation
        footerTextPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Thank you for your business!", PAGE_WIDTH / 2f, FOOTER_Y, footerTextPaint)

        // Right: Page numbering
        footerTextPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNumber of $totalPages", CONTENT_RIGHT, FOOTER_Y, footerTextPaint)
    }

    // --- Helper Utilities ---

    private fun decodeLogo(base64: String?): Bitmap? {
        if (base64.isNullOrBlank()) return null
        return try {
            val clean = if (base64.contains(",")) base64.substringAfter(",") else base64
            val bytes = Base64.decode(clean, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val normalized = text.replace("\r\n", "\n").replace("\r", "\n")
        val result = mutableListOf<String>()
        val paragraphs = normalized.split("\n")
        for (para in paragraphs) {
            if (para.isBlank()) {
                result.add("")
                continue
            }
            val words = para.split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                val test = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(test) <= maxWidth) {
                    currentLine = StringBuilder(test)
                } else {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine.toString())
                        currentLine = StringBuilder(word)
                    } else {
                        // Word is longer than column width; segment it safely
                        var remaining = word
                        while (paint.measureText(remaining) > maxWidth && remaining.length > 1) {
                            var end = remaining.length - 1
                            while (end > 0 && paint.measureText(remaining.substring(0, end)) > maxWidth) {
                                end--
                            }
                            if (end == 0) end = 1
                            result.add(remaining.substring(0, end))
                            remaining = remaining.substring(end)
                        }
                        currentLine = StringBuilder(remaining)
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine.toString())
            }
        }
        return result
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Document PDF"))
    }

    fun viewPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF"))
    }
}
