package com.tripdm.agency.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripdm.agency.data.model.BookingRequest
import com.tripdm.agency.ui.components.BookingRequestCard
import com.tripdm.agency.ui.theme.*
import com.tripdm.agency.viewmodel.BookingFilterTab

@Composable
fun AgencyBookingsScreen(
    bookings: List<BookingRequest>,
    selectedTab: BookingFilterTab,
    onTabSelected: (BookingFilterTab) -> Unit,
    onConfirmBooking: (String) -> Unit,
    onCancelBooking: (String) -> Unit,
    onChatWithCustomer: (BookingRequest) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
    ) {
        // Tab Header
        Surface(color = Color.White, shadowElevation = 1.dp) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BookingFilterTab.values()) { tab ->
                    val isSelected = selectedTab == tab
                    val label = when (tab) {
                        BookingFilterTab.ALL -> "All (${bookings.size})"
                        BookingFilterTab.PENDING -> "Pending"
                        BookingFilterTab.CONFIRMED -> "Confirmed"
                        BookingFilterTab.CANCELLED -> "Cancelled"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = InterFontFamily
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryOrange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No bookings in this category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy,
                        fontFamily = PoppinsFontFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "When customers book your travel packages, their requests will appear here.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontFamily = InterFontFamily,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(bookings, key = { it.id }) { booking ->
                    BookingRequestCard(
                        booking = booking,
                        onConfirm = { onConfirmBooking(booking.id) },
                        onCancel = { onCancelBooking(booking.id) },
                        onChat = { onChatWithCustomer(booking) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
