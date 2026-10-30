package com.example.ui.modals

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyModal(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val fullPolicySummary = """
PRIVACY POLICY - EAS-INVOICE & QUOTE GENERATOR
Application: EAS-Invoice & Quote Generator
Package ID: com.aistudio.easinvoice.zlcyvk
Developer: Kryptic DSD (Email: krypticdsd@gmail.com)
Effective Date: October 30, 2026

1. SUMMARY & CORE PRINCIPLE
EAS-Invoice & Quote Generator is an offline-first tool. All client records, quotes, invoices, product lists, and banking details are stored exclusively on your device in an encrypted local database. Kryptic DSD does NOT collect, transmit, store, or sell any personal or business data.

2. DATA STORAGE & PROCESSING
- Client Data: Stored locally in on-device SQLite database. Never sent to remote servers.
- Business & Banking Data: Stored strictly on device.
- Document Records: All invoices and quotations reside on device.
- Zero Developer Telemetry: No third-party analytics, no tracking SDKs, no advertising networks.

3. PERMISSIONS & ZERO-PERMISSION ARCHITECTURE
- No dangerous runtime permissions required (No Contacts, No Location, No Camera, No Storage).
- Uses Android System Photo Picker (PickVisualMedia) to safely pick logos.
- Uses Android FileProvider to share PDFs strictly to recipient apps selected by you.

4. USER RIGHTS & DATA ERASURE (POPIA & GDPR)
- You have full ownership of your data.
- Records can be deleted individually in-app or permanently cleared via Android System Settings > Apps > Storage > Clear Data.
- Full unencrypted or AES-encrypted backups can be exported at any time.

5. CONTACT
Kryptic DSD
Email: krypticdsd@gmail.com
    """.trimIndent()

    AppModalOverlay(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("privacy_policy_modal"),
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
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Privacy Policy",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Google Play, POPIA & GDPR Compliant",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_privacy_modal")
                    ) {
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
                    // Highlights Banner
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "100% Offline • Zero Server Data Transmission",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "EAS-Invoice & Quote Generator does not collect, transmit, store, or sell any user or client data. All information remains exclusively in your device's local database.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Privacy Policy", fullPolicySummary)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Privacy Policy text copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_copy_policy_text"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Text", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Privacy Policy - EAS-Invoice & Quote Generator")
                                    putExtra(Intent.EXTRA_TEXT, fullPolicySummary)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Privacy Policy"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_share_policy"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Policy Sections
                    PolicySection(title = "1. Application & Developer Identification", icon = Icons.Default.Security) {
                        PolicyBullet("App Name", "EAS-Invoice & Quote Generator (Remix EAS Invoice)")
                        PolicyBullet("Package Identifier", "com.aistudio.easinvoice.zlcyvk")
                        PolicyBullet("Developer Organization", "Kryptic DSD")
                        PolicyBullet("Developer Contact", "krypticdsd@gmail.com")
                        PolicyBullet("Effective Date", "October 30, 2026")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PolicySection(title = "2. Data Storage & Zero Server Collection", icon = Icons.Default.Lock) {
                        PolicyBullet("Local Storage", "All client records, invoice history, prices, VAT rates, company information, and banking details are saved strictly in your device's local SQLite database (Android Room).")
                        PolicyBullet("No External Transmission", "No data is ever transmitted, synchronized, or uploaded to Kryptic DSD or any external cloud servers.")
                        PolicyBullet("No Tracking or Telemetry", "There are zero analytics SDKs, zero advertising SDKs, and zero crash reporting beacons embedded in this application.")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PolicySection(title = "3. Device Permissions & System Pickers", icon = Icons.Default.Security) {
                        PolicyBullet("Zero Dangerous Permissions", "The application does not request access to device contacts, location, camera, or microphone.")
                        PolicyBullet("Photo Picker", "When selecting your company logo, the app invokes Android's zero-permission system photo picker, accessing only the file you explicitly select.")
                        PolicyBullet("File Sharing", "Exported PDFs and CSV spreadsheets are shared via Android's secure FileProvider, granting temporary scoped read permissions only to the user-selected destination application.")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PolicySection(title = "4. User Rights & Data Deletion (Right to Erasure)", icon = Icons.Default.VerifiedUser) {
                        PolicyBullet("Immediate In-App Deletion", "You can edit or delete individual client contacts, product catalog items, past invoices, or device snapshots at any time.")
                        PolicyBullet("Total Data Erasure", "To completely wipe all data, navigate to Android Settings > Apps > EAS-Invoice > Storage > Clear Data, or uninstall the application.")
                        PolicyBullet("Data Portability", "You can export full JSON backups (with optional AES password encryption) or CSV summaries at any time from Settings.")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PolicySection(title = "5. POPIA, GDPR & CCPA Compliance", icon = Icons.Default.Security) {
                        PolicyBullet("POPIA (South Africa)", "Under Act No. 4 of 2013, you as the business operator are the 'Responsible Party' for customer data entered into the app. Kryptic DSD does not access this data.")
                        PolicyBullet("GDPR (European Union)", "Because no data leaves your device, Kryptic DSD is not a Data Controller or Data Processor. Processing is strictly local.")
                        PolicyBullet("CCPA / CPRA", "We do not sell, share, or monetize any personal information.")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PolicySection(title = "6. Developer Support", icon = Icons.Default.Email) {
                        Text(
                            text = "For inquiries regarding this Privacy Policy or Google Play compliance, contact:\nKryptic DSD • Email: krypticdsd@gmail.com",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_close_privacy_policy")
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun PolicyBullet(label: String, detail: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = "• $label", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
