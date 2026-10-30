package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ClientEntity
import com.example.data.DocumentEntity
import com.example.data.InvoiceRepository
import com.example.data.ProductEntity
import com.example.data.SnapshotEntity
import com.example.model.AppPreferences
import com.example.model.BankSettings
import com.example.model.CompanySettings
import com.example.model.DocumentStamp
import com.example.model.DocumentTotals
import com.example.model.DocumentType
import com.example.model.LineItem
import com.example.pdf.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ActiveModal {
    NONE,
    SETTINGS,
    CLIENTS,
    PRODUCTS,
    HISTORY,
    HELP,
    PREVIEW,
    PRIVACY_POLICY
}

class InvoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InvoiceRepository(AppDatabase.getDatabase(application))
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // --- State Variables ---
    private val _docType = MutableStateFlow(DocumentType.TAX_INVOICE)
    val docType: StateFlow<DocumentType> = _docType.asStateFlow()

    private val _docNumber = MutableStateFlow("INV-2026-001")
    val docNumber: StateFlow<String> = _docNumber.asStateFlow()

    val clientName = MutableStateFlow("")
    val clientAddress = MutableStateFlow("")
    val clientCell = MutableStateFlow("")
    val clientEmail = MutableStateFlow("")
    val accountNo = MutableStateFlow("")

    val docDate = MutableStateFlow(dateFormat.format(Date()))
    val dueDate = MutableStateFlow("")

    val stamp = MutableStateFlow(DocumentStamp.NONE)
    val vatEnabled = MutableStateFlow(true)
    val terms = MutableStateFlow("")

    val items = MutableStateFlow(listOf(LineItem(desc = "", price = 0.0, qty = 1, discountPercent = 0.0)))

    val companySettings = MutableStateFlow(CompanySettings())
    val bankSettings = MutableStateFlow(BankSettings())
    val preferences = MutableStateFlow(AppPreferences())

    val isDarkMode = MutableStateFlow(false)
    val activeModal = MutableStateFlow(ActiveModal.NONE)

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Room Flows
    val clientsList: StateFlow<List<ClientEntity>> = repository.getAllClients()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val productsList: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val documentsList: StateFlow<List<DocumentEntity>> = repository.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val snapshotsList: StateFlow<List<SnapshotEntity>> = repository.getAllSnapshots()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Live Totals calculation
    val totals: StateFlow<DocumentTotals> = combine(items, vatEnabled, preferences) { itemList, hasVat, prefs ->
        var subtotal = 0.0
        var totalDiscount = 0.0
        var totalVat = 0.0
        var grandTotal = 0.0

        for (item in itemList) {
            val s = item.price * item.qty
            val disc = s * (item.discountPercent / 100.0)
            val after = s - disc
            val vat = if (hasVat) after * (prefs.vatRate / 100.0) else 0.0
            val total = after + vat

            subtotal += s
            totalDiscount += disc
            totalVat += vat
            grandTotal += total
        }

        DocumentTotals(
            subtotal = subtotal,
            totalDiscount = totalDiscount,
            totalVat = totalVat,
            grandTotal = grandTotal
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        DocumentTotals(0.0, 0.0, 0.0, 0.0)
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val comp = repository.getCompanySettings()
            companySettings.value = comp

            val bank = repository.getBankSettings()
            bankSettings.value = bank

            val prefs = repository.getPreferences()
            preferences.value = prefs
            isDarkMode.value = prefs.isDarkMode

            val nextNum = repository.generateNextDocNumber(_docType.value)
            _docNumber.value = nextNum
        }
    }

    private fun getFutureDateString(daysAhead: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysAhead)
        return dateFormat.format(cal.time)
    }

    private var editingDocumentId: Long? = null

    fun setDocType(type: DocumentType) {
        _docType.value = type
        editingDocumentId = null
        viewModelScope.launch {
            _docNumber.value = repository.generateNextDocNumber(type)
        }
    }

    fun onClientSelected(client: ClientEntity) {
        clientName.value = client.name
        clientAddress.value = client.address
        clientCell.value = client.cell
        clientEmail.value = client.email
        accountNo.value = client.account
    }

    fun addItemRow(desc: String = "", price: Double = 0.0, qty: Int = 1, discount: Double = 0.0) {
        val current = items.value.toMutableList()
        current.add(LineItem(desc = desc, price = price, qty = qty, discountPercent = discount))
        items.value = current
    }

    fun updateItem(index: Int, updated: LineItem) {
        val current = items.value.toMutableList()
        if (index in current.indices) {
            current[index] = updated
            items.value = current
        }
    }

    fun removeItem(index: Int) {
        val current = items.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            if (current.isEmpty()) {
                current.add(LineItem())
            }
            items.value = current
        }
    }

    fun addProductToInvoice(product: ProductEntity) {
        // If the first row is completely empty, populate it instead of leaving a blank row above
        val current = items.value.toMutableList()
        if (current.size == 1 && current[0].desc.isBlank() && current[0].price == 0.0) {
            current[0] = LineItem(desc = product.desc, price = product.price, qty = 1, discountPercent = product.discountPercent)
            items.value = current
        } else {
            addItemRow(desc = product.desc, price = product.price, qty = 1, discount = product.discountPercent)
        }
        viewModelScope.launch {
            _toastEvent.emit("Added \"${product.desc}\" to invoice")
        }
    }

    fun finalizeAndGeneratePdf(context: Context, onPdfGenerated: (File) -> Unit) {
        val cName = clientName.value.trim()
        if (cName.isBlank()) {
            viewModelScope.launch {
                _toastEvent.emit("Please enter a client name before finalizing.")
            }
            return
        }

        val hasValidItem = items.value.any { it.desc.isNotBlank() || it.price > 0.0 }
        if (!hasValidItem) {
            viewModelScope.launch {
                _toastEvent.emit("Please add at least one line item with a description or price.")
            }
            return
        }

        viewModelScope.launch {
            // Auto-save client if not yet existing, or enrich blank fields on existing client
            val existing = clientsList.value.firstOrNull { it.name.equals(cName, ignoreCase = true) }
            if (existing == null) {
                repository.saveClient(
                    ClientEntity(
                        name = cName,
                        address = clientAddress.value.trim(),
                        cell = clientCell.value.trim(),
                        email = clientEmail.value.trim(),
                        account = accountNo.value.trim()
                    )
                )
            } else {
                val updatedClient = existing.copy(
                    address = clientAddress.value.trim().ifBlank { existing.address },
                    cell = clientCell.value.trim().ifBlank { existing.cell },
                    email = clientEmail.value.trim().ifBlank { existing.email },
                    account = accountNo.value.trim().ifBlank { existing.account }
                )
                if (updatedClient != existing) {
                    repository.saveClient(updatedClient)
                }
            }

            // Save or update document in History
            val isUpdatingExistingDoc = editingDocumentId != null
            val currentTotals = totals.value
            val docEntity = DocumentEntity(
                id = editingDocumentId ?: 0L,
                docType = _docType.value.title,
                docNumber = _docNumber.value,
                clientName = cName,
                clientAddress = clientAddress.value.trim(),
                clientCell = clientCell.value.trim(),
                clientEmail = clientEmail.value.trim(),
                accountNo = accountNo.value.trim(),
                docDate = docDate.value,
                dueDate = dueDate.value.trim(),
                stamp = stamp.value.label,
                vatEnabled = vatEnabled.value,
                vatRate = preferences.value.vatRate,
                currency = preferences.value.currencyCode,
                terms = terms.value.trim(),
                itemsJson = repository.serializeLineItems(items.value),
                subtotal = currentTotals.subtotal,
                totalDiscount = currentTotals.totalDiscount,
                totalVat = currentTotals.totalVat,
                grandTotal = currentTotals.grandTotal
            )
            repository.saveDocument(docEntity)

            // Generate PDF
            val pdfData = PdfGenerator.InvoicePdfData(
                docType = _docType.value,
                docNumber = _docNumber.value,
                clientName = cName,
                clientAddress = clientAddress.value.trim(),
                clientCell = clientCell.value.trim(),
                clientEmail = clientEmail.value.trim(),
                accountNo = accountNo.value.trim(),
                docDate = docDate.value,
                dueDate = dueDate.value.trim(),
                stamp = stamp.value,
                vatEnabled = vatEnabled.value,
                vatRate = preferences.value.vatRate,
                currencySymbol = preferences.value.currencySymbol,
                terms = terms.value.trim(),
                items = items.value,
                totals = currentTotals,
                company = companySettings.value,
                bank = bankSettings.value
            )

            try {
                val file = withContext(Dispatchers.IO) {
                    PdfGenerator.generatePdf(context, pdfData)
                }

                // Advance counter only for newly issued documents, not edits of historical docs
                if (!isUpdatingExistingDoc) {
                    repository.incrementCounter(_docType.value)
                }
                editingDocumentId = null
                val newDocNumber = repository.generateNextDocNumber(_docType.value)
                _docNumber.value = newDocNumber

                // Reset form fields
                resetFormFields(keepDocType = true)

                _toastEvent.emit(
                    if (isUpdatingExistingDoc) "Document updated! Ready for: $newDocNumber"
                    else "Document finalized! Ready for: $newDocNumber"
                )
                onPdfGenerated(file)
            } catch (e: Exception) {
                _toastEvent.emit("Error generating PDF: ${e.message}")
            }
        }
    }

    fun generateCurrentPdf(context: Context): File {
        val pdfData = PdfGenerator.InvoicePdfData(
            docType = _docType.value,
            docNumber = _docNumber.value,
            clientName = clientName.value.trim(),
            clientAddress = clientAddress.value.trim(),
            clientCell = clientCell.value.trim(),
            clientEmail = clientEmail.value.trim(),
            accountNo = accountNo.value.trim(),
            docDate = docDate.value,
            dueDate = dueDate.value.trim(),
            stamp = stamp.value,
            vatEnabled = vatEnabled.value,
            vatRate = preferences.value.vatRate,
            currencySymbol = preferences.value.currencySymbol,
            terms = terms.value.trim(),
            items = items.value,
            totals = totals.value,
            company = companySettings.value,
            bank = bankSettings.value
        )
        return PdfGenerator.generatePdf(context, pdfData)
    }

    fun generateCurrentPdfAsync(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val pdfData = PdfGenerator.InvoicePdfData(
                    docType = _docType.value,
                    docNumber = _docNumber.value,
                    clientName = clientName.value.trim(),
                    clientAddress = clientAddress.value.trim(),
                    clientCell = clientCell.value.trim(),
                    clientEmail = clientEmail.value.trim(),
                    accountNo = accountNo.value.trim(),
                    docDate = docDate.value,
                    dueDate = dueDate.value.trim(),
                    stamp = stamp.value,
                    vatEnabled = vatEnabled.value,
                    vatRate = preferences.value.vatRate,
                    currencySymbol = preferences.value.currencySymbol,
                    terms = terms.value.trim(),
                    items = items.value,
                    totals = totals.value,
                    company = companySettings.value,
                    bank = bankSettings.value
                )
                val file = withContext(Dispatchers.IO) {
                    PdfGenerator.generatePdf(context, pdfData)
                }
                onReady(file)
            } catch (e: Exception) {
                _toastEvent.emit("Error generating PDF: ${e.message}")
            }
        }
    }

    fun generateHistoryPdfAsync(context: Context, doc: DocumentEntity, onReady: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val pdfData = PdfGenerator.InvoicePdfData(
                        docType = DocumentType.fromString(doc.docType),
                        docNumber = doc.docNumber,
                        clientName = doc.clientName,
                        clientAddress = doc.clientAddress,
                        clientCell = doc.clientCell,
                        clientEmail = doc.clientEmail,
                        accountNo = doc.accountNo,
                        docDate = doc.docDate,
                        dueDate = doc.dueDate.trim(),
                        stamp = DocumentStamp.fromString(doc.stamp),
                        vatEnabled = doc.vatEnabled,
                        vatRate = doc.vatRate,
                        currencySymbol = preferences.value.currencySymbol,
                        terms = doc.terms,
                        items = repository.deserializeLineItems(doc.itemsJson),
                        totals = DocumentTotals(doc.subtotal, doc.totalDiscount, doc.totalVat, doc.grandTotal),
                        company = companySettings.value,
                        bank = bankSettings.value
                    )
                    PdfGenerator.generatePdf(context, pdfData)
                }
                onReady(file)
            } catch (e: Exception) {
                _toastEvent.emit("Error generating PDF: ${e.message}")
            }
        }
    }

    fun resetForm(keepDocType: Boolean = false) {
        resetFormFields(keepDocType)
        viewModelScope.launch {
            val newNum = repository.generateNextDocNumber(_docType.value)
            _docNumber.value = newNum
            _toastEvent.emit("Form reset.")
        }
    }

    private fun resetFormFields(keepDocType: Boolean) {
        editingDocumentId = null
        clientName.value = ""
        clientAddress.value = ""
        clientCell.value = ""
        clientEmail.value = ""
        accountNo.value = ""
        docDate.value = dateFormat.format(Date())
        dueDate.value = ""
        stamp.value = DocumentStamp.NONE
        vatEnabled.value = true
        terms.value = ""
        items.value = listOf(LineItem())

        if (!keepDocType) {
            _docType.value = DocumentType.TAX_INVOICE
        }
    }

    fun loadSavedDocument(doc: DocumentEntity) {
        editingDocumentId = doc.id
        _docType.value = DocumentType.fromString(doc.docType)
        _docNumber.value = doc.docNumber
        clientName.value = doc.clientName
        clientAddress.value = doc.clientAddress
        clientCell.value = doc.clientCell
        clientEmail.value = doc.clientEmail
        accountNo.value = doc.accountNo
        docDate.value = doc.docDate
        dueDate.value = doc.dueDate
        stamp.value = DocumentStamp.fromString(doc.stamp)
        vatEnabled.value = doc.vatEnabled
        terms.value = doc.terms

        val loadedItems = repository.deserializeLineItems(doc.itemsJson)
        items.value = if (loadedItems.isNotEmpty()) loadedItems else listOf(LineItem())

        activeModal.value = ActiveModal.NONE
        viewModelScope.launch {
            _toastEvent.emit("Editing ${doc.docNumber}")
        }
    }

    fun deserializeItems(json: String): List<LineItem> {
        return repository.deserializeLineItems(json)
    }

    fun deleteSavedDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            if (editingDocumentId == doc.id) {
                editingDocumentId = null
            }
            _toastEvent.emit("Document removed from history")
        }
    }

    // Settings actions
    fun saveCompany(settings: CompanySettings) {
        companySettings.value = settings
        viewModelScope.launch {
            repository.saveCompanySettings(settings)
            _toastEvent.emit("Company details saved")
        }
    }

    fun saveBank(settings: BankSettings) {
        bankSettings.value = settings
        viewModelScope.launch {
            repository.saveBankSettings(settings)
            _toastEvent.emit("Bank details saved")
        }
    }

    fun savePrefs(prefs: AppPreferences) {
        preferences.value = prefs
        viewModelScope.launch {
            repository.savePreferences(prefs)
            if (editingDocumentId == null) {
                _docNumber.value = repository.generateNextDocNumber(_docType.value)
            }
            _toastEvent.emit("Preferences saved")
        }
    }

    fun updateCounterDirect(docType: DocumentType, nextNum: Int) {
        viewModelScope.launch {
            repository.updateCounterManual(docType, nextNum)
            val updated = repository.getPreferences()
            preferences.value = updated
            if (_docType.value == docType && editingDocumentId == null) {
                _docNumber.value = repository.generateNextDocNumber(docType)
            }
        }
    }

    fun resetCountersToDefault() {
        viewModelScope.launch {
            repository.resetCounters()
            val updated = repository.getPreferences()
            preferences.value = updated
            if (editingDocumentId == null) {
                _docNumber.value = repository.generateNextDocNumber(_docType.value)
            }
            _toastEvent.emit("Document counters reset to 1")
        }
    }

    // Client Management
    fun saveClient(client: ClientEntity) {
        viewModelScope.launch {
            val isUpdate = client.id > 0
            repository.saveClient(client)
            if (clientName.value == client.name || (accountNo.value.isNotBlank() && accountNo.value == client.account)) {
                clientName.value = client.name
                clientAddress.value = client.address
                clientCell.value = client.cell
                clientEmail.value = client.email
                accountNo.value = client.account
            }
            _toastEvent.emit(if (isUpdate) "Client \"${client.name}\" updated" else "Client \"${client.name}\" saved")
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.deleteClient(client)
            _toastEvent.emit("Client removed")
        }
    }

    // Product Management
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            val isUpdate = product.id > 0
            repository.saveProduct(product)
            _toastEvent.emit(if (isUpdate) "Product \"${product.desc}\" updated" else "Product \"${product.desc}\" saved")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _toastEvent.emit("Product removed")
        }
    }

    fun toggleDarkMode() {
        val next = !isDarkMode.value
        isDarkMode.value = next
        viewModelScope.launch {
            val current = preferences.value
            repository.savePreferences(current.copy(isDarkMode = next))
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    suspend fun importBackupJson(jsonString: String, password: String? = null): InvoiceRepository.RestoreSummary {
        val result = repository.importBackupJson(jsonString, password)
        if (result.success) {
            loadInitialData()
            _toastEvent.emit(result.message)
        } else {
            _toastEvent.emit("Restore failed: ${result.message}")
        }
        return result
    }

    fun importBackup(
        jsonString: String,
        password: String? = null,
        onComplete: ((InvoiceRepository.RestoreSummary) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = importBackupJson(jsonString, password)
            onComplete?.invoke(result)
        }
    }

    fun createEncryptedBackup(context: Context, customPassword: String? = null, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val file = repository.createEncryptedBackupFile(context, customPassword)
                _toastEvent.emit("Encrypted backup generated")
                onComplete(file)
            } catch (e: Exception) {
                _toastEvent.emit("Backup failed: ${e.localizedMessage}")
            }
        }
    }

    fun exportCsvReport(context: Context, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val file = repository.exportCsvReportFile(context)
                _toastEvent.emit("Spreadsheet CSV report ready")
                onComplete(file)
            } catch (e: Exception) {
                _toastEvent.emit("Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun createSnapshot(title: String? = null, onComplete: ((SnapshotEntity) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val snapshot = repository.createSnapshot(title)
                _toastEvent.emit("Snapshot \"${snapshot.title}\" saved locally")
                onComplete?.invoke(snapshot)
            } catch (e: Exception) {
                _toastEvent.emit("Failed to create snapshot: ${e.localizedMessage}")
            }
        }
    }

    fun restoreSnapshot(snapshot: SnapshotEntity, onComplete: ((InvoiceRepository.RestoreSummary) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val result = repository.restoreSnapshot(snapshot)
                if (result.success) {
                    loadInitialData()
                    _toastEvent.emit("Restored snapshot \"${snapshot.title}\"")
                } else {
                    _toastEvent.emit("Snapshot restore failed: ${result.message}")
                }
                onComplete?.invoke(result)
            } catch (e: Exception) {
                _toastEvent.emit("Failed to restore snapshot: ${e.localizedMessage}")
            }
        }
    }

    fun deleteSnapshot(snapshotId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteSnapshot(snapshotId)
                _toastEvent.emit("Snapshot removed")
            } catch (e: Exception) {
                _toastEvent.emit("Failed to delete snapshot")
            }
        }
    }
}
