import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    id("jacoco")
}

// ── Jacoco Configuration ──
jacoco {
    toolVersion = "0.8.13"
}

kotlin {
    android {
        namespace = "com.antcashmanager.shared"
        compileSdk = 37
        minSdk = 26
        withHostTest {}

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            // Kotlin compiler optimizations
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-progressive",
                "-Xjvm-default=all", // Inline interface methods
                "-Xstring-concat=inline", // Optimize string concatenation
            )
            // Release-only aggressive optimizations (when building in CI)
            if (System.getenv("CI") != null) {
                freeCompilerArgs.addAll(
                    "-Xno-param-assertions", // Remove parameter null checks
                    "-Xno-call-assertions", // Remove call assertions
                    "-Xno-receiver-assertions", // Remove receiver null checks
                )
            }
        }
    }

    // Enforce explicit API visibility for better tree-shaking and clearer API design
    explicitApi()

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kermit)
                implementation(libs.datastore.preferences.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
            }
        }
        getByName("androidMain") {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.datastore.preferences)
                api(libs.room.runtime)
                implementation(libs.room.ktx)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.mockk)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Test parallelism configuration
tasks.withType<Test> {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

// ── Jacoco Coverage Report Task ──
tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        fileTree("${layout.buildDirectory}/classes/kotlin/android/main") {
            exclude(
                "**/R.class",
                "**/R\$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
            )
        },
    )

    sourceDirectories.setFrom(
        files(
            "src/androidMain/kotlin",
            "src/commonMain/kotlin",
        ),
    )
    executionData.setFrom(
        fileTree("${layout.buildDirectory}") {
            include("jacoco/*.exec")
        },
    )
}

// Convenience task
tasks.register("testCoverageReport") {
    dependsOn("jacocoTestReport")
    doLast {
        println("\n✅ Shared module Jacoco coverage report generated:")
        println("   📦 Report: build/reports/jacoco/jacocoTestReport/html/index.html")
    }
}
