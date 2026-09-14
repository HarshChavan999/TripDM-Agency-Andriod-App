# TripDM Agency Partner Android App 🏢✈️

A modern, native Android application built with **Jetpack Compose** and **Material 3** for travel agencies, tour operators, and partner agents on the **TripDM** platform.

This app serves as the dedicated mobile operational counterpart to the **TripDM WebApp Agency Portal** and works in harmony with the **TripDM User Android App** to facilitate seamless real-time booking management, customer chat, listing creation, and business analytics.

---

## 🌟 Key Features

### 🔐 1. Agency Authentication & Verification Gate
- **Dual Sign-In Modes**: Secure Email/Password authentication & Google Sign-In.
- **Onboarding & Registration**: Comprehensive agency registration workflow including company details, contact person, verified phone number with international country codes, business location, operational model (Office vs. Remote/Home), and policies.
- **Approval Gatekeeping**: Real-time checking of admin approval status (`pending`, `approved`, `rejected`) with a dedicated pending approval review screen.
- **Intelligent Error Handling**: Resilient credentials validation and role differentiation between travelers and agencies.

### 📊 2. Agency Dashboard & Real-Time Analytics
- **Performance Overview**: Live indicators for Total Packages, Live Packages, Pending Bookings, and Traveler Leads.
- **Quick Action Hub**: Instant shortcuts for "New Package" creation and "Recharge Credits".
- **Credits & Balance Widget**: Real-time agency wallet balance display directly in the header.

### 📦 3. Package & Listing Management
- **Listing Catalogue**: Searchable and filterable package list (All, Approved, Pending Review).
- **Comprehensive Listing Editor**: Create and edit multi-day travel packages with:
  - Title, destination, category, and theme
  - Dynamic pricing (base price, discounted price, seasonal pricing)
  - Day-by-day itinerary planning
  - Inclusions & exclusions checklist
  - Visual gallery & thumbnail management

### 📅 4. Booking Request Processing
- **Real-Time Booking Feed**: Live tracking of booking requests with status badges (`Pending`, `Confirmed`, `Completed`, `Cancelled`).
- **One-Tap Actions**: Accept or decline traveler bookings with automated status sync back to Firestore.
- **Customer Details**: Traveler name, contact info, requested travel dates, and guest headcount.

### 💬 5. Direct Traveler Messaging & Leads
- **Instant Chat System**: Real-time 1-on-1 messaging between agencies and interested travelers.
- **Chat List**: Displays recent message snippets, unread badges, and timestamp sorting.
- **Push Notifications**: Firebase Cloud Messaging (FCM) integration for immediate notification on incoming inquiries.

### 💳 6. Agency Wallet & Credit System
- **Balance Monitoring**: Available credit balance display.
- **Recharge Packages**: Credit replenishment plans tailored for agency lead generation.
- **Transaction History**: Audit trail of credits spent unlocking traveler leads or earned through promotions.

### 👤 7. Business Profile Management
- **Company Branding**: Logo, business description, and location.
- **Operational Model**: Workplace status (Office Address vs. Home/Remote).
- **Policies**: Terms & cancellation / refund policy management.
- **Session Management**: One-tap secure logout.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Language** | Kotlin 2.0+ (100% Kotlin) |
| **UI Framework** | Jetpack Compose (Declarative UI) with Material 3 |
| **Design System** | TripDM Warm Travel Palette (Primary Orange `#FF6B00`, Deep Navy `#0D1B2A`, Coral, Sage) |
| **Typography** | Google Fonts (Poppins & Inter) |
| **Architecture** | MVVM (Model-View-ViewModel) + Clean Repository Pattern |
| **Concurrency** | Kotlin Coroutines, StateFlow, SharedFlow, Channels |
| **Backend & Cloud** | Firebase Authentication, Cloud Firestore, Firebase Cloud Messaging (FCM) |
| **Image Loading** | Coil Compose |
| **Build System** | Gradle (Kotlin DSL, compileSdk 34, minSdk 24, targetSdk 34, Java 17) |

---

## 📁 Architecture & Directory Structure

```
TripDM-Agency-Andriod-App/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/tripdm/agency/
│   │   │   ├── AgencyMainActivity.kt         # Single-activity Compose host
│   │   │   ├── data/
│   │   │   │   ├── model/                     # Domain & Data Models
│   │   │   │   │   ├── AgencyProfile.kt
│   │   │   │   │   ├── AgencyListing.kt
│   │   │   │   │   ├── BookingRequest.kt
│   │   │   │   │   ├── ChatMessage.kt
│   │   │   │   │   ├── AgencyCredits.kt
│   │   │   │   │   └── AnalyticsSummary.kt
│   │   │   │   └── repository/                # Firebase & Firestore Repositories
│   │   │   │       ├── AgencyAuthRepository.kt
│   │   │   │       ├── AgencyDashboardRepository.kt
│   │   │   │       ├── AgencyListingRepository.kt
│   │   │   │       ├── AgencyBookingRepository.kt
│   │   │   │       ├── AgencyChatRepository.kt
│   │   │   │       └── AgencyCreditsRepository.kt
│   │   │   ├── viewmodel/                     # Lifecycle-aware ViewModels
│   │   │   │   ├── AgencyAuthViewModel.kt
│   │   │   │   ├── AgencyDashboardViewModel.kt
│   │   │   │   ├── AgencyListingViewModel.kt
│   │   │   │   ├── AgencyBookingViewModel.kt
│   │   │   │   ├── AgencyChatViewModel.kt
│   │   │   │   └── AgencyProfileViewModel.kt
│   │   │   ├── ui/
│   │   │   │   ├── theme/                     # Colors, Typography & Theme
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Type.kt
│   │   │   │   │   └── Theme.kt
│   │   │   │   ├── components/                # Reusable UI widgets
│   │   │   │   │   ├── AgencyBottomNavBar.kt
│   │   │   │   │   ├── BookingRequestCard.kt
│   │   │   │   │   └── MetricCard.kt
│   │   │   │   └── screens/                   # Top-level composable screens
│   │   │   │       ├── AgencyLoginScreen.kt
│   │   │   │       ├── AgencyRegisterScreen.kt
│   │   │   │       ├── AgencyPendingApprovalScreen.kt
│   │   │   │       ├── AgencyDashboardScreen.kt
│   │   │   │       ├── AgencyListingsScreen.kt
│   │   │   │       ├── CreateEditListingScreen.kt
│   │   │   │       ├── AgencyBookingsScreen.kt
│   │   │   │       ├── AgencyChatListScreen.kt
│   │   │   │       ├── AgencyChatScreen.kt
│   │   │   │       ├── AgencyCreditsScreen.kt
│   │   │   │       └── AgencyProfileScreen.kt
│   │   │   └── service/
│   │   │       ├── AgencyMessagingService.kt  # FCM Push Notification Service
│   │   │       └── AgencyNotificationHelper.kt
│   │   └── res/                               # Icons, XML configs, strings
│   ├── google-services.json                   # Firebase Client Configuration
│   ├── debug.keystore                         # Development signing key
│   └── build.gradle.kts                       # App module configuration
├── gradle/
│   └── libs.versions.toml                     # Version Catalog
└── build.gradle.kts                           # Root build configuration
```

---

## 🚀 Getting Started & Setup

### Prerequisites
- **JDK 17** (e.g., OpenJDK 17)
- **Android SDK 34** (compileSdk 34, build-tools 34.0.0+)
- **Android Studio Hedgehog / Iguana / Jellyfish** or newer (or command-line tools)

### Building via Terminal
```bash
# 1. Set JAVA_HOME to Java 17
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"

# 2. Build the Debug APK
./gradlew assembleDebug

# 3. Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Running on Emulator / Physical Device
```bash
# Install to connected device or running emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch the application
adb shell am start -n com.example.mychat/com.tripdm.agency.AgencyMainActivity
```

---

## 🔗 Related Repositories

- **User Android App**: [TripDM-User-Andriod-App](https://github.com/HarshChavan999/TripDM-User-Andriod-App)
- **Web Application & Agency Portal**: [TripDM-WebApp](https://github.com/HarshChavan999/TripDM-WebApp)
