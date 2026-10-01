package com.pulsechat.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_messages")
data class CachedMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val type: String,
    val text: String?,
    val mediaUrl: String?,
    val status: String,
    val createdAt: Long,
    val clientId: String?
)

@Entity(tableName = "pending_messages")
data class PendingMessageEntity(
    @PrimaryKey val clientId: String,
    val conversationId: String,
    val type: String,
    val text: String?,
    val localMediaPath: String?,
    val createdAt: Long
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM cached_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun observeMessages(conversationId: String): Flow<List<CachedMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<CachedMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: CachedMessageEntity)

    @Query("DELETE FROM cached_messages WHERE conversationId = :conversationId")
    suspend fun clearConversation(conversationId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueuePending(pending: PendingMessageEntity)

    @Query("SELECT * FROM pending_messages ORDER BY createdAt ASC")
    suspend fun getPending(): List<PendingMessageEntity>

    @Query("DELETE FROM pending_messages WHERE clientId = :clientId")
    suspend fun removePending(clientId: String)
}

@Database(
    entities = [CachedMessageEntity::class, PendingMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
