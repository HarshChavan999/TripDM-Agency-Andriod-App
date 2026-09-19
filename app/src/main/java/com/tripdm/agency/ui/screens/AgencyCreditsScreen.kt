package com.tripdm.agency.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripdm.agency.data.model.CreditPlan
import com.tripdm.agency.data.model.CreditTransaction
import com.tripdm.agency.data.model.SampleCreditPlans
import com.tripdm.agency.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyCreditsScreen(
    currentCredits: Int,
    currentPlan: String = "Free",
    creditPlans: List<CreditPlan> = SampleCreditPlans,
    transactions: List<CreditTransaction>,
    isPurchasing: Boolean,
    purchaseMessage: String?,
    onPurchasePlan: (CreditPlan) -> Unit,
    onClearPurchaseMessage: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Credits & Plans",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = PrimaryOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Current Plan: ${currentPlan.ifBlank { "Free" }}",
                                color = PrimaryOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Remaining Balance",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontFamily = InterFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$currentCredits Credits",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Credits are used to publish new packages and contact leads.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }

            if (!purchaseMessage.isNullOrBlank()) {
                item {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = purchaseMessage,
                                color = M3Success,
                                fontSize = 13.sp,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (onClearPurchaseMessage != null) {
                                TextButton(onClick = onClearPurchaseMessage) {
                                    Text("DISMISS", fontSize = 11.sp, color = M3Success)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Choose a Recharge Plan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )
            }

            // Plans — Flat row design with plain divider line separation
            items(creditPlans) { plan ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = plan.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                fontFamily = PoppinsFontFamily
                            )
                            Text(
                                text = plan.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontFamily = InterFontFamily
                            )
                        }
                        if (plan.isPopular) {
                            Box(
                                modifier = Modifier
                                    .background(PrimaryOrange, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "POPULAR",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${plan.price.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "for +${plan.credits} Credits",
                            fontSize = 13.sp,
                            color = PrimaryOrange,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    plan.features.forEach { feat ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = M3Success,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feat,
                                fontSize = 12.sp,
                                color = SlateGray,
                                fontFamily = InterFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onPurchasePlan(plan) },
                        enabled = !isPurchasing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (plan.isPopular) PrimaryOrange else DeepNavy)
                    ) {
                        Text(
                            text = "Recharge ${plan.name}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0x1F000000), thickness = 0.5.dp)
                }
            }

            // Payment History / Transactions — Plain lines separation
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Payment & Credit History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontFamily = PoppinsFontFamily
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transaction records found.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            } else {
                items(transactions) { tx ->
                    val timeStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.description,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepNavy,
                                    fontFamily = InterFontFamily
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = timeStr,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily
                                )
                            }
                            Text(
                                text = "₹${tx.amount.toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange,
                                fontFamily = InterFontFamily
                            )
                        }
                        HorizontalDivider(color = Color(0x10000000), thickness = 0.5.dp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
