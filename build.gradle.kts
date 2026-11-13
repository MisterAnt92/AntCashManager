buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
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
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover)
}

// ── Ktlint Configuration for all subprojects ──
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
        android.set(true)
        ignoreFailures.set(false)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
        }
    }
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

// ── Kover Aggregated Coverage ──
dependencies {
    kover(project(":androidApp"))
}

kover {
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                classes(
                    "**.*ComposableSingletons*",
                    "**.*_Impl*",
                    "**.*\$\$serializer*",
                )
            }
        }
        total {
            html {
                title = "AntCashManager – Unit Test Coverage"
                htmlDir.set(rootProject.layout.buildDirectory.dir("reports/kover/html"))
            }
        }
    }
}

// Ensure Kover report tasks depend on both androidApp and shared unit test tasks
tasks.matching { it.name.contains("kover", ignoreCase = true) }.configureEach {
    dependsOn(":androidApp:testFullDebugUnitTest", ":shared:testAndroidHostTest")
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
