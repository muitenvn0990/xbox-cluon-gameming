package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.cloudplay.StreamController
import com.example.data.SettingsRepository
import com.example.vpn.NetworkMode
import com.example.vpn.VpnManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CloudPlay Switcher", appName)
    }

    @Test
    fun `verify default vpn state and settings`() {
        val vpnState = VpnManager.state.value
        assertEquals(NetworkMode.DISCONNECTED, vpnState.mode)
        assertNotNull(vpnState.currentServer)
        assertTrue(vpnState.currentServer.name.contains("Japan"))

        val config = SettingsRepository.config.value
        assertTrue(config.autoBypassEnabled)
        assertEquals(3, config.autoBypassDelaySeconds)
    }

    @Test
    fun `verify stream controller auto bypass trigger`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = StreamController(context)

        controller.onGameLaunchInitiated("Fortnite")
        assertEquals(true, controller.uiState.value.isGameActive)
        assertEquals("Fortnite", controller.uiState.value.gameTitle)
    }

    @Test
    fun `verify xbox locale parsing and fallback to en-US`() {
        assertEquals("en-US", SettingsRepository.parseLocaleFromInput("xbox.com/en-US"))
        assertEquals("en-US", SettingsRepository.parseLocaleFromInput("https://www.xbox.com/en-US/play"))
        assertEquals("en-GB", SettingsRepository.parseLocaleFromInput("en-GB"))
        assertEquals("ko-KR", SettingsRepository.parseLocaleFromInput("ko-KR"))
        assertEquals("ja-JP", SettingsRepository.parseLocaleFromInput("ja-JP"))

        // Format Xbox URLs with locale
        val originalUrl = "https://www.xbox.com/ja-JP/play/games/forza"
        val adaptedEn = SettingsRepository.formatXboxUrlWithLocale(originalUrl, "en-US")
        assertEquals("https://www.xbox.com/en-US/play/games/forza", adaptedEn)

        val noLocaleUrl = "https://www.xbox.com/play"
        val adaptedKo = SettingsRepository.formatXboxUrlWithLocale(noLocaleUrl, "ko-KR")
        assertEquals("https://www.xbox.com/ko-KR/play", adaptedKo)

        // Custom locale configuration
        SettingsRepository.updateCustomLocale("en-US")
        assertEquals("en-US", SettingsRepository.config.value.getEffectiveLocale())
        assertEquals("https://www.xbox.com/en-US/play", SettingsRepository.config.value.targetRegionUrl)
    }
}
