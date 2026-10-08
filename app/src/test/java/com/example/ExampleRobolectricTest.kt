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
}
