package com.example.ui.modals

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DocumentStamp
import com.example.model.DocumentType
import com.example.pdf.PdfGenerator
import com.example.ui.InvoiceViewModel

@Composable
fun PreviewModal(
    viewModel: InvoiceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onDismiss() }

    val docType by viewModel.docType.collectAsState()
    val docNumber by viewModel.docNumber.collectAsState()
    val clientName by viewModel.clientName.collectAsState()
    val clientAddress by viewModel.clientAddress.collectAsState()
    val clientCell by viewModel.clientCell.collectAsState()
    val clientEmail by viewModel.clientEmail.collectAsState()
    val accountNo by viewModel.accountNo.collectAsState()
    val docDate by viewModel.docDate.collectAsState()
    val dueDate by viewModel.dueDate.collectAsState()
    val stamp by viewModel.stamp.collectAsState()
    val vatEnabled by viewModel.vatEnabled.collectAsState()
    val terms by viewModel.terms.collectAsState()
    val items by viewModel.items.collectAsState()
    val totals by viewModel.totals.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    val company by viewModel.companySettings.collectAsState()
    val bank by viewModel.bankSettings.collectAsState()

    val logoBitmap = remember(company.logoBase64) {
        if (!company.logoBase64.isNullOrBlank()) {
            try {
                val clean = if (company.logoBase64!!.contains(",")) company.logoBase64!!.substringAfter(",") else company.logoBase64!!
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    AppModalOverlay(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("preview_modal"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Document Preview",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Document Sheet
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Top Stationery Accent Band
                        Row(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                            Box(modifier = Modifier.weight(0.35f).height(4.dp).background(Color(0xFFD97706)))
                            Box(modifier = Modifier.weight(0.65f).height(4.dp).background(Color(0xFF0F2439)))
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Company Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                // Monogram / Logo
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (logoBitmap != null) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier
                                                .height(44.dp)
                                                .widthIn(min = 44.dp, max = 110.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    bitmap = logoBitmap.asImageBitmap(),
                                                    contentDescription = "Company Logo",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF0F2439),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                val initial = if (company.name.isNotBlank()) company.name.trim().take(1).uppercase() else "E"
                                                Text(initial, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = company.name.ifBlank { "Company Name" },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F2439)
                                        )
                                        Text("Professional Services & Solutions", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }

                                // Company info right aligned
                                Column(horizontalAlignment = Alignment.End) {
                                    if (company.address.isNotBlank()) Text(company.address, fontSize = 10.sp, color = Color(0xFF475569))
                                    if (company.contact.isNotBlank()) Text("Tel: ${company.contact}", fontSize = 10.sp, color = Color(0xFF475569))
                                    if (company.email.isNotBlank()) Text("Email: ${company.email}", fontSize = 10.sp, color = Color(0xFF475569))
                                    if (company.website.isNotBlank()) Text(company.website, fontSize = 10.sp, color = Color(0xFF475569))
                                    val regVat = listOfNotNull(
                                        company.vat.takeIf { it.isNotBlank() }?.let { "VAT: $it" },
                                        company.reg.takeIf { it.isNotBlank() }?.let { "Reg: $it" }
                                    ).joinToString(" | ")
                                    if (regVat.isNotBlank()) Text(regVat, fontSize = 9.5.sp, color = Color(0xFF64748B))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Title & Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = docType.title.uppercase(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F2439)
                                    )
                                    Text("OFFICIAL ACCOUNTING DOCUMENT", fontSize = 9.sp, color = Color(0xFF64748B))
                                }

                                if (stamp != DocumentStamp.NONE) {
                                    val (badgeBg, badgeBorder, badgeTextCol) = when (stamp) {
                                        DocumentStamp.PAID_IN_FULL -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), Color(0xFF15803D))
                                        DocumentStamp.PAYMENT_DUE -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), Color(0xFF1D4ED8))
                                        DocumentStamp.OVERDUE -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFFB91C1C))
                                        DocumentStamp.URGENTLY_DUE -> Triple(Color(0xFFFFF1F2), Color(0xFFE11D48), Color(0xFFBE123C))
                                        DocumentStamp.QUOTE -> Triple(Color(0xFFF0FDF4), Color(0xFF059669), Color(0xFF047857))
                                        else -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), Color(0xFF475569))
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = badgeBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder)
                                    ) {
                                        Text(
                                            text = "● ${stamp.label.uppercase()}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeTextCol,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Metadata Cards: BILLED TO & DOCUMENT DETAILS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Billed to
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (docType == DocumentType.QUOTATION) "QUOTATION FOR" else "INVOICE TO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(clientName.ifBlank { "Valued Client" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        if (clientAddress.isNotBlank()) Text(clientAddress, fontSize = 10.sp, color = Color(0xFF475569))
                                        if (clientCell.isNotBlank()) Text("Cell: $clientCell", fontSize = 10.sp, color = Color(0xFF475569))
                                        if (clientEmail.isNotBlank()) Text("Email: $clientEmail", fontSize = 10.sp, color = Color(0xFF475569))
                                        if (accountNo.isNotBlank()) {
                                            Text("Account: #$accountNo", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F2439))
                                        }
                                    }
                                }

                                // Document Details
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("DOCUMENT DETAILS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Doc #:", fontSize = 10.sp, color = Color(0xFF64748B))
                                            Text(docNumber, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Date Issued:", fontSize = 10.sp, color = Color(0xFF64748B))
                                            Text(docDate, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                        }
                                        if (dueDate.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Due Date:", fontSize = 10.sp, color = Color(0xFF64748B))
                                                Text(dueDate.trim(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Table header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9))
                                    .border(0.8.dp, Color(0xFFCBD5E1))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439), modifier = Modifier.width(20.dp))
                                Text("DESCRIPTION", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439), modifier = Modifier.weight(1f))
                                Text("PRICE", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439), modifier = Modifier.width(65.dp))
                                Text("QTY", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439), modifier = Modifier.width(28.dp))
                                Text("SUBTOTAL", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439), modifier = Modifier.width(65.dp))
                                Text("TOTAL", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439), modifier = Modifier.width(70.dp))
                            }

                            // Table items
                            items.forEachIndexed { index, item ->
                                val rowBg = if (index % 2 == 1) Color(0xFFFAFBFD) else Color.White
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(rowBg)
                                        .border(0.5.dp, Color(0xFFE2E8F0))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${index + 1}", fontSize = 9.5.sp, color = Color(0xFF64748B), modifier = Modifier.width(20.dp))
                                    Text(item.desc.ifBlank { "—" }, fontSize = 9.5.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(1f))
                                    Text(PdfGenerator.formatMoney(prefs.currencySymbol, item.price), fontSize = 9.5.sp, color = Color(0xFF334155), modifier = Modifier.width(65.dp))
                                    Text("${item.qty}", fontSize = 9.5.sp, color = Color(0xFF334155), modifier = Modifier.width(28.dp))
                                    Text(PdfGenerator.formatMoney(prefs.currencySymbol, item.subtotal), fontSize = 9.5.sp, color = Color(0xFF334155), modifier = Modifier.width(65.dp))
                                    Text(PdfGenerator.formatMoney(prefs.currencySymbol, item.totalAmount(prefs.vatRate, vatEnabled)), fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439), modifier = Modifier.width(70.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bottom Section: Banking on Left, Totals on Right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Bank Card
                                Card(
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.weight(1.1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("PAYMENT & BANKING DETAILS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Bank: ${bank.name.ifBlank { "—" }}", fontSize = 9.sp, color = Color(0xFF334155))
                                        Text("Account Name: ${bank.holder.ifBlank { "—" }}", fontSize = 9.sp, color = Color(0xFF334155))
                                        Text("Account #: ${bank.number.ifBlank { "—" }}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Text("Branch: ${bank.branch.ifBlank { "—" }}", fontSize = 9.sp, color = Color(0xFF334155))
                                        Text("Reference: ${bank.ref.ifBlank { docNumber }}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439))
                                    }
                                }

                                // Totals Card with dark navy Grand Total banner
                                Card(
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Subtotal", fontSize = 9.5.sp, color = Color(0xFF64748B))
                                            Text(PdfGenerator.formatMoney(prefs.currencySymbol, totals.subtotal), fontSize = 9.5.sp, color = Color(0xFF0F172A))
                                        }
                                        if (totals.totalDiscount > 0) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Discount", fontSize = 9.5.sp, color = Color(0xFFD97706))
                                                Text("- ${PdfGenerator.formatMoney(prefs.currencySymbol, totals.totalDiscount)}", fontSize = 9.5.sp, color = Color(0xFFD97706))
                                            }
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            val vatText = if (vatEnabled) "VAT (${prefs.vatRate}%)" else "VAT (Zero-Rated)"
                                            Text(vatText, fontSize = 9.5.sp, color = Color(0xFF64748B))
                                            Text(PdfGenerator.formatMoney(prefs.currencySymbol, totals.totalVat), fontSize = 9.5.sp, color = Color(0xFF0F172A))
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Total Payable Banner
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF0F2439),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("TOTAL DUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(PdfGenerator.formatMoney(prefs.currencySymbol, totals.grandTotal), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }

                            // Signature block for Quotation
                            if (docType == DocumentType.QUOTATION) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("QUOTATION ACCEPTANCE & AUTHORIZATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2439))
                                        Text("I confirm acceptance of goods/services as quoted above.", fontSize = 8.5.sp, color = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Signature: __________________________", fontSize = 8.5.sp, color = Color(0xFF475569))
                                            Text("Date: ____________", fontSize = 8.5.sp, color = Color(0xFF475569))
                                        }
                                    }
                                }
                            }

                            // Terms
                            if (terms.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("TERMS & CONDITIONS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(terms, fontSize = 8.5.sp, color = Color(0xFF334155), lineHeight = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons: Share PDF, Open PDF, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Close")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.generateCurrentPdfAsync(context) { file ->
                                    PdfGenerator.viewPdf(context, file)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open PDF")
                        }

                        Button(
                            onClick = {
                                viewModel.generateCurrentPdfAsync(context) { file ->
                                    PdfGenerator.sharePdf(context, file)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share PDF")
                        }
                    }
                }
            }
        }
    }
}
