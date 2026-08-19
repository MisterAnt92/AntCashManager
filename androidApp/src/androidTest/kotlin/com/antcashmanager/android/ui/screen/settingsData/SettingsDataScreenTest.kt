package com.antcashmanager.android.ui.screen.settingsData

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.antcashmanager.android.R
import com.antcashmanager.android.test.base.BaseInstrumentationTest
import com.antcashmanager.android.ui.screen.settings.dataManagement.SettingsDataContent
import com.antcashmanager.android.ui.screen.settings.dataManagement.SettingsDataState
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDataScreenTest : BaseInstrumentationTest() {

    @Test
    fun restoreData_shouldReportError_whenFilePickerUnavailable() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val restoreLabel = ctx.getString(R.string.settings_restore)
        val unavailableMessage = ctx.getString(R.string.settings_data_file_picker_unavailable)
        var receivedError = ""

        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                AntCashManagerTheme {
                    SettingsDataContent(
                        state = SettingsDataState(),
                        onRestoreFileReadError = { receivedError = it },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(restoreLabel).performClick()
        composeTestRule.waitForIdle()

        assertEquals(unavailableMessage, receivedError)
    }

    @Test
    fun backupData_shouldReportError_whenPendingBackupAndFilePickerUnavailable() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val unavailableMessage = ctx.getString(R.string.settings_data_file_picker_unavailable)
        var receivedError = ""

        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                AntCashManagerTheme {
                    SettingsDataContent(
                        state = SettingsDataState(
                            pendingBackupData = "{\"version\":1}",
                            pendingBackupFileName = "antcashmanager_backup_test.json",
                        ),
                        onBackupFileSaveError = { receivedError = it },
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        assertEquals(unavailableMessage, receivedError)
    }
}

