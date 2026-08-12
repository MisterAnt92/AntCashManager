import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

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
    toolVersion = "0.8.10"
}

android {
    namespace = "com.antcashmanager.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sformica.ant_cashmanager"
        minSdk = 26
        targetSdk = 37
        versionCode = 19
        versionName = "1.7.0"
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
                "proguard-rules.pro"
            )
            configure<CrashlyticsExtension> {
                // Upload automatico del mapping file per deobfuscation stacktrace release.
                mappingFileUploadEnabled = true
            }
        }
    }
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
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
            )
        }
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.compose.ui.test.junit4)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
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
    androidTestImplementation(libs.androidx.junit.ext)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Android Test Orchestrator for better test isolation and parallelism
    androidTestUtil("androidx.test:orchestrator:1.4.2")
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
                "**/*Test*.*"
            )
        }
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
                "**/*Test*.*"
            )
        }
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
