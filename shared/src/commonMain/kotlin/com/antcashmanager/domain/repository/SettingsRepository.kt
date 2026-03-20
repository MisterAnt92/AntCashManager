package com.antcashmanager.domain.repository

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
    fun getLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
    fun getShowCharts(): Flow<Boolean>
    suspend fun setShowCharts(show: Boolean)
    fun getHighContrast(): Flow<Boolean>
    suspend fun setHighContrast(enabled: Boolean)
    fun getLargeText(): Flow<Boolean>
    suspend fun setLargeText(enabled: Boolean)
    fun getReduceMotion(): Flow<Boolean>
    suspend fun setReduceMotion(enabled: Boolean)
}
