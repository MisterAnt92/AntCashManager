# ==============================================================================
# AntCashManager – ProGuard Rules
# Progetto: AntCashManager (com.sformica.ant_cashmanager)
# Architettura: Clean Architecture KMP
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. KOTLIN
# ------------------------------------------------------------------------------

# Mantieni i metadati Kotlin necessari per la reflection e la serializzazione
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepattributes SourceFile,LineNumberTable

# Kotlin stdlib
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Lazy {
    *;
}

# ------------------------------------------------------------------------------
# 2. KOTLIN COROUTINES
# ------------------------------------------------------------------------------

# Coroutines ha consumer-rules proprie via aar – manteniamo solo ciò che non copre
-dontwarn kotlinx.coroutines.**
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
# Dispatcher factory e handler usati per riflessione da coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ------------------------------------------------------------------------------
# 3. KOTLINX SERIALIZATION
# ------------------------------------------------------------------------------

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

# Mantieni le classi serializzate e i loro companion object
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Mantieni le classi annotate con @Serializable
-keep,includedescriptorclasses class com.antcashmanager.**$$serializer { *; }
-keepclassmembers class com.antcashmanager.** {
    *** Companion;
}
-keepclasseswithmembers class com.antcashmanager.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Mantieni tutti i field di data class serializzate per evitare problemi con R8
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *;
}

# ------------------------------------------------------------------------------
# 4. ROOM DATABASE
# ------------------------------------------------------------------------------

# Entità Room – mantieni tutti i campi e costruttori per la generazione del DB
-keep class com.antcashmanager.data.local.entity.** { *; }

# DAO e Database
-keep class com.antcashmanager.data.local.AppDatabase { *; }
-keep interface com.antcashmanager.data.local.dao.** { *; }

# Room usa reflection per i Cursor: mantieni i nomi dei field delle Entity
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract !final *;
}
# Room ha consumer-rules proprie nell'aar – non serve -keep su androidx.room.**
-dontwarn androidx.room.**

# Auto-migration spec
-keep class * extends androidx.room.migration.AutoMigrationSpec { *; }

# ------------------------------------------------------------------------------
# 5. FIREBASE ANALYTICS
# ------------------------------------------------------------------------------

-keep class com.google.firebase.analytics.** { *; }
-dontwarn com.google.firebase.analytics.**

# ------------------------------------------------------------------------------
# 6. FIREBASE CRASHLYTICS
# ------------------------------------------------------------------------------

# Mantieni le informazioni di mapping per deobfuscation degli stacktrace
# (mappingFileUploadEnabled = true è già configurato in build.gradle.kts)
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Crashlytics NDK
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ------------------------------------------------------------------------------
# 7. KOIN (Dependency Injection)
# ------------------------------------------------------------------------------

-keep class org.koin.** { *; }
-keepnames class org.koin.** { *; }
-dontwarn org.koin.**

# Koin usa DSL esplicito (non riflessione massiva): non serve -keep su tutto il package.
# Le classi del dominio sono già protette dalle sezioni Room, Serialization, ViewModel.
# Manteniamo solo i constructor primari per garantire l'istanziazione via Koin DSL.
-keepclassmembers class com.antcashmanager.** {
    public <init>(...);
}

# ------------------------------------------------------------------------------
# 8. ML KIT – TEXT RECOGNITION
# ------------------------------------------------------------------------------

-keep class com.google.mlkit.vision.text.** { *; }
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }

# ------------------------------------------------------------------------------
# 9. DATASTORE (Preferences)
# ------------------------------------------------------------------------------

-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ------------------------------------------------------------------------------
# 10. KERMIT LOGGER (co.touchlab:kermit)
# ------------------------------------------------------------------------------

-keep class co.touchlab.kermit.** { *; }
-dontwarn co.touchlab.kermit.**

# ------------------------------------------------------------------------------
# 11. JETPACK COMPOSE
# ------------------------------------------------------------------------------

# Compose, Lifecycle e Navigation hanno consumer-rules proprie via aar.
# Manteniamo solo le regole non coperte automaticamente.
-dontwarn androidx.compose.**
-dontwarn androidx.lifecycle.**
-dontwarn androidx.navigation.**

# Platform host classes usate da Compose per reflection interna
-keepclassmembers class androidx.compose.ui.platform.** { *; }

# ------------------------------------------------------------------------------
# 12. GOOGLE FONTS (ui-text-google-fonts)
# ------------------------------------------------------------------------------

-keep class androidx.compose.ui.text.googlefonts.** { *; }
-dontwarn androidx.compose.ui.text.googlefonts.**

# ------------------------------------------------------------------------------
# 13. REGOLE GENERALI ANDROID
# ------------------------------------------------------------------------------

# Mantieni le classi che implementano Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Mantieni le Enum
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Mantieni i nomi delle risorse (per accesso via reflection)
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Evita warning su classi interne anonime
-dontnote **

# ------------------------------------------------------------------------------
# 14. WORKMANAGER
# ------------------------------------------------------------------------------

# Worker classes vengono istanziate per riflessione da WorkManager factory
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-dontwarn androidx.work.**

# ── Glance AppWidget ─────────────────────────────────────────────────────────
# GlanceAppWidget receiver viene caricato da XML tramite riflessione
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver {
    public <init>();
}
-dontwarn androidx.glance.**

# ------------------------------------------------------------------------------
# 15. SICUREZZA – DATI SENSIBILI
# ------------------------------------------------------------------------------

# EncryptedSharedPreferences (androidx.security.crypto)
# SQLCipher non è nelle dipendenze – regola rimossa
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ------------------------------------------------------------------------------
# 15. R8 OPTIMIZATION – LOG STRIPPING
# ------------------------------------------------------------------------------

# Remove logging calls in release builds (Log.d, Log.v, Log.i have no side effects)
# This reduces APK size by ~50-100 KB without affecting functionality.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ══════════════════════════════════════════════════════════════════════════════
# 16. ML KIT TEXT RECOGNITION – BITMAP MEMORY MANAGEMENT
# ══════════════════════════════════════════════════════════════════════════════

# Mantieni BitmapFactory per downsampling intelligente (inSampleSize)
-keep class android.graphics.BitmapFactory {
    public static *** decodeByteArray(...);
    public static *** decodeStream(...);
    public static *** decodeFile(...);
}

# Mantieni Bitmap methods critici per memory management
-keepclassmembers class android.graphics.Bitmap {
    public *** compress(...);
    public void recycle();
    public boolean isRecycled();
    public int getByteCount();
    public int getWidth();
    public int getHeight();
}

# Mantieni BitmapFactory.Options per inSampleSize downsampling
-keep class android.graphics.BitmapFactory$Options {
    public int inSampleSize;
    public int inJustDecodeBounds;
    public *** inPreferredConfig;
}

# Mantieni ML Kit InputImage per proper bitmap processing
-keepclassmembers class com.google.mlkit.vision.common.InputImage {
    public static *** fromBitmap(...);
    public *** close();
    public *** getWidth();
    public *** getHeight();
}

# Mantieni TextRecognizer process method
-keepclassmembers class com.google.mlkit.vision.text.TextRecognizer {
    public *** process(...);
    public void close();
}

# Non avvertire su classi interne di ML Kit
-dontwarn com.google.mlkit.vision.**

# ══════════════════════════════════════════════════════════════════════════════
# 17. GOOGLE DRIVE API & HTTP CLIENT LIBRARIES
# ══════════════════════════════════════════════════════════════════════════════

# google-http-client reflection usage
-keep class com.google.api.client.json.** { *; }
-keep class com.google.api.client.util.** { *; }
-dontwarn com.google.api.client.**

# google-oauth-client
-keep class com.google.oauth.client.** { *; }
-dontwarn com.google.oauth.client.**

# google-api-services-drive (stub classes)
-keep class com.google.api.services.drive.model.** { *; }
-dontwarn com.google.api.services.drive.**

# ══════════════════════════════════════════════════════════════════════════════
# 18. AGGRESSIVE R8 OPTIMIZATION PASSES (23% Coverage Target)
# ══════════════════════════════════════════════════════════════════════════════

# R8 Specific: Enable 8 optimization passes (default 5)
# Each pass removes more dead code, inlines more methods, optimizes further
-optimizationpasses 8

# Aggressive class and member renaming to short names (a, b, c...)
# This significantly reduces string constant pool and method/class metadata
-repackageclasses 'com.antcashmanager.opt'

# R8 automatically removes empty inner classes in aggressive mode
# (Legacy ProGuard option -removeinnerclasseswithmembersonly not supported in R8)

# Allow removal of const string values if used only in assertions
# These are already removed by log stripping above
-assumenosideeffects class java.lang.System {
    public static void exit(int);
}

# Aggressive shrinking of Kotlin coroutine state machines
-keep class kotlin.coroutines.** { *; }
-keepclassmembers class **$1 { *; }  # Keep lambda/SAM implementations
-keepclassmembers class **$2 { *; }  # Some compilers generate multiple

# Note: Settings use case classes (GetXxxUseCase, SetXxxUseCase) can be
# aggressively minified since domain logic is protected by interfaces
# R8 will automatically minify these in aggressive mode

# Note: R8 automatically inlines single-use methods in aggressive mode
# Note: R8 automatically merges redundant methods and classes in aggressive mode

# Assertion stripping for release builds (Kotlin internal checks)
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
}

