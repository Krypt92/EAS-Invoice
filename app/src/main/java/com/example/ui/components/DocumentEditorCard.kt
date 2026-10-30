package com.example.ui.components

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClientEntity
import com.example.model.DocumentStamp
import com.example.model.DocumentTotals
import com.example.model.DocumentType
import com.example.model.LineItem
import com.example.pdf.PdfGenerator
import com.example.ui.InvoiceViewModel
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocumentEditorCard(
    viewModel: InvoiceViewModel,
    onOpenClientManager: () -> Unit,
    onOpenProductManager: () -> Unit,
    onOpenPreview: () -> Unit,
    onSharePdf: () -> Unit,
    onFinalize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
    val clientsList by viewModel.clientsList.collectAsState()

    var showDocTypeMenu by remember { mutableStateOf(false) }
    var showStampMenu by remember { mutableStateOf(false) }
    var clientDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("document_editor_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Document Editor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 1. Document Type & Document Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Doc Type Selector
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "Document Type",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { showDocTypeMenu = true }
                                .testTag("doc_type_dropdown"),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = docType.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(text = "▼", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showDocTypeMenu,
                        onDismissRequest = { showDocTypeMenu = false }
                    ) {
                        DocumentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.title, fontWeight = if (type == docType) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    viewModel.setDocType(type)
                                    showDocTypeMenu = false
                                }
                            )
                        }
                    }
                }

                // Doc Number Display
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Document Number",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = docNumber,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.testTag("doc_number_display")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Client Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Client Details",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Client Name row with Autocomplete & Manage button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = clientName,
                                onValueChange = {
                                    viewModel.clientName.value = it
                                    clientDropdownExpanded = it.isNotBlank() && clientsList.any { c ->
                                        c.name.contains(it, ignoreCase = true)
                                    }
                                },
                                label = { Text("Client Full Name") },
                                placeholder = { Text("e.g. John Doe / Acme Corp") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("client_name_input"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Autocomplete suggestion dropdown (non-focusable so IME stays active while typing)
                            val filteredClients = clientsList.filter {
                                clientName.isNotBlank() && it.name.contains(clientName, ignoreCase = true)
                            }
                            if (filteredClients.isNotEmpty()) {
                                DropdownMenu(
                                    expanded = clientDropdownExpanded,
                                    onDismissRequest = { clientDropdownExpanded = false },
                                    properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                                ) {
                                    filteredClients.take(5).forEach { c ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(c.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    if (c.email.isNotBlank()) Text(c.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                viewModel.onClientSelected(c)
                                                clientDropdownExpanded = false
                                                focusManager.clearFocus()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenClientManager,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("btn_manage_clients")
                        ) {
                            Icon(imageVector = Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { viewModel.clientAddress.value = it },
                        label = { Text("Client Address") },
                        placeholder = { Text("Street, City, Postal Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = clientCell,
                            onValueChange = { viewModel.clientCell.value = it },
                            label = { Text("Cell / Phone") },
                            placeholder = { Text("e.g. +27 82 123 4567") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = clientEmail,
                            onValueChange = { viewModel.clientEmail.value = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("client@email.com") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = accountNo,
                        onValueChange = { viewModel.accountNo.value = it },
                        label = { Text("Account Number") },
                        placeholder = { Text("e.g. ACC-1002") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Date Issued
            OutlinedTextField(
                value = docDate,
                onValueChange = { viewModel.docDate.value = it },
                label = { Text("Date Issued") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        showDatePicker(context) { viewModel.docDate.value = it }
                    }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", modifier = Modifier.size(18.dp))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("doc_date_input"),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Due Date (Optional — omitted on PDF if left blank)
            OutlinedTextField(
                value = dueDate,
                onValueChange = { viewModel.dueDate.value = it },
                label = { Text("Due Date") },
                placeholder = { Text("Optional — leave blank to omit") },
                singleLine = true,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (dueDate.isNotBlank()) {
                            IconButton(onClick = {
                                viewModel.dueDate.value = ""
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Due Date", modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            showDatePicker(context) { viewModel.dueDate.value = it }
                        }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Due Date", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("due_date_input"),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stamp Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Document Stamp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { showStampMenu = true },
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (stamp == DocumentStamp.NONE) "None" else stamp.badgeText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(text = "▼", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                DropdownMenu(
                    expanded = showStampMenu,
                    onDismissRequest = { showStampMenu = false }
                ) {
                    DocumentStamp.entries.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(if (s == DocumentStamp.NONE) "None" else s.badgeText) },
                            onClick = {
                                viewModel.stamp.value = s
                                showStampMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Charge VAT Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.vatEnabled.value = !vatEnabled }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = vatEnabled,
                    onCheckedChange = { viewModel.vatEnabled.value = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("vat_checkbox")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Charge VAT (${prefs.vatRate}%)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Line Items Header & Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Line Items (${items.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenProductManager,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_quick_products")
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Products", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.addItemRow() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_add_item")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Line items list
            items.forEachIndexed { index, item ->
                LineItemEditorRow(
                    index = index,
                    item = item,
                    currencySymbol = prefs.currencySymbol,
                    vatRate = prefs.vatRate,
                    vatEnabled = vatEnabled,
                    onUpdate = { updated -> viewModel.updateItem(index, updated) },
                    onDelete = { viewModel.removeItem(index) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Totals Display
            TotalsSummaryCard(
                totals = totals,
                currencySymbol = prefs.currencySymbol,
                vatEnabled = vatEnabled,
                vatRate = prefs.vatRate
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Terms & Conditions
            TermsAndConditionsSection(
                terms = terms,
                onTermsChange = { viewModel.terms.value = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 8. Action Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onFinalize,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .testTag("btn_finalize_download")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalize & Download", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = onOpenPreview,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("btn_preview")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preview", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onSharePdf,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("btn_share_pdf")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.resetForm(keepDocType = true) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("btn_reset_form")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun LineItemEditorRow(
    index: Int,
    item: LineItem,
    currencySymbol: String,
    vatRate: Double,
    vatEnabled: Boolean,
    onUpdate: (LineItem) -> Unit,
    onDelete: () -> Unit
) {
    var priceText by remember(item.id) {
        mutableStateOf(
            if (item.price == 0.0) ""
            else if (item.price % 1.0 == 0.0) item.price.toLong().toString()
            else item.price.toString()
        )
    }
    var qtyText by remember(item.id) {
        mutableStateOf(item.qty.toString())
    }
    var discText by remember(item.id) {
        mutableStateOf(
            if (item.discountPercent == 0.0) ""
            else if (item.discountPercent % 1.0 == 0.0) item.discountPercent.toLong().toString()
            else item.discountPercent.toString()
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_row_$index"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Item #${index + 1}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description input
            OutlinedTextField(
                value = item.desc,
                onValueChange = { onUpdate(item.copy(desc = it)) },
                placeholder = { Text("Item / Service Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price, QTY, Discount%
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            priceText = filtered
                            val parsed = (filtered.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                            onUpdate(item.copy(price = parsed))
                        }
                    },
                    label = { Text("Price (excl)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(6.dp)
                )

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() }.take(6)
                        qtyText = filtered
                        val parsed = (filtered.toIntOrNull() ?: 1).coerceAtLeast(1)
                        onUpdate(item.copy(qty = parsed))
                    },
                    label = { Text("QTY") },
                    placeholder = { Text("1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(6.dp)
                )

                OutlinedTextField(
                    value = discText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            val parsed = (filtered.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
                            discText = if (parsed == 100.0 && (filtered.toDoubleOrNull() ?: 0.0) > 100.0) "100" else filtered
                            onUpdate(item.copy(discountPercent = parsed))
                        }
                    },
                    label = { Text("Disc %") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Live Calculated subtotal, VAT, Total incl
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Subtotal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = PdfGenerator.formatMoney(currencySymbol, item.subtotal),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(text = "VAT", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = PdfGenerator.formatMoney(currencySymbol, item.vatAmount(vatRate, vatEnabled)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total (incl)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = PdfGenerator.formatMoney(currencySymbol, item.totalAmount(vatRate, vatEnabled)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalsSummaryCard(
    totals: DocumentTotals,
    currencySymbol: String,
    vatEnabled: Boolean,
    vatRate: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            TotalLineRow(label = "Subtotal", value = PdfGenerator.formatMoney(currencySymbol, totals.subtotal))
            Spacer(modifier = Modifier.height(4.dp))
            TotalLineRow(label = "Total Discount", value = "- ${PdfGenerator.formatMoney(currencySymbol, totals.totalDiscount)}")
            Spacer(modifier = Modifier.height(4.dp))
            val vatLabel = if (vatEnabled) "VAT ($vatRate%)" else "VAT (not charged)"
            TotalLineRow(label = vatLabel, value = PdfGenerator.formatMoney(currencySymbol, totals.totalVat))

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grand Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = PdfGenerator.formatMoney(currencySymbol, totals.grandTotal),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("grand_total_display")
                )
            }
        }
    }
}

@Composable
private fun TotalLineRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun TermsAndConditionsSection(
    terms: String,
    onTermsChange: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(false) }

    val presetTemplates = listOf(
        "Due on Receipt" to "Payment is due immediately upon receipt of invoice.",
        "Net 30 Days" to "Strictly 30 days from date of invoice. Late payments subject to interest.",
        "Net 14 Days" to "Payment due within 14 calendar days from date of invoice.",
        "50% Deposit" to "50% non-refundable deposit required upon acceptance. Remaining 50% due on delivery.",
        "EFT Prior to Delivery" to "Goods will only be dispatched once EFT payment reflects in bank account.",
        "Title Retention" to "All goods remain the property of the seller until paid for in full."
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header row with Icon, Title, Subtitle, and Tool actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Terms & Conditions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Printed on document footer & quotation approvals",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Tool Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Paste from clipboard button
                    IconButton(
                        onClick = {
                            val clipboardText = clipboardManager.getText()?.text
                            if (!clipboardText.isNullOrBlank()) {
                                val clean = clipboardText.replace("\r\n", "\n").replace("\r", "\n")
                                val newTerms = if (terms.isBlank()) clean else "$terms\n$clean"
                                onTermsChange(newTerms)
                                Toast.makeText(context, "Pasted terms from clipboard", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste from clipboard",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Clear button
                    if (terms.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onTermsChange("")
                                Toast.makeText(context, "Terms cleared", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear terms",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Expand / Collapse toggle
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse terms view" else "Expand full terms view",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick preset pills row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetTemplates.forEach { (chipLabel, fullText) ->
                    AssistChip(
                        onClick = {
                            if (terms.isBlank()) {
                                onTermsChange(fullText)
                            } else if (!terms.contains(fullText)) {
                                onTermsChange("$terms\n$fullText")
                            }
                        },
                        label = { Text(chipLabel, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            0.8.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Robust multi-line text input
            OutlinedTextField(
                value = terms,
                onValueChange = { input ->
                    val clean = input.replace("\r\n", "\n").replace("\r", "\n")
                    onTermsChange(clean)
                },
                placeholder = {
                    Text(
                        text = "Enter or paste payment terms, warranty, banking instructions, or delivery conditions...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                minLines = 4,
                maxLines = if (isExpanded) Int.MAX_VALUE else 12,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("terms_input"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Bottom stats & status bar
            if (terms.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Full view active" else "Tap arrows above to expand editor",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val lineCount = terms.lines().count { it.isNotBlank() }
                    Text(
                        text = "$lineCount line${if (lineCount == 1) "" else "s"} • ${terms.length} chars",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun showDatePicker(context: Context, onDatePicked: (String) -> Unit) {
    val cal = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
            onDatePicked(formatted)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}
