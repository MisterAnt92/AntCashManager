plugins {
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-progressive",
        )
    }
    explicitApi()
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kermit)
}
