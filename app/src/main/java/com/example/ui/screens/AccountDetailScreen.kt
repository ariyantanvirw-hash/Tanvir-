package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.TallyViewModel
import com.example.ui.components.ContactAvatar
import com.example.ui.components.ReminderBottomSheet
import com.example.ui.components.TallyFormatter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    account: ContactAccount,
    transactions: List<KhataTransaction>,
    profile: BusinessProfile,
    viewModel: TallyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBn = profile.language == "bn"
    val isCustomer = account.type == AccountType.CUSTOMER

    var showTransactionDialog by remember { mutableStateOf(false) }
    var activeTransactionType by remember { mutableStateOf(TransactionType.GAVE) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        if (account.phone.isNotBlank()) {
                            Text(
                                text = account.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald200
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                actions = {
                    // Call button
                    if (account.phone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${account.phone}")
                                }
                                try {
                                    context.startActivity(dialIntent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.testTag("call_contact_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = White
                            )
                        }
                    }

                    // Reminder Button (if has due)
                    if (account.currentBalance != 0.0) {
                        IconButton(
                            onClick = { showReminderSheet = true },
                            modifier = Modifier.testTag("reminder_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminder",
                                tint = Amber500
                            )
                        }
                    }

                    // Delete Contact
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("delete_contact_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Emerald700
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left button: GAVE (বাকি দিলাম / মালামাল নিলাম)
                    Button(
                        onClick = {
                            activeTransactionType = if (isCustomer) TransactionType.GAVE else TransactionType.TAKEN
                            showTransactionDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCustomer) Crimson600 else Amber600
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("action_gave_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCustomer) {
                                if (isBn) "দিলাম (বাকি)" else "Gave (Credit)"
                            } else {
                                if (isBn) "নিলাম (বাকি)" else "Took (Credit)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Right button: RECEIVED (পেলাম / সাপ্লায়ারকে দিলাম)
                    Button(
                        onClick = {
                            activeTransactionType = if (isCustomer) TransactionType.RECEIVED else TransactionType.PAID
                            showTransactionDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("action_received_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCustomer) {
                                if (isBn) "পেলাম (জমা)" else "Received (Paid)"
                            } else {
                                if (isBn) "শোধ করলাম" else "Paid Supplier"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Balance Card Banner
            val balance = account.currentBalance
            val isPabona = balance > 0
            val isDena = balance < 0
            val bannerBg = when {
                isPabona -> Emerald100
                isDena -> Crimson100
                else -> Stone200
            }
            val bannerFg = when {
                isPabona -> Emerald900
                isDena -> Crimson700
                else -> Stone800
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = bannerBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isCustomer) {
                                if (isPabona) (if (isBn) "বর্তমান বাকি পাওনা" else "Net Receivable")
                                else if (isDena) (if (isBn) "অগ্রিম জমা / দেনা" else "Advance Balance")
                                else (if (isBn) "হিসাব পরিশোধিত" else "Account Cleared")
                            } else {
                                if (isDena) (if (isBn) "সাপ্লায়ারের দেনা" else "Net Payable to Supplier")
                                else if (isPabona) (if (isBn) "অগ্রিম দেওয়া আছে" else "Advance Paid")
                                else (if (isBn) "হিসাব পরিশোধিত" else "Account Cleared")
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = bannerFg.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = TallyFormatter.formatMoney(Math.abs(balance), profile.currencySymbol, isBn),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = bannerFg
                        )
                    }

                    if (account.currentBalance != 0.0) {
                        Button(
                            onClick = { showReminderSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = bannerFg),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "তাগাদা" else "Remind",
                                color = White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Ledger Statement Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "লেনদেনের বিবরণী (${transactions.size})" else "Transaction Statement (${transactions.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Stone700
                )
            }

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "এখনও কোনো লেনদেন যোগ করা হয়নি" else "No transactions recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Stone500
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            isCustomer = isCustomer,
                            currencySymbol = profile.currencySymbol,
                            isBn = isBn
                        )
                    }
                }
            }
        }
    }

    if (showTransactionDialog) {
        RecordTransactionDialog(
            accountName = account.name,
            transactionType = activeTransactionType,
            isCustomer = isCustomer,
            currencySymbol = profile.currencySymbol,
            isBn = isBn,
            onDismiss = { showTransactionDialog = false },
            onConfirm = { amount, note, billNo, syncCash, mode ->
                viewModel.recordTransaction(
                    accountId = account.id,
                    amount = amount,
                    type = activeTransactionType,
                    note = note,
                    billNumber = billNo,
                    syncWithCashBox = syncCash,
                    paymentMode = mode
                )
                showTransactionDialog = false
            }
        )
    }

    if (showReminderSheet) {
        ReminderBottomSheet(
            account = account,
            profile = profile,
            isBn = isBn,
            onDismiss = { showReminderSheet = false }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(if (isBn) "হিসাব মুছে ফেলতে চান?" else "Delete Account?")
            },
            text = {
                Text(
                    if (isBn)
                        "'${account.name}' এর সকল লেনদেনের তথ্য মুছে ফেলা হবে। এই কাজটি আর পূর্বাবস্থায় ফিরিয়ে আনা যাবে না।"
                    else
                        "All transaction history for '${account.name}' will be permanently deleted."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteContact(account)
                        showDeleteConfirmDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Crimson600),
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete", color = White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionRowItem(
    transaction: KhataTransaction,
    isCustomer: Boolean,
    currencySymbol: String,
    isBn: Boolean
) {
    val isCredit = transaction.type == TransactionType.GAVE || transaction.type == TransactionType.TAKEN
    val amountColor = if (transaction.type == TransactionType.GAVE || transaction.type == TransactionType.TAKEN) Crimson600 else Emerald700

    val typeLabel = when (transaction.type) {
        TransactionType.GAVE -> if (isBn) "বাকি দেওয়া হয়েছে" else "Credit Given"
        TransactionType.RECEIVED -> if (isBn) "টাকা জমা / আদায়" else "Payment Received"
        TransactionType.TAKEN -> if (isBn) "বাকি মালামাল নেওয়া" else "Goods Taken"
        TransactionType.PAID -> if (isBn) "সাপ্লায়ারকে পরিশোধ" else "Supplier Paid"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCredit) Crimson100 else Emerald100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (isCredit) Crimson700 else Emerald700,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Stone900
                )

                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = Stone700
                    )
                }

                if (transaction.billNumber.isNotBlank()) {
                    Text(
                        text = "${if (isBn) "মেমো নং" else "Bill"}: ${transaction.billNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald700
                    )
                }

                Text(
                    text = TallyFormatter.formatDate(transaction.timestamp, isBn),
                    style = MaterialTheme.typography.labelSmall,
                    color = Stone500
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"} ${TallyFormatter.formatMoney(transaction.amount, currencySymbol, isBn)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                Text(
                    text = "${if (isBn) "জের" else "Bal"}: ${TallyFormatter.formatMoney(Math.abs(transaction.runningBalance), currencySymbol, isBn)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Stone500
                )
            }
        }
    }
}

@Composable
fun RecordTransactionDialog(
    accountName: String,
    transactionType: TransactionType,
    isCustomer: Boolean,
    currencySymbol: String,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String, billNumber: String, syncCashBox: Boolean, paymentMode: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var billNumber by remember { mutableStateOf("") }
    var syncCashBox by remember { mutableStateOf(transactionType == TransactionType.RECEIVED || transactionType == TransactionType.PAID) }
    var selectedPaymentMode by remember { mutableStateOf("ক্যাশ (Cash)") }

    val dialogTitle = when (transactionType) {
        TransactionType.GAVE -> if (isBn) "বাকি দেওয়া (মাল বিক্রি)" else "Give Credit"
        TransactionType.RECEIVED -> if (isBn) "টাকা জমা / বাকি আদায়" else "Receive Payment"
        TransactionType.TAKEN -> if (isBn) "সাপ্লায়ার থেকে বাকি নেওয়া" else "Take from Supplier"
        TransactionType.PAID -> if (isBn) "সাপ্লায়ারকে পরিশোধ" else "Pay Supplier"
    }

    val quickPresets = listOf(100, 500, 1000, 2000, 5000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = dialogTitle, fontWeight = FontWeight.Bold)
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isBn) "টাকার পরিমাণ ($currencySymbol) *" else "Amount ($currencySymbol) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input")
                )

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPresets.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Stone100,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val current = amountStr.toDoubleOrNull() ?: 0.0
                                    amountStr = (current + preset).toInt().toString()
                                }
                        ) {
                            Text(
                                text = "+${TallyFormatter.formatNumber(preset, isBn)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald700,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBn) "বিবরণ / মালের নাম (ঐচ্ছিক)" else "Notes / Item Details") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = billNumber,
                    onValueChange = { billNumber = it },
                    label = { Text(if (isBn) "বিল / চালান নম্বর (ঐচ্ছিক)" else "Bill / Memo No. (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cash Box Sync checkbox (for payments)
                if (transactionType == TransactionType.RECEIVED || transactionType == TransactionType.PAID) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = syncCashBox,
                            onCheckedChange = { syncCashBox = it }
                        )
                        Text(
                            text = if (isBn) "ক্যাশ বক্সে স্বয়ংক্রিয়ভাবে হিসাব যোগ করুন" else "Auto-sync with Cash Box",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (syncCashBox) {
                        Text(
                            text = if (isBn) "পেমেন্ট মাধ্যম:" else "Payment Mode:",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("ক্যাশ (Cash)", "বিকাশ (bKash)", "নগদ (Nagad)", "ব্যাংক (Bank)").forEach { mode ->
                                FilterChip(
                                    selected = selectedPaymentMode == mode,
                                    onClick = { selectedPaymentMode = mode },
                                    label = { Text(mode.split(" ")[0], fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, note, billNumber, syncCashBox, selectedPaymentMode)
                    }
                },
                enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (transactionType == TransactionType.GAVE || transactionType == TransactionType.TAKEN) Crimson600 else Emerald700
                ),
                modifier = Modifier.testTag("save_transaction_btn")
            ) {
                Text(if (isBn) "হিসাব নিশ্চিত করুন" else "Confirm Entry", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
