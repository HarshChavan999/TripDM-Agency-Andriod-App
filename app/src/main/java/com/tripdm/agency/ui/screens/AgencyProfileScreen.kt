package com.tripdm.agency.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.tripdm.agency.ui.components.StatusBadge
import com.tripdm.agency.ui.theme.*

@Composable
fun AgencyProfileScreen(
    profile: AgencyProfile,
    onViewCreditsClick: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGray)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(PrimaryOrange.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = PrimaryOrange,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = profile.companyName.ifEmpty { "Agency Name" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = profile.contactPersonName,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontFamily = InterFontFamily
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatusBadge(status = profile.approvalStatus)
            }
        }

        // Quick Credits Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Available Balance",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontFamily = InterFontFamily
                    )
                    Text(
                        text = "${profile.credits} Credits",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryOrange,
                        fontFamily = PoppinsFontFamily
                    )
                }

                Button(
                    onClick = onViewCreditsClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Recharge", fontSize = 12.sp, fontFamily = InterFontFamily)
                }
            }
        }

        // Business Contact & Location
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Agency Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )
                Spacer(modifier = Modifier.height(14.dp))

                ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = profile.email)
                ProfileInfoRow(icon = Icons.Default.Phone, label = "Phone", value = "${profile.countryCode} ${profile.phone}")
                ProfileInfoRow(icon = Icons.Default.LocationOn, label = "Location", value = profile.businessLocation.ifEmpty { "Not specified" })
                ProfileInfoRow(icon = Icons.Default.Home, label = "Address", value = profile.fullAddress.ifEmpty { "Not specified" })
            }
        }

        // Operational Setup
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Operational Model",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )
                Spacer(modifier = Modifier.height(12.dp))

                val modeText = if (profile.operatingFromOffice) "Commercial Office" else "Home / Remote Office"
                ProfileInfoRow(icon = Icons.Default.Store, label = "Workplace", value = modeText)
                if (profile.operatingFromOffice && profile.officeAddress.isNotBlank()) {
                    ProfileInfoRow(icon = Icons.Default.Map, label = "Office Address", value = profile.officeAddress)
                }
            }
        }

        // Refund Policy
        if (profile.refundPolicy.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Refund & Cancellation Policy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy,
                        fontFamily = PoppinsFontFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.refundPolicy,
                        fontSize = 13.sp,
                        color = SlateGray,
                        fontFamily = InterFontFamily,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Sign Out Button
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = M3Error)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = M3Error)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold, fontFamily = InterFontFamily)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontFamily = InterFontFamily
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = DeepNavy,
                fontFamily = InterFontFamily
            )
        }
    }
}
