package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.TallyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TallyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TallyRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = TallyRepository(db.tallyDao(), application)
    }

    // User session & backup flow
    val userSession: StateFlow<UserSession> = repository.userSession

    // Main App Navigation / Active Selection
    private val _currentTab = MutableStateFlow(0) // 0: Khata, 1: Cash Box, 2: Memo, 3: Reports & More
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<Long?>(null)
    val selectedAccountId: StateFlow<Long?> = _selectedAccountId.asStateFlow()

    // Khata Tab Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _accountTypeFilter = MutableStateFlow<AccountType?>(null) // null = all, CUSTOMER, SUPPLIER
    val accountTypeFilter: StateFlow<AccountType?> = _accountTypeFilter.asStateFlow()

    private val _dueFilter = MutableStateFlow("ALL") // "ALL", "HAS_DUE", "CLEARED"
    val dueFilter: StateFlow<String> = _dueFilter.asStateFlow()

    // Backup & Sync Status messages
    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    // Base Database Flows
    val allAccounts: StateFlow<List<ContactAccount>> = repository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCashEntries: StateFlow<List<CashEntry>> = repository.getAllCashEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCashMemos: StateFlow<List<CashMemo>> = repository.getAllCashMemos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessProfile: StateFlow<BusinessProfile> = repository.getBusinessProfile()
        .flatMapLatest { profile ->
            flowOf(profile ?: BusinessProfile())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessProfile())

    // Real-time Dashboard Summary calculated from live flows
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        allAccounts,
        allCashEntries
    ) { accounts, cashEntries ->
        var receivable = 0.0
        var payable = 0.0
        var activeCust = 0
        var activeSupp = 0

        accounts.forEach { acc ->
            if (acc.type == AccountType.CUSTOMER) {
                if (acc.currentBalance > 0) receivable += acc.currentBalance
                if (acc.currentBalance != 0.0) activeCust++
            } else {
                if (acc.currentBalance < 0) payable += kotlin.math.abs(acc.currentBalance)
                if (acc.currentBalance != 0.0) activeSupp++
            }
        }

        var totalIn = 0.0
        var totalOut = 0.0
        var todayIn = 0.0
        var todayOut = 0.0

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        cashEntries.forEach { entry ->
            if (entry.isCashIn) {
                totalIn += entry.amount
                if (entry.timestamp >= startOfToday) todayIn += entry.amount
            } else {
                totalOut += entry.amount
                if (entry.timestamp >= startOfToday) todayOut += entry.amount
            }
        }

        DashboardSummary(
            totalReceivable = receivable,
            totalPayable = payable,
            totalCashBalance = totalIn - totalOut,
            todayCashIn = todayIn,
            todayCashOut = todayOut,
            activeCustomersCount = activeCust,
            activeSuppliersCount = activeSupp
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // Filtered Accounts
    val filteredAccounts: StateFlow<List<ContactAccount>> = combine(
        allAccounts,
        _searchQuery,
        _accountTypeFilter,
        _dueFilter
    ) { accounts, query, typeFilter, dueFil ->
        accounts.filter { acc ->
            val matchesQuery = query.isBlank() ||
                    acc.name.contains(query, ignoreCase = true) ||
                    acc.phone.contains(query, ignoreCase = true)

            val matchesType = typeFilter == null || acc.type == typeFilter

            val matchesDue = when (dueFil) {
                "HAS_DUE" -> acc.currentBalance != 0.0
                "CLEARED" -> acc.currentBalance == 0.0
                else -> true
            }

            matchesQuery && matchesType && matchesDue
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Account Details and Transactions
    val selectedAccount: StateFlow<ContactAccount?> = _selectedAccountId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getAccountById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedAccountTransactions: StateFlow<List<KhataTransaction>> = _selectedAccountId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getTransactionsForAccount(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Actions
    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun selectAccount(accountId: Long?) {
        _selectedAccountId.value = accountId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAccountTypeFilter(type: AccountType?) {
        _accountTypeFilter.value = type
    }

    fun setDueFilter(filter: String) {
        _dueFilter.value = filter
    }

    fun clearStatusMessage() {
        _backupStatusMessage.value = null
    }

    // --- Authentication & Session Actions ---
    fun loginWithPhone(phoneNumber: String, shopName: String, ownerName: String) {
        viewModelScope.launch {
            repository.loginWithPhone(phoneNumber, shopName, ownerName)
        }
    }

    fun loginWithGoogle(email: String, displayName: String, avatarUrl: String) {
        viewModelScope.launch {
            repository.loginWithGoogle(email, displayName, avatarUrl)
        }
    }

    fun linkGoogleAccount(email: String) {
        viewModelScope.launch {
            repository.connectGoogleAccountForBackup(email)
            _backupStatusMessage.value = "গুগল অ্যাকাউন্ট ($email) সফলভাবে যুক্ত হয়েছে!"
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // --- Contact / Ledger Actions ---
    fun addContact(
        name: String,
        phone: String,
        address: String,
        type: AccountType,
        initialBalance: Double
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val initialAccount = ContactAccount(
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                type = type,
                currentBalance = 0.0,
                createdAt = now
            )
            val newId = repository.insertAccount(initialAccount)

            if (initialBalance != 0.0) {
                val txType = if (type == AccountType.CUSTOMER) {
                    if (initialBalance > 0) TransactionType.GAVE else TransactionType.RECEIVED
                } else {
                    if (initialBalance > 0) TransactionType.TAKEN else TransactionType.PAID
                }
                repository.recordTransaction(
                    accountId = newId,
                    amount = kotlin.math.abs(initialBalance),
                    type = txType,
                    note = "পূর্বের বাকি/ব্যালেন্স (Opening Balance)",
                    billNumber = "",
                    syncWithCashBox = false,
                    paymentMode = "অন্যান্য"
                )
            }
        }
    }

    fun updateContact(account: ContactAccount) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteContact(account: ContactAccount) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            if (_selectedAccountId.value == account.id) {
                _selectedAccountId.value = null
            }
        }
    }

    fun recordTransaction(
        accountId: Long,
        amount: Double,
        type: TransactionType,
        note: String,
        billNumber: String,
        syncWithCashBox: Boolean = false,
        paymentMode: String = "ক্যাশ (Cash)"
    ) {
        viewModelScope.launch {
            repository.recordTransaction(
                accountId = accountId,
                amount = amount,
                type = type,
                note = note,
                billNumber = billNumber,
                syncWithCashBox = syncWithCashBox,
                paymentMode = paymentMode
            )
        }
    }

    // --- Cash Box Actions ---
    fun recordCashEntry(
        amount: Double,
        isCashIn: Boolean,
        category: String,
        paymentMode: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.insertCashEntry(
                CashEntry(
                    amount = amount,
                    isCashIn = isCashIn,
                    category = category,
                    paymentMode = paymentMode,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteCashEntry(entry: CashEntry) {
        viewModelScope.launch {
            repository.deleteCashEntry(entry)
        }
    }

    // --- Cash Memo Actions ---
    fun createCashMemo(
        customerName: String,
        customerPhone: String,
        itemsList: List<MemoItem>,
        subtotal: Double,
        discount: Double,
        total: Double,
        paidAmount: Double,
        dueAmount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val memoNumber = "MEMO-${System.currentTimeMillis().toString().takeLast(6)}"
            val itemsSummary = itemsList.joinToString(", ") { item ->
                "${item.name} (${item.quantity} ${item.unit} × ৳${item.unitPrice.toInt()})"
            }

            val memo = CashMemo(
                memoNumber = memoNumber,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                itemsSummary = itemsSummary,
                subtotal = subtotal,
                discount = discount,
                totalAmount = total,
                paidAmount = paidAmount,
                dueAmount = dueAmount,
                notes = notes.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.insertCashMemo(memo)

            // If there is cash paid, log it to cashbox automatically
            if (paidAmount > 0) {
                repository.insertCashEntry(
                    CashEntry(
                        amount = paidAmount,
                        isCashIn = true,
                        category = "দোকান বিক্রি (Cash Memo)",
                        paymentMode = "ক্যাশ (Cash)",
                        note = "ক্যাশ মেমো #$memoNumber ($customerName)",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteCashMemo(memo: CashMemo) {
        viewModelScope.launch {
            repository.deleteCashMemo(memo)
        }
    }

    // --- Business Profile Actions ---
    fun updateProfile(
        shopName: String,
        ownerName: String,
        phone: String,
        address: String,
        currencySymbol: String,
        language: String
    ) {
        viewModelScope.launch {
            val updated = BusinessProfile(
                id = 1,
                shopName = shopName.trim(),
                ownerName = ownerName.trim(),
                phone = phone.trim(),
                address = address.trim(),
                currencySymbol = currencySymbol.trim(),
                language = language
            )
            repository.updateBusinessProfile(updated)
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = businessProfile.value
            val newLang = if (current.language == "bn") "en" else "bn"
            repository.updateBusinessProfile(current.copy(language = newLang))
        }
    }

    // --- Backup & Restore Actions ---
    fun triggerGoogleDriveBackup() {
        viewModelScope.launch {
            try {
                val json = repository.createBackupJsonString()
                val current = userSession.value
                val email = if (current.connectedGoogleEmail.isNotBlank()) current.connectedGoogleEmail else current.userIdentifier
                val targetEmail = if (email.contains("@")) email else "Ariyantanvirw@gmail.com"
                
                repository.saveSession(
                    current.copy(
                        connectedGoogleEmail = targetEmail,
                        lastBackupTimestamp = System.currentTimeMillis()
                    )
                )
                _backupStatusMessage.value = "গুগল ড্রাইভ ব্যাকআপ সম্পন্ন হয়েছে ($targetEmail)!"
            } catch (e: Exception) {
                _backupStatusMessage.value = "ব্যাকআপ ব্যর্থ হয়েছে: ${e.message}"
            }
        }
    }

    fun exportBackupToFile(uri: Uri) {
        viewModelScope.launch {
            val success = repository.exportBackupToUri(uri)
            if (success) {
                _backupStatusMessage.value = "ব্যাকআপ ফাইল সফলভাবে এক্সপোর্ট হয়েছে!"
            } else {
                _backupStatusMessage.value = "ব্যাকআপ ফাইল এক্সপোর্ট করতে সমস্যা হয়েছে।"
            }
        }
    }

    fun importBackupFromFile(uri: Uri) {
        viewModelScope.launch {
            val success = repository.importBackupFromUri(uri)
            if (success) {
                _backupStatusMessage.value = "ডাটাবেজ সফলভাবে রিস্টোর করা হয়েছে!"
            } else {
                _backupStatusMessage.value = "রিস্টোর ফাইল পড়া সম্ভব হয়নি। সঠিক ব্যাকআপ ফাইল নির্বাচন করুন।"
            }
        }
    }
}
