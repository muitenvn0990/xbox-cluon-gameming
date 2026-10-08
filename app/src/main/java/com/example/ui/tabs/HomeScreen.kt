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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.DirectCyan
import com.example.ui.theme.JapanRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.XboxCardBorder
import com.example.ui.theme.XboxDark
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxNeonGreen
import com.example.ui.theme.XboxSurface
import com.example.ui.theme.XboxSurfaceVariant
import com.example.vpn.DEFAULT_SERVERS
import com.example.vpn.GeoIpResult
import com.example.vpn.NetworkMode
import com.example.vpn.ServerLocation
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Pulse VPN & Direct ISP Status Card
        LivePulseStatusCard(
            vpnState = vpnState,
            onConnectVpn = onConnectVpn,
            onDisconnectVpn = onDisconnectVpn,
            onSwitchEngine = onSwitchEngine,
            onOpenXboxCloud = onOpenXboxCloud
        )

        // 2. Real-time Live IP & Country Verification Card
        LiveIpVerifierCard(
            geoIp = vpnState.detectedGeoIp,
            currentServer = vpnState.currentServer,
            onRefreshIp = { VpnManager.checkCurrentIp() },
            onSwitchNextServer = { VpnManager.switchNextBackupServer(context) }
        )

        // 3. Hardware Controller Diagnostic
        HardwareControllerBanner(gamepadStatus = gamepadStatus)

        // 4. Featured Games Showcase Carousel
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "GAME NỔI BẬT TRÊN CLOUD",
                        color = XboxNeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .clickable { onNavigateToLibrary() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Xem tất cả",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(featuredGames.take(5)) { game ->
                    FeaturedGameCard(
                        game = game,
                        onPlay = { onLaunchGame(game.playUrl) }
                    )
                }
            }
        }

        // 5. Live Ping Chart & Performance Metric
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

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun LiveIpVerifierCard(
    geoIp: GeoIpResult?,
    currentServer: ServerLocation,
    onRefreshIp: () -> Unit,
    onSwitchNextServer: () -> Unit
) {
    val isJapanOrSupported = geoIp?.isXboxSupported == true
    val statusColor = if (isJapanOrSupported) XboxNeonGreen else WarningAmber
    val allServers = VpnManager.getAllServers()
    val serverIndex = allServers.indexOfFirst { it.id == currentServer.id }.coerceAtLeast(0) + 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isJapanOrSupported) XboxNeonGreen.copy(alpha = 0.6f) else WarningAmber.copy(alpha = 0.6f),
                RoundedCornerShape(16.dp)
            )
            .testTag("live_ip_verifier_card"),
        colors = CardDefaults.cardColors(containerColor = XboxSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isJapanOrSupported) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "KIỂM TRA IP & KHU VỰC THỰC TẾ",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isJapanOrSupported) "HỢP LỆ XBOX • ${geoIp?.countryCode}" else "CHƯA QUA NHẬT • ${geoIp?.countryCode ?: "VN"}",
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // IP & Location details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "IP Ra Ngoài: ${geoIp?.ip ?: "Đang quét..."}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vị trí: ${geoIp?.countryName ?: "Đang kiểm tra"} ${geoIp?.flagEmoji ?: ""} ${if (geoIp?.city?.isNotBlank() == true) "(${geoIp.city})" else ""}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Server #$serverIndex/${allServers.size}",
                    color = DirectCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Dual action buttons: Change backup server + Refresh IP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSwitchNextServer,
                    modifier = Modifier.weight(1.3f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = XboxGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("Đổi Server Dự Phòng 🔄", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onRefreshIp,
                    modifier = Modifier.weight(0.9f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Text("Quét lại", color = TextPrimary, fontSize = 11.sp)
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
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val activeColor = when (vpnState.mode) {
        NetworkMode.VPN_JAPAN -> JapanRed
        NetworkMode.DIRECT_NETWORK -> DirectCyan
        NetworkMode.DISCONNECTED -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                if (vpnState.mode == NetworkMode.DIRECT_NETWORK) DirectCyan.copy(alpha = 0.5f) else XboxCardBorder,
                RoundedCornerShape(20.dp)
            )
            .testTag("vpn_status_hero_card"),
        colors = CardDefaults.cardColors(containerColor = XboxSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Engine Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EngineTabItem(
                    label = "Smart Bypass 🇯🇵",
                    sub = "Khuyên dùng • 0 Lag",
                    selected = vpnState.engine == VpnEngine.SMART_BYPASS,
                    onClick = { onSwitchEngine(VpnEngine.SMART_BYPASS) },
                    modifier = Modifier.weight(1f)
                )
                EngineTabItem(
                    label = "Proxy Route 🌐",
                    sub = "20+ Máy chủ",
                    selected = vpnState.engine == VpnEngine.PROXY_TUNNEL,
                    onClick = { onSwitchEngine(VpnEngine.PROXY_TUNNEL) },
                    modifier = Modifier.weight(1f)
                )
                EngineTabItem(
                    label = "VPN Ngoài 🛡️",
                    sub = "1.1.1.1 / WARP",
                    selected = vpnState.engine == VpnEngine.EXTERNAL_VPN,
                    onClick = { onSwitchEngine(VpnEngine.EXTERNAL_VPN) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Status Row with Animated Pulse Ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (vpnState.mode != NetworkMode.DISCONNECTED) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(activeColor.copy(alpha = 0.35f))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                        )
                    }

                    Column {
                        Text(
                            text = when (vpnState.mode) {
                                NetworkMode.VPN_JAPAN -> "VPN Nhật Bản Đang Bật 🇯🇵"
                                NetworkMode.DIRECT_NETWORK -> "Mạng Trực Tiếp (Direct ISP) ⚡"
                                NetworkMode.DISCONNECTED -> "Chưa Bật VPN"
                            },
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = vpnState.statusMessage,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (vpnState.mode) {
                        NetworkMode.DIRECT_NETWORK -> DirectCyan.copy(alpha = 0.18f)
                        NetworkMode.VPN_JAPAN -> XboxNeonGreen.copy(alpha = 0.18f)
                        NetworkMode.DISCONNECTED -> Color.DarkGray.copy(alpha = 0.5f)
                    }
                ) {
                    Text(
                        text = when (vpnState.mode) {
                            NetworkMode.DIRECT_NETWORK -> "Ping Thấp Nhất"
                            NetworkMode.VPN_JAPAN -> "Vượt Rào OK"
                            NetworkMode.DISCONNECTED -> "Cần VPN để vào"
                        },
                        color = when (vpnState.mode) {
                            NetworkMode.DIRECT_NETWORK -> DirectCyan
                            NetworkMode.VPN_JAPAN -> XboxNeonGreen
                            NetworkMode.DISCONNECTED -> Color.LightGray
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (vpnState.mode == NetworkMode.VPN_JAPAN) onDisconnectVpn() else onConnectVpn()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("button_toggle_vpn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vpnState.mode == NetworkMode.VPN_JAPAN) Color(0xFF332024) else XboxSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
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
                                text = if (vpnState.mode == NetworkMode.VPN_JAPAN) "Tắt VPN" else "Bật VPN JP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Button(
                    onClick = onOpenXboxCloud,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
                        .testTag("button_open_xbox_cloud"),
                    colors = ButtonDefaults.buttonColors(containerColor = XboxGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Mở Xbox Cloud",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(1.dp),
        color = if (selected) XboxGreen else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = if (selected) Color.White else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = sub,
                color = if (selected) Color.White.copy(alpha = 0.85f) else Color.Gray,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun HardwareControllerBanner(gamepadStatus: GamepadStatus) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, XboxCardBorder, RoundedCornerShape(12.dp)),
        color = XboxSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = null,
                    tint = if (gamepadStatus.hasPhysicalController) XboxNeonGreen else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = if (gamepadStatus.hasPhysicalController) "Tay cầm: ${gamepadStatus.controllerName}" else "Chưa cắm tay cầm Bluetooth",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (gamepadStatus.hasPhysicalController) "Sẵn sàng chiến game 100%" else "Bật tay cầm ảo trên màn hình hoặc cắm tay cầm",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (gamepadStatus.hasPhysicalController) XboxNeonGreen else WarningAmber)
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
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, XboxCardBorder, RoundedCornerShape(16.dp))
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 60f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(11.dp))
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
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = game.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
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
                        fontSize = 10.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = CircleShape,
                        color = XboxNeonGreen
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Chơi",
                            tint = Color.Black,
                            modifier = Modifier
                                .padding(5.dp)
                                .size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
