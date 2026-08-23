package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BusinessProfile
import com.example.data.model.DashboardSummary
import com.example.ui.TallyViewModel
import com.example.ui.components.TallyFormatter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: TallyViewModel,
    summary: DashboardSummary,
    profile: BusinessProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBn = profile.language == "bn"
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val statusMsg by viewModel.backupStatusMessage.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showGoogleLinkDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // Export file launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportBackupToFile(it) }
    }

    // Import file launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importBackupFromFile(it) }
    }

    val netBusinessBalance = summary.totalReceivable - summary.totalPayable

    val statementSummaryText = buildString {
        appendLine("==================================")
        appendLine("       ${profile.shopName}")
        appendLine("       ব্যবসার সার্বিক হিসাব বিবরণী")
        appendLine("==================================")
        appendLine("প্রোপ্রাইটর: ${profile.ownerName}")
        appendLine("ঠিকানা: ${profile.address}")
        appendLine("মোবাইল: ${profile.phone}")
        appendLine("তারিখ: ${TallyFormatter.formatDateOnly(System.currentTimeMillis(), isBn)}")
        appendLine("----------------------------------")
        appendLine("মোট কাস্টমার পাওনা (Receivable): ${TallyFormatter.formatMoney(summary.totalReceivable, profile.currencySymbol, isBn)}")
        appendLine("মোট সাপ্লায়ার দেনা (Payable): ${TallyFormatter.formatMoney(summary.totalPayable, profile.currencySymbol, isBn)}")
        appendLine("মোট নীট ব্যবসায়িক অবস্থান: ${TallyFormatter.formatMoney(netBusinessBalance, profile.currencySymbol, isBn)}")
        appendLine("হাতে নগদ ক্যাশ ব্যালেন্স: ${TallyFormatter.formatMoney(summary.totalCashBalance, profile.currencySymbol, isBn)}")
        appendLine("আজকের ক্যাশ বিক্রি/ইন: ${TallyFormatter.formatMoney(summary.todayCashIn, profile.currencySymbol, isBn)}")
        appendLine("আজকের ক্যাশ খরচ/আউট: ${TallyFormatter.formatMoney(summary.todayCashOut, profile.currencySymbol, isBn)}")
        appendLine("----------------------------------")
        appendLine("অ্যাক্টিভ কাস্টমার সংখ্যা: ${summary.activeCustomersCount}")
        appendLine("অ্যাক্টিভ সাপ্লায়ার সংখ্যা: ${summary.activeSuppliersCount}")
        appendLine("==================================")
        appendLine("ডিজিটাল খাতা ও ক্যাশ হিসাব - TallyKhata")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBn) "হিসাব রিপোর্ট ও সেটিংস" else "Reports & Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("reports_lang_btn")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = White.copy(alpha = 0.2f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isBn) "EN" else "বাং",
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Emerald800
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Stone50)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Shop Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald800),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.shopName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${profile.ownerName} • ${profile.phone.ifBlank { if (isBn) "মোবাইল যুক্ত নেই" else "No phone" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald100
                            )
                            if (profile.address.isNotBlank()) {
                                Text(
                                    text = profile.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald200
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = White
                            )
                        }
                    }
                }
            }

            // Google Drive Auto-Backup & Sync Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Emerald50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBn) "গুগল ড্রাইভ ও ক্লাউড ব্যাকআপ" else "Google Drive & Cloud Backup",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Stone900
                                )
                                Text(
                                    text = if (userSession.connectedGoogleEmail.isNotBlank())
                                        "${if (isBn) "সংযুক্ত জিমেইল:" else "Linked Gmail:"} ${userSession.connectedGoogleEmail}"
                                    else
                                        if (isBn) "১০০% নিরাপদ ও সুরক্ষিত ক্লাউড ব্যাকআপ" else "100% Safe & Secure Cloud Backup",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (userSession.connectedGoogleEmail.isNotBlank()) Emerald700 else Stone500
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (userSession.lastBackupTimestamp > 0) {
                            Surface(
                                color = Emerald50,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Emerald700,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${if (isBn) "সর্বশেষ সফল ব্যাকআপ:" else "Last Backup:"} ${TallyFormatter.formatDate(userSession.lastBackupTimestamp, isBn)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Emerald900,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Backup Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerGoogleDriveBackup() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_drive_backup_now"),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "এখনই ব্যাকআপ নিন" else "Backup Now",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { showGoogleLinkDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_link_gmail"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (userSession.connectedGoogleEmail.isNotBlank())
                                        (if (isBn) "জিমেইল বদলান" else "Change Gmail")
                                    else
                                        (if (isBn) "জিমেইল যুক্ত করুন" else "Link Gmail"),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Stone100)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Offline File Export & Import
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val fileName = "TallyKhata_Backup_${System.currentTimeMillis()}.json"
                                    exportLauncher.launch(fileName)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_export_json"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "ফাইল এক্সপোর্ট" else "Export File", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    importLauncher.launch(arrayOf("application/json", "*/*"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_import_json"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "ডাটা রিস্টোর" else "Restore Data", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Financial Breakdown Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isBn) "ব্যবসায়িক সার্বিক আর্থিক বিবরণী" else "Business Financial Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Stone900
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        ReportStatRow(
                            label = if (isBn) "মোট কাস্টমার পাওনা (Receivable)" else "Total Customer Due",
                            value = TallyFormatter.formatMoney(summary.totalReceivable, profile.currencySymbol, isBn),
                            color = Emerald700,
                            icon = Icons.Default.ArrowDownward
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Stone100)
                        Spacer(modifier = Modifier.height(10.dp))

                        ReportStatRow(
                            label = if (isBn) "মোট সাপ্লায়ার দেনা (Payable)" else "Total Supplier Due",
                            value = TallyFormatter.formatMoney(summary.totalPayable, profile.currencySymbol, isBn),
                            color = Crimson600,
                            icon = Icons.Default.ArrowUpward
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Stone100)
                        Spacer(modifier = Modifier.height(10.dp))

                        ReportStatRow(
                            label = if (isBn) "নীট দেনা-পাওনা ব্যালেন্স" else "Net Due Balance",
                            value = TallyFormatter.formatMoney(netBusinessBalance, profile.currencySymbol, isBn),
                            color = if (netBusinessBalance >= 0) Emerald700 else Crimson600,
                            icon = Icons.Default.AccountBalance
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Stone100)
                        Spacer(modifier = Modifier.height(10.dp))

                        ReportStatRow(
                            label = if (isBn) "হাতে নগদ ক্যাশ ব্যালেন্স" else "Cash in Hand",
                            value = TallyFormatter.formatMoney(summary.totalCashBalance, profile.currencySymbol, isBn),
                            color = Teal700,
                            icon = Icons.Default.Payments
                        )
                    }
                }
            }

            // Quick Operations & Language Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isBn) "অ্যাপ সেটিংস ও সুবিধা" else "App Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Language Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleLanguage() }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = Emerald700)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(if (isBn) "ভাষা পরিবর্তন (Language)" else "Change Language")
                            }
                            Text(
                                text = if (isBn) "বাংলা" else "English",
                                fontWeight = FontWeight.Bold,
                                color = Emerald700
                            )
                        }

                        HorizontalDivider(color = Stone100)

                        // Share Statement
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, statementSummaryText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Emerald700)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(if (isBn) "হিসাব বিবরণী রিপোর্ট শেয়ার করুন" else "Share Business Statement")
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Stone500)
                        }

                        HorizontalDivider(color = Stone100)

                        // Logout / Reset Session
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.logout() }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = Crimson600)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(if (isBn) "লগআউট / অ্যাকাউন্ট পরিবর্তন" else "Logout / Switch Account", color = Crimson600)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Stone500)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            profile = profile,
            isBn = isBn,
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { shopName, owner, phone, address, currency ->
                viewModel.updateProfile(shopName, owner, phone, address, currency, profile.language)
                showEditProfileDialog = false
            }
        )
    }

    if (showGoogleLinkDialog) {
        var emailInput by remember { mutableStateOf(userSession.connectedGoogleEmail.ifBlank { "Ariyantanvirw@gmail.com" }) }
        AlertDialog(
            onDismissRequest = { showGoogleLinkDialog = false },
            title = {
                Text(
                    text = if (isBn) "গুগল ড্রাইভ অ্যাকাউন্ট লিঙ্ক করুন" else "Link Google Drive Account",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBn)
                            "আপনার ক্লাউড ব্যাকআপ নিরাপদে গুগল ড্রাইভে সংরক্ষণ করতে আপনার জিমেইল অ্যাকাউন্ট লিখুন:"
                        else
                            "Enter your Gmail account to safely store automatic backups in Google Drive:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Stone700
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text(if (isBn) "গুগল ইমেইল (Gmail)" else "Google Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailInput.isNotBlank()) {
                            viewModel.linkGoogleAccount(emailInput.trim())
                            showGoogleLinkDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text(if (isBn) "যুক্ত করুন" else "Connect", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleLinkDialog = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun ReportStatRow(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Stone700
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun EditProfileDialog(
    profile: BusinessProfile,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (shopName: String, owner: String, phone: String, address: String, currency: String) -> Unit
) {
    var shopName by remember { mutableStateOf(profile.shopName) }
    var ownerName by remember { mutableStateOf(profile.ownerName) }
    var phone by remember { mutableStateOf(profile.phone) }
    var address by remember { mutableStateOf(profile.address) }
    var currency by remember { mutableStateOf(profile.currencySymbol) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "দোকানের তথ্য সম্পাদনা" else "Edit Business Profile",
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
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text(if (isBn) "দোকান / ব্যবসার নাম *" else "Shop Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text(if (isBn) "মালিকের নাম" else "Owner Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                    label = { Text(if (isBn) "দোকানের ঠিকানা" else "Shop Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text(if (isBn) "মুদ্রা প্রতীক" else "Currency Symbol") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shopName.isNotBlank()) {
                        onConfirm(shopName, ownerName, phone, address, currency)
                    }
                },
                enabled = shopName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
            ) {
                Text(if (isBn) "সংরক্ষণ করুন" else "Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}
