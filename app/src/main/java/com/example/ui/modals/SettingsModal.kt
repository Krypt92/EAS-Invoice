package com.example.ui.modals

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import com.example.data.BackupCrypto
import com.example.data.InvoiceRepository
import com.example.data.SnapshotEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BankSettings
import com.example.model.CURRENCIES
import com.example.model.CompanySettings
import com.example.model.DocumentType
import com.example.ui.InvoiceViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

@Composable
fun SettingsModal(
    viewModel: InvoiceViewModel,
    onDismiss: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    val handleDismiss = {
        focusManager.clearFocus()
        onDismiss()
    }

    val compCurrent by viewModel.companySettings.collectAsState()
    val bankCurrent by viewModel.bankSettings.collectAsState()
    val prefsCurrent by viewModel.preferences.collectAsState()

    // Form states keyed to current flows so restoring a backup/snapshot immediately updates UI
    var companyName by remember(compCurrent) { mutableStateOf(compCurrent.name) }
    var companyAddress by remember(compCurrent) { mutableStateOf(compCurrent.address) }
    var companyContact by remember(compCurrent) { mutableStateOf(compCurrent.contact) }
    var companyEmail by remember(compCurrent) { mutableStateOf(compCurrent.email) }
    var companyWebsite by remember(compCurrent) { mutableStateOf(compCurrent.website) }
    var companyVat by remember(compCurrent) { mutableStateOf(compCurrent.vat) }
    var companyReg by remember(compCurrent) { mutableStateOf(compCurrent.reg) }
    var companyLogoBase64 by remember(compCurrent) { mutableStateOf(compCurrent.logoBase64) }

    var bankHolder by remember(bankCurrent) { mutableStateOf(bankCurrent.holder) }
    var bankNumber by remember(bankCurrent) { mutableStateOf(bankCurrent.number) }
    var bankName by remember(bankCurrent) { mutableStateOf(bankCurrent.name) }
    var bankBranch by remember(bankCurrent) { mutableStateOf(bankCurrent.branch) }
    var bankRef by remember(bankCurrent) { mutableStateOf(bankCurrent.ref) }

    var currencyCode by remember(prefsCurrent) { mutableStateOf(prefsCurrent.currencyCode) }
    var vatRate by remember(prefsCurrent) { mutableDoubleStateOf(prefsCurrent.vatRate) }

    var nextInvoiceStr by remember(prefsCurrent) { mutableStateOf(prefsCurrent.nextInvoiceNumber.toString()) }
    var nextQuoteStr by remember(prefsCurrent) { mutableStateOf(prefsCurrent.nextQuoteNumber.toString()) }
    var nextProformaStr by remember(prefsCurrent) { mutableStateOf(prefsCurrent.nextProformaNumber.toString()) }

    var currencyDropdownOpen by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var importPassword by remember { mutableStateOf("") }
    var isEncryptedBackupFile by remember { mutableStateOf(false) }

    // Snapshots state
    val snapshots by viewModel.snapshotsList.collectAsState()
    var showCreateSnapshotDialog by remember { mutableStateOf(false) }
    var snapshotTitleInput by remember { mutableStateOf("") }
    var snapshotToRestore by remember { mutableStateOf<SnapshotEntity?>(null) }
    var snapshotToDelete by remember { mutableStateOf<SnapshotEntity?>(null) }

    // Backup file picker (bounded to 25 MB and read on IO dispatcher to prevent UI stalls/OOM)
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val content = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        val maxBackupBytes = 25 * 1024 * 1024
                        val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                            val buffer = ByteArray(maxBackupBytes + 1)
                            var totalRead = 0
                            while (totalRead < buffer.size) {
                                val read = stream.read(buffer, totalRead, buffer.size - totalRead)
                                if (read == -1) break
                                totalRead += read
                            }
                            if (totalRead > maxBackupBytes) {
                                throw IllegalArgumentException("Selected file is too large (> 25 MB) to be a valid backup.")
                            }
                            buffer.copyOf(totalRead)
                        }
                        bytes?.toString(Charsets.UTF_8)
                    }
                    if (!content.isNullOrBlank()) {
                        importJsonText = content
                        isEncryptedBackupFile = BackupCrypto.isEncryptedBackup(content)
                        importPassword = ""
                        showImportDialog = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareExportedFile(file: File, mimeType: String, title: String, subject: String, desc: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, desc)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Intercept Android Back button inside SettingsModal
    BackHandler {
        focusManager.clearFocus()
        if (showCreateSnapshotDialog) {
            showCreateSnapshotDialog = false
        } else if (snapshotToRestore != null) {
            snapshotToRestore = null
        } else if (snapshotToDelete != null) {
            snapshotToDelete = null
        } else if (showImportDialog) {
            showImportDialog = false
        } else {
            handleDismiss()
        }
    }

    // Photo picker for company logo (decoded on IO dispatcher with safe bounds to prevent UI stalls/OOM)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val encodedLogo = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        val maxImageBytes = 20 * 1024 * 1024
                        val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                            val buffer = ByteArray(maxImageBytes + 1)
                            var totalRead = 0
                            while (totalRead < buffer.size) {
                                val read = stream.read(buffer, totalRead, buffer.size - totalRead)
                                if (read == -1) break
                                totalRead += read
                            }
                            if (totalRead > maxImageBytes) {
                                throw IllegalArgumentException("Image file exceeds 20 MB limit.")
                            }
                            buffer.copyOf(totalRead)
                        }
                        if (bytes != null && bytes.isNotEmpty()) {
                            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOpts)
                            val maxDim = 500
                            var sampleSize = 1
                            while (boundsOpts.outWidth / sampleSize > maxDim * 2 || boundsOpts.outHeight / sampleSize > maxDim * 2) {
                                sampleSize *= 2
                            }
                            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
                            if (originalBitmap != null) {
                                val scale = if (originalBitmap.width > maxDim || originalBitmap.height > maxDim) {
                                    maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                                } else 1f
                                val scaledBitmap = if (scale < 1f) {
                                    Bitmap.createScaledBitmap(
                                        originalBitmap,
                                        (originalBitmap.width * scale).toInt().coerceAtLeast(1),
                                        (originalBitmap.height * scale).toInt().coerceAtLeast(1),
                                        true
                                    )
                                } else {
                                    originalBitmap
                                }
                                val baos = ByteArrayOutputStream()
                                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 95, baos)
                                Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                            } else null
                        } else null
                    }
                    if (encodedLogo != null) {
                        companyLogoBase64 = encodedLogo
                        Toast.makeText(context, "Company logo selected", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Could not decode selected image", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val previewBitmap: Bitmap? = remember(companyLogoBase64) {
        if (!companyLogoBase64.isNullOrBlank()) {
            try {
                val clean = if (companyLogoBase64!!.contains(",")) companyLogoBase64!!.substringAfter(",") else companyLogoBase64!!
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    AppModalOverlay(onDismissRequest = handleDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("settings_modal"),
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
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Company & Bank Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = handleDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. INVOICE PREFERENCES
                    SettingsSection(title = "Invoice Preferences", icon = Icons.Default.Tune) {
                        // Currency Selector
                        Text("Currency", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .clickable { currencyDropdownOpen = true },
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val currentCurr = CURRENCIES.firstOrNull { it.code == currencyCode }
                                    Text(
                                        text = currentCurr?.name ?: currencyCode,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text("▼", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            DropdownMenu(
                                expanded = currencyDropdownOpen,
                                onDismissRequest = { currencyDropdownOpen = false }
                            ) {
                                CURRENCIES.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr.name, fontSize = 13.sp) },
                                        onClick = {
                                            currencyCode = curr.code
                                            currencyDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // VAT Rate slider + number input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Default VAT Rate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${vatRate}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Slider(
                            value = vatRate.toFloat(),
                            onValueChange = { vatRate = (it * 10).roundToInt() / 10.0 },
                            valueRange = 0f..30f,
                            steps = 59,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. COMPANY DETAILS
                    SettingsSection(title = "Company Details", icon = Icons.Default.Business) {
                        // Company Logo Card
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Company Logo",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (previewBitmap != null) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "Active",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your logo will be displayed on generated PDF invoices & quotations.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (previewBitmap != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier
                                                .height(60.dp)
                                                .widthIn(min = 60.dp, max = 130.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    bitmap = previewBitmap.asImageBitmap(),
                                                    contentDescription = "Uploaded Company Logo",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    keyboardController?.hide()
                                                    photoPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Change Logo", fontSize = 12.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    companyLogoBase64 = null
                                                    Toast.makeText(context, "Logo removed", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Remove Logo", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.AddPhotoAlternate,
                                                contentDescription = "Upload Logo",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Upload Company Logo",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "Supports PNG, JPG, or WEBP",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Company Name") },
                            placeholder = { Text("Your Company Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = companyAddress,
                            onValueChange = { companyAddress = it },
                            label = { Text("Physical Address") },
                            placeholder = { Text("Street, City, Postal") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = companyContact,
                                onValueChange = { companyContact = it },
                                label = { Text("Contact Phone") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = companyEmail,
                                onValueChange = { companyEmail = it },
                                label = { Text("Company Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = companyWebsite,
                            onValueChange = { companyWebsite = it },
                            label = { Text("Website") },
                            placeholder = { Text("www.company.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = companyVat,
                                onValueChange = { companyVat = it },
                                label = { Text("VAT Number") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = companyReg,
                                onValueChange = { companyReg = it },
                                label = { Text("Company Reg No") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. BANK DETAILS
                    SettingsSection(title = "Bank Details", icon = Icons.Default.AccountBalance) {
                        OutlinedTextField(
                            value = bankHolder,
                            onValueChange = { bankHolder = it },
                            label = { Text("Account Holder") },
                            placeholder = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bankNumber,
                                onValueChange = { bankNumber = it },
                                label = { Text("Account Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = bankName,
                                onValueChange = { bankName = it },
                                label = { Text("Bank Name") },
                                placeholder = { Text("e.g. Capitec / FNB") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bankBranch,
                                onValueChange = { bankBranch = it },
                                label = { Text("Branch Code") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = bankRef,
                                onValueChange = { bankRef = it },
                                label = { Text("Deposit Reference") },
                                placeholder = { Text("e.g. Initials & Surname") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. DOCUMENT NUMBERING
                    SettingsSection(title = "Document Numbering", icon = Icons.Default.FormatListNumbered) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = nextInvoiceStr,
                                onValueChange = { nextInvoiceStr = it },
                                label = { Text("Next Invoice #") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = nextQuoteStr,
                                onValueChange = { nextQuoteStr = it },
                                label = { Text("Next Quote #") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = nextProformaStr,
                                onValueChange = { nextProformaStr = it },
                                label = { Text("Next Proforma #") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.resetCountersToDefault()
                                nextInvoiceStr = "1"
                                nextQuoteStr = "1"
                                nextProformaStr = "1"
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Counters to Default (1)", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. DIRECT GOOGLE DRIVE BACKUP
                    SettingsSection(title = "Direct Google Drive Backup", icon = Icons.Default.CloudUpload) {
                        Text(
                            text = "1-Tap backup packaging all invoices, quotes, and client data into an encrypted JSON file that opens via the Android share sheet to save directly to your Google Drive, WhatsApp, or local device storage.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.createEncryptedBackup(context) { file ->
                                    shareExportedFile(
                                        file = file,
                                        mimeType = "application/json",
                                        title = "Save Backup to Google Drive, WhatsApp, or Storage",
                                        subject = "EAS-Invoice & Quote Generator Encrypted Backup",
                                        desc = "Encrypted backup containing all EAS-Invoice documents, clients, and settings."
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1-Tap Direct Cloud Backup (Google Drive / Share)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Secured with AES-256-GCM encryption & PBKDF2 key derivation", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. SPREADSHEET REPORTS
                    SettingsSection(title = "Spreadsheet Reports", icon = Icons.Default.TableChart) {
                        Text(
                            text = "Exports all jobs and invoices into a CSV file compatible with Microsoft Excel and Google Sheets, complete with line items, client addresses, VAT, and grand totals.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.exportCsvReport(context) { file ->
                                    shareExportedFile(
                                        file = file,
                                        mimeType = "text/csv",
                                        title = "Open CSV in Excel, Google Sheets, or Share",
                                        subject = "EAS-Invoice & Quotes Report (CSV)",
                                        desc = "Spreadsheet report containing all EAS invoices and quotations."
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export All Documents to Spreadsheet (CSV)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7. INTERNAL DEVICE SNAPSHOTS
                    SettingsSection(title = "Internal Device Snapshots", icon = Icons.Default.Restore) {
                        Text(
                            text = "Create, restore, or delete safety restore points directly on your device with zero internet setup required. Safely roll back your data at any time.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                                snapshotTitleInput = "Safety Point - $dateStr"
                                showCreateSnapshotDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Safety Restore Point", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (snapshots.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("No restore points saved on device yet.", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Create a safety point anytime before making large changes.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                snapshots.forEach { snap ->
                                    val snapDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(snap.timestamp))
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(snap.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text(snapDate, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Button(
                                                        onClick = { snapshotToRestore = snap },
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Restore", fontSize = 11.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    IconButton(
                                                        onClick = { snapshotToDelete = snap },
                                                        modifier = Modifier.size(30.dp)
                                                    ) {
                                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "${snap.documentCount} docs",
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Surface(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "${snap.clientCount} clients",
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Surface(
                                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "${prefsCurrent.currencyCode} ${String.format(Locale.getDefault(), "%,.0f", snap.totalRevenue)}",
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 8. STANDARD BACKUP & RESTORE
                    SettingsSection(title = "Standard Backup JSON", icon = Icons.Default.Download) {
                        Text(
                            text = "Export or import raw JSON compatible with EAS web app and cross-platform tools.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val json = viewModel.exportBackupJson()
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, json)
                                            putExtra(Intent.EXTRA_SUBJECT, "EAS-Invoice & Quote Generator Backup")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Export EAS-Invoice Backup"))
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export JSON", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    importJsonText = ""
                                    importPassword = ""
                                    isEncryptedBackupFile = false
                                    showImportDialog = true
                                 },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import JSON", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsSection(title = "Privacy Policy & Compliance", icon = Icons.Default.Security) {
                        Text(
                            text = "EAS-Invoice & Quote Generator is 100% offline and collects zero personal or customer data. Meets Google Play Store, POPIA, and GDPR data safety standards.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onOpenPrivacyPolicy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_settings_privacy_policy"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Official Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Footer Save / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = handleDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            viewModel.saveCompany(
                                compCurrent.copy(
                                    name = companyName.trim(),
                                    address = companyAddress.trim(),
                                    contact = companyContact.trim(),
                                    email = companyEmail.trim(),
                                    website = companyWebsite.trim(),
                                    vat = companyVat.trim(),
                                    reg = companyReg.trim(),
                                    logoBase64 = companyLogoBase64
                                )
                            )
                            viewModel.saveBank(
                                BankSettings(
                                    holder = bankHolder.trim(),
                                    number = bankNumber.trim(),
                                    name = bankName.trim(),
                                    branch = bankBranch.trim(),
                                    ref = bankRef.trim()
                                )
                            )
                            viewModel.savePrefs(
                                prefsCurrent.copy(
                                    currencyCode = currencyCode,
                                    vatRate = vatRate,
                                    nextInvoiceNumber = nextInvoiceStr.toIntOrNull() ?: prefsCurrent.nextInvoiceNumber,
                                    nextQuoteNumber = nextQuoteStr.toIntOrNull() ?: prefsCurrent.nextQuoteNumber,
                                    nextProformaNumber = nextProformaStr.toIntOrNull() ?: prefsCurrent.nextProformaNumber
                                )
                            )
                            handleDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_save_settings")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Create Snapshot Dialog
    if (showCreateSnapshotDialog) {
        AppModalOverlay(onDismissRequest = { showCreateSnapshotDialog = false }, zIndex = 20f) {
            Card(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Create Safety Restore Point", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Captures an instant snapshot of all invoices, quotes, clients, and settings directly on your device with 0 internet required.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = snapshotTitleInput,
                        onValueChange = { snapshotTitleInput = it },
                        label = { Text("Snapshot Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showCreateSnapshotDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.createSnapshot(snapshotTitleInput) {
                                    showCreateSnapshotDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save Snapshot")
                        }
                    }
                }
            }
        }
    }

    // Confirm Snapshot Restore Dialog
    if (snapshotToRestore != null) {
        val snap = snapshotToRestore!!
        val snapDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(snap.timestamp))
        AppModalOverlay(onDismissRequest = { snapshotToRestore = null }, zIndex = 20f) {
            Card(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Restore Safety Snapshot?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Are you sure you want to restore the data from:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(snap.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Saved on $snapDate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Contains: ${snap.documentCount} invoices/quotes, ${snap.clientCount} clients (${prefsCurrent.currencyCode} ${String.format(Locale.getDefault(), "%,.2f", snap.totalRevenue)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current documents and settings will be restored to this exact state. Zero internet needed.", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { snapshotToRestore = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.restoreSnapshot(snap) {
                                    snapshotToRestore = null
                                    handleDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            )
                        ) {
                            Text("Restore Now")
                        }
                    }
                }
            }
        }
    }

    // Confirm Snapshot Delete Dialog
    if (snapshotToDelete != null) {
        val snap = snapshotToDelete!!
        AppModalOverlay(onDismissRequest = { snapshotToDelete = null }, zIndex = 20f) {
            Card(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delete Restore Point?", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Are you sure you want to permanently delete \"${snap.title}\"? This action cannot be undone.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { snapshotToDelete = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.deleteSnapshot(snap.id)
                                snapshotToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }

    // Import Backup Dialog
    if (showImportDialog) {
        AppModalOverlay(onDismissRequest = { showImportDialog = false }, zIndex = 20f) {
            Card(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Import Backup Data", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Pick an encrypted backup file (.eas.json) or paste EAS JSON below:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { importFileLauncher.launch("*/*") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Backup File (.json / .eas.json)", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = {
                            importJsonText = it
                            isEncryptedBackupFile = BackupCrypto.isEncryptedBackup(it)
                        },
                        placeholder = { Text("Paste JSON or pick a backup file...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (isEncryptedBackupFile) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Encrypted Backup (AES-256-GCM)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = importPassword,
                            onValueChange = { importPassword = it },
                            label = { Text("Password (leave blank for default key)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showImportDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (importJsonText.isBlank()) {
                                    Toast.makeText(context, "Please pick a backup file or paste JSON", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.importBackup(
                                    jsonString = importJsonText,
                                    password = if (importPassword.isBlank()) null else importPassword
                                ) { summary: InvoiceRepository.RestoreSummary ->
                                    if (summary.success) {
                                        Toast.makeText(context, summary.message, Toast.LENGTH_SHORT).show()
                                        showImportDialog = false
                                        handleDismiss()
                                    } else {
                                        Toast.makeText(context, "Restore error: ${summary.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            )
                        ) {
                            Text("Restore")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
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
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
