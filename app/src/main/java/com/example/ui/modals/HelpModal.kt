package com.example.ui.modals

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun HelpModal(
    onDismiss: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    AppModalOverlay(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("help_modal"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Help & Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // How to use
                    HelpSection(title = "How to Use & Latest Features", icon = Icons.Default.MenuBook) {
                        HelpBulletPoint(num = "1", title = "Company & Bank Setup", text = "Open Settings to configure your company details, logo, contact information, tax numbers (VAT/Reg), and bank payment instructions.")
                        HelpBulletPoint(num = "2", title = "Currency & Tax Rates", text = "Select your currency symbol (R, $, €, £, etc.) and default VAT/tax rate in Settings. Toggle 'Charge VAT' on or off per invoice.")
                        HelpBulletPoint(num = "3", title = "Client Management & Editing", text = "Manage clients in 1 tap. Add new clients, search saved contacts, and tap the Edit button to update client details anytime.")
                        HelpBulletPoint(num = "4", title = "Product & Service Catalog", text = "Save commonly billed items with preset descriptions, rates, and discounts. Insert them into your document with one tap.")
                        HelpBulletPoint(num = "5", title = "Document Types & Stamps", text = "Generate Tax Invoices, Quotations (with client acceptance signature block), or Proforma Invoices. Apply status stamps such as Paid in Full, Payment Due, or Overdue.")
                        HelpBulletPoint(num = "6", title = "Date Issued & Optional Due Date", text = "Set the issue date and an optional payment due date. If due date is left blank, it is cleanly omitted from the final document.")
                        HelpBulletPoint(num = "7", title = "Full-Width Terms & Conditions", text = "Type detailed payment terms, delivery notes, or warranties. The terms box utilizes full width and wraps dynamically on the PDF.")
                        HelpBulletPoint(num = "8", title = "Finalize, PDF Generation & Share", text = "Tap 'Finalize & Download' to save the record, advance the document number, and generate a print-ready PDF shareable via WhatsApp, Email, or Print.")
                        HelpBulletPoint(num = "9", title = "History & Re-editing", text = "View past documents in History, filter by type, re-share PDFs, or reopen any past document to edit and update details.")
                        HelpBulletPoint(num = "10", title = "Snapshots & Secure Backups", text = "Export full JSON backups or CSV summaries of your data, create point-in-time snapshots, and easily restore on new devices.")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Troubleshooting
                    HelpSection(title = "Tips & Troubleshooting", icon = Icons.Default.Build) {
                        TroubleBullet(title = "Due Date handling", text = "Leave the Due Date field empty if your document doesn't require a payment deadline; the field and label will not appear on the generated PDF.")
                        TroubleBullet(title = "Updating client details", text = "Go to Manage Clients to edit any saved phone number, address, email, or account number directly without re-typing from scratch.")
                        TroubleBullet(title = "Document numbering sequence", text = "If you ever need to adjust your starting invoice or quote number, go to Settings → Document Numbering to set the next number.")
                        TroubleBullet(title = "Sharing PDFs via WhatsApp / Email", text = "Tap the Share button on the editor, preview screen, or history list. Android's native share sheet allows direct sharing to WhatsApp, Gmail, Drive, or wireless printing.")
                        TroubleBullet(title = "Transferring to a new phone", text = "Go to Settings → Backup & Snapshots → Export Backup. Save the JSON file, send it to your new device, and tap 'Import Backup' to restore all clients, products, settings, and history.")
                        TroubleBullet(title = "Dark theme", text = "Tap the theme toggle icon in the top header bar anytime to switch between crisp light mode and high-contrast dark mode.")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // POPIA & Data Privacy
                    HelpSection(title = "POPIA & Data Privacy Compliance", icon = Icons.Default.Security) {
                        Card(
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Protection of Personal Information Act (POPIA Act No. 4 of 2013) & Privacy by Design",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        TroubleBullet(
                            title = "100% Offline & Private Local Storage",
                            text = "This application operates completely offline. All customer records, phone numbers, email addresses, physical addresses, bank details, invoices, and quotations are stored strictly in an encrypted on-device SQLite database. No data is ever transmitted to external servers."
                        )
                        TroubleBullet(
                            title = "Zero Developer Data Collection",
                            text = "Kryptic DSD does NOT collect, does NOT transmit, does NOT inspect, and does NOT store any of your business or client data. There are zero remote tracking SDKs, zero telemetry services, and zero third-party advertising trackers in this app."
                        )
                        TroubleBullet(
                            title = "User as the Responsible Party",
                            text = "Under POPIA, you as the business operator are the 'Responsible Party' for data entered into this application. You retain full control over client consent, data accuracy, device physical security (PIN/biometrics), and distribution of exported PDFs."
                        )
                        TroubleBullet(
                            title = "Data Portability & Right to Erasure",
                            text = "You maintain complete ownership of all records. You can export complete JSON and CSV archives at any time, delete individual client records or documents, or clear app storage to permanently destroy all saved data."
                        )
                        TroubleBullet(
                            title = "Zero Invasive Device Permissions",
                            text = "EAS-Invoice & Quote Generator does not require or request contacts, camera, location, microphone, or broad storage access. All file imports, logo selections, and PDF sharing utilize official zero-permission Android system pickers."
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = onOpenPrivacyPolicy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_view_full_privacy_policy"),
                            shape = RoundedCornerShape(8.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Read Full Google Play Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contact
                    HelpSection(title = "Contact & Support", icon = Icons.Default.Email) {
                        Text(
                            text = "Developed by Kryptic DSD.\nFor inquiries, support, or custom business software solutions:\nEmail: krypticdsd@gmail.com",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // License
                    HelpSection(title = "License & Copyright", icon = Icons.Default.Info) {
                        Card(
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("© 2026 Kryptic DSD – All rights reserved.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Professional offline invoicing application. Built with modern Android Jetpack Compose, Room Database, and Android Material 3.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun HelpBulletPoint(num: String, title: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = "$num. ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TroubleBullet(title: String, text: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = "• $title", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Text(text = text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
