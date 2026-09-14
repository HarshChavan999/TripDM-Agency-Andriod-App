package com.tripdm.agency.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripdm.agency.data.model.AgencyProfile
import com.tripdm.agency.data.model.AnalyticsSummary
import com.tripdm.agency.data.model.BookingRequest
import com.tripdm.agency.data.model.ChatConversation
import com.tripdm.agency.ui.components.StatCard
import com.tripdm.agency.ui.components.StatusBadge
import com.tripdm.agency.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AgencyDashboardScreen(
    profile: AgencyProfile,
    analytics: AnalyticsSummary,
    recentBookings: List<BookingRequest>,
    recentChats: List<ChatConversation>,
    onCreateListingClick: () -> Unit,
    onViewListingsClick: () -> Unit,
    onViewBookingsClick: () -> Unit,
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back,",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontFamily = InterFontFamily
                            )
                            Text(
                                text = profile.companyName.ifEmpty { "Agency Partner" },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = PoppinsFontFamily
                            )
                        }

                        // Credits Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryOrange.copy(alpha = 0.2f),
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${profile.credits} Credits",
                                    color = PrimaryOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onCreateListingClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Package", fontSize = 13.sp, fontFamily = InterFontFamily)
                        }

                        FilledTonalButton(
                            onClick = onViewCreditsClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Recharge", fontSize = 13.sp, fontFamily = InterFontFamily)
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
                    title = "Bookings",
                    value = "${analytics.totalBookings}",
                    icon = Icons.Default.CalendarMonth,
                    iconTint = M3Success,
                    iconBgColor = M3Success.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f),
                    subtitle = "${analytics.confirmedBookings} Confirmed"
                )

                StatCard(
                    title = "Inquiries",
                    value = "${analytics.totalInquiries}",
                    icon = Icons.Default.Chat,
                    iconTint = M3Info,
                    iconBgColor = M3Info.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f),
                    subtitle = "Traveler leads"
                )
            }
        }

        // Recent Inbound Bookings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Booking Requests",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )
                TextButton(onClick = onViewBookingsClick) {
                    Text("See all", color = PrimaryOrange, fontSize = 13.sp, fontFamily = InterFontFamily)
                }
            }
        }

        if (recentBookings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No booking requests received yet.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }
        } else {
            items(recentBookings) { booking ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewBookingsClick() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = booking.listingTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepNavy,
                                fontFamily = InterFontFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${booking.userName} • ${booking.travelers} Travelers",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontFamily = InterFontFamily
                            )
                        }
                        StatusBadge(status = booking.status)
                    }
                }
            }
        }

        // Recent Customer Chats
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Traveler Inquiries",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )
            }
        }

        if (recentChats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(chat.lastMessageTimestamp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChatClick(chat) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryOrange.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.otherUserName.take(1).uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = chat.otherUserName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepNavy,
                                    fontFamily = InterFontFamily
                                )
                                Text(
                                    text = timeStr,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = chat.lastMessage,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontFamily = InterFontFamily,
                                maxLines = 1
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
