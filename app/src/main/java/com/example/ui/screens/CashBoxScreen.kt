package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.data.model.CashEntry
import com.example.data.model.DashboardSummary
import com.example.ui.TallyViewModel
import com.example.ui.components.SummaryMetricCard
import com.example.ui.components.TallyFormatter
import com.example.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBoxScreen(
    viewModel: TallyViewModel,
    cashEntries: List<CashEntry>,
    summary: DashboardSummary,
    profile: BusinessProfile,
    modifier: Modifier = Modifier
) {
    val isBn = profile.language == "bn"
    var dateFilter by remember { mutableStateOf("TODAY") } // TODAY, WEEK, MONTH, ALL
    var showCashEntryDialog by remember { mutableStateOf(false) }
    var isCashInAction by remember { mutableStateOf(true) }

    val filteredEntries = remember(cashEntries, dateFilter) {
        val now = Calendar.getInstance()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        when (dateFilter) {
            "TODAY" -> cashEntries.filter { it.timestamp >= startOfToday }
            "WEEK" -> cashEntries.filter { it.timestamp >= startOfWeek }
            "MONTH" -> cashEntries.filter { it.timestamp >= startOfMonth }
            else -> cashEntries
        }
    }

    var periodCashIn = 0.0
    var periodCashOut = 0.0
    filteredEntries.forEach { entry ->
        if (entry.isCashIn) periodCashIn += entry.amount else periodCashOut += entry.amount
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Emerald700)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "ক্যাশ বাক্স (ক্যাশ হিসাব)" else "Cash Box (Daily Register)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = if (isBn) "দৈনিক আয় ও ব্যয়ের নিখুঁত হিসাব" else "Daily income and expense bookkeeping",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald200
                        )
                    }

                    // Total Cash in Hand Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Emerald900
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = if (isBn) "হাতে নগদ ক্যাশ" else "Cash in Hand",
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald200
                            )
                            Text(
                                text = TallyFormatter.formatMoney(summary.totalCashBalance, profile.currencySymbol, isBn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // In & Out Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = if (dateFilter == "TODAY") (if (isBn) "আজকের ক্যাশ ইন" else "Today's In")
                        else (if (isBn) "মোট ক্যাশ ইন" else "Total In"),
                        amount = "+ ${TallyFormatter.formatMoney(periodCashIn, profile.currencySymbol, isBn)}",
                        icon = Icons.Default.AddCircle,
                        containerColor = Emerald100,
                        contentColor = Emerald900,
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMetricCard(
                        title = if (dateFilter == "TODAY") (if (isBn) "আজকের ক্যাশ আউট" else "Today's Out")
                        else (if (isBn) "মোট ক্যাশ আউট" else "Total Out"),
                        amount = "- ${TallyFormatter.formatMoney(periodCashOut, profile.currencySymbol, isBn)}",
                        icon = Icons.Default.RemoveCircle,
                        containerColor = Crimson100,
                        contentColor = Crimson700,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
                    Button(
                        onClick = {
                            isCashInAction = true
                            showCashEntryDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("add_cash_in_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "+ ক্যাশ ইন (প্রাপ্তি)" else "+ Cash In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Button(
                        onClick = {
                            isCashInAction = false
                            showCashEntryDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Crimson600),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("add_cash_out_btn")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "- ক্যাশ আউট (খরচ)" else "- Cash Out",
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
            // Date Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = dateFilter == "TODAY",
                        onClick = { dateFilter = "TODAY" },
                        label = { Text(if (isBn) "আজ" else "Today") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald700,
                            selectedLabelColor = White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = dateFilter == "WEEK",
                        onClick = { dateFilter = "WEEK" },
                        label = { Text(if (isBn) "এই সপ্তাহ" else "This Week") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald700,
                            selectedLabelColor = White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = dateFilter == "MONTH",
                        onClick = { dateFilter = "MONTH" },
                        label = { Text(if (isBn) "এই মাস" else "This Month") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald700,
                            selectedLabelColor = White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = dateFilter == "ALL",
                        onClick = { dateFilter = "ALL" },
                        label = { Text(if (isBn) "সব সময়" else "All Time") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald700,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            // Entries List
            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Stone300,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBn) "নির্বাচিত সময়ে কোনো ক্যাশ এন্ট্রি নেই" else "No cash entries for selected period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Stone500
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { entry ->
                        CashEntryCard(
                            entry = entry,
                            currencySymbol = profile.currencySymbol,
                            isBn = isBn,
                            onDelete = { viewModel.deleteCashEntry(entry) }
                        )
                    }
                }
            }
        }
    }

    if (showCashEntryDialog) {
        CashEntryDialog(
            isCashIn = isCashInAction,
            currencySymbol = profile.currencySymbol,
            isBn = isBn,
            onDismiss = { showCashEntryDialog = false },
            onConfirm = { amount, category, mode, note ->
                viewModel.recordCashEntry(amount, isCashInAction, category, mode, note)
                showCashEntryDialog = false
            }
        )
    }
}

@Composable
fun CashEntryCard(
    entry: CashEntry,
    currencySymbol: String,
    isBn: Boolean,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (entry.isCashIn) Emerald100 else Crimson100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (entry.isCashIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (entry.isCashIn) Emerald700 else Crimson700,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Stone900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Stone100
                    ) {
                        Text(
                            text = entry.paymentMode.split(" ")[0],
                            style = MaterialTheme.typography.labelSmall,
                            color = Stone700,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (entry.note.isNotBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = Stone700
                    )
                }

                Text(
                    text = TallyFormatter.formatDate(entry.timestamp, isBn),
                    style = MaterialTheme.typography.labelSmall,
                    color = Stone500
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (entry.isCashIn) "+" else "-"} ${TallyFormatter.formatMoney(entry.amount, currencySymbol, isBn)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isCashIn) Emerald700 else Crimson600
                )

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = Stone300,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isBn) "ক্যাশ এন্ট্রি মুছে ফেলবেন?" else "Delete Entry?") },
            text = { Text(if (isBn) "এই ক্যাশ রেকর্ডটি মুছে ফেলা হবে।" else "This cash record will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Crimson600)
                ) {
                    Text(if (isBn) "মুছে ফেলুন" else "Delete", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun CashEntryDialog(
    isCashIn: Boolean,
    currencySymbol: String,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, paymentMode: String, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(if (isCashIn) (if (isBn) "দোকান বিক্রি" else "Shop Sales") else (if (isBn) "মালামাল ক্রয়" else "Inventory Purchase"))
    }
    var selectedPaymentMode by remember { mutableStateOf("ক্যাশ (Cash)") }
    var note by remember { mutableStateOf("") }

    val cashInCategories = if (isBn) {
        listOf("দোকান বিক্রি", "বাকি আদায়", "অর্ডার অগ্রিম", "ব্যাংক থেকে উত্তোলন", "অন্যান্য প্রাপ্তি")
    } else {
        listOf("Shop Sales", "Due Collected", "Advance Order", "Bank Withdrawal", "Other Income")
    }

    val cashOutCategories = if (isBn) {
        listOf("মালামাল ক্রয়", "দোকান ভাড়া", "বিদ্যুৎ বিল", "কর্মচারীর বেতন", "পরিবহন খরচ", "চা-নাস্তা ও আপ্যায়ন", "ব্যক্তিগত খরচ", "বিবিধ খরচ")
    } else {
        listOf("Inventory", "Shop Rent", "Electricity Bill", "Salary", "Transport", "Snacks & Refreshment", "Personal", "Miscellaneous")
    }

    val categories = if (isCashIn) cashInCategories else cashOutCategories
    val quickPresets = listOf(100, 500, 1000, 2000, 5000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isCashIn) {
                    if (isBn) "+ ক্যাশ ইন (টাকা এসেছে)" else "+ Cash In (Income)"
                } else {
                    if (isBn) "- ক্যাশ আউট (টাকা গেছে)" else "- Cash Out (Expense)"
                },
                fontWeight = FontWeight.Bold,
                color = if (isCashIn) Emerald700 else Crimson600
            )
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
                        .testTag("cash_amount_input")
                )

                // Quick presets
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
                                color = if (isCashIn) Emerald700 else Crimson600,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Text(
                    text = if (isBn) "খাতের ধরণ নির্বাচন করুন:" else "Select Category:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                // Categories Wrap / Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isCashIn) Emerald700 else Crimson600,
                                selectedLabelColor = White
                            )
                        )
                    }
                }

                Text(
                    text = if (isBn) "মাধ্যম:" else "Payment Mode:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
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

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBn) "মন্তব্য / বিবরণ (ঐচ্ছিক)" else "Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, selectedCategory, selectedPaymentMode, note)
                    }
                },
                enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCashIn) Emerald700 else Crimson600
                ),
                modifier = Modifier.testTag("save_cash_entry_btn")
            ) {
                Text(if (isBn) "ক্যাশ বক্সে যোগ করুন" else "Save to Cash Box", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
