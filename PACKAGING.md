# Cliptomic - macOS App Packaging Guide

This guide explains how to package Cliptomic as a distributable macOS application.

## Overview

Cliptomic is configured to build as a native macOS application using Compose Multiplatform's packaging capabilities. The app is packaged as both a `.app` bundle and a `.dmg` disk image for easy distribution.

## Prerequisites

- macOS development environment
- JDK 11 or higher
- Gradle (included via wrapper)

## Building the macOS App

### Quick Build
To build the DMG package for distribution:

```bash
./gradlew :composeApp:packageDmg
```

### Development Build
To build just the app bundle for testing:

```bash
./gradlew :composeApp:createDistributable
```

### Clean Build
To ensure a fresh build:

```bash
./gradlew :composeApp:clean :composeApp:packageDmg
```

## Code Signing for Distribution

To distribute your app without macOS showing "unidentified developer" warnings, you need to sign the DMG with an Apple Developer certificate and optionally notarize it.

### Prerequisites for Signing

1. **Apple Developer Account**: Enroll in the [Apple Developer Program](https://developer.apple.com/programs/)
2. **Developer ID Certificate**: Create a "Developer ID Application" certificate in your Apple Developer account and install it in your Keychain
3. **App-Specific Password**: Generate an app-specific password at [appleid.apple.com](https://appleid.apple.com) (required for notarization)

### Finding Your Signing Identity

List available signing identities in your Keychain:

```bash
security find-identity -v -p codesigning
```

Look for an identity like: `Developer ID Application: Your Name (TEAM_ID)`

### Building a Signed DMG

To build a signed DMG, provide your signing identity:

```bash
./gradlew :composeApp:packageDmg -PmacSigningIdentity="Developer ID Application: Your Name (TEAM_ID)"
```

### Building a Signed and Notarized DMG

For full distribution (recommended), also notarize the DMG with Apple:

```bash
./gradlew :composeApp:packageDmg \
  -PmacSigningIdentity="Developer ID Application: Your Name (TEAM_ID)" \
  -PappleId="your-apple-id@example.com" \
  -PapplePassword="your-app-specific-password" \
  -PappleTeamId="YOUR_TEAM_ID"
```

### Using gradle.properties (Recommended for CI/CD)

Instead of passing parameters on the command line, you can set them in `~/.gradle/gradle.properties` (user-level) or in the project's `gradle.properties`:

```properties
# macOS Code Signing
macSigningIdentity=Developer ID Application: Your Name (TEAM_ID)

# Notarization (optional but recommended)
appleId=your-apple-id@example.com
applePassword=your-app-specific-password
appleTeamId=YOUR_TEAM_ID
```

**Security Note**: Never commit credentials to version control. Use environment variables or a local `gradle.properties` file.

### Verifying the Signature

After building, verify the signature:

```bash
codesign --verify --verbose=4 composeApp/build/compose/binaries/main/app/Cliptomic.app
spctl --assess --verbose=4 --type execute composeApp/build/compose/binaries/main/app/Cliptomic.app
```

## Generated Files

After successful packaging, you'll find:

### DMG File (for distribution)
- **Location**: `composeApp/build/compose/binaries/main/dmg/Cliptomic-1.0.0.dmg`
- **Size**: ~80MB
- **Purpose**: Distributable disk image that users can download and install

### App Bundle (for development/testing)
- **Location**: `composeApp/build/compose/binaries/main/app/Cliptomic.app`
- **Purpose**: Native macOS application bundle that can be run directly

## App Configuration

The app is configured with the following metadata:

- **App Name**: Cliptomic
- **Bundle ID**: com.paleblueapps.cliptomic
- **Version**: 1.0.0
- **Category**: Productivity
- **Description**: AI-powered clipboard manager for macOS
- **Vendor**: Pale Blue Apps

## Permissions & Entitlements

The app includes an `entitlements.plist` file that grants necessary permissions:

- **Clipboard Access**: Required for clipboard monitoring and management
- **Accessibility Features**: Required for global hotkey functionality
- **Network Access**: Required for OpenRouter API integration
- **File System Access**: Required for configuration storage
- **Background Execution**: App runs outside sandbox for system-level access

## Installation Instructions for Users

1. Download the `Cliptomic-1.0.0.dmg` file
2. Double-click the DMG to mount it
3. Drag the Cliptomic app to the Applications folder
4. Launch Cliptomic from Applications
5. Grant necessary permissions when prompted:
   - Accessibility access (for global hotkeys)
   - Any other system permissions as requested

## Distribution Notes

- The app is configured to hide from the macOS dock (runs as menu bar app)
- Users may need to grant accessibility permissions for global hotkey functionality
- The app requires macOS 10.14 or later (typical for Compose Desktop apps)
- The DMG is ready for distribution via direct download or app stores

## Troubleshooting

### Build Issues
- Ensure you have the latest JDK installed
- Try cleaning the build: `./gradlew clean`
- Check that all dependencies are available

### Runtime Issues
- Verify accessibility permissions are granted in System Preferences
- Check that the app has necessary entitlements for clipboard access
- Ensure network connectivity for AI features

## Development Notes

- Main entry point: `com.paleblueapps.cliptomic.MainKt`
- The app uses Compose Desktop for UI
- Services include clipboard monitoring, global hotkeys, and AI integration
- Configuration is managed through a settings screen accessible via system tray