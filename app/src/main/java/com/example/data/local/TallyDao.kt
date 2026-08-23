package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountType
import com.example.data.model.BusinessProfile
import com.example.data.model.CashEntry
import com.example.data.model.CashMemo
import com.example.data.model.ContactAccount
import com.example.data.model.KhataTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TallyDao {

    // --- Accounts ---
    @Query("SELECT * FROM contact_accounts ORDER BY updatedAt DESC")
    fun getAllAccounts(): Flow<List<ContactAccount>>

    @Query("SELECT * FROM contact_accounts WHERE type = :type ORDER BY updatedAt DESC")
    fun getAccountsByType(type: AccountType): Flow<List<ContactAccount>>

    @Query("SELECT * FROM contact_accounts WHERE id = :accountId LIMIT 1")
    fun getAccountById(accountId: Long): Flow<ContactAccount?>

    @Query("SELECT * FROM contact_accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccountByIdSync(accountId: Long): ContactAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: ContactAccount): Long

    @Update
    suspend fun updateAccount(account: ContactAccount)

    @Delete
    suspend fun deleteAccount(account: ContactAccount)

    @Query("DELETE FROM contact_accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: Long)

    // --- Transactions ---
    @Query("SELECT * FROM khata_transactions WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<KhataTransaction>>

    @Query("SELECT * FROM khata_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<KhataTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: KhataTransaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: KhataTransaction)

    @Query("DELETE FROM khata_transactions WHERE accountId = :accountId")
    suspend fun deleteTransactionsForAccount(accountId: Long)

    // --- Cash Entries ---
    @Query("SELECT * FROM cash_entries ORDER BY timestamp DESC")
    fun getAllCashEntries(): Flow<List<CashEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashEntry(cashEntry: CashEntry): Long

    @Delete
    suspend fun deleteCashEntry(cashEntry: CashEntry)

    // --- Cash Memos ---
    @Query("SELECT * FROM cash_memos ORDER BY timestamp DESC")
    fun getAllCashMemos(): Flow<List<CashMemo>>

    @Query("SELECT * FROM cash_memos WHERE id = :memoId LIMIT 1")
    fun getCashMemoById(memoId: Long): Flow<CashMemo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashMemo(cashMemo: CashMemo): Long

    @Delete
    suspend fun deleteCashMemo(cashMemo: CashMemo)

    // --- Business Profile ---
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getBusinessProfile(): Flow<BusinessProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: BusinessProfile)
    // --- Batch & Backup Queries ---
    @Query("SELECT * FROM contact_accounts ORDER BY id ASC")
    suspend fun getAllAccountsSync(): List<ContactAccount>

    @Query("SELECT * FROM khata_transactions ORDER BY id ASC")
    suspend fun getAllTransactionsSync(): List<KhataTransaction>

    @Query("SELECT * FROM cash_entries ORDER BY id ASC")
    suspend fun getAllCashEntriesSync(): List<CashEntry>

    @Query("SELECT * FROM cash_memos ORDER BY id ASC")
    suspend fun getAllCashMemosSync(): List<CashMemo>

    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    suspend fun getBusinessProfileSync(): BusinessProfile?

    @Query("DELETE FROM contact_accounts")
    suspend fun clearAllAccounts()

    @Query("DELETE FROM khata_transactions")
    suspend fun clearAllTransactions()

    @Query("DELETE FROM cash_entries")
    suspend fun clearAllCashEntries()

    @Query("DELETE FROM cash_memos")
    suspend fun clearAllCashMemos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountsBatch(accounts: List<ContactAccount>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionsBatch(transactions: List<KhataTransaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashEntriesBatch(entries: List<CashEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashMemosBatch(memos: List<CashMemo>)
}
