package com.tripdm.agency

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.data.model.AgencyProfile
import com.tripdm.agency.data.model.ChatConversation
import com.tripdm.agency.data.repository.AgencyAuthRepository
import com.tripdm.agency.data.repository.AgencyChatRepository
import com.tripdm.agency.service.AgencyMessagingService
import com.tripdm.agency.service.AgencyNotificationHelper
import com.tripdm.agency.ui.components.AgencyBottomNavBar
import com.tripdm.agency.ui.screens.*
import com.tripdm.agency.ui.theme.TripDMAgencyTheme
import com.tripdm.agency.viewmodel.*

enum class AgencyScreen {
    DASHBOARD,
    LISTINGS,
    CREATE_EDIT_LISTING,
    CHAT_LIST,
    CHAT_THREAD,
    PROFILE,
    CREDITS
}

class AgencyMainActivity : ComponentActivity() {

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var onGoogleSignInResult: ((com.google.android.gms.auth.api.signin.GoogleSignInAccount?) -> Unit)? = null
    private val authRepo = AgencyAuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AgencyNotificationHelper.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                android.util.Log.d("AgencyMainActivity", "POST_NOTIFICATIONS granted: $isGranted")
                if (isGranted) {
                    AgencyMessagingService.registerFCMToken()
                }
            }
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                AgencyMessagingService.registerFCMToken()
            }
        } else {
            AgencyMessagingService.registerFCMToken()
        }

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    onGoogleSignInResult?.invoke(account)
                } catch (e: ApiException) {
                    onGoogleSignInResult?.invoke(null)
                }
            } else {
                onGoogleSignInResult?.invoke(null)
            }
        }

        setContent {
            TripDMAgencyTheme {
                AgencyApp(
                    onGoogleSignInClick = {
                        val client = authRepo.getGoogleSignInClient(this@AgencyMainActivity)
                        googleSignInLauncher.launch(client.signInIntent)
                    },
                    setGoogleCallback = { cb ->
                        onGoogleSignInResult = cb
                    }
                )
            }
        }
    }
}

@Composable
fun AgencyApp(
    onGoogleSignInClick: () -> Unit,
    setGoogleCallback: (((com.google.android.gms.auth.api.signin.GoogleSignInAccount?) -> Unit)?) -> Unit,
    authViewModel: AgencyAuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val currentProfile by authViewModel.currentProfile.collectAsState()

    var isRegistering by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        setGoogleCallback { account ->
            val idToken = account?.idToken
            if (idToken != null) {
                authViewModel.signInWithGoogle(idToken)
            }
        }
        onDispose {
            setGoogleCallback(null)
        }
    }

    when (val state = authState) {
        is AgencyAuthState.PendingApproval -> {
            AgencyPendingApprovalScreen(
                profile = state.profile,
                onRefresh = { authViewModel.checkCurrentAuth() },
                onSignOut = { authViewModel.signOut() }
            )
        }
        is AgencyAuthState.Authenticated -> {
            AgencyMainPortal(
                profile = state.profile,
                onSignOut = { authViewModel.signOut() }
            )
        }
        else -> {
            val isLoading = authState is AgencyAuthState.Loading
            val errorMessage = (authState as? AgencyAuthState.Error)?.message

            BackHandler(enabled = isRegistering) {
                isRegistering = false
            }

            if (isRegistering) {
                AgencyRegisterScreen(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onRegister = { profile, password ->
                        authViewModel.register(profile, password)
                    },
                    onBackToLogin = { isRegistering = false }
                )
            } else {
                AgencyLoginScreen(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onLogin = { email, pass ->
                        authViewModel.signIn(email, pass)
                    },
                    onNavigateToRegister = {
                        authViewModel.clearError()
                        isRegistering = true
                    },
                    onGoogleSignIn = onGoogleSignInClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyMainPortal(
    profile: AgencyProfile,
    onSignOut: () -> Unit,
    dashboardViewModel: AgencyDashboardViewModel = viewModel(),
    listingViewModel: AgencyListingViewModel = viewModel(),
    chatViewModel: AgencyChatViewModel = viewModel(),
    profileViewModel: AgencyProfileViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentScreen by remember { mutableStateOf(AgencyScreen.DASHBOARD) }
    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var editingListing by remember { mutableStateOf<AgencyListing?>(null) }
    var activeChatConv by remember { mutableStateOf<ChatConversation?>(null) }

    // Sync active chat conversation with AgencyNotificationHelper to suppress alerts while actively chatting
    LaunchedEffect(activeChatConv) {
        AgencyNotificationHelper.activeChatUserId = activeChatConv?.otherUserId
    }

    // Register FCM push token for notifications
    LaunchedEffect(profile.id) {
        AgencyMessagingService.registerFCMToken(profile.id)
    }

    // Handle incoming notification intent click
    LaunchedEffect(Unit) {
        val activity = context as? ComponentActivity
        val openChatId = activity?.intent?.getStringExtra("OPEN_CHAT_USER_ID")
        if (!openChatId.isNullOrBlank()) {
            selectedBottomTab = 2
            currentScreen = AgencyScreen.CHAT_LIST
        }
    }

    // Init ViewModels with agency ID and sync real-time credits
    LaunchedEffect(profile.id, profile.credits) {
        dashboardViewModel.initialize(profile.id, profile.credits)
    }

    LaunchedEffect(profile.id) {
        listingViewModel.loadAgencyListings(profile.id)
        chatViewModel.loadConversations(profile.id)
        profileViewModel.loadTransactions(profile.id)
    }

    // Collect States
    val analytics by dashboardViewModel.analytics.collectAsState()
    val recentChats by dashboardViewModel.recentChats.collectAsState()

    val filteredListings by listingViewModel.filteredListings.collectAsState()
    val listingTab by listingViewModel.selectedTab.collectAsState()
    val searchQuery by listingViewModel.searchQuery.collectAsState()
    val isSubmittingListing by listingViewModel.isSubmitting.collectAsState()
    val listingMessage by listingViewModel.operationMessage.collectAsState()

    val conversations by chatViewModel.conversations.collectAsState()
    val activeMessages by chatViewModel.activeMessages.collectAsState()

    val transactions by profileViewModel.transactions.collectAsState()
    val creditPlans by profileViewModel.creditPlans.collectAsState()
    val isPurchasingCredits by profileViewModel.isPurchasing.collectAsState()
    val purchaseSuccessMsg by profileViewModel.purchaseSuccess.collectAsState()

    val unreadLeadsCount = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    // Handle back button for sub-screens
    BackHandler(enabled = currentScreen != AgencyScreen.DASHBOARD) {
        when (currentScreen) {
            AgencyScreen.CREATE_EDIT_LISTING -> {
                editingListing = null
                currentScreen = AgencyScreen.LISTINGS
            }
            AgencyScreen.CHAT_THREAD -> {
                chatViewModel.closeActiveConversation()
                activeChatConv = null
                currentScreen = AgencyScreen.CHAT_LIST
            }
            AgencyScreen.CREDITS -> {
                selectedBottomTab = 0
                currentScreen = AgencyScreen.DASHBOARD
            }
            else -> {
                selectedBottomTab = 0
                currentScreen = AgencyScreen.DASHBOARD
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentScreen != AgencyScreen.CREATE_EDIT_LISTING && currentScreen != AgencyScreen.CHAT_THREAD) {
                AgencyBottomNavBar(
                    selectedTab = selectedBottomTab,
                    onTabSelected = { index ->
                        selectedBottomTab = index
                        currentScreen = when (index) {
                            0 -> AgencyScreen.DASHBOARD
                            1 -> AgencyScreen.LISTINGS
                            2 -> AgencyScreen.CHAT_LIST
                            3 -> AgencyScreen.CREDITS
                            4 -> AgencyScreen.PROFILE
                            else -> AgencyScreen.DASHBOARD
                        }
                    },
                    unreadLeadsCount = unreadLeadsCount
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                AgencyScreen.DASHBOARD -> {
                    AgencyDashboardScreen(
                        profile = profile,
                        analytics = analytics,
                        recentChats = recentChats,
                        onCreateListingClick = {
                            editingListing = null
                            currentScreen = AgencyScreen.CREATE_EDIT_LISTING
                        },
                        onViewListingsClick = {
                            selectedBottomTab = 1
                            currentScreen = AgencyScreen.LISTINGS
                        },
                        onViewChatsClick = {
                            selectedBottomTab = 2
                            currentScreen = AgencyScreen.CHAT_LIST
                        },
                        onViewCreditsClick = {
                            selectedBottomTab = 3
                            currentScreen = AgencyScreen.CREDITS
                        },
                        onChatClick = { conv ->
                            activeChatConv = conv
                            chatViewModel.openConversation(profile.id, conv)
                            currentScreen = AgencyScreen.CHAT_THREAD
                        }
                    )
                }
                AgencyScreen.LISTINGS -> {
                    AgencyListingsScreen(
                        listings = filteredListings,
                        selectedTab = listingTab,
                        searchQuery = searchQuery,
                        onSearchChange = { listingViewModel.setSearchQuery(it) },
                        onTabSelected = { listingViewModel.setFilterTab(it) },
                        onCreateListingClick = {
                            editingListing = null
                            currentScreen = AgencyScreen.CREATE_EDIT_LISTING
                        },
                        onEditListingClick = { listing ->
                            editingListing = listing
                            currentScreen = AgencyScreen.CREATE_EDIT_LISTING
                        },
                        onDeleteListingClick = { id ->
                            listingViewModel.deleteListing(id)
                        },
                        onListingClick = { listing ->
                            editingListing = listing
                            currentScreen = AgencyScreen.CREATE_EDIT_LISTING
                        }
                    )
                }
                AgencyScreen.CREATE_EDIT_LISTING -> {
                    CreateEditListingScreen(
                        agencyId = profile.id,
                        agencyName = profile.companyName,
                        initialListing = editingListing,
                        isSubmitting = isSubmittingListing,
                        onSave = { listing ->
                            if (editingListing == null) {
                                listingViewModel.createListing(listing) { success ->
                                    if (success) {
                                        currentScreen = AgencyScreen.LISTINGS
                                    }
                                }
                            } else {
                                listingViewModel.updateListing(listing) { success ->
                                    if (success) {
                                        editingListing = null
                                        currentScreen = AgencyScreen.LISTINGS
                                    }
                                }
                            }
                        },
                        onBack = {
                            editingListing = null
                            currentScreen = AgencyScreen.LISTINGS
                        }
                    )
                }
                AgencyScreen.CHAT_LIST -> {
                    AgencyChatListScreen(
                        conversations = conversations,
                        onConversationClick = { conv ->
                            activeChatConv = conv
                            chatViewModel.openConversation(profile.id, conv)
                            currentScreen = AgencyScreen.CHAT_THREAD
                        }
                    )
                }
                AgencyScreen.CHAT_THREAD -> {
                    activeChatConv?.let { conv ->
                        AgencyChatScreen(
                            currentAgencyId = profile.id,
                            currentAgencyName = profile.companyName,
                            conversation = conv,
                            messages = activeMessages,
                            onSendMessage = { content, replyToId, replyToContent, replyToSenderName ->
                                chatViewModel.sendMessage(
                                    agencyId = profile.id,
                                    agencyName = profile.companyName,
                                    content = content,
                                    replyToId = replyToId,
                                    replyToContent = replyToContent,
                                    replyToSenderName = replyToSenderName
                                )
                            },
                            onDeleteMessage = { msgId -> chatViewModel.deleteMessage(msgId) },
                            onEditMessage = { msgId, newContent -> chatViewModel.editMessage(msgId, newContent) },
                            onReactToMessage = { msgId, emoji -> chatViewModel.reactToMessage(msgId, profile.id, emoji) },
                            onBack = {
                                chatViewModel.closeActiveConversation()
                                activeChatConv = null
                                currentScreen = AgencyScreen.CHAT_LIST
                            }
                        )
                    }
                }
                AgencyScreen.PROFILE -> {
                    AgencyProfileScreen(
                        profile = profile,
                        transactions = transactions,
                        onViewCreditsClick = {
                            selectedBottomTab = 3
                            currentScreen = AgencyScreen.CREDITS
                        },
                        onSignOut = onSignOut
                    )
                }
                AgencyScreen.CREDITS -> {
                    AgencyCreditsScreen(
                        currentCredits = profile.credits,
                        currentPlan = profile.plan,
                        creditPlans = creditPlans,
                        transactions = transactions,
                        isPurchasing = isPurchasingCredits,
                        purchaseMessage = purchaseSuccessMsg,
                        onPurchasePlan = { plan ->
                            profileViewModel.purchasePlan(profile.id, plan)
                        },
                        onClearPurchaseMessage = {
                            profileViewModel.clearPurchaseMessage()
                        },
                        onBack = {
                            selectedBottomTab = 0
                            currentScreen = AgencyScreen.DASHBOARD
                        }
                    )
                }
            }
        }
    }
}
