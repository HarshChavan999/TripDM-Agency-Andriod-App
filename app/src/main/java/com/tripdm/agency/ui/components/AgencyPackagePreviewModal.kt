package com.tripdm.agency.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.ui.theme.*

@Composable
fun AgencyPackagePreviewModal(
    listing: AgencyListing,
    onDismiss: () -> Unit,
    onSubmitForApproval: ((AgencyListing) -> Unit)? = null,
    onEditClick: ((AgencyListing) -> Unit)? = null,
    isSubmitting: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp), // Leaves status bar space nicely
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Modal Header ──────────────────────────────────────
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFFF7ED), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👁️", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = "Package Preview",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    fontFamily = PoppinsFontFamily
                                )
                                Text(
                                    text = "How travelers see your package",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = DeepNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                // ── Main Scrollable Preview Content ───────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF8FAFC))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // 1. Hero Card (Title, Price, Location, Agency Name)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Badges (Type + Approval Status)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFFF7ED),
                                    border = BorderStroke(1.dp, Color(0xFFFFD8A8))
                                ) {
                                    val typeText = if (listing.packageType == "international") "🌏 International" else "🌴 Domestic"
                                    Text(
                                        text = typeText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFEA580C),
                                        fontFamily = InterFontFamily,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                val statusNorm = listing.approvalStatus.lowercase().trim()
                                val (bg, border, textColor, statusLabel) = when (statusNorm) {
                                    "approved", "active", "confirmed" -> QuadStatus(Color(0xFFECFDF5), Color(0xFFA7F3D0), Color(0xFF047857), "Approved")
                                    "rejected", "cancelled" -> QuadStatus(Color(0xFFFEF2F2), Color(0xFFFEE2E2), Color(0xFFDC2626), "Rejected")
                                    else -> QuadStatus(Color(0xFFFFF7ED), Color(0xFFFFD8A8), Color(0xFFD97706), "Pending Approval")
                                }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = bg,
                                    border = BorderStroke(1.dp, border)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        fontFamily = InterFontFamily,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Package Photos Gallery (matches webapp preview)
                            val allPhotos = (listing.photos + listing.itinerary.flatMap { it.imageUrls }).distinct().filter { it.isNotBlank() }
                            if (allPhotos.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    items(allPhotos) { photoUrl ->
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = "Package Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .width(160.dp)
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                        )
                                    }
                                }
                            }

                            // Package Title
                            Text(
                                text = listing.title.ifBlank { "Untitled Package" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                fontFamily = PoppinsFontFamily,
                                lineHeight = 24.sp
                            )

                            // Agency Name
                            if (listing.agencyName.isNotBlank()) {
                                Text(
                                    text = "Offered by ${listing.agencyName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily
                                )
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                            // Highlights Row: Duration & Pricing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "DURATION",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 0.5.sp,
                                        fontFamily = InterFontFamily
                                    )
                                    val nights = if (listing.duration > 1) listing.duration - 1 else 0
                                    Text(
                                        text = "${listing.duration} Days / $nights Nights",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy,
                                        fontFamily = InterFontFamily
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "STARTING FROM",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 0.5.sp,
                                        fontFamily = InterFontFamily
                                    )
                                    Text(
                                        text = "₹${listing.cost.toInt()}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryOrange,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }
                        }
                    }

                    // 2. Destinations & Logistics Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "📍 Travel Details & Locations",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                fontFamily = PoppinsFontFamily
                            )

                            // Covered States / Countries
                            val destinations = run {
                                val states = listing.stateNames.ifEmpty { listOfNotNull(listing.stateName.ifEmpty { null }) }
                                val countries = listing.countryNames.ifEmpty { listOfNotNull(listing.countryName.ifEmpty { null }) }
                                (states + countries).distinct().joinToString(", ")
                            }
                            if (destinations.isNotBlank()) {
                                DetailRow(label = "Destinations", value = destinations)
                            }

                            if (listing.pickUpLocation.isNotBlank()) {
                                DetailRow(label = "Pick-up Point", value = listing.pickUpLocation)
                            }

                            if (listing.dropLocation.isNotBlank()) {
                                DetailRow(label = "Drop Point", value = listing.dropLocation)
                            }

                            // Hotel Types
                            val hotels = when {
                                listing.hotelTypes.isNotEmpty() -> listing.hotelTypes.joinToString(", ") { it.capitalizeWords() }
                                listing.hotelType.isNotBlank() -> listing.hotelType.capitalizeWords()
                                else -> ""
                            }
                            if (hotels.isNotBlank()) {
                                DetailRow(label = "Hotel Category", value = hotels)
                            }

                            // Meal Plans
                            val meals = when {
                                listing.mealPlans.isNotEmpty() -> listing.mealPlans.joinToString(", ") { formatMealPlan(it) }
                                listing.mealPlan.isNotBlank() -> formatMealPlan(listing.mealPlan)
                                else -> ""
                            }
                            if (meals.isNotBlank()) {
                                DetailRow(label = "Meal Plan", value = meals)
                            }
                        }
                    }

                    // 3. Tour Categories & Experience Chips
                    if (listing.tourCategories.isNotEmpty() || listing.experienceType.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "🏷️ Categories & Experiences",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    fontFamily = PoppinsFontFamily
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    (listing.tourCategories + listing.experienceType).distinct().forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = DeepNavy,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Day-by-Day Itinerary Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "🗺️ Day-wise Itinerary",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                fontFamily = PoppinsFontFamily
                            )

                            if (listing.itinerary.isEmpty()) {
                                Text(
                                    text = "No itinerary details added yet.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily
                                )
                            } else {
                                listing.itinerary.forEachIndexed { idx, day ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PrimaryOrange
                                            ) {
                                                Text(
                                                    text = "Day ${day.day.takeIf { it > 0 } ?: (idx + 1)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontFamily = InterFontFamily,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Text(
                                                text = day.placeName.ifBlank { "Day Plan" },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepNavy,
                                                fontFamily = PoppinsFontFamily
                                            )
                                        }

                                        if (day.description.isNotBlank()) {
                                            Text(
                                                text = day.description,
                                                fontSize = 13.sp,
                                                color = Color(0xFF334155),
                                                fontFamily = InterFontFamily,
                                                lineHeight = 18.sp
                                            )
                                        }

                                         if (day.imageUrls.any { it.isNotBlank() }) {
                                             LazyRow(
                                                 horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                 modifier = Modifier.padding(top = 4.dp)
                                             ) {
                                                 items(day.imageUrls.filter { it.isNotBlank() }) { imgUrl ->
                                                     AsyncImage(
                                                         model = imgUrl,
                                                         contentDescription = "Itinerary Day Image",
                                                         contentScale = ContentScale.Crop,
                                                         modifier = Modifier
                                                             .size(90.dp)
                                                             .clip(RoundedCornerShape(8.dp))
                                                             .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                                     )
                                                 }
                                             }
                                         }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Inclusions Card
                    if (listing.inclusions.any { it.isNotBlank() }) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "✅ Package Inclusions",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857),
                                    fontFamily = PoppinsFontFamily
                                )

                                listing.inclusions.filter { it.isNotBlank() }.forEach { inc ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Text(
                                            text = inc,
                                            fontSize = 13.sp,
                                            color = DeepNavy,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6. Exclusions Card
                    if (listing.exclusions.any { it.isNotBlank() }) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "❌ Package Exclusions",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626),
                                    fontFamily = PoppinsFontFamily
                                )

                                listing.exclusions.filter { it.isNotBlank() }.forEach { exc ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Cancel,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Text(
                                            text = exc,
                                            fontSize = 13.sp,
                                            color = DeepNavy,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ── Sticky Modal Action Footer ────────────────────────
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Submit for Approval Button (if editing or approving draft)
                        if (onSubmitForApproval != null) {
                            Button(
                                onClick = { onSubmitForApproval(listing) },
                                enabled = !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Submit for Approval",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onEditClick != null) {
                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onEditClick(listing)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, PrimaryOrange),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit Package", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepNavy)
                            ) {
                                Text("Close Preview", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            fontFamily = InterFontFamily,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = DeepNavy,
            fontFamily = InterFontFamily,
            modifier = Modifier.weight(1f)
        )
    }
}

private data class QuadStatus(val bg: Color, val border: Color, val textColor: Color, val label: String)

private fun String.capitalizeWords(): String =
    this.split("-", "_", " ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

private fun formatMealPlan(plan: String): String = when (plan.lowercase()) {
    "no-meal" -> "No Meal"
    "breakfast" -> "Breakfast Only"
    "lunch" -> "Lunch Only"
    "dinner" -> "Dinner Only"
    "breakfast-lunch" -> "Breakfast + Lunch"
    "breakfast-dinner" -> "Breakfast + Dinner"
    "lunch-dinner" -> "Lunch + Dinner"
    "all-meals" -> "All Meals Included"
    else -> plan.capitalizeWords()
}
