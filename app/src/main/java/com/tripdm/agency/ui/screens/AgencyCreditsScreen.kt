package com.tripdm.agency.ui.screens

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tripdm.agency.data.model.CreditPlan
import com.tripdm.agency.data.model.CreditTransaction
import com.tripdm.agency.data.model.SampleCreditPlans
import com.tripdm.agency.data.payment.GooglePayHelper
import com.tripdm.agency.data.payment.UpiPaymentResult
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
    onPurchasePlan: (CreditPlan) -> Unit = {},
    onPurchaseCompleted: (plan: CreditPlan, paymentMethod: String, paymentId: String, approvalRefNo: String) -> Unit = { plan, _, _, _ ->
        onPurchasePlan(plan)
    },
    onClearPurchaseMessage: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Payment state tracking
    var pendingPlan by remember { mutableStateOf<CreditPlan?>(null) }
    var pendingPaymentMethod by remember { mutableStateOf("Google Pay") }
    var pendingTxId by remember { mutableStateOf<String?>(null) }

    var selectedPlanForFallback by remember { mutableStateOf<CreditPlan?>(null) }
    var showGPayNotInstalledDialog by remember { mutableStateOf(false) }
    var showNoUpiAppsDialog by remember { mutableStateOf(false) }
    var successDialogTx by remember { mutableStateOf<CreditTransaction?>(null) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    // Launcher for handling the UPI activity result returned by Google Pay or other UPI apps
    val upiPaymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val activePlan = pendingPlan
        val activeTxId = pendingTxId
        if (activePlan != null) {
            val paymentResult = GooglePayHelper.parseUpiResponse(
                resultCode = result.resultCode,
                data = result.data,
                plan = activePlan,
                paymentMethod = pendingPaymentMethod
            )

            when (paymentResult) {
                is UpiPaymentResult.Success -> {
                    val finalTx = CreditTransaction(
                        id = paymentResult.paymentId,
                        amount = activePlan.price,
                        credits = activePlan.credits,
                        description = "Purchased ${activePlan.name} (+${activePlan.credits} credits via ${paymentResult.paymentMethod})",
                        timestamp = System.currentTimeMillis(),
                        status = "completed",
                        paymentMethod = paymentResult.paymentMethod,
                        paymentId = paymentResult.paymentId,
                        approvalRefNo = paymentResult.approvalRefNo ?: ""
                    )
                    successDialogTx = finalTx
                    onPurchaseCompleted(
                        activePlan,
                        paymentResult.paymentMethod,
                        paymentResult.paymentId,
                        paymentResult.approvalRefNo ?: ""
                    )
                }
                is UpiPaymentResult.Cancelled -> {
                    Toast.makeText(
                        context,
                        "Payment cancelled. No amount was debited.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is UpiPaymentResult.Failed -> {
                    errorDialogMessage = paymentResult.message
                }
            }

            pendingPlan = null
            pendingTxId = null
        }
    }

    // Direct Google Pay launcher
    fun launchGooglePay(plan: CreditPlan) {
        val txId = GooglePayHelper.generateTransactionId(plan)
        if (GooglePayHelper.isGooglePayInstalled(context)) {
            pendingPlan = plan
            pendingPaymentMethod = "Google Pay"
            pendingTxId = txId
            try {
                val gpayIntent = GooglePayHelper.createGooglePayIntent(plan, txId)
                upiPaymentLauncher.launch(gpayIntent)
            } catch (_: ActivityNotFoundException) {
                selectedPlanForFallback = plan
                showGPayNotInstalledDialog = true
            } catch (e: Exception) {
                errorDialogMessage = "Could not open Google Pay: ${e.message}"
            }
        } else {
            selectedPlanForFallback = plan
            showGPayNotInstalledDialog = true
        }
    }

    // Fallback generic UPI chooser launcher
    fun launchFallbackUpiChooser(plan: CreditPlan) {
        val txId = GooglePayHelper.generateTransactionId(plan)
        if (GooglePayHelper.isAnyUpiAppInstalled(context, plan, txId)) {
            pendingPlan = plan
            pendingPaymentMethod = "UPI App"
            pendingTxId = txId
            try {
                val chooserIntent = GooglePayHelper.createChooserUpiIntent(
                    plan = plan,
                    transactionId = txId,
                    title = "Pay ₹${plan.price.toInt()} with UPI"
                )
                upiPaymentLauncher.launch(chooserIntent)
            } catch (e: Exception) {
                errorDialogMessage = "Could not open UPI chooser: ${e.message}"
            }
        } else {
            selectedPlanForFallback = plan
            showNoUpiAppsDialog = true
        }
    }

    // Developer / Demo simulation for emulators or testing
    fun simulatePayment(plan: CreditPlan) {
        val simTxId = "SIM-GPAY-${System.currentTimeMillis()}"
        val tx = CreditTransaction(
            id = simTxId,
            amount = plan.price,
            credits = plan.credits,
            description = "Purchased ${plan.name} (+${plan.credits} credits via Google Pay Simulation)",
            timestamp = System.currentTimeMillis(),
            status = "completed",
            paymentMethod = "Google Pay (Demo)",
            paymentId = simTxId,
            approvalRefNo = "APPR_SIM_${System.currentTimeMillis().toString().takeLast(6)}"
        )
        successDialogTx = tx
        onPurchaseCompleted(plan, "Google Pay (Demo)", simTxId, tx.approvalRefNo)
    }

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

            // Subscription & Recharge Plans
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary GPay Direct Payment Button
                    Button(
                        onClick = { launchGooglePay(plan) },
                        enabled = !isPurchasing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepNavy,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // GPay brand badge
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "G",
                                        color = Color(0xFF4285F4),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Pay",
                                        color = Color(0xFF5F6368),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Text(
                                text = "Pay ₹${plan.price.toInt()} with GPay",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = InterFontFamily
                            )
                        }
                    }

                    // Fallback to Other UPI Apps button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { launchFallbackUpiChooser(plan) },
                            enabled = !isPurchasing,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Or pay via PhonePe, Paytm, other UPI apps",
                                fontSize = 12.sp,
                                color = SlateGray,
                                fontWeight = FontWeight.Medium,
                                fontFamily = InterFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0x1F000000), thickness = 0.5.dp)
                }
            }

            // Payment History / Transactions
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = tx.description,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DeepNavy,
                                        fontFamily = InterFontFamily,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (tx.paymentMethod.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (tx.paymentMethod.contains("Google", ignoreCase = true)) Color(0xFFE8F0FE) else PrimaryOrange.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (tx.paymentMethod.contains("Google", ignoreCase = true)) "GPay" else tx.paymentMethod,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (tx.paymentMethod.contains("Google", ignoreCase = true)) Color(0xFF1A73E8) else PrimaryOrange,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = timeStr,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
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

    // Google Pay Not Installed Dialog (offers Fallback UPI apps or Play Store)
    if (showGPayNotInstalledDialog && selectedPlanForFallback != null) {
        val targetPlan = selectedPlanForFallback!!
        AlertDialog(
            onDismissRequest = {
                showGPayNotInstalledDialog = false
                selectedPlanForFallback = null
            },
            title = {
                Text(
                    text = "Google Pay Not Found",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = "Google Pay is not installed on this device to complete the ₹${targetPlan.price.toInt()} subscription payment.",
                        fontSize = 14.sp,
                        fontFamily = InterFontFamily,
                        color = SlateGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "You can pay using any other installed UPI app (PhonePe, Paytm, BHIM), download Google Pay from Play Store, or simulate the payment in demo mode.",
                        fontSize = 12.sp,
                        fontFamily = InterFontFamily,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val plan = targetPlan
                        showGPayNotInstalledDialog = false
                        selectedPlanForFallback = null
                        launchFallbackUpiChooser(plan)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Pay with other UPI App", fontFamily = InterFontFamily)
                }
            },
            dismissButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(
                        onClick = {
                            try {
                                context.startActivity(GooglePayHelper.createPlayStoreIntent())
                            } catch (_: Exception) {
                                Toast.makeText(context, "Could not open Play Store", Toast.LENGTH_SHORT).show()
                            }
                            showGPayNotInstalledDialog = false
                            selectedPlanForFallback = null
                        }
                    ) {
                        Text("Install Google Pay", color = DeepNavy, fontFamily = InterFontFamily)
                    }
                    TextButton(
                        onClick = {
                            val plan = targetPlan
                            showGPayNotInstalledDialog = false
                            selectedPlanForFallback = null
                            simulatePayment(plan)
                        }
                    ) {
                        Text("Simulate Payment (Dev/Demo)", color = PrimaryOrange, fontFamily = InterFontFamily)
                    }
                    TextButton(
                        onClick = {
                            showGPayNotInstalledDialog = false
                            selectedPlanForFallback = null
                        }
                    ) {
                        Text("Cancel", color = TextSecondary, fontFamily = InterFontFamily)
                    }
                }
            }
        )
    }

    // No UPI Apps Dialog
    if (showNoUpiAppsDialog && selectedPlanForFallback != null) {
        val targetPlan = selectedPlanForFallback!!
        AlertDialog(
            onDismissRequest = {
                showNoUpiAppsDialog = false
                selectedPlanForFallback = null
            },
            title = {
                Text(
                    text = "No UPI Apps Found",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Text(
                    text = "No UPI app (Google Pay, PhonePe, Paytm) was found on this device to pay ₹${targetPlan.price.toInt()}. Please install Google Pay from the Play Store or simulate the payment in demo mode.",
                    fontSize = 13.sp,
                    fontFamily = InterFontFamily,
                    color = SlateGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            context.startActivity(GooglePayHelper.createPlayStoreIntent())
                        } catch (_: Exception) {
                            Toast.makeText(context, "Could not open Play Store", Toast.LENGTH_SHORT).show()
                        }
                        showNoUpiAppsDialog = false
                        selectedPlanForFallback = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Install Google Pay")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val plan = targetPlan
                            showNoUpiAppsDialog = false
                            selectedPlanForFallback = null
                            simulatePayment(plan)
                        }
                    ) {
                        Text("Simulate Payment (Demo)", color = PrimaryOrange)
                    }
                    TextButton(
                        onClick = {
                            showNoUpiAppsDialog = false
                            selectedPlanForFallback = null
                        }
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        )
    }

    // Payment Success Confirmation Dialog
    successDialogTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { successDialogTx = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = M3Success,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Payment Successful!",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "₹${tx.amount.toInt()} Paid",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy,
                        fontFamily = PoppinsFontFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+${tx.credits} Credits added to your account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = M3Success,
                        fontFamily = InterFontFamily
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = LightGray,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Payment Method: ${tx.paymentMethod}",
                                fontSize = 12.sp,
                                color = SlateGray,
                                fontFamily = InterFontFamily
                            )
                            if (tx.id.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Transaction ID: ${tx.id}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily
                                )
                            }
                            if (tx.approvalRefNo.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Approval Ref: ${tx.approvalRefNo}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { successDialogTx = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Awesome!", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Payment Failed Dialog
    errorDialogMessage?.let { errMsg ->
        AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = M3Error,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text(
                    text = "Payment Failed",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            text = {
                Text(
                    text = errMsg,
                    fontSize = 13.sp,
                    fontFamily = InterFontFamily,
                    color = SlateGray
                )
            },
            confirmButton = {
                Button(
                    onClick = { errorDialogMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK", fontFamily = InterFontFamily)
                }
            }
        )
    }
}
