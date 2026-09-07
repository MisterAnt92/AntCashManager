# ==============================================================================
# ProGuard Rules for LITE Variant (No Google Drive Backup)
# ==============================================================================
# These rules apply ONLY to the 'lite' build variant
# They exclude Google Drive API dependencies that are not used in lite builds

# ── LITE Variant: No Google Drive API ──
# When INCLUDE_DRIVE_BACKUP = false, Google Drive classes are not referenced
# R8 will remove them during shrinking, but we can be explicit:

# Remove all Google Drive API classes (not referenced in lite variant)
-dontwarn com.google.api.services.drive.**
-dontwarn com.google.api.client.**
-dontwarn com.google.oauth.client.**

# Remove unused HTTP client classes
-dontwarn com.google.http.client.**
-dontwarn org.apache.http.**

# Remove Jackson dependencies (pulled by google-http-client)
-dontwarn com.fasterxml.jackson.**

# Remove Guava (pulled by google-api-client)
-dontwarn com.google.common.**

# ── R8 Aggressive Shrinking ──
# For lite variant, allow even more aggressive shrinking
# (same as production, but with explicit Drive API removal)
-optimizationpasses 8
-repackageclasses 'com.antcashmanager.opt'

# ── Keep only essential classes ──
# Keep everything from main proguard-rules.pro
# These rules will be merged with base rules
