package com.tripdm.agency.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.ui.theme.DeepNavy
import com.tripdm.agency.ui.theme.InterFontFamily
import com.tripdm.agency.ui.theme.PoppinsFontFamily

private data class CardStatusStyle(val bg: Color, val border: Color, val textColor: Color, val label: String)

@Composable
fun AgencyListingCard(
    listing: AgencyListing,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Content Row: Icon Box + Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left: Yellow Palm Tree Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFF7ED), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFFFD8A8), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌴", fontSize = 22.sp)
                }

                // Right: Package Information
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Package Title
                    Text(
                        text = listing.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily,
                        color = DeepNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Type & Location (e.g. Domestic • Meghalaya, Assam)
                    val locations = run {
                        val states = listing.stateNames.ifEmpty {
                            listOfNotNull(listing.stateName.ifEmpty { null })
                        }
                        val countries = listing.countryNames.ifEmpty {
                            listOfNotNull(listing.countryName.ifEmpty { null })
                        }
                        (states + countries).distinct().joinToString(", ")
                    }
                    val typeStr = listing.packageType.replaceFirstChar { it.uppercase() }
                    Text(
                        text = "$typeStr • $locations",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        fontFamily = InterFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Duration, Price & Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${listing.duration} days  •  ₹${listing.cost.toInt()}",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            fontFamily = InterFontFamily
                        )

                        // Status Pill Badge
                        val statusNorm = listing.approvalStatus.lowercase().trim()
                        val (bg, border, textColor, label) = when (statusNorm) {
                            "approved", "active", "confirmed" -> CardStatusStyle(Color(0xFFECFDF5), Color(0xFFA7F3D0), Color(0xFF047857), "Approved")
                            "rejected", "cancelled" -> CardStatusStyle(Color(0xFFFEF2F2), Color(0xFFFEE2E2), Color(0xFFDC2626), "Rejected")
                            else -> CardStatusStyle(Color(0xFFFFF7ED), Color(0xFFFFD8A8), Color(0xFFD97706), "Pending")
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = bg,
                            border = BorderStroke(1.dp, border)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontFamily = InterFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Places Covered
                    val placesStr = listing.itinerary.mapNotNull { it.placeName.ifBlank { null } }.joinToString(", ")
                    Text(
                        text = "Places: $placesStr",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontFamily = InterFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions: Preview | Edit | Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepNavy),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Preview",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily
                    )
                }

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepNavy),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily
                    )
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFDC2626)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily
                    )
                }
            }
        }
    }
}
