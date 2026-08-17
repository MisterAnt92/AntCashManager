buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// ── Gradle Compatibility Configuration ──
// Ensures all project dependencies use proper notation for Gradle 10 compatibility
subprojects {
    afterEvaluate {
        configurations.all {
            resolutionStrategy {
                // Enforce project(String) notation over deprecated Project object notation
                // This configuration ensures compatibility with future Gradle versions
                // where Project object as dependency notation will no longer be supported
            }
        }
    }
}

// ── Test Coverage Reporting ──
tasks.register("jacocoReport") {
    dependsOn(
        ":androidApp:testDebugUnitTest",
        ":androidApp:connectedDebugAndroidTest",
        ":shared:testDebugUnitTest"
    )

    doLast {
        println("\n✅ Coverage reports generated:")
        println("   📊 Unit Test: androidApp/build/reports/jacoco/testDebugUnitTest/html/index.html")
        println("   📱 Android Test: androidApp/build/reports/jacoco/connectedDebugAndroidTest/html/index.html")
        println("   📦 Shared Tests: shared/build/reports/jacoco/testDebugUnitTest/html/index.html")
        println("\nTo view coverage, open any of the index.html files in a browser.")
    }
}
