#!/bin/bash

# Exit on error
set -e

# Navigate to the project root
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Building KMP Shared Framework..."
./gradlew :shared:assembleSharedReleaseFramework-iosArm64

# Navigate to iosApp directory
cd iosApp

# Ensure build directory exists
mkdir -p build

echo "Archiving iOS Application..."
xcodebuild archive \
    -project iosApp.xcodeproj \
    -scheme iosApp \
    -configuration Release \
    -archivePath build/iosApp.xcarchive \
    -destination 'generic/platform=iOS'

echo "Exporting IPA..."
xcodebuild -exportArchive \
    -archivePath build/iosApp.xcarchive \
    -exportOptionsPlist ExportOptions.plist \
    -exportPath build

echo "IPA generated successfully in iosApp/build/"
