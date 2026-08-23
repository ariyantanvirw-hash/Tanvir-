package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TallyViewModel
import com.example.ui.screens.*
import com.example.ui.theme.Emerald700
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TallyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                TallyKhataApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TallyKhataApp(viewModel: TallyViewModel) {
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()

    if (!userSession.isLoggedIn) {
        AuthScreen(
            viewModel = viewModel,
            onContinueGuest = {
                viewModel.loginWithPhone(
                    phoneNumber = "",
                    shopName = "আমার ব্যবসা প্রতিষ্ঠান",
                    ownerName = "দোকানের মালিক"
                )
            }
        )
        return
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedAccountId by viewModel.selectedAccountId.collectAsStateWithLifecycle()
    val selectedAccount by viewModel.selectedAccount.collectAsStateWithLifecycle()
    val selectedAccountTransactions by viewModel.selectedAccountTransactions.collectAsStateWithLifecycle()

    val filteredAccounts by viewModel.filteredAccounts.collectAsStateWithLifecycle()
    val cashEntries by viewModel.allCashEntries.collectAsStateWithLifecycle()
    val memos by viewModel.allCashMemos.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()

    val isBn = profile.language == "bn"

    // If an account is selected, handle back to list
    if (selectedAccountId != null && selectedAccount != null) {
        BackHandler {
            viewModel.selectAccount(null)
        }

        AccountDetailScreen(
            account = selectedAccount!!,
            transactions = selectedAccountTransactions,
            profile = profile,
            viewModel = viewModel,
            onBack = { viewModel.selectAccount(null) }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // Tab 0: Khata
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.setTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 0) Icons.Default.MenuBook else Icons.Outlined.MenuBook,
                                contentDescription = "Khata",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isBn) "খাতা" else "Khata",
                                fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald700,
                            selectedTextColor = Emerald700,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_khata")
                    )

                    // Tab 1: Cash Box
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.setTab(1) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 1) Icons.Default.Payments else Icons.Outlined.Payments,
                                contentDescription = "Cash Box",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isBn) "ক্যাশ বাক্স" else "Cash Box",
                                fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald700,
                            selectedTextColor = Emerald700,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_cashbox")
                    )

                    // Tab 2: Cash Memo
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.setTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 2) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                contentDescription = "Memo",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isBn) "ক্যাশ মেমো" else "Memo",
                                fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald700,
                            selectedTextColor = Emerald700,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_memo")
                    )

                    // Tab 3: Reports & More
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.setTab(3) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 3) Icons.Default.Assessment else Icons.Outlined.Assessment,
                                contentDescription = "Reports",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isBn) "রিপোর্ট" else "Reports",
                                fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald700,
                            selectedTextColor = Emerald700,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_reports")
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> KhataScreen(
                            viewModel = viewModel,
                            accounts = filteredAccounts,
                            summary = summary,
                            profile = profile,
                            onSelectAccount = { accountId ->
                                viewModel.selectAccount(accountId)
                            }
                        )
                        1 -> CashBoxScreen(
                            viewModel = viewModel,
                            cashEntries = cashEntries,
                            summary = summary,
                            profile = profile
                        )
                        2 -> CashMemoScreen(
                            viewModel = viewModel,
                            memos = memos,
                            profile = profile
                        )
                        3 -> ReportsScreen(
                            viewModel = viewModel,
                            summary = summary,
                            profile = profile
                        )
                    }
                }
            }
        }
    }
}
