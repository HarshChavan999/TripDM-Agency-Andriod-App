package com.tripdm.agency.data.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import com.tripdm.agency.data.model.CreditPlan
import java.util.Locale

sealed class UpiPaymentResult {
    data class Success(
        val plan: CreditPlan,
        val paymentId: String,
        val approvalRefNo: String?,
        val paymentMethod: String = "Google Pay",
        val rawResponse: String? = null
    ) : UpiPaymentResult()

    data class Failed(
        val plan: CreditPlan,
        val message: String
    ) : UpiPaymentResult()

    data class Cancelled(
        val plan: CreditPlan
    ) : UpiPaymentResult()
}

data class InstalledUpiApp(
    val name: String,
    val packageName: String,
    val icon: Drawable?
)

object GooglePayHelper {
    const val GOOGLE_PAY_PACKAGE = "com.google.android.apps.nbu.paisa.user"
    const val DEFAULT_UPI_ID = "tripdm@okaxis"
    const val DEFAULT_PAYEE_NAME = "TripDM Subscriptions"
    const val DEFAULT_MCC = "4722" // Travel Agencies, Tour Operators

    /**
     * Build standard NPCI-compliant UPI URI with the exact subscription plan amount.
     * Format: upi://pay?pa=...&pn=...&mc=...&tr=...&tn=...&am=...&cu=INR
     * The 'am' parameter is strictly formatted with 2 decimal places (e.g., 2000.00).
     * This causes Google Pay to directly display this exact amount on its payment sheet.
     */
    fun buildUpiUri(
        plan: CreditPlan,
        transactionId: String,
        upiId: String = DEFAULT_UPI_ID,
        payeeName: String = DEFAULT_PAYEE_NAME
    ): Uri {
        val sanitizedNote = ("TripDM " + plan.name.trim())
            .replace("[^a-zA-Z0-9 ]".toRegex(), "")
            .take(30)
        val formattedAmount = String.format(Locale.US, "%.2f", plan.price)

        return Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", payeeName)
            .appendQueryParameter("mc", DEFAULT_MCC)
            .appendQueryParameter("tr", transactionId)
            .appendQueryParameter("tn", sanitizedNote)
            .appendQueryParameter("am", formattedAmount)
            .appendQueryParameter("cu", "INR")
            .build()
    }

    /**
     * Creates an explicit Intent directly targeting Google Pay (Tez/India)
     * with the pre-filled subscription plan amount.
     */
    fun createGooglePayIntent(plan: CreditPlan, transactionId: String): Intent {
        val uri = buildUpiUri(plan, transactionId)
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(GOOGLE_PAY_PACKAGE)
        }
    }

    /**
     * Creates a generic UPI Intent for fallback when user chooses any other UPI app
     * or when Google Pay is not installed.
     */
    fun createGenericUpiIntent(plan: CreditPlan, transactionId: String): Intent {
        val uri = buildUpiUri(plan, transactionId)
        return Intent(Intent.ACTION_VIEW, uri)
    }

    /**
     * Creates an Android system chooser intent displaying all available UPI apps on the device.
     */
    fun createChooserUpiIntent(
        plan: CreditPlan,
        transactionId: String,
        title: String = "Pay ₹${plan.price.toInt()} with UPI"
    ): Intent {
        val genericIntent = createGenericUpiIntent(plan, transactionId)
        return Intent.createChooser(genericIntent, title)
    }

    /**
     * Checks if Google Pay is installed on the user's device.
     */
    fun isGooglePayInstalled(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo(GOOGLE_PAY_PACKAGE, PackageManager.GET_ACTIVITIES)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks if any UPI-capable application is installed on the device.
     */
    fun isAnyUpiAppInstalled(context: Context, plan: CreditPlan, txId: String = "probe"): Boolean {
        return try {
            val intent = createGenericUpiIntent(plan, txId)
            intent.resolveActivity(context.packageManager) != null
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Discovers all installed UPI apps on the device (e.g., PhonePe, Paytm, BHIM, Cred, GPay).
     */
    fun getInstalledUpiApps(context: Context, plan: CreditPlan, txId: String = "probe"): List<InstalledUpiApp> {
        val pm = context.packageManager
        val genericIntent = createGenericUpiIntent(plan, txId)
        val resolveList = pm.queryIntentActivities(genericIntent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveList.map { ri ->
            InstalledUpiApp(
                name = ri.loadLabel(pm).toString(),
                packageName = ri.activityInfo.packageName,
                icon = try { ri.loadIcon(pm) } catch (_: Exception) { null }
            )
        }
    }

    /**
     * Intent to redirect the user to Google Pay on Google Play Store.
     */
    fun createPlayStoreIntent(): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$GOOGLE_PAY_PACKAGE")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Generates a unique transaction reference ID.
     */
    fun generateTransactionId(plan: CreditPlan): String {
        return "TX-${plan.id.uppercase()}-${System.currentTimeMillis()}"
    }

    /**
     * Parses the UPI response data returned in onActivityResult by Google Pay or other UPI apps.
     */
    fun parseUpiResponse(
        resultCode: Int,
        data: Intent?,
        plan: CreditPlan,
        paymentMethod: String = "Google Pay"
    ): UpiPaymentResult {
        if (data == null) {
            return if (resultCode == Activity.RESULT_OK) {
                UpiPaymentResult.Success(
                    plan = plan,
                    paymentId = "gpay_ok_${System.currentTimeMillis()}",
                    approvalRefNo = null,
                    paymentMethod = paymentMethod
                )
            } else {
                UpiPaymentResult.Cancelled(plan)
            }
        }

        val rawResponse = data.getStringExtra("response")
            ?: data.dataString
            ?: ""

        val extras = data.extras
        val statusExtra = extras?.getString("Status") ?: extras?.getString("status")
        val txnIdExtra = extras?.getString("txnId") ?: extras?.getString("txnid")
        val approvalRefExtra = extras?.getString("ApprovalRefNo") ?: extras?.getString("approvalrefno")
        val responseCodeExtra = extras?.getString("responseCode") ?: extras?.getString("responsecode")

        val paramsMap = if (rawResponse.isNotBlank()) {
            rawResponse.split("&").mapNotNull { pair ->
                val tokens = pair.split("=")
                if (tokens.size >= 2) {
                    tokens[0].trim().lowercase() to tokens[1].trim()
                } else null
            }.toMap()
        } else {
            emptyMap()
        }

        val status = (statusExtra ?: paramsMap["status"] ?: "").lowercase()
        val txnId = txnIdExtra ?: paramsMap["txnid"] ?: paramsMap["txnref"]
        val approvalRef = approvalRefExtra ?: paramsMap["approvalrefno"]
        val responseCode = (responseCodeExtra ?: paramsMap["responsecode"] ?: "").lowercase()

        return when {
            status == "success" || responseCode in listOf("00", "0", "success") -> {
                UpiPaymentResult.Success(
                    plan = plan,
                    paymentId = txnId ?: approvalRef ?: "upi_txn_${System.currentTimeMillis()}",
                    approvalRefNo = approvalRef,
                    paymentMethod = paymentMethod,
                    rawResponse = rawResponse
                )
            }
            status == "submitted" -> {
                // Bank accepted the transaction, pending settlement
                UpiPaymentResult.Success(
                    plan = plan,
                    paymentId = txnId ?: "upi_sub_${System.currentTimeMillis()}",
                    approvalRefNo = approvalRef,
                    paymentMethod = paymentMethod,
                    rawResponse = rawResponse
                )
            }
            status in listOf("failure", "failed") -> {
                UpiPaymentResult.Failed(
                    plan = plan,
                    message = "Payment failed or was declined in $paymentMethod."
                )
            }
            resultCode == Activity.RESULT_CANCELED -> {
                UpiPaymentResult.Cancelled(plan)
            }
            resultCode == Activity.RESULT_OK -> {
                UpiPaymentResult.Success(
                    plan = plan,
                    paymentId = txnId ?: approvalRef ?: "upi_txn_${System.currentTimeMillis()}",
                    approvalRefNo = approvalRef,
                    paymentMethod = paymentMethod,
                    rawResponse = rawResponse
                )
            }
            else -> {
                if (!txnId.isNullOrBlank() || !approvalRef.isNullOrBlank()) {
                    UpiPaymentResult.Success(
                        plan = plan,
                        paymentId = txnId ?: approvalRef!!,
                        approvalRefNo = approvalRef,
                        paymentMethod = paymentMethod,
                        rawResponse = rawResponse
                    )
                } else {
                    UpiPaymentResult.Cancelled(plan)
                }
            }
        }
    }
}
