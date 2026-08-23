package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.BusinessProfile
import com.example.data.model.ContactAccount
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TallyFormatter {
    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun formatMoney(amount: Double, symbol: String = "৳", isBn: Boolean = true): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        formatter.minimumFractionDigits = 0
        val formattedNumber = formatter.format(Math.abs(amount))

        val finalNumber = if (isBn) {
            formattedNumber.map { ch ->
                if (ch in '0'..'9') bnDigits[ch - '0'] else ch
            }.joinToString("")
        } else {
            formattedNumber
        }

        return "$symbol $finalNumber"
    }

    fun formatNumber(number: Number, isBn: Boolean = true): String {
        val str = number.toString()
        return if (isBn) {
            str.map { ch ->
                if (ch in '0'..'9') bnDigits[ch - '0'] else ch
            }.joinToString("")
        } else {
            str
        }
    }

    fun formatDate(timestamp: Long, isBn: Boolean = true): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val formatted = sdf.format(date)
        return if (isBn) {
            formatted.map { ch ->
                if (ch in '0'..'9') bnDigits[ch - '0'] else ch
            }.joinToString("")
                .replace("AM", "সকাল")
                .replace("PM", "বিকাল/রাত")
        } else {
            formatted
        }
    }

    fun formatDateOnly(timestamp: Long, isBn: Boolean = true): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
        val formatted = sdf.format(date)
        return if (isBn) {
            formatted.map { ch ->
                if (ch in '0'..'9') bnDigits[ch - '0'] else ch
            }.joinToString("")
        } else {
            formatted
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    amount: String,
    subtitle: String? = null,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
fun ContactAvatar(
    name: String,
    colorHex: Long,
    size: Int = 46,
    modifier: Modifier = Modifier
) {
    val initial = name.trim().take(1).uppercase()
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(colorHex)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.ifEmpty { "TK" },
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.42).sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderBottomSheet(
    account: ContactAccount,
    profile: BusinessProfile,
    isBn: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val amountStr = TallyFormatter.formatMoney(Math.abs(account.currentBalance), profile.currencySymbol, isBn)

    val defaultMessage = if (isBn) {
        "আসসালামু আলাইকুম ${account.name},\n'${profile.shopName}' থেকে জানানো যাচ্ছে যে, আপনার কাছে বাকি বাবদ $amountStr পাওনা রয়েছে। অনুগ্রহ করে সুবিধাজনক সময়ে পরিশোধ করার অনুরোধ রইল।\n- ${profile.shopName}, মোবাইল: ${profile.phone}"
    } else {
        "Dear ${account.name},\nThis is a polite reminder from '${profile.shopName}' regarding your pending balance of $amountStr. Kindly settle it at your earliest convenience.\n- ${profile.shopName}, Ph: ${profile.phone}"
    }

    var messageText by remember { mutableStateOf(defaultMessage) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Emerald100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Emerald700
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isBn) "বাকি আদায়ের তাগাদা মেসেজ" else "Due Payment Reminder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${account.name} • ${account.phone.ifEmpty { if (isBn) "ফোন নম্বর নেই" else "No phone" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text(if (isBn) "মেসেজ সম্পাদনা করুন" else "Edit Message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald600,
                    unfocusedBorderColor = Stone300
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // WhatsApp Button
                Button(
                    onClick = {
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=${account.phone.replace("+", "").replace("-", "")}&text=${Uri.encode(messageText)}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to regular share
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, messageText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("whatsapp_reminder_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", color = White, fontWeight = FontWeight.Bold)
                }

                // SMS / Share Button
                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:${account.phone}")
                            putExtra("sms_body", messageText)
                        }
                        try {
                            context.startActivity(sendIntent)
                        } catch (e: Exception) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, messageText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sms_reminder_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "এসএমএস পাঠান" else "Send SMS", color = White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
