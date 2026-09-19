package com.tripdm.agency.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripdm.agency.data.model.AgencyProfile
import com.tripdm.agency.data.model.AnalyticsSummary
import com.tripdm.agency.data.model.ChatConversation
import com.tripdm.agency.ui.components.StatCard
import com.tripdm.agency.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AgencyDashboardScreen(
    profile: AgencyProfile,
    analytics: AnalyticsSummary,
    recentChats: List<ChatConversation>,
    onCreateListingClick: () -> Unit,
    onViewListingsClick: () -> Unit,
    onViewChatsClick: () -> Unit,
    onViewCreditsClick: () -> Unit,
    onChatClick: (ChatConversation) -> Unit
) {
    val todaysLeadsCount = remember(recentChats) {
        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        recentChats.count { chat ->
            val chatCal = Calendar.getInstance().apply { timeInMillis = chat.lastMessageTimestamp }
            chatCal.get(Calendar.YEAR) == todayYear && chatCal.get(Calendar.DAY_OF_YEAR) == todayDay
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
            val greeting = remember(currentHour) {
                when (currentHour) {
                    in 4..11 -> "Good Morning,"
                    in 12..16 -> "Good Afternoon,"
                    else -> "Good Evening,"
                }
            }
            val totalUnreadCount = remember(recentChats) {
                recentChats.sumOf { it.unreadCount }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF092540),
                                    Color(0xFF0F3B66),
                                    Color(0xFF145388)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    // Right Decorative Graphic (Explore Connect Grow motif)
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 46.dp, top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Explore\nConnect\nGrow",
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Light,
                            color = Color.White.copy(alpha = 0.35f),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(45f)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Top row: Greeting on left, Notification Bell on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = greeting,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontFamily = InterFontFamily
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = profile.companyName.ifEmpty { "BOMYTRA" }.uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontFamily = PoppinsFontFamily,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Turn Travelers Into Journeys",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontFamily = InterFontFamily
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                // Orange Underline Accent Bar
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(3.5.dp)
                                        .background(PrimaryOrange, RoundedCornerShape(2.dp))
                                )
                            }

                            // Notification Bell Button with red badge counter
                            Box(
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    onClick = onViewChatsClick,
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.35f),
                                    contentColor = Color.White,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Notifications,
                                            contentDescription = "Notifications",
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                if (totalUnreadCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFEF4444),
                                        contentColor = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-2).dp)
                                    ) {
                                        Text("$totalUnreadCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Credits Pill — full width row
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryOrange.copy(alpha = 0.22f),
                            modifier = Modifier
                                .wrapContentWidth()
                                .clickable { onViewCreditsClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = "Credits",
                                    tint = PrimaryOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${profile.credits} Credits",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onCreateListingClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Package", fontSize = 13.sp, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold)
                            }

                            FilledTonalButton(
                                onClick = onViewCreditsClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.18f),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Recharge", fontSize = 13.sp, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Stats Grid — flat bordered container, no card shadows
        item {
            Text(
                text = "Performance Overview",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepNavy,
                fontFamily = PoppinsFontFamily
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Packages",
                        value = "${analytics.totalListings}",
                        icon = Icons.Default.Luggage,
                        iconTint = PrimaryOrange,
                        iconBgColor = PrimaryOrange.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f),
                        subtitle = "${analytics.approvedListings} Live"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .align(Alignment.CenterVertically)
                            .background(Color(0xFFE2E8F0))
                    )
                    StatCard(
                        title = "Pending Review",
                        value = "${analytics.pendingListings}",
                        icon = Icons.Default.HourglassTop,
                        iconTint = Color(0xFFF57F17),
                        iconBgColor = Color(0xFFFFF8E1),
                        modifier = Modifier.weight(1f),
                        subtitle = "Awaiting approval"
                    )
                }
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Traveler Leads",
                        value = "$todaysLeadsCount",
                        icon = Icons.Default.Chat,
                        iconTint = M3Info,
                        iconBgColor = M3Info.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f),
                        subtitle = "Today's active inquiries"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .align(Alignment.CenterVertically)
                            .background(Color(0xFFE2E8F0))
                    )
                    StatCard(
                        title = "Available Credits",
                        value = "${profile.credits}",
                        icon = Icons.Default.CurrencyRupee,
                        iconTint = PrimaryOrange,
                        iconBgColor = PrimaryOrange.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f),
                        subtitle = "Tap to recharge"
                    )
                }
            }
        }

        // Recent Customer Chats / Leads
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Traveler Leads & Inquiries",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )
                TextButton(onClick = onViewChatsClick) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("See all", color = PrimaryOrange, fontSize = 13.sp, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        if (recentChats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No traveler messages yet.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontFamily = InterFontFamily
                    )
                }
            }
        } else {
            // Wrap all lead rows in a single bordered container
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        .background(Color.White)
                ) {
                    recentChats.forEachIndexed { index, chat ->
                        val timeStr = remember(chat.lastMessageTimestamp) {
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(chat.lastMessageTimestamp))
                        }
                        val dateStr = remember(chat.lastMessageTimestamp) {
                            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(chat.lastMessageTimestamp))
                        }

                        val avatarBgColors = remember {
                            listOf(
                                Color(0xFFE8F5E9) to Color(0xFF2E7D32),
                                Color(0xFFE3F2FD) to Color(0xFF1565C0),
                                Color(0xFFFFF3E0) to Color(0xFFE65100),
                                Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
                            )
                        }
                        val colorPair = avatarBgColors[kotlin.math.abs(chat.otherUserId.hashCode()) % avatarBgColors.size]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChatClick(chat) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar circle
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(colorPair.first, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val initialText = if (chat.otherUserName.startsWith("Lead #")) {
                                    "#" + chat.otherUserName.removePrefix("Lead #")
                                } else {
                                    chat.otherUserName.take(1).uppercase()
                                }
                                Text(
                                    text = initialText,
                                    fontSize = if (initialText.length > 2) 12.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorPair.second
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Middle: name + subtitle
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chat.otherUserName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepNavy,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (!chat.relatedListingTitle.isNullOrBlank()) chat.relatedListingTitle else chat.lastMessage,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Right: time + badge
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = timeStr,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = InterFontFamily
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (chat.unreadCount > 0) Color(0xFFE8F5E9) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = if (chat.unreadCount > 0) "New" else "Replied",
                                        color = if (chat.unreadCount > 0) Color(0xFF2E7D32) else Color(0xFF64748B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }
                        }

                        if (index < recentChats.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFE2E8F0),
                                thickness = 0.8.dp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
