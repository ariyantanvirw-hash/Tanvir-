package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.BusinessProfile
import com.example.data.model.ContactAccount
import com.example.data.model.DashboardSummary
import com.example.ui.TallyViewModel
import com.example.ui.components.ContactAvatar
import com.example.ui.components.SummaryMetricCard
import com.example.ui.components.TallyFormatter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataScreen(
    viewModel: TallyViewModel,
    accounts: List<ContactAccount>,
    summary: DashboardSummary,
    profile: BusinessProfile,
    onSelectAccount: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBn = profile.language == "bn"
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.accountTypeFilter.collectAsState()
    val dueFilter by viewModel.dueFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Emerald700)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top Header with Shop Name and Language Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = profile.shopName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = if (isBn) "ডিজিটাল খাতা ও ক্যাশ বুক" else "Digital Khata & Cash Book",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald200
                        )
                    }

                    // Language Chip Toggle
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Emerald800,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.toggleLanguage() }
                            .testTag("lang_toggle_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "বাংলা" else "EN",
                                color = White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Receivables / Payables Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = if (isBn) "মোট পাবো (পাওনা)" else "You Will Get",
                        amount = TallyFormatter.formatMoney(summary.totalReceivable, profile.currencySymbol, isBn),
                        subtitle = if (isBn) "${summary.activeCustomersCount} জন কাস্টমার" else "${summary.activeCustomersCount} Customers",
                        icon = Icons.Default.ArrowDownward,
                        containerColor = Emerald100,
                        contentColor = Emerald900,
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMetricCard(
                        title = if (isBn) "মোট দেবো (দেনা)" else "You Will Give",
                        amount = TallyFormatter.formatMoney(summary.totalPayable, profile.currencySymbol, isBn),
                        subtitle = if (isBn) "${summary.activeSuppliersCount} জন সাপ্লায়ার" else "${summary.activeSuppliersCount} Suppliers",
                        icon = Icons.Default.ArrowUpward,
                        containerColor = Amber100,
                        contentColor = Amber700,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Emerald700,
                contentColor = White,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("add_customer_fab")
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "+ নতুন হিসাব যোগ" else "+ Add Contact",
                    fontWeight = FontWeight.Bold
                )
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
            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            if (isBn) "কাস্টমার বা সাপ্লায়ার খুঁজুন..." else "Search customer or supplier...",
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Stone500
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Filter Tabs (কাস্টমার / সাপ্লায়ার / সকল)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = typeFilter == null,
                    onClick = { viewModel.setAccountTypeFilter(null) },
                    label = { Text(if (isBn) "সকল (${accounts.size})" else "All (${accounts.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald700,
                        selectedLabelColor = White
                    )
                )

                FilterChip(
                    selected = typeFilter == AccountType.CUSTOMER,
                    onClick = { viewModel.setAccountTypeFilter(AccountType.CUSTOMER) },
                    label = { Text(if (isBn) "কাস্টমার" else "Customers") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald700,
                        selectedLabelColor = White
                    )
                )

                FilterChip(
                    selected = typeFilter == AccountType.SUPPLIER,
                    onClick = { viewModel.setAccountTypeFilter(AccountType.SUPPLIER) },
                    label = { Text(if (isBn) "সাপ্লায়ার" else "Suppliers") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald700,
                        selectedLabelColor = White
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Due filter toggle
                IconButton(
                    onClick = {
                        val nextFilter = when (dueFilter) {
                            "ALL" -> "HAS_DUE"
                            "HAS_DUE" -> "CLEARED"
                            else -> "ALL"
                        }
                        viewModel.setDueFilter(nextFilter)
                    },
                    modifier = Modifier.testTag("filter_due_btn")
                ) {
                    Icon(
                        imageVector = if (dueFilter == "HAS_DUE") Icons.Default.FilterAlt else Icons.Outlined.FilterAlt,
                        contentDescription = "Filter",
                        tint = if (dueFilter != "ALL") Emerald700 else Stone500
                    )
                }
            }

            if (dueFilter != "ALL") {
                Text(
                    text = if (dueFilter == "HAS_DUE") {
                        if (isBn) "ফিল্টার: শুধুমাত্র যাদের বাকি আছে" else "Filter: Only with Due Balance"
                    } else {
                        if (isBn) "ফিল্টার: পরিশোধিত হিসাব" else "Filter: Cleared Accounts"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Emerald700,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                )
            }

            // List of Accounts
            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Emerald50),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isBn) "কোনো হিসাব পাওয়া যায়নি" else "No accounts found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Stone700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "নতুন কাস্টমার বা সাপ্লায়ার যোগ করতে নিচের বোতামে চাপুন" else "Tap below to add a new customer or supplier",
                            style = MaterialTheme.typography.bodySmall,
                            color = Stone500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    items(accounts, key = { it.id }) { account ->
                        AccountItemCard(
                            account = account,
                            currencySymbol = profile.currencySymbol,
                            isBn = isBn,
                            onClick = { onSelectAccount(account.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            isBn = isBn,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, address, type, initialBalance ->
                viewModel.addContact(name, phone, address, type, initialBalance)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AccountItemCard(
    account: ContactAccount,
    currencySymbol: String,
    isBn: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("account_card_${account.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                name = account.name,
                colorHex = account.avatarColorHex,
                size = 46
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (account.type == AccountType.CUSTOMER) Emerald50 else Amber50
                    ) {
                        Text(
                            text = if (account.type == AccountType.CUSTOMER) {
                                if (isBn) "কাস্টমার" else "Customer"
                            } else {
                                if (isBn) "সাপ্লায়ার" else "Supplier"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (account.type == AccountType.CUSTOMER) Emerald700 else Amber700,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (account.phone.isNotBlank()) account.phone else (if (isBn) "ফোন নম্বর নেই" else "No phone"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Stone500
                )

                Text(
                    text = TallyFormatter.formatDate(account.updatedAt, isBn),
                    style = MaterialTheme.typography.labelSmall,
                    color = Stone500.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Balance indicator
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val balance = account.currentBalance
                val isPabona = balance > 0
                val isDena = balance < 0
                val isCleared = balance == 0.0

                val balanceText = when {
                    isPabona -> if (isBn) "পাবেন" else "You'll Get"
                    isDena -> if (isBn) "দেবেন" else "You'll Give"
                    else -> if (isBn) "পরিশোধিত" else "Cleared"
                }

                val balanceColor = when {
                    isPabona -> Emerald700
                    isDena -> Crimson600
                    else -> Stone500
                }

                Text(
                    text = TallyFormatter.formatMoney(Math.abs(balance), currencySymbol, isBn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )

                Text(
                    text = balanceText,
                    style = MaterialTheme.typography.labelSmall,
                    color = balanceColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddContactDialog(
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, type: AccountType, initialBalance: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.CUSTOMER) }
    var initialBalanceStr by remember { mutableStateOf("") }
    var isPabonaForCustomer by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "নতুন হিসাব খুলুন" else "Add New Contact",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Switcher (Customer vs Supplier)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedType = AccountType.CUSTOMER },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == AccountType.CUSTOMER) Emerald700 else Stone200,
                            contentColor = if (selectedType == AccountType.CUSTOMER) White else Stone800
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isBn) "কাস্টমার" else "Customer", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedType = AccountType.SUPPLIER },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == AccountType.SUPPLIER) Amber600 else Stone200,
                            contentColor = if (selectedType == AccountType.SUPPLIER) White else Stone800
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isBn) "সাপ্লায়ার" else "Supplier", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBn) "নাম *" else "Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBn) "মোবাইল নম্বর" else "Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isBn) "ঠিকানা / এলাকা" else "Address / Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = initialBalanceStr,
                    onValueChange = { initialBalanceStr = it },
                    label = { Text(if (isBn) "পূর্বের বাকি/জের (যদি থাকে)" else "Opening Balance (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (initialBalanceStr.isNotBlank() && (initialBalanceStr.toDoubleOrNull() ?: 0.0) > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isBn) "জের এর ধরণ:" else "Balance Type:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        FilterChip(
                            selected = isPabonaForCustomer,
                            onClick = { isPabonaForCustomer = true },
                            label = { Text(if (isBn) "পাবো" else "Receivable") }
                        )
                        FilterChip(
                            selected = !isPabonaForCustomer,
                            onClick = { isPabonaForCustomer = false },
                            label = { Text(if (isBn) "দেবো" else "Payable") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val amt = initialBalanceStr.toDoubleOrNull() ?: 0.0
                        val finalBal = if (selectedType == AccountType.CUSTOMER) {
                            if (isPabonaForCustomer) amt else -amt
                        } else {
                            if (isPabonaForCustomer) amt else -amt
                        }
                        onConfirm(name, phone, address, selectedType, finalBal)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                modifier = Modifier.testTag("save_contact_btn")
            ) {
                Text(if (isBn) "সংরক্ষণ করুন" else "Save Contact", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel", color = Stone700)
            }
        }
    )
}
