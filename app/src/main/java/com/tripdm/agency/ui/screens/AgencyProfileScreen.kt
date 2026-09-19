package com.tripdm.agency.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tripdm.agency.data.model.AgencyPolicyData
import com.tripdm.agency.data.model.AgencyPolicyTab
import com.tripdm.agency.data.model.AgencyProfile
import com.tripdm.agency.data.model.CreditTransaction
import com.tripdm.agency.ui.components.StatusBadge
import com.tripdm.agency.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AgencyProfileScreen(
    profile: AgencyProfile,
    transactions: List<CreditTransaction> = emptyList(),
    onViewCreditsClick: () -> Unit,
    onUpdateProfile: (AgencyProfile) -> Unit = {},
    onSignOut: () -> Unit
) {
    var activePolicyTab by remember { mutableStateOf<AgencyPolicyTab?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (activePolicyTab != null) {
        AgencyPolicyViewerScreen(
            policyTab = activePolicyTab!!,
            onBack = { activePolicyTab = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(PrimaryOrange.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = profile.logoUrl,
                            contentDescription = "Agency Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = profile.companyName.ifEmpty { "Agency Name" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )

                if (profile.contactPersonName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = profile.contactPersonName,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontFamily = InterFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(status = profile.approvalStatus)

                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = PrimaryOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit Agency Details",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryOrange,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0x1F000000), thickness = 0.5.dp)

            // 1. Agency Details Section (FIRST)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BusinessCenter,
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agency Details",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            fontFamily = PoppinsFontFamily
                        )
                    }

                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Agency Details",
                            tint = PrimaryOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = profile.email)
                HorizontalDivider(color = Color(0x10000000), thickness = 0.5.dp)
                ProfileInfoRow(icon = Icons.Default.Phone, label = "Phone", value = "${profile.countryCode} ${profile.phone}".trim())
                HorizontalDivider(color = Color(0x10000000), thickness = 0.5.dp)
                ProfileInfoRow(icon = Icons.Default.LocationOn, label = "Location", value = profile.businessLocation.ifEmpty { "Not specified" })
                HorizontalDivider(color = Color(0x10000000), thickness = 0.5.dp)
                ProfileInfoRow(icon = Icons.Default.Home, label = "Address", value = profile.fullAddress.ifEmpty { "Not specified" })
                HorizontalDivider(color = Color(0x10000000), thickness = 0.5.dp)

                val modeText = if (profile.operatingFromOffice) "Commercial Office" else "Home / Remote Office"
                ProfileInfoRow(icon = Icons.Default.Store, label = "Workplace", value = modeText)
                if (profile.operatingFromOffice && profile.officeAddress.isNotBlank()) {
                    HorizontalDivider(color = Color(0x10000000), thickness = 0.5.dp)
                    ProfileInfoRow(icon = Icons.Default.Map, label = "Office Address", value = profile.officeAddress)
                }
            }

            HorizontalDivider(color = Color(0x1F000000), thickness = 0.5.dp)

            // 2. Billing & Subscription Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Billing & Subscription",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                    Surface(
                        color = PrimaryOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = profile.plan.ifBlank { "Free" },
                            color = PrimaryOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            fontSize = 24.sp,
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

            HorizontalDivider(color = Color(0x1F000000), thickness = 0.5.dp)

            // 3. Transaction History Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transaction History",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                    if (transactions.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${transactions.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TextHint,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No transactions recorded yet.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                } else {
                    val displayTxs = transactions.take(5)
                    displayTxs.forEachIndexed { index, tx ->
                        val timeStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (tx.type == "purchase") PrimaryOrange.copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.type == "purchase") Icons.Default.CurrencyRupee else Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = if (tx.type == "purchase") PrimaryOrange else SlateGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tx.description,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DeepNavy,
                                        fontFamily = InterFontFamily,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = timeStr,
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }

                            Text(
                                text = if (tx.amount > 0) "₹${tx.amount.toInt()}" else "+${tx.credits} Cr",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange,
                                fontFamily = InterFontFamily
                            )
                        }

                        if (index < displayTxs.size - 1) {
                            HorizontalDivider(
                                color = Color(0x10000000),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onViewCreditsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Manage Plans & View All History",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = InterFontFamily
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0x1F000000), thickness = 0.5.dp)

            // 4. Legal & Platform Policies Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Legal & Platform Policies",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )
                Spacer(modifier = Modifier.height(12.dp))

                PolicyRowItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "Vendor Code of Conduct & Dealing Policy",
                    onClick = { activePolicyTab = AgencyPolicyTab.VENDOR_CODE_OF_CONDUCT }
                )
            }

            HorizontalDivider(color = Color(0x1F000000), thickness = 0.5.dp)

            // 5. Sign Out Button
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

    if (showEditDialog) {
        EditAgencyProfileDialog(
            profile = profile,
            onDismiss = { showEditDialog = false },
            onSave = { updatedProfile ->
                showEditDialog = false
                onUpdateProfile(updatedProfile)
            }
        )
    }
}

@Composable
fun EditAgencyProfileDialog(
    profile: AgencyProfile,
    onDismiss: () -> Unit,
    onSave: (AgencyProfile) -> Unit
) {
    var logoUrl by remember { mutableStateOf(profile.logoUrl) }
    var companyName by remember { mutableStateOf(profile.companyName) }
    var contactPersonName by remember { mutableStateOf(profile.contactPersonName) }
    var phone by remember { mutableStateOf(profile.phone) }
    var countryCode by remember { mutableStateOf(profile.countryCode) }
    var businessLocation by remember { mutableStateOf(profile.businessLocation) }
    var fullAddress by remember { mutableStateOf(profile.fullAddress) }
    var operatingFromOffice by remember { mutableStateOf(profile.operatingFromOffice) }
    var officeAddress by remember { mutableStateOf(profile.officeAddress) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Agency Details",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DeepNavy,
                fontFamily = PoppinsFontFamily
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("Agency Logo Image URL") },
                    placeholder = { Text("https://example.com/logo.png") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Agency / Company Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactPersonName,
                    onValueChange = { contactPersonName = it },
                    label = { Text("Contact Person Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = { countryCode = it },
                        label = { Text("Code") },
                        singleLine = true,
                        modifier = Modifier.weight(0.35f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.weight(0.65f)
                    )
                }

                OutlinedTextField(
                    value = businessLocation,
                    onValueChange = { businessLocation = it },
                    label = { Text("City / State Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = { fullAddress = it },
                    label = { Text("Full Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Workplace Operating Setup",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepNavy,
                    fontFamily = InterFontFamily,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !operatingFromOffice,
                        onClick = { operatingFromOffice = false },
                        label = { Text("Home / Remote") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryOrange
                        )
                    )
                    FilterChip(
                        selected = operatingFromOffice,
                        onClick = { operatingFromOffice = true },
                        label = { Text("Commercial Office") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryOrange.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryOrange
                        )
                    )
                }

                if (operatingFromOffice) {
                    OutlinedTextField(
                        value = officeAddress,
                        onValueChange = { officeAddress = it },
                        label = { Text("Commercial Office Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = profile.copy(
                        logoUrl = logoUrl.trim(),
                        companyName = companyName.trim(),
                        contactPersonName = contactPersonName.trim(),
                        phone = phone.trim(),
                        countryCode = countryCode.trim(),
                        businessLocation = businessLocation.trim(),
                        fullAddress = fullAddress.trim(),
                        operatingFromHome = !operatingFromOffice,
                        operatingFromOffice = operatingFromOffice,
                        officeAddress = if (operatingFromOffice) officeAddress.trim() else ""
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SlateGray)
            }
        }
    )
}

@Composable
private fun PolicyRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DeepNavy,
                fontFamily = InterFontFamily
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyPolicyViewerScreen(
    policyTab: AgencyPolicyTab,
    onBack: () -> Unit
) {
    val title = when (policyTab) {
        AgencyPolicyTab.VENDOR_CODE_OF_CONDUCT -> "Vendor Code of Conduct & Dealing Policy"
        AgencyPolicyTab.PRIVACY_POLICY -> "Privacy Policy"
        AgencyPolicyTab.CONDITIONS_OF_USE -> "Conditions of Use & Sale"
        AgencyPolicyTab.INTERNET_BASED_POLICY -> "Internet-Based Policy"
    }

    val text = when (policyTab) {
        AgencyPolicyTab.VENDOR_CODE_OF_CONDUCT -> AgencyPolicyData.VENDOR_CODE_OF_CONDUCT
        AgencyPolicyTab.PRIVACY_POLICY -> ""
        AgencyPolicyTab.CONDITIONS_OF_USE -> ""
        AgencyPolicyTab.INTERNET_BASED_POLICY -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = PoppinsFontFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = DeepNavy
                )
            )
        },
        containerColor = LightGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AgencyPolicyContent(text = text)
        }
    }
}

@Composable
fun AgencyPolicyContent(text: String) {
    val lines = remember(text) { text.split("\n") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        items(lines.size) { index ->
            val line = lines[index].trim()
            if (line.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            } else if (
                line.firstOrNull()?.isDigit() == true && 
                (line.contains(". ") || line.contains(" "))
            ) {
                Text(
                    text = line,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
            } else if (line.startsWith("•")) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = SlateGray,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = line.substring(1).trim(),
                        fontSize = 14.sp,
                        color = SlateGray,
                        fontFamily = InterFontFamily,
                        lineHeight = 20.sp
                    )
                }
            } else if (line.startsWith("Effective Date:") || line.startsWith("Last Updated:")) {
                Text(
                    text = line,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    fontFamily = InterFontFamily,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else if (line.contains("Code of Conduct", ignoreCase = true) ||
                       line.equals("Privacy Policy", ignoreCase = true) ||
                       line.equals("Conditions of Use & Sale", ignoreCase = true)) {
                Text(
                    text = line,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            } else {
                Text(
                    text = line,
                    fontSize = 14.sp,
                    color = SlateGray,
                    fontFamily = InterFontFamily,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
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

