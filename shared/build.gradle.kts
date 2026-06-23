plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "com.antcashmanager.shared"
        compileSdk = 37
        minSdk = 26
        withHostTest {}

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kermit)
                implementation(libs.datastore.preferences.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.datastore.preferences)
                api(libs.room.runtime)
                implementation(libs.room.ktx)
            }
        }
        val commonTest by getting {
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
