package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.data.local.TallyDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class TallyRepository(
    private val tallyDao: TallyDao,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tallykhata_prefs", Context.MODE_PRIVATE)

    private val _userSession = MutableStateFlow(loadUserSession())
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private fun loadUserSession(): UserSession {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val authType = prefs.getString("auth_type", "PHONE") ?: "PHONE"
        val userIdentifier = prefs.getString("user_identifier", "") ?: ""
        val displayName = prefs.getString("display_name", "") ?: ""
        val shopName = prefs.getString("shop_name", "") ?: ""
        val avatarUrl = prefs.getString("avatar_url", "") ?: ""
        val connectedGoogleEmail = prefs.getString("google_email", "") ?: ""
        val lastBackupTimestamp = prefs.getLong("last_backup_time", 0L)
        val autoBackupEnabled = prefs.getBoolean("auto_backup_enabled", true)

        return UserSession(
            isLoggedIn = isLoggedIn,
            authType = authType,
            userIdentifier = userIdentifier,
            displayName = displayName,
            shopName = shopName,
            avatarUrl = avatarUrl,
            connectedGoogleEmail = connectedGoogleEmail,
            lastBackupTimestamp = lastBackupTimestamp,
            autoBackupEnabled = autoBackupEnabled
        )
    }

    suspend fun saveSession(session: UserSession) {
        prefs.edit().apply {
            putBoolean("is_logged_in", session.isLoggedIn)
            putString("auth_type", session.authType)
            putString("user_identifier", session.userIdentifier)
            putString("display_name", session.displayName)
            putString("shop_name", session.shopName)
            putString("avatar_url", session.avatarUrl)
            putString("google_email", session.connectedGoogleEmail)
            putLong("last_backup_time", session.lastBackupTimestamp)
            putBoolean("auto_backup_enabled", session.autoBackupEnabled)
            apply()
        }
        _userSession.value = session
    }

    suspend fun loginWithPhone(phoneNumber: String, shopName: String, ownerName: String) {
        val session = UserSession(
            isLoggedIn = true,
            authType = "PHONE",
            userIdentifier = phoneNumber,
            displayName = ownerName.ifBlank { "দোকানদার" },
            shopName = shopName.ifBlank { "আমার দোকান" },
            connectedGoogleEmail = "",
            lastBackupTimestamp = 0L,
            autoBackupEnabled = true
        )
        saveSession(session)

        // Also update the business profile
        val currentProfile = tallyDao.getBusinessProfileSync()
        tallyDao.insertOrUpdateProfile(
            BusinessProfile(
                id = 1,
                shopName = session.shopName,
                ownerName = session.displayName,
                phone = phoneNumber,
                address = currentProfile?.address ?: "বাংলাদেশ",
                currencySymbol = currentProfile?.currencySymbol ?: "৳",
                language = currentProfile?.language ?: "bn"
            )
        )
    }

    suspend fun loginWithGoogle(email: String, displayName: String, avatarUrl: String) {
        val currentProfile = tallyDao.getBusinessProfileSync()
        val shop = if (currentProfile?.shopName.isNullOrBlank() || currentProfile?.shopName == "আমার ব্যবসা প্রতিষ্ঠান") {
            "${displayName}-এর দোকান"
        } else {
            currentProfile!!.shopName
        }

        val session = UserSession(
            isLoggedIn = true,
            authType = "GOOGLE",
            userIdentifier = email,
            displayName = displayName,
            shopName = shop,
            avatarUrl = avatarUrl,
            connectedGoogleEmail = email,
            lastBackupTimestamp = 0L,
            autoBackupEnabled = true
        )
        saveSession(session)

        tallyDao.insertOrUpdateProfile(
            BusinessProfile(
                id = 1,
                shopName = shop,
                ownerName = displayName,
                phone = currentProfile?.phone ?: "",
                address = currentProfile?.address ?: "বাংলাদেশ",
                currencySymbol = currentProfile?.currencySymbol ?: "৳",
                language = currentProfile?.language ?: "bn"
            )
        )
    }

    suspend fun connectGoogleAccountForBackup(googleEmail: String) {
        val current = _userSession.value
        val updated = current.copy(connectedGoogleEmail = googleEmail)
        saveSession(updated)
    }

    suspend fun logout() {
        prefs.edit().clear().apply()
        _userSession.value = UserSession(isLoggedIn = false)
    }

    // --- Accounts Flow ---
    fun getAllAccounts(): Flow<List<ContactAccount>> = tallyDao.getAllAccounts()

    fun getAccountsByType(type: AccountType): Flow<List<ContactAccount>> = tallyDao.getAccountsByType(type)

    fun getAccountById(id: Long): Flow<ContactAccount?> = tallyDao.getAccountById(id)

    suspend fun insertAccount(account: ContactAccount): Long = tallyDao.insertAccount(account)

    suspend fun updateAccount(account: ContactAccount) = tallyDao.updateAccount(account)

    suspend fun deleteAccount(account: ContactAccount) {
        tallyDao.deleteTransactionsForAccount(account.id)
        tallyDao.deleteAccount(account)
    }

    // --- Transactions Flow ---
    fun getTransactionsForAccount(accountId: Long): Flow<List<KhataTransaction>> =
        tallyDao.getTransactionsForAccount(accountId)

    suspend fun recordTransaction(
        accountId: Long,
        amount: Double,
        type: TransactionType,
        note: String,
        billNumber: String,
        syncWithCashBox: Boolean,
        paymentMode: String
    ) {
        val account = tallyDao.getAccountByIdSync(accountId) ?: return

        // Customer Logic: GAVE (+ due / receivable), RECEIVED (- due / payment)
        // Supplier Logic: TAKEN (- payable / goods taken on credit), PAID (+ payment made)
        val balanceDelta = when (type) {
            TransactionType.GAVE -> amount
            TransactionType.RECEIVED -> -amount
            TransactionType.TAKEN -> -amount
            TransactionType.PAID -> amount
        }

        val newBalance = account.currentBalance + balanceDelta
        val now = System.currentTimeMillis()

        // 1. Insert Transaction
        val transaction = KhataTransaction(
            accountId = accountId,
            amount = amount,
            type = type,
            note = note,
            billNumber = billNumber,
            timestamp = now,
            runningBalance = newBalance
        )
        tallyDao.insertTransaction(transaction)

        // 2. Update Account
        val updatedAccount = account.copy(
            currentBalance = newBalance,
            updatedAt = now
        )
        tallyDao.updateAccount(updatedAccount)

        // 3. Sync with CashBox if chosen
        if (syncWithCashBox) {
            val isCashIn = (type == TransactionType.RECEIVED)
            val category = if (isCashIn) "বাকি আদায় (${account.name})" else "সাপ্লায়ার পরিশোধ (${account.name})"
            tallyDao.insertCashEntry(
                CashEntry(
                    amount = amount,
                    isCashIn = isCashIn,
                    category = category,
                    paymentMode = paymentMode,
                    note = "খাতা অটো সিঙ্ক: $note",
                    timestamp = now
                )
            )
        }
    }

    // --- Cash Box Flow ---
    fun getAllCashEntries(): Flow<List<CashEntry>> = tallyDao.getAllCashEntries()

    suspend fun insertCashEntry(entry: CashEntry): Long = tallyDao.insertCashEntry(entry)

    suspend fun deleteCashEntry(entry: CashEntry) = tallyDao.deleteCashEntry(entry)

    // --- Cash Memos Flow ---
    fun getAllCashMemos(): Flow<List<CashMemo>> = tallyDao.getAllCashMemos()

    suspend fun insertCashMemo(memo: CashMemo): Long = tallyDao.insertCashMemo(memo)

    suspend fun deleteCashMemo(memo: CashMemo) = tallyDao.deleteCashMemo(memo)

    // --- Business Profile ---
    fun getBusinessProfile(): Flow<BusinessProfile?> = tallyDao.getBusinessProfile()

    suspend fun updateBusinessProfile(profile: BusinessProfile) = tallyDao.insertOrUpdateProfile(profile)

    // ==========================================
    // OFFLINE BACKUP & RESTORE ENGINE (JSON / DRIVE SYNC)
    // ==========================================

    suspend fun createBackupJsonString(): String = withContext(Dispatchers.IO) {
        val accounts = tallyDao.getAllAccountsSync()
        val transactions = tallyDao.getAllTransactionsSync()
        val cashEntries = tallyDao.getAllCashEntriesSync()
        val cashMemos = tallyDao.getAllCashMemosSync()
        val profile = tallyDao.getBusinessProfileSync()

        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "TallyKhata")
        root.put("createdAt", System.currentTimeMillis())
        root.put("googleEmail", _userSession.value.connectedGoogleEmail)

        // Profile
        if (profile != null) {
            val pObj = JSONObject().apply {
                put("shopName", profile.shopName)
                put("ownerName", profile.ownerName)
                put("phone", profile.phone)
                put("address", profile.address)
                put("currencySymbol", profile.currencySymbol)
                put("language", profile.language)
            }
            root.put("profile", pObj)
        }

        // Accounts
        val accArray = JSONArray()
        accounts.forEach { acc ->
            val obj = JSONObject().apply {
                put("id", acc.id)
                put("name", acc.name)
                put("phone", acc.phone)
                put("address", acc.address)
                put("type", acc.type.name)
                put("currentBalance", acc.currentBalance)
                put("createdAt", acc.createdAt)
                put("updatedAt", acc.updatedAt)
            }
            accArray.put(obj)
        }
        root.put("accounts", accArray)

        // Transactions
        val txArray = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("accountId", tx.accountId)
                put("amount", tx.amount)
                put("type", tx.type.name)
                put("note", tx.note)
                put("billNumber", tx.billNumber)
                put("timestamp", tx.timestamp)
                put("runningBalance", tx.runningBalance)
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // Cash Entries
        val ceArray = JSONArray()
        cashEntries.forEach { ce ->
            val obj = JSONObject().apply {
                put("id", ce.id)
                put("amount", ce.amount)
                put("isCashIn", ce.isCashIn)
                put("category", ce.category)
                put("paymentMode", ce.paymentMode)
                put("note", ce.note)
                put("timestamp", ce.timestamp)
            }
            ceArray.put(obj)
        }
        root.put("cashEntries", ceArray)

        // Cash Memos
        val memoArray = JSONArray()
        cashMemos.forEach { cm ->
            val obj = JSONObject().apply {
                put("id", cm.id)
                put("memoNumber", cm.memoNumber)
                put("customerName", cm.customerName)
                put("customerPhone", cm.customerPhone)
                put("itemsSummary", cm.itemsSummary)
                put("subtotal", cm.subtotal)
                put("discount", cm.discount)
                put("totalAmount", cm.totalAmount)
                put("paidAmount", cm.paidAmount)
                put("dueAmount", cm.dueAmount)
                put("notes", cm.notes)
                put("timestamp", cm.timestamp)
            }
            memoArray.put(obj)
        }
        root.put("cashMemos", memoArray)

        // Update last backup timestamp
        val now = System.currentTimeMillis()
        saveSession(_userSession.value.copy(lastBackupTimestamp = now))

        root.toString(2)
    }

    suspend fun restoreFromJsonString(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("accounts") || !root.has("transactions")) return@withContext false

            // Clear old data
            tallyDao.clearAllAccounts()
            tallyDao.clearAllTransactions()
            tallyDao.clearAllCashEntries()
            tallyDao.clearAllCashMemos()

            // Restore Profile
            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                tallyDao.insertOrUpdateProfile(
                    BusinessProfile(
                        id = 1,
                        shopName = p.optString("shopName", "আমার দোকান"),
                        ownerName = p.optString("ownerName", "মালিক"),
                        phone = p.optString("phone", ""),
                        address = p.optString("address", "বাংলাদেশ"),
                        currencySymbol = p.optString("currencySymbol", "৳"),
                        language = p.optString("language", "bn")
                    )
                )
            }

            // Restore Accounts
            val accArray = root.getJSONArray("accounts")
            val accountsList = mutableListOf<ContactAccount>()
            for (i in 0 until accArray.length()) {
                val obj = accArray.getJSONObject(i)
                val typeStr = obj.optString("type", "CUSTOMER")
                val accType = try { AccountType.valueOf(typeStr) } catch (_: Exception) { AccountType.CUSTOMER }

                accountsList.add(
                    ContactAccount(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        phone = obj.optString("phone", ""),
                        address = obj.optString("address", ""),
                        type = accType,
                        currentBalance = obj.optDouble("currentBalance", 0.0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
            if (accountsList.isNotEmpty()) tallyDao.insertAccountsBatch(accountsList)

            // Restore Transactions
            val txArray = root.getJSONArray("transactions")
            val txList = mutableListOf<KhataTransaction>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val typeStr = obj.optString("type", "GAVE")
                val txType = try { TransactionType.valueOf(typeStr) } catch (_: Exception) { TransactionType.GAVE }
                txList.add(
                    KhataTransaction(
                        id = obj.optLong("id", 0L),
                        accountId = obj.getLong("accountId"),
                        amount = obj.getDouble("amount"),
                        type = txType,
                        note = obj.optString("note", ""),
                        billNumber = obj.optString("billNumber", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        runningBalance = obj.optDouble("runningBalance", 0.0)
                    )
                )
            }
            if (txList.isNotEmpty()) tallyDao.insertTransactionsBatch(txList)

            // Restore Cash Entries
            if (root.has("cashEntries")) {
                val ceArray = root.getJSONArray("cashEntries")
                val ceList = mutableListOf<CashEntry>()
                for (i in 0 until ceArray.length()) {
                    val obj = ceArray.getJSONObject(i)
                    ceList.add(
                        CashEntry(
                            id = obj.optLong("id", 0L),
                            amount = obj.getDouble("amount"),
                            isCashIn = obj.optBoolean("isCashIn", true),
                            category = obj.optString("category", "দোকান বিক্রি"),
                            paymentMode = obj.optString("paymentMode", "ক্যাশ (Cash)"),
                            note = obj.optString("note", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                if (ceList.isNotEmpty()) tallyDao.insertCashEntriesBatch(ceList)
            }

            // Restore Cash Memos
            if (root.has("cashMemos")) {
                val cmArray = root.getJSONArray("cashMemos")
                val cmList = mutableListOf<CashMemo>()
                for (i in 0 until cmArray.length()) {
                    val obj = cmArray.getJSONObject(i)
                    cmList.add(
                        CashMemo(
                            id = obj.optLong("id", 0L),
                            memoNumber = obj.optString("memoNumber", "MEMO-1"),
                            customerName = obj.optString("customerName", ""),
                            customerPhone = obj.optString("customerPhone", ""),
                            itemsSummary = obj.optString("itemsSummary", ""),
                            subtotal = obj.optDouble("subtotal", 0.0),
                            discount = obj.optDouble("discount", 0.0),
                            totalAmount = obj.optDouble("totalAmount", 0.0),
                            paidAmount = obj.optDouble("paidAmount", 0.0),
                            dueAmount = obj.optDouble("dueAmount", 0.0),
                            notes = obj.optString("notes", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                if (cmList.isNotEmpty()) tallyDao.insertCashMemosBatch(cmList)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportBackupToUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = createBackupJsonString()
            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os).use { writer ->
                    writer.write(json)
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importBackupFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val stringBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }
            restoreFromJsonString(stringBuilder.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
