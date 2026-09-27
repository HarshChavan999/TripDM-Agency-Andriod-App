#!/bin/bash
#
# Firebase App Distribution Deployment Script - Agency App
# Deploys the built release APK to testers via Firebase App Distribution
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APK_PATH="$SCRIPT_DIR/app/build/outputs/apk/release/app-release.apk"
APP_ID="1:387994411670:android:dd2b057de22b1fdb9f18b7"
PROJECT_ID="travel-agent-management-29c27"
TESTER_GROUPS="only-me"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE} TripDM Agency App - Firebase Distribution${NC}"
echo -e "${BLUE}========================================${NC}"

if ! command -v firebase &> /dev/null; then
    echo -e "${RED}❌ Firebase CLI is not installed.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Building Release APK...${NC}"
JAVA_HOME="/opt/homebrew/opt/openjdk@17" "$SCRIPT_DIR/gradlew" assembleRelease

if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ APK not found at: $APK_PATH${NC}"
    exit 1
fi
echo -e "${GREEN}✅ APK built successfully: $APK_PATH${NC}"

RELEASE_NOTES="Direct Google Pay Subscription & UPI Fallback:
• Integrated direct one-click payment via Google Pay with exact subscription amount pre-filled.
• Fallback to other installed UPI apps (PhonePe, Paytm, BHIM, Cred, etc.).
• Android 11+ package visibility declarations.
• Real-time credit balance and plan status update with payment method tracking."

echo ""
echo "📤 Uploading APK to Firebase App Distribution..."
firebase appdistribution:distribute "$APK_PATH" \
    --app "$APP_ID" \
    --project "$PROJECT_ID" \
    --groups "$TESTER_GROUPS" \
    --release-notes "$RELEASE_NOTES"

echo -e "${GREEN}✅ Distributed to $TESTER_GROUPS group successfully!${NC}"
