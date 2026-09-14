package com.tripdm.agency.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.data.model.ItineraryDay
import com.tripdm.agency.data.model.PlaceCovered
import com.tripdm.agency.ui.theme.*

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
    var title by remember { mutableStateOf(initialListing?.title ?: "") }
    var packageType by remember { mutableStateOf(initialListing?.packageType ?: "domestic") }
    var countryName by remember { mutableStateOf(initialListing?.countryName ?: "India") }
    var stateName by remember { mutableStateOf(initialListing?.stateName ?: "") }
    var pickUpLocation by remember { mutableStateOf(initialListing?.pickUpLocation ?: "") }
    var dropLocation by remember { mutableStateOf(initialListing?.dropLocation ?: "") }
    var costStr by remember { mutableStateOf(if ((initialListing?.cost ?: 0.0) > 0) initialListing?.cost?.toInt().toString() else "") }
    var durationStr by remember { mutableStateOf(if ((initialListing?.duration ?: 0) > 0) initialListing?.duration.toString() else "3") }
    var hotelType by remember { mutableStateOf(initialListing?.hotelType ?: "deluxe") }
    var mealPlan by remember { mutableStateOf(initialListing?.mealPlan ?: "breakfast") }
    var selectedCategory by remember { mutableStateOf(initialListing?.tourCategories?.firstOrNull() ?: "Family") }
    var photoUrl by remember { mutableStateOf(initialListing?.photos?.firstOrNull() ?: "") }

    var placesCoveredList by remember {
        mutableStateOf(
            if (!initialListing?.placesCovered.isNullOrEmpty()) {
                initialListing!!.placesCovered.map { it.name }
            } else {
                listOf("")
            }
        )
    }

    var itineraryDays by remember {
        mutableStateOf(
            if (!initialListing?.itinerary.isNullOrEmpty()) {
                initialListing!!.itinerary
            } else {
                listOf(
                    ItineraryDay(1, "Arrival & Sightseeing", "Welcome to destination, hotel check-in and evening local sightseeing."),
                    ItineraryDay(2, "Full Day Tour", "Exploration of iconic landmarks and cultural attractions."),
                    ItineraryDay(3, "Departure", "Hotel check-out and drop off for return journey.")
                )
            }
        )
    }

    var inclusionsText by remember {
        mutableStateOf(
            initialListing?.inclusions?.joinToString("\n")
                ?: "Hotel Accommodation\nDaily Breakfast\nAC Transport for Sightseeing\nDriver Allowance & Tolls"
        )
    }

    var exclusionsText by remember {
        mutableStateOf(
            initialListing?.exclusions?.joinToString("\n")
                ?: "Airfare / Train tickets\nPersonal expenses & tips\nMonument entry fees\nTravel Insurance"
        )
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightGray)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (validationError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(text = validationError!!, color = M3Error, fontSize = 12.sp, fontFamily = InterFontFamily)
                }
            }

            // 1. Basic Package Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Basic Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepNavy, fontFamily = PoppinsFontFamily)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Package Title *") },
                        placeholder = { Text("e.g. 5 Days Exotic Kashmir Paradise Tour") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Package Type (Domestic vs International)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = packageType == "domestic",
                            onClick = {
                                packageType = "domestic"
                                countryName = "India"
                            },
                            label = { Text("Domestic (India)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = packageType == "international",
                            onClick = { packageType = "international" },
                            label = { Text("International") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = countryName,
                            onValueChange = { countryName = it },
                            label = { Text("Country *") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stateName,
                            onValueChange = { stateName = it },
                            label = { Text("State / Region") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = pickUpLocation,
                            onValueChange = { pickUpLocation = it },
                            label = { Text("Pick-up Location *") },
                            placeholder = { Text("e.g. Srinagar Airport") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dropLocation,
                            onValueChange = { dropLocation = it },
                            label = { Text("Drop Location *") },
                            placeholder = { Text("e.g. Srinagar Airport") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = photoUrl,
                        onValueChange = { photoUrl = it },
                        label = { Text("Cover Image URL (Web or Unsplash)") },
                        placeholder = { Text("https://images.unsplash.com/...") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 2. Pricing & Accommodations
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Pricing & Category", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepNavy, fontFamily = PoppinsFontFamily)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = costStr,
                            onValueChange = { costStr = it },
                            label = { Text("Total Cost (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text("Duration (Days) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Tour Category", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary, fontFamily = InterFontFamily)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Family", "Honeymoon", "Friends", "Religious").forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Hotel Tier", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary, fontFamily = InterFontFamily)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("budget" to "Budget", "deluxe" to "Deluxe (3-Star)", "premium" to "Premium (4/5-Star)").forEach { (value, label) ->
                            FilterChip(
                                selected = hotelType == value,
                                onClick = { hotelType = value },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Meal Plan", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary, fontFamily = InterFontFamily)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("no-meal" to "No Meal", "breakfast" to "Breakfast", "all-meals" to "All Meals").forEach { (value, label) ->
                            FilterChip(
                                selected = mealPlan == value,
                                onClick = { mealPlan = value },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // 3. Places Covered
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Places Covered", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepNavy, fontFamily = PoppinsFontFamily)
                        TextButton(onClick = { placesCoveredList = placesCoveredList + "" }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Place")
                        }
                    }

                    placesCoveredList.forEachIndexed { index, place ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = place,
                                onValueChange = { newText ->
                                    val updated = placesCoveredList.toMutableList()
                                    updated[index] = newText
                                    placesCoveredList = updated
                                },
                                placeholder = { Text("e.g. Gulmarg, Pahalgam, Dal Lake") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            if (placesCoveredList.size > 1) {
                                IconButton(onClick = {
                                    val updated = placesCoveredList.toMutableList()
                                    updated.removeAt(index)
                                    placesCoveredList = updated
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = M3Error)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Day-wise Itinerary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Itinerary (${itineraryDays.size} Days)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepNavy, fontFamily = PoppinsFontFamily)
                        TextButton(onClick = {
                            val newDayNum = itineraryDays.size + 1
                            itineraryDays = itineraryDays + ItineraryDay(newDayNum, "Day $newDayNum Sightseeing", "Description of day activities.")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Day")
                        }
                    }

                    itineraryDays.forEachIndexed { index, day ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = LightGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Day ${day.day}", fontWeight = FontWeight.Bold, color = PrimaryOrange, fontSize = 13.sp)
                                    if (itineraryDays.size > 1) {
                                        IconButton(
                                            onClick = {
                                                val updated = itineraryDays.toMutableList()
                                                updated.removeAt(index)
                                                itineraryDays = updated.mapIndexed { idx, itm -> itm.copy(day = idx + 1) }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Day", tint = M3Error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = day.placeName,
                                    onValueChange = { newPlace ->
                                        val updated = itineraryDays.toMutableList()
                                        updated[index] = updated[index].copy(placeName = newPlace)
                                        itineraryDays = updated
                                    },
                                    label = { Text("Day Title / Place") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = day.description,
                                    onValueChange = { newDesc ->
                                        val updated = itineraryDays.toMutableList()
                                        updated[index] = updated[index].copy(description = newDesc)
                                        itineraryDays = updated
                                    },
                                    label = { Text("Day Description") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )
                            }
                        }
                    }
                }
            }

            // 5. Inclusions & Exclusions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Inclusions & Exclusions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepNavy, fontFamily = PoppinsFontFamily)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inclusionsText,
                        onValueChange = { inclusionsText = it },
                        label = { Text("Inclusions (one per line)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = exclusionsText,
                        onValueChange = { exclusionsText = it },
                        label = { Text("Exclusions (one per line)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            // Submit Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        validationError = "Please enter a package title."
                        return@Button
                    }
                    val cost = costStr.toDoubleOrNull() ?: 0.0
                    if (cost <= 0) {
                        validationError = "Please enter a valid package cost."
                        return@Button
                    }

                    validationError = null
                    val cleanPlaces = placesCoveredList
                        .filter { it.isNotBlank() }
                        .mapIndexed { idx, name -> PlaceCovered(id = "place_$idx", name = name.trim()) }

                    val inclusionsList = inclusionsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val exclusionsList = exclusionsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

                    val listing = (initialListing ?: AgencyListing()).copy(
                        agencyId = agencyId,
                        agencyName = agencyName,
                        title = title.trim(),
                        packageType = packageType,
                        countryName = countryName.trim(),
                        stateName = stateName.trim(),
                        pickUpLocation = pickUpLocation.trim(),
                        dropLocation = dropLocation.trim(),
                        cost = cost,
                        price = cost,
                        duration = durationStr.toIntOrNull() ?: itineraryDays.size,
                        hotelType = hotelType,
                        mealPlan = mealPlan,
                        tourCategories = listOf(selectedCategory),
                        placesCovered = cleanPlaces,
                        itinerary = itineraryDays,
                        inclusions = inclusionsList,
                        exclusions = exclusionsList,
                        photos = if (photoUrl.isNotBlank()) listOf(photoUrl.trim()) else emptyList()
                    )
                    onSave(listing)
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = if (initialListing == null) "Submit Package for Review" else "Update Package",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
