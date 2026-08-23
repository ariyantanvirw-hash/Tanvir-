package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.data.model.CashMemo
import com.example.data.model.MemoItem
import com.example.ui.TallyViewModel
import com.example.ui.components.TallyFormatter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashMemoScreen(
    viewModel: TallyViewModel,
    memos: List<CashMemo>,
    profile: BusinessProfile,
    modifier: Modifier = Modifier
) {
    val isBn = profile.language == "bn"
    var showCreateMemoDialog by remember { mutableStateOf(false) }
    var selectedMemoForSlip by remember { mutableStateOf<CashMemo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBn) "ডিজিটাল ক্যাশ মেমো ও রশিদ" else "Digital Cash Memo & Invoices",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = if (isBn) "মুহূর্তেই রশিদ তৈরি ও কাস্টমারকে শেয়ার করুন" else "Create and share digital receipts instantly",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald200
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Emerald700)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateMemoDialog = true },
                containerColor = Emerald700,
                contentColor = White,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("create_memo_fab")
            ) {
                Icon(Icons.Default.PostAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "+ নতুন মেমো তৈরি" else "+ Create Memo",
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
            if (memos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Emerald100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isBn) "কোনো ক্যাশ মেমো তৈরি করা হয়নি" else "No cash memos generated yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Stone700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBn) "কাস্টমারের বিক্রয় রশিদ বা চালান তৈরি করতে নিচের বাটনে চাপুন" else "Tap below to create an itemized digital cash memo",
                            style = MaterialTheme.typography.bodySmall,
                            color = Stone500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memos, key = { it.id }) { memo ->
                        CashMemoItemCard(
                            memo = memo,
                            currencySymbol = profile.currencySymbol,
                            isBn = isBn,
                            onClick = { selectedMemoForSlip = memo }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    if (showCreateMemoDialog) {
        CreateMemoDialog(
            profile = profile,
            isBn = isBn,
            onDismiss = { showCreateMemoDialog = false },
            onConfirm = { name, phone, itemsList, subtotal, discount, total, paid, due, notes ->
                viewModel.createCashMemo(name, phone, itemsList, subtotal, discount, total, paid, due, notes)
                showCreateMemoDialog = false
            }
        )
    }

    selectedMemoForSlip?.let { memo ->
        MemoSlipDialog(
            memo = memo,
            profile = profile,
            isBn = isBn,
            onDismiss = { selectedMemoForSlip = null },
            onDelete = {
                viewModel.deleteCashMemo(memo)
                selectedMemoForSlip = null
            }
        )
    }
}

@Composable
fun CashMemoItemCard(
    memo: CashMemo,
    currencySymbol: String,
    isBn: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = Emerald700,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = memo.memoNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                }

                Text(
                    text = TallyFormatter.formatDate(memo.timestamp, isBn),
                    style = MaterialTheme.typography.labelSmall,
                    color = Stone500
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${if (isBn) "ক্রেতা" else "Customer"}: ${memo.customerName}" + if (memo.customerPhone.isNotBlank()) " (${memo.customerPhone})" else "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Stone900
            )

            if (memo.itemsSummary.isNotBlank()) {
                Text(
                    text = memo.itemsSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Stone700,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Stone200)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "মোট টাকা" else "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = Stone500
                    )
                    Text(
                        text = TallyFormatter.formatMoney(memo.totalAmount, currencySymbol, isBn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Stone900
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBn) "পরিশোধিত" else "Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald700
                    )
                    Text(
                        text = TallyFormatter.formatMoney(memo.paidAmount, currencySymbol, isBn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald700
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isBn) "বাকি" else "Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (memo.dueAmount > 0) Crimson600 else Stone500
                    )
                    Text(
                        text = TallyFormatter.formatMoney(memo.dueAmount, currencySymbol, isBn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (memo.dueAmount > 0) Crimson600 else Stone500
                    )
                }
            }
        }
    }
}

@Composable
fun CreateMemoDialog(
    profile: BusinessProfile,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        customerName: String,
        customerPhone: String,
        itemsList: List<MemoItem>,
        subtotal: Double,
        discount: Double,
        total: Double,
        paid: Double,
        due: Double,
        notes: String
    ) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var discountStr by remember { mutableStateOf("") }
    var paidStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Dynamic items list
    val itemsList = remember {
        mutableStateListOf(
            MemoItem(name = "", quantity = 1.0, unit = if (isBn) "কেজি" else "kg", unitPrice = 0.0)
        )
    }

    val subtotal = itemsList.sumOf { it.quantity * it.unitPrice }
    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val total = (subtotal - discount).coerceAtLeast(0.0)
    val paid = paidStr.toDoubleOrNull() ?: total
    val due = (total - paid).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "নতুন ডিজিটাল ক্যাশ মেমো" else "Create Digital Cash Memo",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text(if (isBn) "কাস্টমারের নাম *" else "Customer Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memo_customer_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text(if (isBn) "মোবাইল নম্বর" else "Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "পণ্যের তালিকা:" else "Items List:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = {
                                itemsList.add(
                                    MemoItem(name = "", quantity = 1.0, unit = if (isBn) "কেজি" else "kg", unitPrice = 0.0)
                                )
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "+ পণ্য যোগ করুন" else "+ Add Item")
                        }
                    }
                }

                items(itemsList.indices.toList()) { index ->
                    val item = itemsList[index]
                    var itemName by remember(item.id) { mutableStateOf(item.name) }
                    var itemQty by remember(item.id) { mutableStateOf(item.quantity.toString()) }
                    var itemPrice by remember(item.id) { mutableStateOf(if (item.unitPrice > 0) item.unitPrice.toInt().toString() else "") }

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Stone100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = itemName,
                                    onValueChange = {
                                        itemName = it
                                        itemsList[index] = item.copy(name = it)
                                    },
                                    placeholder = { Text(if (isBn) "পণ্যের নাম" else "Item Name", fontSize = 13.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                if (itemsList.size > 1) {
                                    IconButton(
                                        onClick = { itemsList.removeAt(index) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Crimson600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = itemQty,
                                    onValueChange = {
                                        itemQty = it
                                        val q = it.toDoubleOrNull() ?: 1.0
                                        itemsList[index] = item.copy(quantity = q)
                                    },
                                    label = { Text(if (isBn) "পরিমাণ" else "Qty", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = itemPrice,
                                    onValueChange = {
                                        itemPrice = it
                                        val p = it.toDoubleOrNull() ?: 0.0
                                        itemsList[index] = item.copy(unitPrice = p)
                                    },
                                    label = { Text(if (isBn) "দর ($profile.currencySymbol)" else "Rate", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                val lineTotal = (itemQty.toDoubleOrNull() ?: 0.0) * (itemPrice.toDoubleOrNull() ?: 0.0)
                                Text(
                                    text = TallyFormatter.formatMoney(lineTotal, profile.currencySymbol, isBn),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700,
                                    modifier = Modifier.width(70.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text(if (isBn) "ছাড় / ডিসকাউন্ট (${profile.currencySymbol})" else "Discount (${profile.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = paidStr,
                        onValueChange = { paidStr = it },
                        label = { Text(if (isBn) "জমা / নগদ প্রদান (${profile.currencySymbol})" else "Paid Amount (${profile.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Calculated Summary
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Emerald50,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isBn) "মোট বিল:" else "Total Bill:", fontWeight = FontWeight.Bold)
                                Text(TallyFormatter.formatMoney(total, profile.currencySymbol, isBn), fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isBn) "বাকি থাকবে:" else "Remaining Due:", color = if (due > 0) Crimson600 else Stone700)
                                Text(
                                    TallyFormatter.formatMoney(due, profile.currencySymbol, isBn),
                                    color = if (due > 0) Crimson600 else Stone700,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isBn) "বিশেষ মন্তব্য (ঐচ্ছিক)" else "Notes (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isNotBlank() && total > 0) {
                        onConfirm(
                            customerName,
                            customerPhone,
                            itemsList.filter { it.name.isNotBlank() },
                            subtotal,
                            discount,
                            total,
                            paid,
                            due,
                            notes
                        )
                    }
                },
                enabled = customerName.isNotBlank() && total > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                modifier = Modifier.testTag("save_memo_btn")
            ) {
                Text(if (isBn) "মেমো তৈরি করুন" else "Generate Memo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
fun MemoSlipDialog(
    memo: CashMemo,
    profile: BusinessProfile,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val memoReceiptText = buildString {
        appendLine("================================")
        appendLine("      ${profile.shopName}")
        appendLine("      ${profile.address}")
        appendLine("      মোবাইল: ${profile.phone}")
        appendLine("================================")
        appendLine("ক্যাশ মেমো নং: ${memo.memoNumber}")
        appendLine("তারিখ: ${TallyFormatter.formatDate(memo.timestamp, isBn)}")
        appendLine("ক্রেতা: ${memo.customerName}")
        if (memo.customerPhone.isNotBlank()) appendLine("ফোন: ${memo.customerPhone}")
        appendLine("--------------------------------")
        appendLine("বিবরণ: ${memo.itemsSummary}")
        appendLine("--------------------------------")
        if (memo.discount > 0) {
            appendLine("মোট মূল্য: ${TallyFormatter.formatMoney(memo.subtotal, profile.currencySymbol, isBn)}")
            appendLine("ছাড়: ${TallyFormatter.formatMoney(memo.discount, profile.currencySymbol, isBn)}")
        }
        appendLine("সর্বমোট: ${TallyFormatter.formatMoney(memo.totalAmount, profile.currencySymbol, isBn)}")
        appendLine("নগদ পরিশোধ: ${TallyFormatter.formatMoney(memo.paidAmount, profile.currencySymbol, isBn)}")
        appendLine("বাকি পাওনা: ${TallyFormatter.formatMoney(memo.dueAmount, profile.currencySymbol, isBn)}")
        if (memo.notes.isNotBlank()) appendLine("মন্তব্য: ${memo.notes}")
        appendLine("================================")
        appendLine("     ধন্যবাদ! আবার আসবেন।")
        appendLine("     Powered by TallyKhata")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(12.dp))
                    .border(1.dp, Stone300, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                // Receipt Header
                Text(
                    text = profile.shopName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald900,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = profile.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Stone500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "মোবাইল: ${profile.phone}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Stone700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Stone300)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = memo.memoNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Emerald700
                    )
                    Text(
                        text = TallyFormatter.formatDate(memo.timestamp, isBn),
                        style = MaterialTheme.typography.labelSmall,
                        color = Stone500
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (isBn) "ক্রেতা" else "Customer"}: ${memo.customerName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (memo.itemsSummary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = memo.itemsSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Stone800
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Stone300)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isBn) "সর্বমোট বিল" else "Grand Total", fontWeight = FontWeight.Bold)
                    Text(TallyFormatter.formatMoney(memo.totalAmount, profile.currencySymbol, isBn), fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isBn) "পরিশোধিত" else "Paid", color = Emerald700, fontWeight = FontWeight.Bold)
                    Text(TallyFormatter.formatMoney(memo.paidAmount, profile.currencySymbol, isBn), color = Emerald700, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isBn) "বাকি" else "Due", color = if (memo.dueAmount > 0) Crimson600 else Stone700, fontWeight = FontWeight.Bold)
                    Text(
                        TallyFormatter.formatMoney(memo.dueAmount, profile.currencySymbol, isBn),
                        color = if (memo.dueAmount > 0) Crimson600 else Stone700,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ধন্যবাদ! আবার আসবেন।",
                    style = MaterialTheme.typography.labelSmall,
                    color = Emerald700,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Crimson600)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isBn) "বন্ধ করুন" else "Close")
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, memoReceiptText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Receipt via"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        modifier = Modifier.testTag("share_memo_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "রশিদ শেয়ার" else "Share Slip", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {}
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isBn) "মেমো মুছে ফেলবেন?" else "Delete Memo?") },
            text = { Text(if (isBn) "এই ক্যাশ মেমোটি স্থায়ীভাবে মুছে যাবে।" else "This memo will be permanently deleted.") },
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
