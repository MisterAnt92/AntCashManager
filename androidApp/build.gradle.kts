import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("jacoco")
}

// ── Jacoco Configuration ──
jacoco {
    toolVersion = "0.8.13"
}

// ── Release Signing ──
// Fill androidApp/signing.properties with your keystore details (gitignored).
// If the file is absent the build falls back to the debug keystore with a warning.
val signingProps =
    Properties().apply {
        rootProject.file("androidApp/signing.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
    }
val hasReleaseKeystore = signingProps.isNotEmpty()
if (!hasReleaseKeystore) {
    logger.warn("⚠️  androidApp/signing.properties not found — release build will use DEBUG keystore. " +
        "Copy signing.properties.example and fill in your keystore details before uploading to Play Store.")
}

android {
    namespace = "com.antcashmanager.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sformica.ant_cashmanager"
        minSdk = 26
        targetSdk = 37
        versionCode = 25
        versionName = "1.7.7"
    }

    // ── Product Flavors for Optimization ──
    // full: Complete app with Google Drive backup
    // lite: Minimal app without Google Drive API dependencies (-2-3 MB)
    flavorDimensions.add("variant")
    productFlavors {
        register("full") {
            dimension = "variant"
            isDefault = true
            buildConfigField("boolean", "INCLUDE_DRIVE_BACKUP", "true")
        }
        register("lite") {
            dimension = "variant"
            buildConfigField("boolean", "INCLUDE_DRIVE_BACKUP", "false")
            // Regole R8 aggiuntive per ulteriore shrinking delle dipendenze Drive rimosse
            proguardFile("proguard-rules-lite.pro")
        }
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = signingProps["storeFile"]?.let { file(it as String) }
                storePassword = signingProps["storePassword"] as String?
                keyAlias = signingProps["keyAlias"] as String?
                keyPassword = signingProps["keyPassword"] as String?
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                if (hasReleaseKeystore) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
            configure<CrashlyticsExtension> {
                // Upload automatico del mapping file per deobfuscation stacktrace release.
                mappingFileUploadEnabled = true
            }
        }
    }

    // ── Build Variant-Specific ProGuard Rules ──
    // Note: Lite variant will use proguard-rules-lite.pro for additional shrinking
    // This can be configured per-variant in the android extension if needed

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes +=
                setOf(
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE-notice.md",
                    "META-INF/INDEX.LIST",
                    "META-INF/DEPENDENCIES",
                    // Java-desktop trust store / resource files not needed on Android
                    "mozilla/public-suffix-list.txt",
                    "com/google/api/client/googleapis/google.p12",
                    "com/google/api/client/googleapis/google.jks",
                    "org/apache/commons/codec/language/bm/**",
                    // Metadata files unused at runtime
                    "META-INF/*.version",
                    "META-INF/proguard/**",
                    "kotlin/**",
                    "**/*.kotlin_builtins",
                )
        }
    }

    // ── Locale Filters: strip library locales not declared by the app ──
    androidResources {
        localeFilters +=
            listOf("en", "it", "fr", "de", "es", "hi", "ja", "ko", "pl", "ru", "uk", "zh", "zh-rTW")
    }

    // ── AAB split config (Play delivers per-ABI/density/language slice) ──
    bundle {
        language { enableSplit = true }
        density { enableSplit = true }
        abi { enableSplit = true }
    }

    // ── Dependencies metadata: remove from APK (useless outside Play), keep in AAB ──
    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.splashscreen)
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.glance.appwidget)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.google.fonts)
    implementation(libs.kermit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose)
    // WorkManager + DocumentFile
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.documentfile)
    // Google Drive Integration
    implementation(libs.google.play.services.auth)
    implementation(libs.google.drive.api)
    implementation(libs.google.http.client)
    implementation(libs.google.oauth.client)
    // Security crypto for encrypted token storage
    implementation(libs.androidx.security.crypto)
    // Foldable device support
    implementation(libs.androidx.window)
    implementation(libs.androidx.window.core)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.junit.ext)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Android Test Orchestrator for better test isolation and parallelism
    androidTestUtil(libs.androidx.test.orchestrator)
}

// ── Jacoco Coverage Report Tasks ──
tasks.register("jacocoTestDebugUnitTestReport", JacocoReport::class) {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        fileTree("${layout.buildDirectory}/intermediates/javac/debug") {
            exclude(
                "**/R.class",
                "**/R\$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
            )
        },
    )

    sourceDirectories.setFrom(files("src/main/kotlin", "src/main/java"))
    executionData.setFrom(files("${layout.buildDirectory}/jacoco/testDebugUnitTest.exec"))
}

tasks.register("jacocoConnectedDebugAndroidTestReport", JacocoReport::class) {
    dependsOn("connectedDebugAndroidTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        fileTree("${layout.buildDirectory}/intermediates/javac/debug") {
            exclude(
                "**/R.class",
                "**/R\$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
            )
        },
    )

    sourceDirectories.setFrom(files("src/main/kotlin", "src/main/java"))
    executionData.setFrom(files("${layout.buildDirectory}/jacoco/connectedDebugAndroidTest.exec"))
}

// Convenience task to generate all coverage reports
tasks.register("testCoverageReport") {
    dependsOn("jacocoTestDebugUnitTestReport", "jacocoConnectedDebugAndroidTestReport")
    doLast {
        println("\n✅ Jacoco coverage reports generated:")
        println("   📊 Unit Tests: build/reports/jacoco/jacocoTestDebugUnitTestReport/html/index.html")
        println("   📱 Instrumentation: build/reports/jacoco/jacocoConnectedDebugAndroidTestReport/html/index.html")
    }
}
