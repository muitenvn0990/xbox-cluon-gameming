package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppConfig(
    val autoBypassEnabled: Boolean = true,
    val autoBypassDelaySeconds: Int = 3,
    val autoReconnectOnMenu: Boolean = true,
    val forceDesktopUserAgent: Boolean = true,
    val clarityBoostEnabled: Boolean = true,
    val showFloatingHud: Boolean = true,
    val showVirtualController: Boolean = false,
    val targetRegionUrl: String = "https://www.xbox.com/ja-JP/play",
    val preferredServerId: String = "jp_tokyo_linode_1"
)

object SettingsRepository {
    private val _config = MutableStateFlow(AppConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    fun getUrlForCountry(countryCode: String): String {
        return when (countryCode.uppercase()) {
            "JP" -> "https://www.xbox.com/ja-JP/play"
            "KR" -> "https://www.xbox.com/ko-KR/play"
            "SG" -> "https://www.xbox.com/en-SG/play"
            "US" -> "https://www.xbox.com/en-US/play"
            else -> "https://www.xbox.com/ja-JP/play"
        }
    }

    fun adaptPlayUrl(url: String, countryCode: String): String {
        val targetLocale = when (countryCode.uppercase()) {
            "JP" -> "ja-JP"
            "KR" -> "ko-KR"
            "SG" -> "en-SG"
            "US" -> "en-US"
            else -> "ja-JP"
        }
        return if (url.contains("/en-US/")) {
            url.replace("/en-US/", "/$targetLocale/")
        } else {
            url
        }
    }

    fun updateAutoBypass(enabled: Boolean) {
        _config.value = _config.value.copy(autoBypassEnabled = enabled)
    }

    fun updateAutoBypassDelay(seconds: Int) {
        _config.value = _config.value.copy(autoBypassDelaySeconds = seconds)
    }

    fun updateDesktopUserAgent(enabled: Boolean) {
        _config.value = _config.value.copy(forceDesktopUserAgent = enabled)
    }

    fun updateClarityBoost(enabled: Boolean) {
        _config.value = _config.value.copy(clarityBoostEnabled = enabled)
    }

    fun updateShowVirtualController(enabled: Boolean) {
        _config.value = _config.value.copy(showVirtualController = enabled)
    }

    fun updateTargetRegionUrl(url: String) {
        _config.value = _config.value.copy(targetRegionUrl = url)
    }
}
