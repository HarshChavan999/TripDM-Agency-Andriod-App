package com.tripdm.agency.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.ui.components.AgencyListingCard
import com.tripdm.agency.ui.components.AgencyPackagePreviewModal
import com.tripdm.agency.ui.theme.*
import com.tripdm.agency.viewmodel.ListingFilterTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyListingsScreen(
    listings: List<AgencyListing>,
    selectedTab: ListingFilterTab,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onTabSelected: (ListingFilterTab) -> Unit,
    onCreateListingClick: () -> Unit,
    onEditListingClick: (AgencyListing) -> Unit,
    onDeleteListingClick: (String) -> Unit,
    onListingClick: (AgencyListing) -> Unit
) {
    var listingToDelete by remember { mutableStateOf<String?>(null) }
    var previewListing by remember { mutableStateOf<AgencyListing?>(null) }

    if (previewListing != null) {
        AgencyPackagePreviewModal(
            listing = previewListing!!,
            onDismiss = { previewListing = null },
            onEditClick = { listing ->
                previewListing = null
                onEditListingClick(listing)
            }
        )
    }

    if (listingToDelete != null) {
        AlertDialog(
            onDismissRequest = { listingToDelete = null },
            title = { Text("Delete Package", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this travel package? This action cannot be undone.", fontFamily = InterFontFamily) },
            confirmButton = {
                Button(
                    onClick = {
                        listingToDelete?.let { onDeleteListingClick(it) }
                        listingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { listingToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateListingClick,
                containerColor = PrimaryOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Package")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(padding)
        ) {
            // ── Webapp Style Header ──────────────────
            Surface(
                color = Color.White,
                shadowElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🌴", fontSize = 22.sp)
                        Text(
                            text = "Your Travel Listings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            fontFamily = PoppinsFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Manage and search your travel packages (${listings.size} total)",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        fontFamily = InterFontFamily
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Search packages by title, state, price...",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontFamily = InterFontFamily
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontFamily = InterFontFamily,
                            color = DeepNavy
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ListingFilterTab.values()) { tab ->
                            val isSelected = selectedTab == tab
                            val label = when (tab) {
                                ListingFilterTab.ALL -> "All (${listings.size})"
                                ListingFilterTab.APPROVED -> "Approved"
                                ListingFilterTab.PENDING -> "Pending Review"
                                ListingFilterTab.REJECTED -> "Rejected"
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
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = DeepNavy
                                ),
                                border = null
                            )
                        }
                    }
                }
            }

            // ── Listings List ────────────────────────
            if (listings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No packages found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first travel package to start receiving customer inquiries.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontFamily = InterFontFamily,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onCreateListingClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Package")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listings, key = { it.id }) { listing ->
                        AgencyListingCard(
                            listing = listing,
                            onEdit = { onEditListingClick(listing) },
                            onDelete = { listingToDelete = listing.id },
                            onClick = { previewListing = listing }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}
