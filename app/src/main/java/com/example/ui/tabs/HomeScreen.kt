package com.example.ui.tabs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.widget.Toast
import com.example.data.SettingsRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.cloudplay.GamepadStatus
import com.example.data.GameBookmark
import com.example.ui.components.PingVisualizerCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPink
import com.example.ui.theme.DirectCyan
import com.example.ui.theme.JapanRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.XboxCardBorder
import com.example.ui.theme.XboxDark
import com.example.ui.theme.XboxGlassBorder
import com.example.ui.theme.XboxGlassSurface
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxNeonGlow
import com.example.ui.theme.XboxNeonGreen
import com.example.ui.theme.XboxSurface
import com.example.ui.theme.XboxSurfaceVariant
import com.example.vpn.DEFAULT_SERVERS
import com.example.vpn.GeoIpResult
import com.example.vpn.NetworkMode
import com.example.vpn.ServerLocation
import com.example.vpn.SmartVpnDecision
import com.example.vpn.SmartVpnPhase
import com.example.vpn.VpnConnectionState
import com.example.vpn.VpnEngine
import com.example.vpn.VpnManager

@Composable
fun HomeScreen(
    vpnState: VpnConnectionState,
    gamepadStatus: GamepadStatus,
    featuredGames: List<GameBookmark>,
    onConnectVpn: () -> Unit,
    onDisconnectVpn: () -> Unit,
    onSwitchEngine: (VpnEngine) -> Unit,
    onLaunchGame: (String) -> Unit,
    onOpenXboxCloud: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val config by SettingsRepository.config.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Pulse VPN & Direct ISP Status Card (Hero Deck)
        LivePulseStatusCard(
            vpnState = vpnState,
            onConnectVpn = onConnectVpn,
            onDisconnectVpn = onDisconnectVpn,
            onSwitchEngine = onSwitchEngine,
            onOpenXboxCloud = onOpenXboxCloud
        )

        // 2. Cuộc Cách Mạng Hệ Thống 1.0 (System Revolution 1.0 Deck)
        SystemRevolutionStatusCard(
            config = config,
            onOptimizeSystem = {
                SettingsRepository.optimizeEntireSystem()
                VpnManager.checkCurrentIp()
                Toast.makeText(context, "⚡ Cuộc cách mạng hệ thống 1.0: Đã kích hoạt toàn bộ 5 tầng tối ưu!", Toast.LENGTH_SHORT).show()
            }
        )

        // 3. Real-time Live IP & Country Verification Radar (Smart VPN Cockpit)
        LiveIpVerifierCard(
            geoIp = vpnState.detectedGeoIp,
            smartDecision = vpnState.smartDecision,
            smartPhase = vpnState.smartVpnPhase,
            currentServer = vpnState.currentServer,
            onRefreshIp = { VpnManager.checkCurrentIp() },
            onSwitchNextServer = { VpnManager.switchNextBackupServer(context) }
        )

        // 3. Hardware Controller Telemetry Banner
        HardwareControllerBanner(gamepadStatus = gamepadStatus)

        // 4. Featured Games Showcase Carousel
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp, 16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(XboxNeonGreen)
                    )
                    Text(
                        text = "GAME NỔI BẬT CLOUD",
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToLibrary() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Xem tất cả",
                        color = XboxNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = XboxNeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(featuredGames.take(6)) { game ->
                    FeaturedGameCard(
                        game = game,
                        onPlay = { onLaunchGame(game.playUrl) }
                    )
                }
            }
        }

        // 5. Live Ping Chart & Performance Telemetry
        val pingToDisplay = if (vpnState.mode == NetworkMode.DIRECT_NETWORK) {
            vpnState.directLatencyMs ?: 28
        } else {
            vpnState.latencyMs ?: 140
        }

        PingVisualizerCard(
            currentPing = pingToDisplay,
            history = vpnState.pingHistory,
            mode = vpnState.mode
        )

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun SystemRevolutionStatusCard(
    config: com.example.data.AppConfig,
    onOptimizeSystem: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(DirectCyan.copy(alpha = 0.8f), XboxNeonGreen.copy(alpha = 0.8f), DirectCyan.copy(alpha = 0.8f))
                ),
                RoundedCornerShape(20.dp)
            )
            .testTag("system_revolution_card"),
        colors = CardDefaults.cardColors(containerColor = XboxSurface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DirectCyan.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = DirectCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CUỘC CÁCH MẠNG HỆ THỐNG 1.0",
                            color = DirectCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Tối Ưu Siêu Cấp 5 Tầng • 0ms VPN Lag",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = XboxNeonGreen.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, XboxNeonGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "5/5 ACTIVE ⚡",
                        color = XboxNeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // 5-Layer Showcase Pills
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                RevolutionLayerRow(
                    layerNum = "1",
                    title = "Stealth Header Spoofing",
                    desc = "Vượt rào kiểm tra vùng Microsoft, mượn IP Tokyo ngầm",
                    isActive = config.stealthHeadersEnabled
                )
                RevolutionLayerRow(
                    layerNum = "2",
                    title = "Mesh Proxy Auto-Healing",
                    desc = "Tự động đổi node dự phòng trong 300ms nếu trễ >150ms",
                    isActive = config.autoHealingProxyMesh
                )
                RevolutionLayerRow(
                    layerNum = "3",
                    title = "WebRTC SDP Bitrate Booster",
                    desc = "Mở khóa 1080p 60FPS / 15Mbps, âm thanh Stereo 128k",
                    isActive = config.sdpBitrateBoostEnabled
                )
                RevolutionLayerRow(
                    layerNum = "4",
                    title = "Zero-Reload Direct Handshake",
                    desc = "Tắt VPN ngay khi vào trận, trả về mạng Viettel/VNPT/FPT",
                    isActive = config.zeroDelayDirectHandshake
                )
                RevolutionLayerRow(
                    layerNum = "5",
                    title = "Anti-Japanese Enforcer",
                    desc = "Ép hiển thị ${config.getEffectiveLocale()}, không bị ép tiếng Nhật",
                    isActive = config.antiJapaneseEnforcer
                )
            }

            // One-Tap Quick Optimize Button
            Button(
                onClick = onOptimizeSystem,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 42.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DirectCyan
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "KÍCH HOẠT TỐI ƯU CÁCH MẠNG 1.0 (1 CHẠM)",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun RevolutionLayerRow(
    layerNum: String,
    title: String,
    desc: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isActive) DirectCyan.copy(alpha = 0.2f) else Color.DarkGray
            ) {
                Text(
                    text = layerNum,
                    color = if (isActive) DirectCyan else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = desc,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isActive) XboxNeonGreen else Color.DarkGray,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun LiveIpVerifierCard(
    geoIp: GeoIpResult?,
    smartDecision: SmartVpnDecision,
    smartPhase: SmartVpnPhase,
    currentServer: ServerLocation,
    onRefreshIp: () -> Unit,
    onSwitchNextServer: () -> Unit
) {
    val isSupported = smartDecision.isSupportedRegion
    val statusColor = if (isSupported) DirectCyan else (if (smartDecision.needsVpn) XboxNeonGreen else DirectCyan)
    val allServers = VpnManager.getAllServers()
    val serverIndex = allServers.indexOfFirst { it.id == currentServer.id }.coerceAtLeast(0) + 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                statusColor.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp)
            )
            .testTag("live_ip_verifier_card"),
        colors = CardDefaults.cardColors(containerColor = XboxSurfaceVariant.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSupported) Icons.Default.CheckCircle else Icons.Default.Bolt,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "TRỢ LÝ VPN THÔNG MINH",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (isSupported) "HỢP LỆ XBOX • ${smartDecision.userCountryCode}" else "CẦN SMART VPN • ${smartDecision.userCountryCode}",
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // IP & Location details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "IP Người Chơi:",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = geoIp?.ip ?: "Đang quét...",
                            color = DirectCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Vị trí: ${smartDecision.userCountryName} ${geoIp?.flagEmoji ?: "🇻🇳"}${if (geoIp?.city?.isNotBlank() == true) " (${geoIp.city})" else ""}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = XboxGreen.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, XboxGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Node #$serverIndex",
                        color = XboxNeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Smart Recommendation Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = if (isSupported) DirectCyan.copy(alpha = 0.12f) else XboxNeonGreen.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isSupported) DirectCyan.copy(alpha = 0.3f) else XboxNeonGreen.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isSupported) DirectCyan else XboxNeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = smartDecision.message,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Visual Lifecycle Workflow Indicator: Stage 1 (Bật) -> Stage 2 (Tắt)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isStage1Active = smartPhase == SmartVpnPhase.BYPASS_ACTIVE
                val isStage2Active = smartPhase == SmartVpnPhase.DIRECT_GAMING || smartPhase == SmartVpnPhase.DIRECT_SUPPORTED_REGION

                // Step 1: Open Game -> Auto ON
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isStage1Active) JapanRed.copy(alpha = 0.25f) else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isStage1Active) JapanRed else Color.DarkGray
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "1. MỞ GAME 🚀",
                            color = if (isStage1Active) JapanRed else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (smartDecision.needsVpn) "BẬT VPN 🇯🇵" else "DIRECT ⚡",
                            color = if (isStage1Active) Color.White else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "➔",
                    color = XboxNeonGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                // Step 2: Stream Connect -> Auto OFF
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isStage2Active) DirectCyan.copy(alpha = 0.25f) else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isStage2Active) DirectCyan else Color.DarkGray
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "2. VÀO TRẬN 🎮",
                            color = if (isStage2Active) DirectCyan else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "TẮT VPN ⚡ (0ms lag)",
                            color = if (isStage2Active) Color.White else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Dual action buttons: Change backup server + Refresh IP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSwitchNextServer,
                    modifier = Modifier
                        .weight(1.2f)
                        .defaultMinSize(minHeight = 40.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = XboxGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Text(
                            "Đổi Server Dự Phòng",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = onRefreshIp,
                    modifier = Modifier
                        .weight(0.8f)
                        .defaultMinSize(minHeight = 40.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                        Text(
                            "Quét Lại IP",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LivePulseStatusCard(
    vpnState: VpnConnectionState,
    onConnectVpn: () -> Unit,
    onDisconnectVpn: () -> Unit,
    onSwitchEngine: (VpnEngine) -> Unit,
    onOpenXboxCloud: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val activeColor = when (vpnState.mode) {
        NetworkMode.VPN_JAPAN -> JapanRed
        NetworkMode.DIRECT_NETWORK -> DirectCyan
        NetworkMode.DISCONNECTED -> Color.Gray
    }

    val glowBrush = Brush.linearGradient(
        colors = when (vpnState.mode) {
            NetworkMode.DIRECT_NETWORK -> listOf(DirectCyan.copy(alpha = 0.8f), XboxNeonGreen.copy(alpha = 0.6f), DirectCyan.copy(alpha = 0.8f))
            NetworkMode.VPN_JAPAN -> listOf(JapanRed.copy(alpha = 0.8f), WarningAmber.copy(alpha = 0.6f), JapanRed.copy(alpha = 0.8f))
            NetworkMode.DISCONNECTED -> listOf(XboxNeonGreen.copy(alpha = 0.5f), Color(0xFF1B3828), XboxCardBorder)
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.5.dp, glowBrush, RoundedCornerShape(22.dp))
            .testTag("vpn_status_hero_card"),
        colors = CardDefaults.cardColors(containerColor = XboxSurface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Engine Selector Tabs with Sleek Glass Pill Design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EngineTabItem(
                    label = "Smart Bypass 🇯🇵",
                    sub = "0 Lag • Khuyên dùng",
                    selected = vpnState.engine == VpnEngine.SMART_BYPASS,
                    onClick = { onSwitchEngine(VpnEngine.SMART_BYPASS) },
                    modifier = Modifier.weight(1f)
                )
                EngineTabItem(
                    label = "Proxy Route 🌐",
                    sub = "20+ Server Live",
                    selected = vpnState.engine == VpnEngine.PROXY_TUNNEL,
                    onClick = { onSwitchEngine(VpnEngine.PROXY_TUNNEL) },
                    modifier = Modifier.weight(1f)
                )
                EngineTabItem(
                    label = "VPN Ngoài 🛡️",
                    sub = "WARP / Wireguard",
                    selected = vpnState.engine == VpnEngine.EXTERNAL_VPN,
                    onClick = { onSwitchEngine(VpnEngine.EXTERNAL_VPN) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Status Row with Animated Pulse Ring & Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (vpnState.mode != NetworkMode.DISCONNECTED) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(activeColor.copy(alpha = 0.3f))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (vpnState.smartVpnPhase) {
                                SmartVpnPhase.BYPASS_ACTIVE -> "Giai đoạn 1: Vượt Rào (VPN Bật) 🇯🇵"
                                SmartVpnPhase.DIRECT_GAMING -> "Giai đoạn 2: Vào Trận (Direct ISP) ⚡"
                                SmartVpnPhase.DIRECT_SUPPORTED_REGION -> "Mạng Trực Tiếp (Vùng Đã Hỗ Trợ) ⚡"
                                SmartVpnPhase.IDLE -> if (vpnState.mode == NetworkMode.VPN_JAPAN) "VPN Nhật Bản Đang Bật 🇯🇵" else "Smart VPN Sẵn Sàng 🤖"
                            },
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = vpnState.statusMessage,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (vpnState.mode) {
                        NetworkMode.DIRECT_NETWORK -> DirectCyan.copy(alpha = 0.18f)
                        NetworkMode.VPN_JAPAN -> XboxNeonGreen.copy(alpha = 0.18f)
                        NetworkMode.DISCONNECTED -> Color.DarkGray.copy(alpha = 0.5f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (vpnState.mode) {
                            NetworkMode.DIRECT_NETWORK -> DirectCyan.copy(alpha = 0.4f)
                            NetworkMode.VPN_JAPAN -> XboxNeonGreen.copy(alpha = 0.4f)
                            NetworkMode.DISCONNECTED -> Color.Gray.copy(alpha = 0.3f)
                        }
                    )
                ) {
                    Text(
                        text = when (vpnState.smartVpnPhase) {
                            SmartVpnPhase.DIRECT_GAMING -> "⚡ ZERO LAG"
                            SmartVpnPhase.BYPASS_ACTIVE -> "✓ VƯỢT RÀO OK"
                            SmartVpnPhase.DIRECT_SUPPORTED_REGION -> "⚡ DIRECT"
                            SmartVpnPhase.IDLE -> "AUTO-SMART"
                        },
                        color = when (vpnState.mode) {
                            NetworkMode.DIRECT_NETWORK -> DirectCyan
                            NetworkMode.VPN_JAPAN -> XboxNeonGreen
                            NetworkMode.DISCONNECTED -> Color.LightGray
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Primary Action: Large Neon "CHƠI XBOX CLOUD NGAY" button + Toggle VPN
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // High-impact Play Button
                Button(
                    onClick = onOpenXboxCloud,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("button_open_xbox_cloud"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = XboxGreen
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (vpnState.smartDecision.needsVpn) "CHƠI XBOX CLOUD (SMART VPN)" else "CHƠI XBOX CLOUD (MẠNG TRỰC TIẾP)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (vpnState.smartDecision.needsVpn) "Tự động: Bật lúc mở ➔ Tắt khi vào trận" else "IP đã hợp lệ (${vpnState.smartDecision.userCountryName}) • 0ms VPN",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Secondary VPN toggle button
                Button(
                    onClick = {
                        if (vpnState.mode == NetworkMode.VPN_JAPAN) onDisconnectVpn() else onConnectVpn()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 42.dp)
                        .testTag("button_toggle_vpn"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vpnState.mode == NetworkMode.VPN_JAPAN) Color(0xFF3B1E22) else Color.Black.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (vpnState.mode == NetworkMode.VPN_JAPAN) JapanRed.copy(alpha = 0.5f) else XboxCardBorder
                    )
                ) {
                    if (vpnState.isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = XboxNeonGreen,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (vpnState.mode == NetworkMode.VPN_JAPAN) Icons.Default.VpnKey else Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (vpnState.mode == NetworkMode.VPN_JAPAN) JapanRed else XboxNeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (vpnState.mode == NetworkMode.VPN_JAPAN) "Tắt VPN (Chuyển Mạng Trực Tiếp)" else "Bật VPN Nhật Bản Thủ Công",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (vpnState.mode == NetworkMode.VPN_JAPAN) JapanRed else TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineTabItem(
    label: String,
    sub: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(1.dp),
        color = if (selected) XboxGreen else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = if (selected) Color.White else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                color = if (selected) Color.White.copy(alpha = 0.9f) else Color.Gray,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HardwareControllerBanner(gamepadStatus: GamepadStatus) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, XboxCardBorder, RoundedCornerShape(14.dp)),
        color = XboxSurfaceVariant.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (gamepadStatus.hasPhysicalController) XboxNeonGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = null,
                        tint = if (gamepadStatus.hasPhysicalController) XboxNeonGreen else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (gamepadStatus.hasPhysicalController) "Tay Cầm: ${gamepadStatus.controllerName}" else "Tay Cầm Ảo / Cảm Ứng Sẵn Sàng",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (gamepadStatus.hasPhysicalController) "Kết nối Bluetooth/USB ổn định • Không delay" else "Hỗ trợ nút bấm ảo trên màn hình & vuốt chạm mượt mà",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (gamepadStatus.hasPhysicalController) XboxNeonGreen else DirectCyan)
            )
        }
    }
}

@Composable
private fun FeaturedGameCard(
    game: GameBookmark,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, XboxCardBorder, RoundedCornerShape(18.dp))
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = XboxSurface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (game.drawableResId != null) {
                Image(
                    painter = painterResource(id = game.drawableResId),
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(game.accentColorHex), XboxDark)
                            )
                        )
                )
            }

            // Glass gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 50f
                        )
                    )
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(12.dp))
                        Text(text = game.rating, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (game.freeToPlay) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = XboxGreen
                    ) {
                        Text(
                            text = "FREE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (game.badge != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DirectCyan.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = game.badge,
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Bottom Title & Play Trigger
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = game.title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = game.category,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = CircleShape,
                        color = XboxNeonGreen,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Chơi",
                            tint = Color.Black,
                            modifier = Modifier
                                .padding(5.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
