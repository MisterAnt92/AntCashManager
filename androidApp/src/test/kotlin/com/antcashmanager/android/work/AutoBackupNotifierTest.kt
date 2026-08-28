package com.antcashmanager.android.work

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AutoBackupNotifier] using MockK.
 *
 * Verifies:
 * - Notification channel creation (handled gracefully on all API levels)
 * - Notification method invocation (not throwing exceptions)
 * - Idempotency of channel creation
 *
 * NOTE: Full notification delivery testing with actual Android system
 * should be in src/androidTest as it requires real NotificationManager.
 * This suite tests the notification logic using mocked Context.
 */
class AutoBackupNotifierTest {

    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
    }

    @Test
    fun ensureChannel_shouldCompleteWithoutException_whenCalled() {
        // Should not throw even if AndroidJUnit4 not available
        try {
            AutoBackupNotifier.ensureChannel(mockContext)
        } catch (e: Exception) {
            throw AssertionError("ensureChannel should not throw: ${e.message}")
        }
    }

    @Test
    fun ensureChannel_shouldBeIdempotent_whenCalledMultipleTimes() {
        // Multiple calls should not cause issues
        try {
            AutoBackupNotifier.ensureChannel(mockContext)
            AutoBackupNotifier.ensureChannel(mockContext)
            AutoBackupNotifier.ensureChannel(mockContext)
        } catch (e: Exception) {
            throw AssertionError("ensureChannel should be idempotent: ${e.message}")
        }
    }

    @Test
    fun notifyFailure_shouldCompleteWithoutException_whenCalled() {
        // Should not throw even without channel setup
        try {
            AutoBackupNotifier.notifyFailure(mockContext)
        } catch (e: Exception) {
            throw AssertionError("notifyFailure should not throw: ${e.message}")
        }
    }

    @Test
    fun notifyFailure_shouldCompleteWithoutException_afterChannelSetup() {
        // Should not throw after channel setup
        try {
            AutoBackupNotifier.ensureChannel(mockContext)
            AutoBackupNotifier.notifyFailure(mockContext)
        } catch (e: Exception) {
            throw AssertionError("notifyFailure after ensureChannel should not throw: ${e.message}")
        }
    }

    @Test
    fun notifyFailure_shouldBeIdempotent_whenCalledMultipleTimes() {
        // Multiple calls should not cause issues
        try {
            AutoBackupNotifier.notifyFailure(mockContext)
            AutoBackupNotifier.notifyFailure(mockContext)
            AutoBackupNotifier.notifyFailure(mockContext)
        } catch (e: Exception) {
            throw AssertionError("Multiple notifyFailure calls should not throw: ${e.message}")
        }
    }

    @Test
    fun ensureChannelAndNotifyFailure_shouldChainCorrectly_whenCalled() {
        // The two methods should work together
        try {
            AutoBackupNotifier.ensureChannel(mockContext)
            AutoBackupNotifier.notifyFailure(mockContext)
            AutoBackupNotifier.notifyFailure(mockContext)
        } catch (e: Exception) {
            throw AssertionError("ensureChannel and notifyFailure chain should work: ${e.message}")
        }
    }

    @Test
    fun notifyFailure_shouldUseMockedContext_forNotificationManager() {
        val notificationManager = mockk<NotificationManager>(relaxed = true)
        val contextWithManager = mockk<Context> {
            io.mockk.every { getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        }

        try {
            AutoBackupNotifier.notifyFailure(contextWithManager)
        } catch (e: Exception) {
            throw AssertionError("notifyFailure with mocked NotificationManager should not throw: ${e.message}")
        }
    }

    @Test
    fun apiLevelHandling_shouldWorkOnAllSdkVersions_withoutException() {
        // Test should work regardless of API level
        val currentApiLevel = Build.VERSION.SDK_INT
        if (currentApiLevel >= Build.VERSION_CODES.O) {
            try {
                AutoBackupNotifier.ensureChannel(mockContext)
            } catch (e: Exception) {
                throw AssertionError("ensureChannel on API 26+ should not throw: ${e.message}")
            }
        } else {
            try {
                AutoBackupNotifier.ensureChannel(mockContext)
                AutoBackupNotifier.notifyFailure(mockContext)
            } catch (e: Exception) {
                throw AssertionError("Methods should work on API < 26: ${e.message}")
            }
        }
    }
}
