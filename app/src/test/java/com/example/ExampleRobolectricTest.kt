package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ClientEntity
import com.example.data.InvoiceRepository
import com.example.data.ProductEntity
import com.example.model.DocumentType
import com.example.model.LineItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: InvoiceRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = InvoiceRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("EAS-Invoice & Quote Generator", appName)
    }

    @Test
    fun `client insertion and retrieval in room database`() = runBlocking {
        val client = ClientEntity(
            name = "Acme Industrial Corp",
            address = "123 Industrial Way, Cape Town",
            cell = "+27 82 555 1234",
            email = "billing@acme.co.za",
            account = "ACC-ACME-01"
        )
        val id = repository.saveClient(client)
        assertTrue(id > 0)

        val clients = repository.getAllClients().first()
        assertEquals(1, clients.size)
        assertEquals("Acme Industrial Corp", clients[0].name)
        assertEquals("ACC-ACME-01", clients[0].account)
    }

    @Test
    fun `product insertion and retrieval`() = runBlocking {
        val product = ProductEntity(
            desc = "CCTV Maintenance & Calibration",
            price = 1250.0,
            discountPercent = 10.0
        )
        val id = repository.saveProduct(product)
        assertTrue(id > 0)

        val products = repository.getAllProducts().first()
        assertEquals(1, products.size)
        assertEquals("CCTV Maintenance & Calibration", products[0].desc)
        assertEquals(1250.0, products[0].price, 0.001)
    }

    @Test
    fun `line item calculation logic`() {
        val item = LineItem(
            desc = "Electrical Diagnostics",
            price = 1000.0,
            qty = 2,
            discountPercent = 10.0
        )
        // Subtotal = 2000
        assertEquals(2000.0, item.subtotal, 0.001)
        // Discount = 200
        assertEquals(200.0, item.discountAmount, 0.001)
        // Net after discount = 1800
        assertEquals(1800.0, item.afterDiscount, 0.001)
        // VAT @ 15% = 270
        assertEquals(270.0, item.vatAmount(15.0, true), 0.001)
        // Total = 2070
        assertEquals(2070.0, item.totalAmount(15.0, true), 0.001)

        // Without VAT
        assertEquals(0.0, item.vatAmount(15.0, false), 0.001)
        assertEquals(1800.0, item.totalAmount(15.0, false), 0.001)
    }

    @Test
    fun `document numbering generation and increment`() = runBlocking {
        val nextInvoice = repository.generateNextDocNumber(DocumentType.TAX_INVOICE)
        assertTrue(nextInvoice.startsWith("INV-"))

        val nextQuote = repository.generateNextDocNumber(DocumentType.QUOTATION)
        assertTrue(nextQuote.startsWith("Q-"))

        repository.incrementCounter(DocumentType.TAX_INVOICE)
        val updatedInvoice = repository.generateNextDocNumber(DocumentType.TAX_INVOICE)
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        assertEquals("INV-$currentYear-002", updatedInvoice)
    }

    @Test
    fun `pdf generator money formatting logic`() {
        assertEquals("$ 1,500.00", com.example.pdf.PdfGenerator.formatMoney("$", 1500.0))
        assertEquals("$ -150.00", com.example.pdf.PdfGenerator.formatMoney("$", -150.0))
        assertEquals("R 0.00", com.example.pdf.PdfGenerator.formatMoney("R", 0.0))
        assertEquals("€ 12,345.67", com.example.pdf.PdfGenerator.formatMoney("€", 12345.67))
    }
}
