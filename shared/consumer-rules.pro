# ==============================================================================
# AntCashManager – Consumer Rules per il modulo :shared (KMP)
# Vengono automaticamente applicate a tutti i moduli che dipendono da :shared
# ==============================================================================

# ------------------------------------------------------------------------------
# Kotlinx Serialization – Classi @Serializable del dominio
# ------------------------------------------------------------------------------

# Mantieni companion object e KSerializer delle classi serializzabili del dominio
-keepclassmembers @kotlinx.serialization.Serializable class com.antcashmanager.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# Mantieni i serializer generati dal plugin
-keep,includedescriptorclasses class com.antcashmanager.**$$serializer { *; }

# ------------------------------------------------------------------------------
# Repository Interfaces (Koin DI)
# ------------------------------------------------------------------------------

# R8 può rimuovere interfacce "inutilizzate" in full mode – proteggi le interfaces
# del dominio che vengono risolte da Koin tramite il tipo dell'interfaccia
-keepnames interface com.antcashmanager.domain.repository.**
-keepnames interface com.antcashmanager.domain.usecase.**

# ------------------------------------------------------------------------------
# Room – Entity e DAO Interfaces
# ------------------------------------------------------------------------------

# Room genera implementazioni per riflessione – i DAO devono mantenere i nomi
-keepnames @androidx.room.Entity class **
-keepnames @androidx.room.Dao interface **
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
