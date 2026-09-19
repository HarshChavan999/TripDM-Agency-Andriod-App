@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tripdm.agency.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Visibility
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.data.model.ItineraryDay
import com.tripdm.agency.ui.components.AgencyPackagePreviewModal
import com.tripdm.agency.ui.theme.*

// ─────────────────────────────────────────────────
// Static data (mirrors webapp constants)
// ─────────────────────────────────────────────────

private val TOUR_CATEGORIES = listOf("Family", "Honeymoon", "Friends", "Religious", "Fix Departure")

private val HOTEL_TYPES = listOf(
    "budget" to "Budget",
    "deluxe" to "Deluxe",
    "premium" to "Premium"
)

private val MEAL_PLANS = listOf(
    "no-meal" to "No Meal",
    "breakfast" to "Breakfast",
    "lunch" to "Lunch",
    "dinner" to "Dinner",
    "breakfast-lunch" to "Breakfast + Lunch",
    "breakfast-dinner" to "Breakfast + Dinner",
    "lunch-dinner" to "Lunch + Dinner",
    "all-meals" to "All Meals"
)

private val EXPERIENCE_PRESETS = listOf(
    "Trekking", "Snow", "Adventure", "Water Sports", "Wildlife", "Cultural", "Sightseeing"
)

private val SEASON_OPTIONS = listOf(
    "" to "Select season",
    "summer" to "Summer Retreat (May – Jul)",
    "monsoon" to "Monsoon Magic (Aug – Oct)",
    "winter" to "Winter Wonderland (Nov – Jan)",
    "spring" to "Spring Getaway (Feb – Apr)",
    "all-seasons" to "All Seasons"
)

private val EVENT_OPTIONS = listOf(
    "" to "Select festival/event",
    "new-year" to "New Year & Christmas Specials",
    "diwali" to "Diwali Specials",
    "summer-vacation" to "Summer Vacations",
    "weekend" to "Long Weekend Escapes"
)

private val INDIAN_STATES = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa",
    "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
    "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
    "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
    "Uttar Pradesh", "Uttarakhand", "West Bengal",
    "Andaman and Nicobar Islands", "Chandigarh",
    "Dadra and Nagar Haveli and Daman and Diu", "Delhi",
    "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
)

private val COUNTRIES = listOf(
    "Afghanistan", "Albania", "Algeria", "Argentina", "Armenia", "Australia", "Austria",
    "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Belgium", "Bhutan", "Bolivia",
    "Brazil", "Cambodia", "Canada", "Chile", "China", "Colombia", "Croatia", "Cuba",
    "Cyprus", "Denmark", "Egypt", "Ethiopia", "Fiji", "Finland", "France", "Germany",
    "Ghana", "Greece", "Guatemala", "Hungary", "Iceland", "India", "Indonesia", "Iran",
    "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan",
    "Kenya", "Kuwait", "Laos", "Latvia", "Lebanon", "Libya", "Luxembourg", "Malaysia",
    "Maldives", "Malta", "Mexico", "Moldova", "Monaco", "Mongolia", "Morocco",
    "Mozambique", "Myanmar", "Nepal", "Netherlands", "New Zealand", "Nigeria",
    "North Korea", "Norway", "Oman", "Pakistan", "Palestine", "Panama", "Paraguay",
    "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia",
    "Rwanda", "Saudi Arabia", "Senegal", "Serbia", "Singapore", "Slovakia", "Slovenia",
    "Somalia", "South Africa", "South Korea", "Spain", "Sri Lanka", "Sweden",
    "Switzerland", "Syria", "Tanzania", "Thailand", "Tunisia", "Turkey", "Uganda",
    "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay",
    "Uzbekistan", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"
)

// ─────────────────────────────────────────────────
// Composable Helpers & Custom Modifier
// ─────────────────────────────────────────────────

/** Custom Modifier extension for drawing dashed borders */
private fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    cornerRadius: Dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 6.dp
) = this.drawWithContent {
    drawContent()
    val strokeWidthPx = strokeWidth.toPx()
    val dashLengthPx = dashLength.toPx()
    val gapLengthPx = gapLength.toPx()
    val cornerRadiusPx = cornerRadius.toPx()

    val pathEffect = PathEffect.dashPathEffect(
        floatArrayOf(dashLengthPx, gapLengthPx),
        0f
    )

    val stroke = Stroke(
        width = strokeWidthPx,
        pathEffect = pathEffect
    )

    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}

/** Numbered section header with orange badge matching the webapp */
@Composable
private fun SectionHeader(number: Int, title: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706),
                    fontFamily = PoppinsFontFamily
                )
            }
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                fontFamily = PoppinsFontFamily
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
    }
}

/** Numbered section header with right-aligned action button */
@Composable
private fun SectionHeaderWithBadgeAndAction(
    number: Int,
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706),
                        fontFamily = PoppinsFontFamily
                    )
                }
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )
            }
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(actionText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = InterFontFamily)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
    }
}

/** Plain numbered title with right-aligned action button (Inclusions/Exclusions) */
@Composable
private fun SectionHeaderWithAction(
    numberTitle: String,
    onAddClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = numberTitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                fontFamily = PoppinsFontFamily
            )
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = InterFontFamily)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
    }
}

/** Full-width boxed option item with left checkbox */
@Composable
private fun FullWidthCheckboxOption(
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryOrange,
                    uncheckedColor = Color(0xFF9CA3AF),
                    checkmarkColor = Color.White
                )
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DeepNavy,
                fontFamily = InterFontFamily
            )
        }
    }
}

/** Sub-section label */
@Composable
private fun SubSectionLabel(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(PrimaryOrange, RoundedCornerShape(2.dp))
        )
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = DeepNavy,
            fontFamily = InterFontFamily
        )
    }
}

/** Tag chip for selected items */
@Composable
private fun TagChip(label: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFF7ED),
        border = BorderStroke(1.dp, Color(0xFFFFD8A8)),
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7C2D12)
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove $label",
                tint = Color(0xFFEA580C),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

/** Searchable multi-tag input with dropdown menu */
@Composable
private fun SearchableMultiTag(
    selected: List<String>,
    allOptions: List<String>,
    placeholder: String,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filtered = allOptions.filter {
        it.contains(query, ignoreCase = true) && !selected.contains(it)
    }

    Column {
        if (selected.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                selected.forEach { tag ->
                    TagChip(label = tag, onRemove = { onRemove(tag) })
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded && (filtered.isNotEmpty() || query.isNotBlank()),
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    expanded = true
                },
                placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF6B7280)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryOrange,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            ExposedDropdownMenu(
                expanded = expanded && (filtered.isNotEmpty() || query.isNotBlank()),
                onDismissRequest = { expanded = false }
            ) {
                filtered.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 13.sp) },
                        onClick = {
                            onAdd(option)
                            query = ""
                            expanded = false
                        }
                    )
                }
                if (filtered.isEmpty() && query.isNotBlank()) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                                Text("Add \"$query\"", fontSize = 13.sp, color = PrimaryOrange, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        onClick = {
                            if (query.isNotBlank()) {
                                onAdd(query.trim())
                                query = ""
                                expanded = false
                            }
                        }
                    )
                }
            }
        }
    }
}

/** Labeled dropdown */
@Composable
private fun LabeledDropdown(
    options: List<Pair<String, String>>,
    selectedValue: String,
    placeholder: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedValue }?.second

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF6B7280)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryOrange,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (value, label) ->
                if (label.isNotBlank() && value.isNotBlank()) {
                    DropdownMenuItem(
                        text = { Text(label, fontSize = 13.sp) },
                        onClick = {
                            onSelect(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditListingScreen(
    agencyId: String,
    agencyName: String,
    initialListing: AgencyListing? = null,
    isSubmitting: Boolean,
    onSave: (AgencyListing) -> Unit,
    onBack: () -> Unit
) {
    // ── State ──────────────────────────────────────

    var title by remember { mutableStateOf(initialListing?.title ?: "") }

    var packageType by remember { mutableStateOf(initialListing?.packageType ?: "domestic") }
    var stateNames by remember {
        mutableStateOf(
            when {
                initialListing?.stateNames?.isNotEmpty() == true -> initialListing.stateNames
                initialListing?.stateName?.isNotBlank() == true ->
                    initialListing.stateName.split(",").map { it.trim() }.filter { it.isNotBlank() }
                else -> emptyList()
            }
        )
    }
    var countryNames by remember {
        mutableStateOf(
            when {
                initialListing?.countryNames?.isNotEmpty() == true -> initialListing.countryNames
                initialListing?.countryName?.isNotBlank() == true ->
                    initialListing.countryName.split(",").map { it.trim() }.filter { it.isNotBlank() }
                else -> emptyList()
            }
        )
    }

    var pickUpLocation by remember { mutableStateOf(initialListing?.pickUpLocation ?: "") }
    var dropLocation by remember { mutableStateOf(initialListing?.dropLocation ?: "") }

    var tourCategories by remember {
        mutableStateOf(initialListing?.tourCategories ?: emptyList())
    }

    var hotelTypes by remember {
        mutableStateOf(
            when {
                initialListing?.hotelTypes?.isNotEmpty() == true -> initialListing.hotelTypes
                initialListing?.hotelType?.isNotBlank() == true -> listOf(initialListing.hotelType)
                else -> emptyList()
            }
        )
    }

    var mealPlans by remember {
        mutableStateOf(
            when {
                initialListing?.mealPlans?.isNotEmpty() == true -> initialListing.mealPlans
                initialListing?.mealPlan?.isNotBlank() == true -> listOf(initialListing.mealPlan)
                else -> emptyList()
            }
        )
    }

    var itineraryDays by remember {
        mutableStateOf(
            if (!initialListing?.itinerary.isNullOrEmpty()) {
                initialListing!!.itinerary
            } else {
                emptyList()
            }
        )
    }

    var costStr by remember {
        mutableStateOf(if ((initialListing?.cost ?: 0.0) > 0) initialListing?.cost?.toInt().toString() else "")
    }

    var experienceTypes by remember {
        mutableStateOf(initialListing?.experienceType ?: emptyList())
    }

    var season by remember { mutableStateOf(initialListing?.season ?: "") }

    var eventType by remember { mutableStateOf(initialListing?.eventType ?: "") }

    var inclusions by remember {
        mutableStateOf(
            if (!initialListing?.inclusions.isNullOrEmpty()) initialListing!!.inclusions.toMutableList()
            else mutableListOf("")
        )
    }
    var exclusions by remember {
        mutableStateOf(
            if (!initialListing?.exclusions.isNullOrEmpty()) initialListing!!.exclusions.toMutableList()
            else mutableListOf("")
        )
    }

    var validationError by remember { mutableStateOf<String?>(null) }
    var showPreviewModal by remember { mutableStateOf(false) }
    var draftPreviewListing by remember { mutableStateOf<AgencyListing?>(null) }

    val buildCurrentDraft: () -> AgencyListing = {
        val cost = costStr.toDoubleOrNull() ?: 0.0
        val cleanInclusions = inclusions.map { it.trim() }.filter { it.isNotEmpty() }
        val cleanExclusions = exclusions.map { it.trim() }.filter { it.isNotEmpty() }
        val cleanItinerary = itineraryDays.filter { it.placeName.isNotBlank() || it.description.isNotBlank() }
        val primaryState = stateNames.firstOrNull() ?: initialListing?.stateName ?: ""
        val primaryCountry = countryNames.firstOrNull() ?: initialListing?.countryName ?: "India"

        (initialListing ?: AgencyListing()).copy(
            agencyId = agencyId,
            agencyName = agencyName,
            title = title.trim(),
            packageType = packageType,
            countryName = primaryCountry,
            stateName = primaryState,
            countryNames = countryNames,
            stateNames = stateNames,
            pickUpLocation = pickUpLocation.trim(),
            dropLocation = dropLocation.trim(),
            tourCategories = tourCategories,
            hotelTypes = hotelTypes,
            hotelType = hotelTypes.firstOrNull() ?: "deluxe",
            mealPlans = mealPlans,
            mealPlan = mealPlans.firstOrNull() ?: "breakfast",
            itinerary = cleanItinerary,
            inclusions = cleanInclusions,
            exclusions = cleanExclusions,
            cost = cost,
            price = cost,
            duration = itineraryDays.size,
            season = season,
            eventType = eventType,
            experienceType = experienceTypes,
            photos = initialListing?.photos ?: emptyList()
        )
    }

    if (showPreviewModal && draftPreviewListing != null) {
        AgencyPackagePreviewModal(
            listing = draftPreviewListing!!,
            isSubmitting = isSubmitting,
            onDismiss = { showPreviewModal = false },
            onSubmitForApproval = { listingToSave ->
                if (title.isBlank()) {
                    validationError = "Please enter a package title."
                    showPreviewModal = false
                    return@AgencyPackagePreviewModal
                }
                val cost = costStr.toDoubleOrNull() ?: 0.0
                if (cost <= 0) {
                    validationError = "Please enter a valid starting price."
                    showPreviewModal = false
                    return@AgencyPackagePreviewModal
                }
                validationError = null
                onSave(listingToSave)
            }
        )
    }

    val totalDays = itineraryDays.size
    val totalNights = if (totalDays > 0) totalDays - 1 else 0

    // ── UI ─────────────────────────────────────────

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialListing == null) "Create Travel Package" else "Edit Package",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PoppinsFontFamily,
                        color = DeepNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DeepNavy)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            draftPreviewListing = buildCurrentDraft()
                            showPreviewModal = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Preview Package",
                            tint = PrimaryOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Preview",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryOrange,
                            fontFamily = InterFontFamily
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Validation Banner ──────────────────
            AnimatedVisibility(
                visible = validationError != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = validationError ?: "",
                        color = M3Error,
                        fontSize = 13.sp,
                        fontFamily = InterFontFamily,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // ══════════════════════════════════════
            // SECTION 1 — Package Title
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(number = 1, title = "Package Title")
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g., 5 Days / 4 Nights Honeymoon Package in Exotic Kerala", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                    )
                )
            }

            // ══════════════════════════════════════
            // SECTION 2 — Package Type
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(number = 2, title = "Package Type")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("international", "domestic").forEach { type ->
                        val isSelected = packageType == type
                        val label = if (type == "international") "International" else "Domestic"
                        Button(
                            onClick = { packageType = type },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PrimaryOrange else Color.White,
                                contentColor = if (isSelected) Color.White else DeepNavy
                            ),
                            border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = InterFontFamily)
                        }
                    }
                }

                AnimatedVisibility(visible = packageType == "international") {
                    Column {
                        Text("Country Name(s)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary, fontFamily = InterFontFamily)
                        Spacer(modifier = Modifier.height(6.dp))
                        SearchableMultiTag(
                            selected = countryNames,
                            allOptions = COUNTRIES,
                            placeholder = "Type to search and add countries…",
                            onAdd = { if (!countryNames.contains(it)) countryNames = countryNames + it },
                            onRemove = { countryNames = countryNames - it }
                        )
                    }
                }

                AnimatedVisibility(visible = packageType == "domestic") {
                    Column {
                        Text("State Name(s)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary, fontFamily = InterFontFamily)
                        Spacer(modifier = Modifier.height(6.dp))
                        SearchableMultiTag(
                            selected = stateNames,
                            allOptions = INDIAN_STATES,
                            placeholder = "Type to search and add states…",
                            onAdd = { if (!stateNames.contains(it)) stateNames = stateNames + it },
                            onRemove = { stateNames = stateNames - it }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SubSectionLabel("Pick-up Location")
                        OutlinedTextField(
                            value = pickUpLocation,
                            onValueChange = { pickUpLocation = it },
                            placeholder = { Text("e.g., Delhi Airport", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        SubSectionLabel("Drop Location")
                        OutlinedTextField(
                            value = dropLocation,
                            onValueChange = { dropLocation = it },
                            placeholder = { Text("e.g., Delhi Airport", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }
            }

            // ══════════════════════════════════════
            // SECTION 3 — Tour Category
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(number = 3, title = "Tour Category")
                TOUR_CATEGORIES.forEach { category ->
                    FullWidthCheckboxOption(
                        label = category,
                        isSelected = tourCategories.contains(category),
                        onToggle = {
                            tourCategories = if (tourCategories.contains(category))
                                tourCategories - category
                            else
                                tourCategories + category
                        }
                    )
                }
            }

            // ══════════════════════════════════════
            // SECTION 4 — Hotel Type
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(number = 4, title = "Hotel Type")
                HOTEL_TYPES.forEach { (value, label) ->
                    FullWidthCheckboxOption(
                        label = label,
                        isSelected = hotelTypes.contains(value),
                        onToggle = {
                            hotelTypes = if (hotelTypes.contains(value))
                                hotelTypes - value
                            else
                                hotelTypes + value
                        }
                    )
                }
            }

            // ══════════════════════════════════════
            // SECTION 5 — Meal Plan
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(number = 5, title = "Meal Plan")
                MEAL_PLANS.forEach { (value, label) ->
                    FullWidthCheckboxOption(
                        label = label,
                        isSelected = mealPlans.contains(value),
                        onToggle = {
                            mealPlans = if (mealPlans.contains(value)) {
                                mealPlans - value
                            } else {
                                when (value) {
                                    "no-meal" -> listOf("no-meal")
                                    "all-meals" -> listOf("all-meals")
                                    else -> (mealPlans.filter { it != "no-meal" && it != "all-meals" }) + value
                                }
                            }
                        }
                    )
                }
            }

            // ══════════════════════════════════════
            // SECTION 6 — Itinerary Builder
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeaderWithBadgeAndAction(
                    number = 6,
                    title = "Itinerary Builder",
                    actionText = "+ Add Day",
                    onActionClick = {
                        val nextDay = itineraryDays.size + 1
                        itineraryDays = itineraryDays + ItineraryDay(nextDay, "", "")
                    }
                )

                if (itineraryDays.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .dashedBorder(
                                strokeWidth = 1.dp,
                                color = Color(0xFFD1D5DB),
                                cornerRadius = 10.dp
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No itinerary days added yet. Click \"Add Day\" to start.",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            fontFamily = InterFontFamily
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        itineraryDays.forEachIndexed { index, day ->
                            ItineraryDayItem(
                                day = day,
                                onUpdate = { updatedDay ->
                                    val updated = itineraryDays.toMutableList()
                                    updated[index] = updatedDay
                                    itineraryDays = updated
                                },
                                onDelete = {
                                    val updated = itineraryDays.toMutableList()
                                    updated.removeAt(index)
                                    itineraryDays = updated.mapIndexed { idx, itm ->
                                        itm.copy(day = idx + 1)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════
            // SECTION 7 — Package Duration
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(number = 7, title = "Package Duration")
                Text(
                    text = "Duration Summary",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DeepNavy,
                    fontFamily = InterFontFamily
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CALCULATED FROM ITINERARY:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            fontFamily = InterFontFamily
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Days:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepNavy,
                                fontFamily = InterFontFamily
                            )
                            Text(
                                text = "$totalDays Days",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA580C),
                                fontFamily = InterFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Nights:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepNavy,
                                fontFamily = InterFontFamily
                            )
                            Text(
                                text = "$totalNights Nights",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA580C),
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════
            // SECTION 8 — Starting Price
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(number = 8, title = "Starting Price")
                Text(
                    text = "Starting Price (per person)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DeepNavy,
                    fontFamily = InterFontFamily
                )
                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    placeholder = { Text("Enter starting price", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text("₹", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryOrange, modifier = Modifier.padding(start = 4.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                    )
                )
            }

            // ══════════════════════════════════════
            // SECTION 9 — Experience Type
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(number = 9, title = "Experience Type")
                SearchableMultiTag(
                    selected = experienceTypes,
                    allOptions = EXPERIENCE_PRESETS,
                    placeholder = "Type to add or search...",
                    onAdd = { if (!experienceTypes.contains(it)) experienceTypes = experienceTypes + it },
                    onRemove = { experienceTypes = experienceTypes - it }
                )
            }

            // ══════════════════════════════════════
            // SECTION 10 — Seasonal Escapes
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(number = 10, title = "Seasonal Escapes")
                LabeledDropdown(
                    options = SEASON_OPTIONS,
                    selectedValue = season,
                    placeholder = "Select season",
                    onSelect = { season = it }
                )
            }

            // ══════════════════════════════════════
            // SECTION 11 — Festive & Event Specials
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(number = 11, title = "Festive & Event Specials")
                LabeledDropdown(
                    options = EVENT_OPTIONS,
                    selectedValue = eventType,
                    placeholder = "Select festival/event",
                    onSelect = { eventType = it }
                )
            }

            // ══════════════════════════════════════
            // SECTION 12 — Inclusions
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeaderWithAction(
                    numberTitle = "12. Inclusions",
                    onAddClick = { inclusions = (inclusions + "").toMutableList() }
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    inclusions.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = item,
                                onValueChange = { newVal ->
                                    val updated = inclusions.toMutableList()
                                    updated[index] = newVal
                                    inclusions = updated
                                },
                                placeholder = { Text("e.g. 3 Star hotel stay, daily breakfast.", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryOrange,
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                                )
                            )
                            IconButton(
                                onClick = {
                                    val updated = inclusions.toMutableList()
                                    updated.removeAt(index)
                                    inclusions = if (updated.isEmpty()) mutableListOf("") else updated
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════
            // SECTION 13 — Exclusions
            // ══════════════════════════════════════
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeaderWithAction(
                    numberTitle = "13. Exclusions",
                    onAddClick = { exclusions = (exclusions + "").toMutableList() }
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exclusions.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = item,
                                onValueChange = { newVal ->
                                    val updated = exclusions.toMutableList()
                                    updated[index] = newVal
                                    exclusions = updated
                                },
                                placeholder = { Text("e.g. Laundry, personal tips, flights...", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryOrange,
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                                )
                            )
                            IconButton(
                                onClick = {
                                    val updated = exclusions.toMutableList()
                                    updated.removeAt(index)
                                    exclusions = if (updated.isEmpty()) mutableListOf("") else updated
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════
            // BOTTOM ACTION BUTTONS
            // ══════════════════════════════════════
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            validationError = "Please enter a package title."
                            return@Button
                        }
                        val cost = costStr.toDoubleOrNull() ?: 0.0
                        if (cost <= 0) {
                            validationError = "Please enter a valid starting price."
                            return@Button
                        }
                        validationError = null

                        val cleanInclusions = inclusions.map { it.trim() }.filter { it.isNotEmpty() }
                        val cleanExclusions = exclusions.map { it.trim() }.filter { it.isNotEmpty() }
                        val cleanItinerary = itineraryDays.filter { it.placeName.isNotBlank() || it.description.isNotBlank() }

                        val primaryState = stateNames.firstOrNull() ?: initialListing?.stateName ?: ""
                        val primaryCountry = countryNames.firstOrNull() ?: initialListing?.countryName ?: "India"

                        val listing = (initialListing ?: AgencyListing()).copy(
                            agencyId = agencyId,
                            agencyName = agencyName,
                            title = title.trim(),
                            packageType = packageType,
                            countryName = primaryCountry,
                            stateName = primaryState,
                            countryNames = countryNames,
                            stateNames = stateNames,
                            pickUpLocation = pickUpLocation.trim(),
                            dropLocation = dropLocation.trim(),
                            tourCategories = tourCategories,
                            hotelTypes = hotelTypes,
                            hotelType = hotelTypes.firstOrNull() ?: "deluxe",
                            mealPlans = mealPlans,
                            mealPlan = mealPlans.firstOrNull() ?: "breakfast",
                            itinerary = cleanItinerary,
                            inclusions = cleanInclusions,
                            exclusions = cleanExclusions,
                            cost = cost,
                            price = cost,
                            duration = itineraryDays.size,
                            season = season,
                            eventType = eventType,
                            experienceType = experienceTypes,
                            photos = initialListing?.photos ?: emptyList()
                        )
                        onSave(listing)
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = if (initialListing == null) "Submit for Approval" else "Update Package",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        draftPreviewListing = buildCurrentDraft()
                        showPreviewModal = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Preview Package",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily
                    )
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepNavy)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ItineraryDayItem(
    day: ItineraryDay,
    onUpdate: (ItineraryDay) -> Unit,
    onDelete: () -> Unit
) {
    var newImgUrl by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(PrimaryOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${day.day}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Day ${day.day}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DeepNavy,
                        fontFamily = PoppinsFontFamily
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Day",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = day.placeName,
                onValueChange = { onUpdate(day.copy(placeName = it)) },
                placeholder = { Text("Place name for Day ${day.day}", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryOrange,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = day.description,
                onValueChange = { onUpdate(day.copy(description = it)) },
                placeholder = { Text("Describe what happens on this day…", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryOrange,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Day Photos / Images",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569),
                fontFamily = InterFontFamily
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (day.imageUrls.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    day.imageUrls.forEachIndexed { imgIdx, _ ->
                        TagChip(
                            label = "Image ${imgIdx + 1}",
                            onRemove = {
                                val updatedImgs = day.imageUrls.toMutableList()
                                updatedImgs.removeAt(imgIdx)
                                onUpdate(day.copy(imageUrls = updatedImgs))
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newImgUrl,
                    onValueChange = { newImgUrl = it },
                    placeholder = { Text("Paste image URL for Day ${day.day}…", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Button(
                    onClick = {
                        if (newImgUrl.isNotBlank()) {
                            onUpdate(day.copy(imageUrls = day.imageUrls + newImgUrl.trim()))
                            newImgUrl = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Image", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
