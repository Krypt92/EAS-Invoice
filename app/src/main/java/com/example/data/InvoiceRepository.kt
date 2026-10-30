package com.example.data

import android.content.Context
import androidx.room.withTransaction
import com.example.model.AppPreferences
import com.example.model.BankSettings
import com.example.model.CompanySettings
import com.example.model.DocumentType
import com.example.model.LineItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class InvoiceRepository(private val database: AppDatabase) {

    private val clientDao = database.clientDao()
    private val productDao = database.productDao()
    private val documentDao = database.documentDao()
    private val settingDao = database.settingDao()
    private val snapshotDao = database.snapshotDao()

    // --- Clients ---
    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllClients()

    fun searchClients(query: String): Flow<List<ClientEntity>> = clientDao.searchClients(query)

    suspend fun saveClient(client: ClientEntity): Long = withContext(Dispatchers.IO) {
        if (client.id > 0) {
            clientDao.updateClient(client)
            client.id
        } else {
            clientDao.insertClient(client)
        }
    }

    suspend fun deleteClient(client: ClientEntity) = withContext(Dispatchers.IO) {
        clientDao.deleteClient(client)
    }

    // --- Products ---
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    suspend fun saveProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        if (product.id > 0) {
            productDao.updateProduct(product)
            product.id
        } else {
            productDao.insertProduct(product)
        }
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    // --- Documents History ---
    fun getAllDocuments(): Flow<List<DocumentEntity>> = documentDao.getAllDocuments()

    suspend fun saveDocument(doc: DocumentEntity): Long = withContext(Dispatchers.IO) {
        documentDao.insertDocument(doc)
    }

    suspend fun deleteDocument(doc: DocumentEntity) = withContext(Dispatchers.IO) {
        documentDao.deleteDocument(doc)
    }

    // --- Settings & Preferences ---
    suspend fun getCompanySettings(): CompanySettings = withContext(Dispatchers.IO) {
        val raw = settingDao.getSetting("companySettings") ?: return@withContext CompanySettings()
        try {
            val json = JSONObject(raw)
            CompanySettings(
                name = json.optString("name", ""),
                address = json.optString("address", ""),
                contact = json.optString("contact", ""),
                email = json.optString("email", ""),
                website = json.optString("website", ""),
                vat = json.optString("vat", ""),
                reg = json.optString("reg", ""),
                logoBase64 = if (json.has("logoBase64") && !json.isNull("logoBase64")) {
                    val str = json.getString("logoBase64")
                    if (str.isNotBlank() && str != "null") str else null
                } else null
            )
        } catch (e: Exception) {
            CompanySettings()
        }
    }

    suspend fun saveCompanySettings(settings: CompanySettings) = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("name", settings.name)
            put("address", settings.address)
            put("contact", settings.contact)
            put("email", settings.email)
            put("website", settings.website)
            put("vat", settings.vat)
            put("reg", settings.reg)
            if (!settings.logoBase64.isNullOrBlank()) {
                put("logoBase64", settings.logoBase64)
            } else {
                put("logoBase64", JSONObject.NULL)
            }
        }
        settingDao.saveSetting(SettingEntity("companySettings", json.toString()))
    }

    suspend fun getBankSettings(): BankSettings = withContext(Dispatchers.IO) {
        val raw = settingDao.getSetting("bankSettings") ?: return@withContext BankSettings()
        try {
            val json = JSONObject(raw)
            BankSettings(
                holder = json.optString("holder", ""),
                number = json.optString("number", ""),
                name = json.optString("name", ""),
                branch = json.optString("branch", ""),
                ref = json.optString("ref", "")
            )
        } catch (e: Exception) {
            BankSettings()
        }
    }

    suspend fun saveBankSettings(settings: BankSettings) = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("holder", settings.holder)
            put("number", settings.number)
            put("name", settings.name)
            put("branch", settings.branch)
            put("ref", settings.ref)
        }
        settingDao.saveSetting(SettingEntity("bankSettings", json.toString()))
    }

    suspend fun getPreferences(): AppPreferences = withContext(Dispatchers.IO) {
        val raw = settingDao.getSetting("appPreferences") ?: return@withContext AppPreferences()
        try {
            val json = JSONObject(raw)
            AppPreferences(
                currencyCode = json.optString("currencyCode", "ZAR"),
                vatRate = json.optDouble("vatRate", 15.0),
                nextInvoiceNumber = json.optInt("nextInvoiceNumber", 1),
                nextQuoteNumber = json.optInt("nextQuoteNumber", 1),
                nextProformaNumber = json.optInt("nextProformaNumber", 1),
                isDarkMode = json.optBoolean("isDarkMode", false)
            )
        } catch (e: Exception) {
            AppPreferences()
        }
    }

    suspend fun savePreferences(prefs: AppPreferences) = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("currencyCode", prefs.currencyCode)
            put("vatRate", prefs.vatRate)
            put("nextInvoiceNumber", prefs.nextInvoiceNumber)
            put("nextQuoteNumber", prefs.nextQuoteNumber)
            put("nextProformaNumber", prefs.nextProformaNumber)
            put("isDarkMode", prefs.isDarkMode)
        }
        settingDao.saveSetting(SettingEntity("appPreferences", json.toString()))
    }

    suspend fun generateNextDocNumber(docType: DocumentType): String = withContext(Dispatchers.IO) {
        val prefs = getPreferences()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val num = when (docType) {
            DocumentType.TAX_INVOICE -> prefs.nextInvoiceNumber
            DocumentType.QUOTATION -> prefs.nextQuoteNumber
            DocumentType.PROFORMA_INVOICE -> prefs.nextProformaNumber
        }
        val formattedNum = num.toString().padStart(3, '0')
        "${docType.prefix}-$currentYear-$formattedNum"
    }

    suspend fun incrementCounter(docType: DocumentType) = withContext(Dispatchers.IO) {
        val prefs = getPreferences()
        val updated = when (docType) {
            DocumentType.TAX_INVOICE -> prefs.copy(nextInvoiceNumber = prefs.nextInvoiceNumber + 1)
            DocumentType.QUOTATION -> prefs.copy(nextQuoteNumber = prefs.nextQuoteNumber + 1)
            DocumentType.PROFORMA_INVOICE -> prefs.copy(nextProformaNumber = prefs.nextProformaNumber + 1)
        }
        savePreferences(updated)
    }

    suspend fun updateCounterManual(docType: DocumentType, nextNum: Int) = withContext(Dispatchers.IO) {
        val prefs = getPreferences()
        val updated = when (docType) {
            DocumentType.TAX_INVOICE -> prefs.copy(nextInvoiceNumber = nextNum)
            DocumentType.QUOTATION -> prefs.copy(nextQuoteNumber = nextNum)
            DocumentType.PROFORMA_INVOICE -> prefs.copy(nextProformaNumber = nextNum)
        }
        savePreferences(updated)
    }

    suspend fun resetCounters() = withContext(Dispatchers.IO) {
        val prefs = getPreferences()
        savePreferences(prefs.copy(nextInvoiceNumber = 1, nextQuoteNumber = 1, nextProformaNumber = 1))
    }

    // --- Serialization Helpers for Line Items ---
    fun serializeLineItems(items: List<LineItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("desc", item.desc)
                put("price", item.price)
                put("qty", item.qty)
                put("discountPercent", item.discountPercent)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeLineItems(jsonString: String): List<LineItem> {
        val list = mutableListOf<LineItem>()
        if (jsonString.isBlank()) return list
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    LineItem(
                        id = obj.optString("id"),
                        desc = obj.optString("desc", ""),
                        price = obj.optDouble("price", 0.0),
                        qty = obj.optInt("qty", 1),
                        discountPercent = obj.optDouble("discountPercent", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    data class RestoreSummary(
        val success: Boolean,
        val documentCount: Int = 0,
        val clientCount: Int = 0,
        val productCount: Int = 0,
        val message: String = ""
    )

    // --- JSON Backup Export / Import ---
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val company = getCompanySettings()
        val bank = getBankSettings()
        val prefs = getPreferences()
        val clients = clientDao.getAllClients().firstOrNull() ?: emptyList()
        val products = productDao.getAllProducts().firstOrNull() ?: emptyList()
        val documents = documentDao.getAllDocuments().firstOrNull() ?: emptyList()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val root = JSONObject().apply {
            put("version", "2.0")
            put("app", "EAS-Invoice & Quote Generator")
            put("exportedAt", System.currentTimeMillis())

            put("company", JSONObject().apply {
                put("name", company.name)
                put("address", company.address)
                put("contact", company.contact)
                put("email", company.email)
                put("website", company.website)
                put("vat", company.vat)
                put("reg", company.reg)
            })

            put("bank", JSONObject().apply {
                put("holder", bank.holder)
                put("number", bank.number)
                put("name", bank.name)
                put("branch", bank.branch)
                put("ref", bank.ref)
            })

            put("clients", JSONArray().apply {
                for (c in clients) {
                    put(JSONObject().apply {
                        put("name", c.name)
                        put("address", c.address)
                        put("cell", c.cell)
                        put("email", c.email)
                        put("account", c.account)
                    })
                }
            })

            put("products", JSONArray().apply {
                for (p in products) {
                    put(JSONObject().apply {
                        put("desc", p.desc)
                        put("price", p.price)
                        put("discount", p.discountPercent)
                    })
                }
            })

            put("documents", JSONArray().apply {
                for (d in documents) {
                    put(JSONObject().apply {
                        put("docType", d.docType)
                        put("docNumber", d.docNumber)
                        put("clientName", d.clientName)
                        put("clientAddress", d.clientAddress)
                        put("clientCell", d.clientCell)
                        put("clientEmail", d.clientEmail)
                        put("accountNo", d.accountNo)
                        put("docDate", d.docDate)
                        put("dueDate", d.dueDate)
                        put("stamp", d.stamp)
                        put("vatEnabled", d.vatEnabled)
                        put("vatRate", d.vatRate)
                        put("currency", d.currency)
                        put("terms", d.terms)
                        put("itemsJson", d.itemsJson)
                        put("subtotal", d.subtotal)
                        put("totalDiscount", d.totalDiscount)
                        put("totalVat", d.totalVat)
                        put("grandTotal", d.grandTotal)
                        put("createdAt", d.createdAt)
                    })
                }
            })

            put("counters", JSONObject().apply {
                put("invoice", JSONObject().apply {
                    put("year", currentYear)
                    put("next", prefs.nextInvoiceNumber)
                })
                put("quote", JSONObject().apply {
                    put("year", currentYear)
                    put("next", prefs.nextQuoteNumber)
                })
                put("proforma", JSONObject().apply {
                    put("year", currentYear)
                    put("next", prefs.nextProformaNumber)
                })
            })

            put("preferences", JSONObject().apply {
                put("vatRate", prefs.vatRate)
                put("currency", prefs.currencyCode)
                put("isDarkMode", prefs.isDarkMode)
            })

            if (company.logoBase64 != null) {
                put("logo", company.logoBase64)
            }
        }
        root.toString(2)
    }

    suspend fun createEncryptedBackupFile(context: Context, customPassword: String? = null): File = withContext(Dispatchers.IO) {
        val jsonPayload = exportBackupJson()
        val documents = documentDao.getAllDocuments().firstOrNull() ?: emptyList()
        val clients = clientDao.getAllClients().firstOrNull() ?: emptyList()
        val products = productDao.getAllProducts().firstOrNull() ?: emptyList()
        val company = getCompanySettings()

        val meta = mapOf(
            "documentCount" to documents.size,
            "clientCount" to clients.size,
            "productCount" to products.size,
            "companyName" to (company.name.ifBlank { "EAS-Invoice" })
        )

        val encryptedJson = BackupCrypto.encrypt(jsonPayload, customPassword, meta)
        val fileDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "EAS_Invoice_Encrypted_Backup_$fileDate.eas.json"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { fos ->
            fos.write(encryptedJson.toByteArray(Charsets.UTF_8))
            fos.flush()
        }
        file
    }

    suspend fun exportCsvReportFile(context: Context): File = withContext(Dispatchers.IO) {
        val documents = documentDao.getAllDocuments().firstOrNull() ?: emptyList()
        CsvExporter.exportCsvToFile(context, documents)
    }

    suspend fun importBackupJson(rawJsonString: String, password: String? = null): RestoreSummary = withContext(Dispatchers.IO) {
        try {
            val jsonString = if (BackupCrypto.isEncryptedBackup(rawJsonString)) {
                BackupCrypto.decrypt(rawJsonString, password)
            } else {
                rawJsonString
            }

            val root = JSONObject(jsonString)

            val hasRecognizedKeys = root.has("company") || root.has("bank") ||
                root.has("clients") || root.has("products") || root.has("documents") ||
                root.has("preferences") || root.has("counters")
            if (!hasRecognizedKeys) {
                return@withContext RestoreSummary(
                    success = false,
                    message = "Unrecognized backup format. No valid invoice, client, or company data found."
                )
            }

            // 1. Parse everything into memory first so a malformed entry never wipes existing records
            val parsedCompany: CompanySettings? = if (root.has("company")) {
                val c = root.getJSONObject("company")
                val importedLogo = if (root.has("logo") && !root.isNull("logo")) {
                    val str = root.getString("logo")
                    if (str.isNotBlank() && str != "null") str else null
                } else null
                CompanySettings(
                    name = c.optString("name", ""),
                    address = c.optString("address", ""),
                    contact = c.optString("contact", ""),
                    email = c.optString("email", ""),
                    website = c.optString("website", ""),
                    vat = c.optString("vat", ""),
                    reg = c.optString("reg", ""),
                    logoBase64 = importedLogo
                )
            } else null

            val parsedBank: BankSettings? = if (root.has("bank")) {
                val b = root.getJSONObject("bank")
                BankSettings(
                    holder = b.optString("holder", ""),
                    number = b.optString("number", ""),
                    name = b.optString("name", ""),
                    branch = b.optString("branch", ""),
                    ref = b.optString("ref", "")
                )
            } else null

            val parsedClients: List<ClientEntity>? = if (root.has("clients")) {
                val array = root.getJSONArray("clients")
                val list = mutableListOf<ClientEntity>()
                for (i in 0 until array.length()) {
                    val c = array.getJSONObject(i)
                    list.add(
                        ClientEntity(
                            name = c.optString("name", ""),
                            address = c.optString("address", ""),
                            cell = c.optString("cell", ""),
                            email = c.optString("email", ""),
                            account = c.optString("account", "")
                        )
                    )
                }
                list
            } else null

            val parsedProducts: List<ProductEntity>? = if (root.has("products")) {
                val array = root.getJSONArray("products")
                val list = mutableListOf<ProductEntity>()
                for (i in 0 until array.length()) {
                    val p = array.getJSONObject(i)
                    list.add(
                        ProductEntity(
                            desc = p.optString("desc", ""),
                            price = p.optDouble("price", 0.0).coerceAtLeast(0.0),
                            discountPercent = p.optDouble("discount", 0.0).coerceIn(0.0, 100.0)
                        )
                    )
                }
                list
            } else null

            val parsedDocuments: List<DocumentEntity>? = if (root.has("documents")) {
                val array = root.getJSONArray("documents")
                val list = mutableListOf<DocumentEntity>()
                for (i in 0 until array.length()) {
                    val d = array.getJSONObject(i)
                    list.add(
                        DocumentEntity(
                            docType = d.optString("docType", "Tax Invoice"),
                            docNumber = d.optString("docNumber", ""),
                            clientName = d.optString("clientName", ""),
                            clientAddress = d.optString("clientAddress", ""),
                            clientCell = d.optString("clientCell", ""),
                            clientEmail = d.optString("clientEmail", ""),
                            accountNo = d.optString("accountNo", ""),
                            docDate = d.optString("docDate", ""),
                            dueDate = d.optString("dueDate", ""),
                            stamp = d.optString("stamp", ""),
                            vatEnabled = d.optBoolean("vatEnabled", true),
                            vatRate = d.optDouble("vatRate", 15.0).coerceIn(0.0, 100.0),
                            currency = d.optString("currency", "ZAR"),
                            terms = d.optString("terms", ""),
                            itemsJson = d.optString("itemsJson", "[]"),
                            subtotal = d.optDouble("subtotal", 0.0),
                            totalDiscount = d.optDouble("totalDiscount", 0.0),
                            totalVat = d.optDouble("totalVat", 0.0),
                            grandTotal = d.optDouble("grandTotal", 0.0),
                            createdAt = d.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                list
            } else null

            val currentPrefs = getPreferences()
            var nextInv = currentPrefs.nextInvoiceNumber
            var nextQ = currentPrefs.nextQuoteNumber
            var nextPI = currentPrefs.nextProformaNumber

            if (root.has("counters")) {
                val counters = root.getJSONObject("counters")
                if (counters.has("invoice")) nextInv = counters.getJSONObject("invoice").optInt("next", nextInv).coerceAtLeast(1)
                if (counters.has("quote")) nextQ = counters.getJSONObject("quote").optInt("next", nextQ).coerceAtLeast(1)
                if (counters.has("proforma")) nextPI = counters.getJSONObject("proforma").optInt("next", nextPI).coerceAtLeast(1)
            }

            var vatRate = currentPrefs.vatRate
            var currency = currentPrefs.currencyCode
            var darkMode = currentPrefs.isDarkMode
            if (root.has("preferences")) {
                val p = root.getJSONObject("preferences")
                vatRate = p.optDouble("vatRate", vatRate).coerceIn(0.0, 100.0)
                currency = p.optString("currency", currency)
                darkMode = p.optBoolean("isDarkMode", darkMode)
            }

            // 2. Apply all database mutations atomically
            database.withTransaction {
                if (parsedClients != null) {
                    clientDao.deleteAllClients()
                    clientDao.insertAll(parsedClients)
                }
                if (parsedProducts != null) {
                    productDao.deleteAllProducts()
                    productDao.insertAll(parsedProducts)
                }
                if (parsedDocuments != null) {
                    documentDao.deleteAllDocuments()
                    documentDao.insertAll(parsedDocuments)
                }
            }

            if (parsedCompany != null) saveCompanySettings(parsedCompany)
            if (parsedBank != null) saveBankSettings(parsedBank)

            savePreferences(
                currentPrefs.copy(
                    nextInvoiceNumber = nextInv,
                    nextQuoteNumber = nextQ,
                    nextProformaNumber = nextPI,
                    vatRate = vatRate,
                    currencyCode = currency,
                    isDarkMode = darkMode
                )
            )

            val restoredDocsCount = parsedDocuments?.size ?: 0
            val restoredClientsCount = parsedClients?.size ?: 0
            val restoredProductsCount = parsedProducts?.size ?: 0

            RestoreSummary(
                success = true,
                documentCount = restoredDocsCount,
                clientCount = restoredClientsCount,
                productCount = restoredProductsCount,
                message = "Restored $restoredDocsCount invoices/quotes, $restoredClientsCount clients, and company settings."
            )
        } catch (e: Exception) {
            e.printStackTrace()
            RestoreSummary(
                success = false,
                message = e.localizedMessage ?: "Failed to import backup"
            )
        }
    }

    // --- Internal Device Snapshots (Offline Restore Points) ---
    fun getAllSnapshots(): Flow<List<SnapshotEntity>> = snapshotDao.getAllSnapshots()

    suspend fun createSnapshot(customTitle: String? = null): SnapshotEntity = withContext(Dispatchers.IO) {
        val documents = documentDao.getAllDocuments().firstOrNull() ?: emptyList()
        val clients = clientDao.getAllClients().firstOrNull() ?: emptyList()
        val products = productDao.getAllProducts().firstOrNull() ?: emptyList()
        val totalRevenue = documents.sumOf { it.grandTotal }
        val dataJson = exportBackupJson()

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val title = if (!customTitle.isNullOrBlank()) {
            customTitle.trim()
        } else {
            "Snapshot - ${dateFormat.format(Date())}"
        }

        val snapshot = SnapshotEntity(
            title = title,
            timestamp = System.currentTimeMillis(),
            documentCount = documents.size,
            clientCount = clients.size,
            productCount = products.size,
            totalRevenue = totalRevenue,
            dataJson = dataJson
        )

        val id = snapshotDao.insertSnapshot(snapshot)
        snapshot.copy(id = id)
    }

    suspend fun restoreSnapshot(snapshot: SnapshotEntity): RestoreSummary = withContext(Dispatchers.IO) {
        importBackupJson(snapshot.dataJson)
    }

    suspend fun deleteSnapshot(id: Long) = withContext(Dispatchers.IO) {
        snapshotDao.deleteSnapshotById(id)
    }
}
