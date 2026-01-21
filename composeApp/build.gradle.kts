import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
            implementation(libs.jnativehook)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.paleblueapps.cliptomic.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Cliptomic"
            packageVersion = "1.0.0"
            description = "AI-powered clipboard manager for macOS"
            copyright = "© 2024 Pale Blue Apps. All rights reserved."
            vendor = "Pale Blue Apps"
            
            macOS {
                bundleID = "com.paleblueapps.cliptomic"
                appCategory = "public.app-category.productivity"
                entitlementsFile.set(project.file("entitlements.plist"))
                
                // Code signing configuration
                // To sign the app, provide the signing identity via gradle property:
                // ./gradlew :composeApp:packageDmg -PmacSigningIdentity="Developer ID Application: Your Name (TEAM_ID)"
                signing {
                    sign.set(project.findProperty("macSigningIdentity") != null)
                    identity.set(project.findProperty("macSigningIdentity") as? String ?: "")
                }
                
                // Notarization configuration (required for distribution outside App Store)
                // Provide these via gradle properties or environment variables:
                // -PappleId="your-apple-id@example.com"
                // -PapplePassword="app-specific-password"
                // -PappleTeamId="YOUR_TEAM_ID"
                if (project.findProperty("appleId") != null) {
                    notarization {
                        appleID.set(project.findProperty("appleId") as? String ?: "")
                        password.set(project.findProperty("applePassword") as? String ?: "")
                        teamID.set(project.findProperty("appleTeamId") as? String ?: "")
                    }
                }
            }
        }
    }
}
