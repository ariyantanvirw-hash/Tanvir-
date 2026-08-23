package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TallyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: TallyViewModel,
    onContinueGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAuthTab by remember { mutableStateOf(0) } // 0: Mobile Login, 1: Google Sign In
    var phone by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    var googleEmail by remember { mutableStateOf("Ariyantanvirw@gmail.com") }
    var googleName by remember { mutableStateOf("Ariyan Tanvir") }

    Scaffold(
        containerColor = Stone50,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Emerald700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "TallyKhata Logo",
                    tint = White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ট্যালিখাতা (TallyKhata)",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Emerald900
            )

            Text(
                text = "সহজ, নিরাপদ ও ডিজিটাল খাতা এবং ক্যাশ হিসাব",
                style = MaterialTheme.typography.bodyMedium,
                color = Stone600,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Auth Tab Selector (Phone / Google)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    TabRow(
                        selectedTabIndex = selectedAuthTab,
                        containerColor = Stone100,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedAuthTab == 0,
                            onClick = { selectedAuthTab = 0 },
                            text = { Text("মোবাইল নম্বর", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_auth_phone")
                        )
                        Tab(
                            selected = selectedAuthTab == 1,
                            onClick = { selectedAuthTab = 1 },
                            text = { Text("গুগল (Google)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_auth_google")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedAuthTab == 0) {
                        // Phone Auth Mode
                        if (!isOtpSent) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("মোবাইল নম্বর (যেমন: 017xxxxxxxx) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Emerald700)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_auth_phone")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = shopName,
                                onValueChange = { shopName = it },
                                label = { Text("দোকান / ব্যবসার নাম (ঐচ্ছিক)") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Stone600)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_auth_shop")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                label = { Text("আপনার নাম (ঐচ্ছিক)") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Stone600)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_auth_owner")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (phone.length >= 6) {
                                        isOtpSent = true
                                        otpCode = "1234" // Default convenience OTP
                                    }
                                },
                                enabled = phone.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_get_otp"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                            ) {
                                Text("পরবর্তী (OTP পাঠান)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        } else {
                            // OTP Verification Screen
                            Text(
                                text = "আমরা ${phone} নম্বরে একটি ওটিপি কোড পাঠিয়েছি।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Stone700
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text("৪-সংখ্যার OTP কোড দিন") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_auth_otp")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.loginWithPhone(
                                        phoneNumber = phone.trim(),
                                        shopName = shopName.trim().ifBlank { "আমার দোকান" },
                                        ownerName = ownerName.trim().ifBlank { "দোকানের মালিক" }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_confirm_otp"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                            ) {
                                Text("লগইন সম্পন্ন করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { isOtpSent = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("নম্বর পরিবর্তন করুন", color = Stone600)
                            }
                        }
                    } else {
                        // Google One-Tap Auth Mode
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "গুগল অ্যাকাউন্ট দিয়ে সরাসরি সাইন ইন করুন। সকল লেনদেনের স্বয়ংক্রিয় ব্যাকআপ গুগল ড্রাইভে সুরক্ষিত থাকবে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = Stone600,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = googleEmail,
                                onValueChange = { googleEmail = it },
                                label = { Text("গুগল ইমেইল (Gmail)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                leadingIcon = {
                                    Icon(Icons.Default.Mail, contentDescription = null, tint = Emerald700)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_auth_gmail")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = googleName,
                                onValueChange = { googleName = it },
                                label = { Text("আপনার নাম") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Emerald700)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_auth_gname")
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    viewModel.loginWithGoogle(
                                        email = googleEmail.trim(),
                                        displayName = googleName.trim().ifBlank { "ব্যবসায়ী" },
                                        avatarUrl = ""
                                    )
                                },
                                enabled = googleEmail.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_google_signin"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Google দিয়ে প্রবেশ করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guest / Offline Mode Skip Button
            TextButton(
                onClick = {
                    viewModel.loginWithPhone(
                        phoneNumber = "",
                        shopName = "আমার ব্যবসা প্রতিষ্ঠান",
                        ownerName = "দোকানের মালিক"
                    )
                },
                modifier = Modifier.testTag("btn_skip_auth")
            ) {
                Text(
                    text = "লগইন ছাড়াই অফলাইন মোডে ব্যবহার করুন →",
                    color = Emerald700,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
