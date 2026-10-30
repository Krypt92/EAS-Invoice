package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR cell LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("DELETE FROM clients")
    suspend fun deleteAllClients()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clients: List<ClientEntity>)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY `desc` ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE `desc` LIKE '%' || :query || '%' ORDER BY `desc` ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    @Query("SELECT * FROM documents WHERE docType = :type ORDER BY createdAt DESC")
    fun getDocumentsByType(type: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity): Long

    @Delete
    suspend fun deleteDocument(doc: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<DocumentEntity>)
}

@Dao
interface SettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    fun getSettingFlow(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: SettingEntity)

    @Query("SELECT * FROM app_settings")
    suspend fun getAllSettings(): List<SettingEntity>
}

@Dao
interface SnapshotDao {
    @Query("SELECT * FROM snapshots ORDER BY timestamp DESC")
    fun getAllSnapshots(): Flow<List<SnapshotEntity>>

    @Query("SELECT * FROM snapshots WHERE id = :id")
    suspend fun getSnapshotById(id: Long): SnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: SnapshotEntity): Long

    @Delete
    suspend fun deleteSnapshot(snapshot: SnapshotEntity)

    @Query("DELETE FROM snapshots WHERE id = :id")
    suspend fun deleteSnapshotById(id: Long)

    @Query("DELETE FROM snapshots")
    suspend fun deleteAllSnapshots()
}
