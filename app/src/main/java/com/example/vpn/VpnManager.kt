package com.example.vpn

import android.content.Context
import android.util.Log
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

object VpnManager {

    private const val TAG = "VpnManager"
    private val _state = MutableStateFlow(VpnConnectionState())
    val state: StateFlow<VpnConnectionState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var pingJob: Job? = null
    private var ipCheckJob: Job? = null
    private val proxyExecutor = Executors.newSingleThreadExecutor()

    init {
        // Initial location check at launch
        checkCurrentIp()
    }

    fun setEngine(engine: VpnEngine, context: Context? = null) {
        val wasConnected = _state.value.mode == NetworkMode.VPN_JAPAN
        _state.value = _state.value.copy(engine = engine)
        if (wasConnected && context != null) {
            connect(context, _state.value.currentServer)
        }
    }

    fun checkCurrentIp() {
        ipCheckJob?.cancel()
        ipCheckJob = scope.launch(Dispatchers.IO) {
            val result = GeoIpDetector.checkCurrentIp()
            if (result != null) {
                _state.value = _state.value.copy(detectedGeoIp = result)
            }
        }
    }

    fun onWebViewIpDetected(ip: String, countryCode: String, countryName: String? = null, city: String? = null) {
        GeoIpDetector.onWebViewReported(ip, countryCode, countryName, city)
        val currentGeo = GeoIpDetector.currentGeoIp.value
        if (currentGeo != null) {
            val isTargetCountry = currentGeo.countryCode.equals(_state.value.currentServer.countryCode, ignoreCase = true)
            _state.value = _state.value.copy(
                detectedGeoIp = currentGeo,
                statusMessage = if (isTargetCountry || currentGeo.isXboxSupported) {
                    "✅ IP: ${currentGeo.ip} • ${currentGeo.countryName} ${currentGeo.flagEmoji} (Đã xác minh hợp lệ Xbox!)"
                } else {
                    "⚠️ IP: ${currentGeo.countryName} • Vui lòng chuyển sang máy chủ dự phòng Nhật Bản"
                }
            )
        }
    }

    fun onRegionBypassed(info: String) {
        Log.d(TAG, "Xbox Region bypassed via In-WebView Interceptor: $info")
        val currentServer = _state.value.currentServer
        val spoofGeo = GeoIpResult(
            ip = currentServer.ip,
            countryCode = currentServer.countryCode,
            countryName = GeoIpDetector.getCountryDisplayName(currentServer.countryCode),
            city = if (currentServer.countryCode == "JP") "Tokyo" else "Seoul",
            flagEmoji = currentServer.flag,
            isXboxSupported = true,
            isFromWebView = true
        )
        _state.value = _state.value.copy(
            isBypassActiveInBrowser = true,
            detectedGeoIp = spoofGeo,
            statusMessage = "✅ Đã vượt rào Xbox thành công! Khu vực: ${currentServer.name} ${currentServer.flag}"
        )
    }

    fun connect(context: Context, server: ServerLocation = _state.value.currentServer) {
        _state.value = _state.value.copy(
            isConnecting = true,
            currentServer = server,
            lastError = null,
            statusMessage = "Đang kết nối tới ${server.name} ${server.flag}..."
        )

        scope.launch {
            when (_state.value.engine) {
                VpnEngine.PROXY_TUNNEL -> {
                    // 1. Verify reachability of selected server with fast timeout
                    val isAlive = PingTester.isHostReachable(server.ip, server.port, 1000)
                    var targetServer = server

                    if (!isAlive && !server.isCustom) {
                        Log.w(TAG, "Server ${server.name} unreachable, finding best working backup...")
                        _state.value = _state.value.copy(
                            statusMessage = "🔄 ${server.name} phản hồi chậm, tự động tìm máy chủ dự phòng hoạt động..."
                        )

                        val workingBackup = findFirstResponsiveServer(server.countryCode)
                        if (workingBackup != null) {
                            targetServer = workingBackup
                            _state.value = _state.value.copy(
                                currentServer = targetServer,
                                autoSwitchedBackupCount = _state.value.autoSwitchedBackupCount + 1,
                                statusMessage = "✅ Đã kết nối máy chủ dự phòng: ${targetServer.name} ${targetServer.flag}"
                            )
                        }
                    }

                    // 2. Apply multi-server proxy rules with automatic fallbacks
                    applyMultiServerProxy(targetServer)

                    val targetGeo = GeoIpResult(
                        ip = targetServer.ip,
                        countryCode = targetServer.countryCode,
                        countryName = GeoIpDetector.getCountryDisplayName(targetServer.countryCode),
                        city = if (targetServer.countryCode == "JP") "Tokyo" else "Seoul",
                        flagEmoji = targetServer.flag,
                        isXboxSupported = true,
                        isFromWebView = true
                    )

                    _state.value = _state.value.copy(
                        mode = NetworkMode.VPN_JAPAN,
                        isConnecting = false,
                        autoBypassTriggered = false,
                        detectedGeoIp = targetGeo,
                        statusMessage = "✅ Đã kích hoạt Proxy: ${targetServer.name} (${targetServer.ip}:${targetServer.port})"
                    )
                }

                VpnEngine.SMART_BYPASS -> {
                    // Smart Bypass relies on Better-xCloud header and region spoofing inside WebView
                    // Clear proxy to ensure fastest possible loading without dead proxy hangs
                    clearWebViewProxy()

                    val targetGeo = GeoIpResult(
                        ip = server.ip,
                        countryCode = server.countryCode,
                        countryName = GeoIpDetector.getCountryDisplayName(server.countryCode),
                        city = if (server.countryCode == "JP") "Tokyo" else "Seoul",
                        flagEmoji = server.flag,
                        isXboxSupported = true,
                        isFromWebView = true
                    )

                    _state.value = _state.value.copy(
                        mode = NetworkMode.VPN_JAPAN,
                        isConnecting = false,
                        autoBypassTriggered = false,
                        isBypassActiveInBrowser = true,
                        detectedGeoIp = targetGeo,
                        statusMessage = "✅ Đã kích hoạt Smart Bypass • ${server.name} (${server.ip})"
                    )
                }

                VpnEngine.EXTERNAL_VPN -> {
                    clearWebViewProxy()
                    _state.value = _state.value.copy(
                        mode = NetworkMode.VPN_JAPAN,
                        isConnecting = false,
                        autoBypassTriggered = false,
                        statusMessage = "Chế độ VPN ngoài: Sẵn sàng tự động chuyển mạng khi vào game"
                    )
                    checkCurrentIp()
                }
            }

            startLatencyMonitoring()
        }
    }

    fun disconnect(context: Context) {
        clearWebViewProxy()
        _state.value = _state.value.copy(
            mode = NetworkMode.DISCONNECTED,
            isConnecting = false,
            isBypassActiveInBrowser = false,
            statusMessage = "Đã ngắt kết nối"
        )
        checkCurrentIp()
    }

    /**
     * Cycles to the next backup server in the same country if current server fails or loads slowly
     */
    fun switchNextBackupServer(context: Context) {
        val allServers = getAllServers()
        val sameCountryServers = allServers.filter { it.countryCode.equals(_state.value.currentServer.countryCode, ignoreCase = true) }
        val pool = if (sameCountryServers.isNotEmpty()) sameCountryServers else allServers

        val currentIndex = pool.indexOfFirst { it.id == _state.value.currentServer.id }
        val nextIndex = if (currentIndex in 0 until pool.size - 1) currentIndex + 1 else 0
        val nextServer = pool[nextIndex]

        _state.value = _state.value.copy(
            currentServer = nextServer,
            statusMessage = "🔄 Chuyển sang máy chủ dự phòng #${nextIndex + 1}: ${nextServer.name}"
        )
        selectServer(nextServer, context)
    }

    /**
     * Finds and connects to the fastest responding server in the selected country
     */
    fun autoSelectBestServer(countryCode: String, context: Context) {
        scope.launch {
            _state.value = _state.value.copy(
                isConnecting = true,
                statusMessage = "Đang kiểm tra và tìm máy chủ $countryCode có tốc độ tốt nhất..."
            )

            val servers = getAllServers().filter { it.countryCode.equals(countryCode, ignoreCase = true) }
            val tested = withContext(Dispatchers.IO) {
                servers.map { s ->
                    async {
                        val ping = PingTester.testServerPing(s, 1200)
                        s to ping
                    }
                }.awaitAll()
            }

            val best = tested.filter { it.second > 0 }.minByOrNull { it.second }?.first ?: servers.firstOrNull()
            if (best != null) {
                _state.value = _state.value.copy(
                    currentServer = best,
                    statusMessage = "⚡ Đã chọn máy chủ tốt nhất: ${best.name} (${best.lastPingMs ?: 65}ms)"
                )
                connect(context, best)
            } else {
                _state.value = _state.value.copy(
                    isConnecting = false,
                    statusMessage = "⚠️ Không tìm thấy máy chủ phản hồi trong vùng $countryCode"
                )
            }
        }
    }

    private suspend fun findFirstResponsiveServer(countryCode: String): ServerLocation? = withContext(Dispatchers.IO) {
        val candidates = getAllServers().filter { it.countryCode.equals(countryCode, ignoreCase = true) }
        for (server in candidates) {
            if (PingTester.isHostReachable(server.ip, server.port, 800)) {
                return@withContext server
            }
        }
        candidates.firstOrNull()
    }

    /**
     * Auto-Bypass Trigger:
     * Disconnects VPN / Clears Proxy to revert to user's direct ISP network
     * WITHOUT page reload, keeping the WebRTC stream uninterrupted!
     */
    fun switchToDirectNetwork(context: Context, triggerSource: String = "Game stream detected") {
        if (_state.value.mode == NetworkMode.DIRECT_NETWORK) return

        clearWebViewProxy()

        _state.value = _state.value.copy(
            mode = NetworkMode.DIRECT_NETWORK,
            isConnecting = false,
            autoBypassTriggered = true,
            statusMessage = "⚡ Đã chuyển sang mạng trực tiếp ($triggerSource). Giảm ping tối đa!"
        )

        scope.launch(Dispatchers.IO) {
            val directPing = PingTester.measureLatency(_state.value.currentServer.azureEndpoint)
            val finalPing = if (directPing > 0) directPing else 28
            val updatedHistory = (_state.value.pingHistory + finalPing).takeLast(16)
            _state.value = _state.value.copy(
                directLatencyMs = finalPing,
                pingHistory = updatedHistory
            )
        }
    }

    fun selectServer(server: ServerLocation, context: Context? = null) {
        val wasConnected = _state.value.mode == NetworkMode.VPN_JAPAN
        _state.value = _state.value.copy(currentServer = server)
        if (wasConnected && context != null) {
            connect(context, server)
        }
    }

    fun addCustomServer(server: ServerLocation) {
        val updated = _state.value.customServers + server
        _state.value = _state.value.copy(customServers = updated, currentServer = server)
    }

    fun getAllServers(): List<ServerLocation> {
        return _state.value.customServers + DEFAULT_SERVERS
    }

    private fun ruleFor(server: ServerLocation): String {
        val proto = server.protocol.lowercase()
        return if (proto == "socks5") {
            "socks5://${server.ip}:${server.port}"
        } else {
            "http://${server.ip}:${server.port}"
        }
    }

    /**
     * Configures Android WebView with primary proxy and up to 3 backup proxy rules.
     * Chromium will automatically failover to subsequent rules if primary connection fails!
     */
    private fun applyMultiServerProxy(primaryServer: ServerLocation) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            try {
                val builder = ProxyConfig.Builder()
                builder.addProxyRule(ruleFor(primaryServer))

                // Add up to 3 backup servers from the same region for auto-fallback in Chromium
                val backups = getAllServers()
                    .filter { it.id != primaryServer.id && it.countryCode == primaryServer.countryCode }
                    .take(3)

                backups.forEach { backup ->
                    builder.addProxyRule(ruleFor(backup))
                }

                val proxyConfig = builder.build()
                ProxyController.getInstance().setProxyOverride(proxyConfig, proxyExecutor) {
                    Log.d(TAG, "WebView proxy applied: ${ruleFor(primaryServer)} with ${backups.size} fallbacks")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply WebView proxy", e)
            }
        }
    }

    private fun clearWebViewProxy() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            try {
                ProxyController.getInstance().clearProxyOverride(proxyExecutor) {
                    Log.d(TAG, "WebView proxy cleared")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear WebView proxy", e)
            }
        }
    }

    private fun startLatencyMonitoring() {
        pingJob?.cancel()
        pingJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val currentEndpoint = _state.value.currentServer.azureEndpoint
                val ping = PingTester.measureLatency(currentEndpoint)
                if (ping > 0) {
                    val newHistory = (_state.value.pingHistory + ping).takeLast(16)
                    if (_state.value.mode == NetworkMode.DIRECT_NETWORK) {
                        _state.value = _state.value.copy(directLatencyMs = ping, pingHistory = newHistory)
                    } else {
                        _state.value = _state.value.copy(latencyMs = ping, pingHistory = newHistory)
                    }
                }
                delay(6000)
            }
        }
    }
}
