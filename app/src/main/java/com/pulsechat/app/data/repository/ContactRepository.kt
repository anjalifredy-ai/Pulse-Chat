package com.pulsechat.app.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pulsechat.app.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceContact(
    val name: String,
    val phoneNumber: String
)

@Singleton
class ContactRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun loadDeviceContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        val list = mutableListOf<DeviceContact>()
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = it.getString(nameIdx) ?: continue
                    val number = it.getString(numIdx)?.replace(" ", "")?.replace("-", "") ?: continue
                    if (number.length >= 7) {
                        list.add(DeviceContact(name, number))
                    }
                }
            }
        } catch (_: SecurityException) {
            // Permission denied
        }
        list.distinctBy { it.phoneNumber }
    }

    /** Match device contacts against registered Pulse users by phone number. */
    suspend fun discoverUsers(deviceContacts: List<DeviceContact>): List<User> {
        if (deviceContacts.isEmpty()) return emptyList()
        val phones = deviceContacts.map { normalizePhone(it.phoneNumber) }.toSet()
        // Firestore does not support large IN queries; batch in chunks of 10
        val results = mutableListOf<User>()
        phones.chunked(10).forEach { chunk ->
            try {
                val snap = firestore.collection("users")
                    .whereIn("phoneNumber", chunk)
                    .get()
                    .await()
                snap.documents.mapNotNull { it.toObject(User::class.java) }
                    .filter { it.uid != auth.currentUser?.uid }
                    .let { results.addAll(it) }
            } catch (_: Exception) {
            }
        }
        return results
    }

    suspend fun searchUsers(query: String): List<User> {
        if (query.isBlank()) return emptyList()
        return try {
            // Simple prefix search on displayName (requires composite index in production)
            val snap = firestore.collection("users")
                .orderBy("displayName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .await()
            snap.documents.mapNotNull { it.toObject(User::class.java) }
                .filter { it.uid != auth.currentUser?.uid }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun normalizePhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return if (digits.startsWith("0")) digits.drop(1) else digits
    }
}
