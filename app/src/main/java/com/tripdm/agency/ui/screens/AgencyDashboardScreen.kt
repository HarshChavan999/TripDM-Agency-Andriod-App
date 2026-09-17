package com.tripdm.agency.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
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

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bottom Row: Credits Pill & Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Credits Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PrimaryOrange.copy(alpha = 0.25f),
                                modifier = Modifier.clickable { onViewCreditsClick() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
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

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onCreateListingClick,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Package", fontSize = 12.sp, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold)
                                }

                                FilledTonalButton(
                                    onClick = onViewCreditsClick,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.18f),
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Recharge", fontSize = 12.sp, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats Grid
        item {
            Text(
                text = "Performance Overview",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                fontFamily = PoppinsFontFamily
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Packages",
                    value = "${analytics.totalListings}",
                    icon = Icons.Default.Luggage,
                    iconTint = PrimaryOrange,
                    iconBgColor = PrimaryOrange.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f),
                    subtitle = "${analytics.approvedListings} Live"
                )

                StatCard(
                    title = "Pending Review",
                    value = "${analytics.pendingListings}",
                    icon = Icons.Default.HourglassTop,
                    iconTint = Color(0xFFF57F17),
                    iconBgColor = Color(0xFFFFF8E1),
                    modifier = Modifier.weight(1f),
                    subtitle = "Awaiting verification"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Traveler Leads",
                    value = "${recentChats.size.coerceAtLeast(analytics.totalInquiries)}",
                    icon = Icons.Default.Chat,
                    iconTint = M3Info,
                    iconBgColor = M3Info.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f),
                    subtitle = "Active inquiries"
                )

                StatCard(
                    title = "Available Credits",
                    value = "${profile.credits}",
                    icon = Icons.Default.MonetizationOn,
                    iconTint = PrimaryOrange,
                    iconBgColor = PrimaryOrange.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f),
                    subtitle = "Tap to recharge"
                )
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No traveler messages yet.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }
        } else {
            items(recentChats) { chat ->
                val timeStr = remember(chat.lastMessageTimestamp) {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(chat.lastMessageTimestamp))
                }
                val dateStr = remember(chat.lastMessageTimestamp) {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(chat.lastMessageTimestamp))
                }

                val avatarBgColors = remember {
                    listOf(
                        Color(0xFFE8F5E9) to Color(0xFF2E7D32), // Soft Green
                        Color(0xFFE3F2FD) to Color(0xFF1565C0), // Soft Blue
                        Color(0xFFFFF3E0) to Color(0xFFE65100), // Soft Orange
                        Color(0xFFF3E5F5) to Color(0xFF7B1FA2)  // Soft Purple
                    )
                }
                val colorPair = avatarBgColors[kotlin.math.abs(chat.otherUserId.hashCode()) % avatarBgColors.size]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChatClick(chat) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Soft-tinted Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
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
                                fontSize = if (initialText.length > 2) 13.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorPair.second
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Middle Details: Name, Package Title, Date
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chat.otherUserName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                fontFamily = InterFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!chat.relatedListingTitle.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = chat.relatedListingTitle,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = chat.lastMessage,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Date Info (📅 15 Oct 2025) - No number of travelers as requested!
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Column: Time, Status Badge (New/Replied), and Direct Chat Button
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = timeStr,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = InterFontFamily
                            )

                            // Status Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (chat.unreadCount > 0) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                            ) {
                                Text(
                                    text = if (chat.unreadCount > 0) "New" else "Replied",
                                    color = if (chat.unreadCount > 0) Color(0xFF2E7D32) else Color(0xFF1976D2),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    fontFamily = InterFontFamily
                                )
                            }

                            // Direct System Chat Button
                            Surface(
                                onClick = { onChatClick(chat) },
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryOrange,
                                contentColor = Color.White,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "Chat Direct",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
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
