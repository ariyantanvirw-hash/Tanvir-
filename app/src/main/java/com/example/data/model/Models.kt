package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType {
    CUSTOMER, // কাস্টমার
    SUPPLIER  // সাপ্লায়ার
}

enum class TransactionType {
    GAVE,     // দেওয়া হয়েছে (বাকি দিলাম)
    RECEIVED, // পাওয়া গেল (আদায়)
    TAKEN,    // নেওয়া হয়েছে (সাপ্লায়ার থেকে বাকি নেওয়া)
    PAID      // শোধ করা হয়েছে (সাপ্লায়ারকে পরিশোধ)
}

@Entity(tableName = "contact_accounts")
data class ContactAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val type: AccountType = AccountType.CUSTOMER,
    val currentBalance: Double = 0.0, // positive = will receive (পাবো), negative = will give (দেবো)
    val avatarColorHex: Long = 0xFF059669,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "khata_transactions")
data class KhataTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val amount: Double,
    val type: TransactionType,
    val note: String = "",
    val billNumber: String = "",
    val runningBalance: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_entries")
data class CashEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val isCashIn: Boolean, // true = Cash In, false = Cash Out
    val category: String,  // দোকান বিক্রি, বাকি আদায়, মালামাল ক্রয়, খরচ, etc.
    val paymentMode: String = "ক্যাশ (Cash)", // Cash, bKash, Nagad, Bank
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_memos")
data class CashMemo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memoNumber: String,
    val customerName: String,
    val customerPhone: String = "",
    val itemsSummary: String = "", // e.g. "চাল ২৫ কেজি, ডাল ২ কেজি, তেল ৫ লিটার"
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MemoItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "কেজি", // কেজি, লিটার, পিস, প্যাকেট, বস্তা, ডজন, গজ
    val unitPrice: Double = 0.0,
    val totalPrice: Double = quantity * unitPrice
)

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "মেসার্স রহিম স্টোর",
    val ownerName: String = "মোহাম্মদ রহিম",
    val phone: String = "01700000000",
    val address: String = "নিউ মার্কেট, ঢাকা",
    val currencySymbol: String = "৳",
    val language: String = "bn" // "bn" or "en"
)

data class DashboardSummary(
    val totalReceivable: Double = 0.0, // মোট পাবো (Customers due)
    val totalPayable: Double = 0.0,    // মোট দেবো (Suppliers due)
    val totalCashBalance: Double = 0.0,// হাতে নগদ ক্যাশ ব্যালেন্স
    val todayCashIn: Double = 0.0,     // আজকের ক্যাশ ইন
    val todayCashOut: Double = 0.0,    // আজকের ক্যাশ আউট
    val activeCustomersCount: Int = 0,
    val activeSuppliersCount: Int = 0
)

data class UserSession(
    val isLoggedIn: Boolean = false,
    val authType: String = "PHONE", // "PHONE" or "GOOGLE"
    val userIdentifier: String = "", // Phone number or Gmail
    val displayName: String = "",
    val shopName: String = "",
    val avatarUrl: String = "",
    val connectedGoogleEmail: String = "",
    val lastBackupTimestamp: Long = 0L,
    val autoBackupEnabled: Boolean = true
)

data class BackupSnapshot(
    val backupVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val appName: String = "TallyKhata",
    val userEmail: String = "",
    val shopName: String = "",
    val accounts: List<ContactAccount> = emptyList(),
    val transactions: List<KhataTransaction> = emptyList(),
    val cashEntries: List<CashEntry> = emptyList(),
    val cashMemos: List<CashMemo> = emptyList(),
    val businessProfile: BusinessProfile? = null
)

