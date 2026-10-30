package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String = "",
    val cell: String = "",
    val email: String = "",
    val account: String = ""
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val desc: String,
    val price: Double,
    val discountPercent: Double = 0.0
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docType: String,
    val docNumber: String,
    val clientName: String,
    val clientAddress: String = "",
    val clientCell: String = "",
    val clientEmail: String = "",
    val accountNo: String = "",
    val docDate: String = "",
    val dueDate: String = "",
    val stamp: String = "",
    val vatEnabled: Boolean = true,
    val vatRate: Double = 15.0,
    val currency: String = "ZAR",
    val terms: String = "",
    val itemsJson: String = "[]",
    val subtotal: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val totalVat: Double = 0.0,
    val grandTotal: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "snapshots")
data class SnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val documentCount: Int = 0,
    val clientCount: Int = 0,
    val productCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val dataJson: String
)
