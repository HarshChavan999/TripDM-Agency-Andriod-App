package com.tripdm.agency.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.tripdm.agency.data.model.AgencyProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AgencyAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "AgencyAuth"
    }

    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("387994411670-q5354384h89rasqgh4qtdiffnfc7hcps.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun signIn(email: String, pass: String): Result<AgencyProfile> {
        return try {
            Log.d(TAG, "Attempting Firebase signInWithEmailAndPassword for: $email")
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: throw Exception("Authentication returned empty user")
            Log.d(TAG, "Firebase Auth successful, uid: ${user.uid}")

            val profile = fetchAgencyProfile(user.uid, user.email)
            if (profile == null) {
                Log.w(TAG, "User exists in Auth, but no agency document found or role is not agency")
                auth.signOut()
                return Result.failure(Exception("This account is registered as a traveler, not an agency. Please use an agency account or register your agency."))
            }
            Log.d(TAG, "Agency profile fetched successfully: ${profile.companyName}, status: ${profile.approvalStatus}")
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in exception: ${e.message}", e)
            val friendlyMsg = when {
                e is FirebaseAuthInvalidCredentialsException ->
                    "Invalid email or password. Please verify your credentials or register a new agency account."
                e is FirebaseAuthException -> when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email. Please register as an agency."
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                    "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                    "ERROR_USER_DISABLED" -> "This account has been disabled. Please contact support."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later."
                    "ERROR_INVALID_CREDENTIAL" -> "Invalid email or password. Please verify your credentials or register a new agency account."
                    else -> e.localizedMessage ?: "Authentication failed: ${e.errorCode}"
                }
                e.message?.contains("credential", ignoreCase = true) == true ->
                    "Invalid email or password. Please verify your credentials or register a new agency account."
                e.message?.contains("password", ignoreCase = true) == true ->
                    "Incorrect password. Please try again."
                e.message?.contains("no user", ignoreCase = true) == true ->
                    "No account found with this email."
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network connection error. Please check your internet connection."
                else -> e.localizedMessage ?: "Sign-in failed. Please try again."
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<AgencyProfile> {
        return try {
            Log.d(TAG, "Attempting Firebase signInWithCredential (Google)")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Google authentication returned empty user")
            Log.d(TAG, "Google Auth successful, uid: ${user.uid}, email: ${user.email}")

            var profile = fetchAgencyProfile(user.uid, user.email)
            if (profile == null) {
                Log.d(TAG, "Creating new pending agency profile for Google user: ${user.email}")
                val cleanEmail = (user.email ?: "").trim().lowercase()
                val agencyMap = hashMapOf<String, Any?>(
                    "id" to user.uid,
                    "role" to "agency",
                    "approved" to false,
                    "approvalStatus" to "pending",
                    "name" to (user.displayName ?: "Agency Partner"),
                    "contactPersonName" to (user.displayName ?: ""),
                    "companyName" to (user.displayName ?: "Travel Agency"),
                    "email" to cleanEmail,
                    "authEmail" to cleanEmail,
                    "contactEmail" to cleanEmail,
                    "credits" to 100,
                    "freeChats" to 2,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(agencyMap).await()
                profile = AgencyProfile(
                    id = user.uid,
                    companyName = user.displayName ?: "Travel Agency",
                    contactPersonName = user.displayName ?: "",
                    email = cleanEmail,
                    approved = false,
                    approvalStatus = "pending",
                    credits = 100,
                    role = "agency"
                )
            }
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun registerAgency(profile: AgencyProfile, pass: String): Result<AgencyProfile> {
        return try {
            val cleanEmail = profile.email.trim().lowercase()
            Log.d(TAG, "Registering new agency user: $cleanEmail")
            val authResult = auth.createUserWithEmailAndPassword(cleanEmail, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Registration failed to create user")

            val agencyData = profile.copy(
                id = uid,
                email = cleanEmail,
                role = "agency",
                approved = false,
                approvalStatus = "pending",
                credits = 100,
                createdAt = System.currentTimeMillis()
            )

            val agencyMap = hashMapOf<String, Any?>(
                "id" to uid,
                "role" to "agency",
                "approved" to false,
                "approvalStatus" to "pending",
                "name" to profile.contactPersonName.ifBlank { profile.companyName },
                "contactPersonName" to profile.contactPersonName,
                "companyName" to profile.companyName,
                "email" to cleanEmail,
                "authEmail" to cleanEmail,
                "contactEmail" to cleanEmail,
                "phone" to "${profile.countryCode} ${profile.phone}".trim(),
                "contactNumber" to "${profile.countryCode} ${profile.phone}".trim(),
                "countryCode" to profile.countryCode,
                "businessLocation" to profile.businessLocation,
                "fullAddress" to profile.fullAddress,
                "description" to profile.description,
                "agencyDescription" to profile.description,
                "refundPolicy" to profile.refundPolicy,
                "operatingFromHome" to profile.operatingFromHome,
                "operatingFromOffice" to profile.operatingFromOffice,
                "officeAddress" to profile.officeAddress,
                "logoUrl" to profile.logoUrl,
                "agencyLogo" to profile.logoUrl,
                "avatarUrl" to profile.logoUrl,
                "credits" to 100,
                "freeChats" to 2,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            Log.d(TAG, "Writing agency document to users/$uid in Firestore")
            firestore.collection("users").document(uid).set(agencyMap).await()
            Log.d(TAG, "Agency document written successfully")
            Result.success(agencyData)
        } catch (e: Exception) {
            Log.e(TAG, "Registration exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAgencyProfile(uid: String, fallbackEmail: String? = null): AgencyProfile? {
        try {
            // 1. Direct document check by UID
            val directSnap = firestore.collection("users").document(uid).get().await()
            if (directSnap.exists()) {
                val p = parseProfile(directSnap)
                if (p != null) return p
            }

            // 2. Query by email if fallbackEmail provided (matching WebApp logic)
            val emailToQuery = fallbackEmail ?: auth.currentUser?.email
            if (!emailToQuery.isNullOrBlank()) {
                val cleanEmail = emailToQuery.trim().lowercase()
                val q1 = firestore.collection("users").whereEqualTo("email", cleanEmail).get().await()
                if (!q1.isEmpty) {
                    val p = parseProfile(q1.documents[0])
                    if (p != null) return p
                }

                val q2 = firestore.collection("users").whereEqualTo("authEmail", cleanEmail).get().await()
                if (!q2.isEmpty) {
                    val p = parseProfile(q2.documents[0])
                    if (p != null) return p
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching agency profile: ${e.message}", e)
        }
        return null
    }

    private fun parseProfile(snapshot: DocumentSnapshot): AgencyProfile? {
        val role = snapshot.getString("role") ?: ""
        val hasAgencyFields = snapshot.contains("companyName") ||
                snapshot.contains("operatingFromOffice") ||
                snapshot.contains("operatingFromHome") ||
                snapshot.contains("agencyDescription")

        // Allow agency, admin, or documents with agency business fields
        if (role != "agency" && role != "admin" && !hasAgencyFields) {
            Log.w(TAG, "User ${snapshot.id} has role '$role' and no agency fields")
            return null
        }

        val creditsVal = try {
            (snapshot.get("credits") as? Number)?.toInt()
                ?: snapshot.getLong("credits")?.toInt()
                ?: snapshot.getString("credits")?.toIntOrNull()
                ?: (snapshot.get("creditBalance") as? Number)?.toInt()
                ?: 100
        } catch (e: Exception) {
            100
        }

        val planVal = snapshot.getString("plan")
            ?: snapshot.getString("subscriptionPlan")
            ?: snapshot.getString("currentPlan")
            ?: snapshot.getString("agencyPlan")
            ?: "Free"

        val createdAtVal = try {
            snapshot.getLong("createdAt")
                ?: (snapshot.get("createdAt") as? Number)?.toLong()
                ?: (snapshot.get("createdAt") as? com.google.firebase.Timestamp)?.toDate()?.time
                ?: (snapshot.get("createdAt") as? String)?.let { str ->
                    try { java.time.Instant.parse(str).toEpochMilli() } catch (e: Exception) { null }
                }
                ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        val isApproved = snapshot.getBoolean("approved") == true ||
                snapshot.getString("approvalStatus") == "approved"

        val approvalStatus = snapshot.getString("approvalStatus")
            ?: if (isApproved) "approved" else "pending"

        val resolvedEmail = snapshot.getString("email")
            ?: snapshot.getString("authEmail")
            ?: snapshot.getString("contactEmail")
            ?: auth.currentUser?.email
            ?: ""

        return AgencyProfile(
            id = snapshot.id,
            companyName = snapshot.getString("companyName")
                ?: snapshot.getString("displayName")
                ?: snapshot.getString("name")
                ?: "Agency Partner",
            contactPersonName = snapshot.getString("contactPersonName")
                ?: snapshot.getString("name")
                ?: snapshot.getString("displayName")
                ?: "",
            email = resolvedEmail,
            phone = snapshot.getString("phone")
                ?: snapshot.getString("contactNumber")
                ?: "",
            countryCode = snapshot.getString("countryCode") ?: "+91",
            businessLocation = snapshot.getString("businessLocation") ?: "",
            fullAddress = snapshot.getString("fullAddress") ?: "",
            description = snapshot.getString("description")
                ?: snapshot.getString("agencyDescription")
                ?: "",
            operatingFromHome = snapshot.getBoolean("operatingFromHome") ?: false,
            operatingFromOffice = snapshot.getBoolean("operatingFromOffice") ?: false,
            officeAddress = snapshot.getString("officeAddress") ?: "",
            refundPolicy = snapshot.getString("refundPolicy") ?: "",
            approved = isApproved,
            approvalStatus = approvalStatus,
            logoUrl = snapshot.getString("logoUrl")
                ?: snapshot.getString("agencyLogo")
                ?: snapshot.getString("avatarUrl")
                ?: "",
            credits = creditsVal,
            plan = planVal,
            role = if (role.isNotEmpty()) role else "agency",
            createdAt = createdAtVal
        )
    }

    fun observeAgencyProfile(uid: String): Flow<AgencyProfile?> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(parseProfile(snapshot))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    fun signOut() {
        auth.signOut()
    }
}
