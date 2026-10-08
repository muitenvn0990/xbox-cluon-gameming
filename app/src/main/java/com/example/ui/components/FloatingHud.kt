package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DirectCyan
import com.example.ui.theme.JapanRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.XboxCardBorder
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxNeonGreen
import com.example.ui.theme.XboxSurface
import com.example.ui.theme.XboxSurfaceVariant
import com.example.vpn.NetworkMode
import com.example.vpn.VpnConnectionState
import kotlin.math.roundToInt

enum class HudMode {
    MINIMIZED,
    COMPACT,
    EXPANDED
}

@Composable
fun FloatingHud(
    vpnState: VpnConnectionState,
    isGameActive: Boolean,
    virtualControllerEnabled: Boolean,
    physicalControllerConnected: Boolean,
    controllerName: String,
    onToggleNetwork: () -> Unit,
    onToggleVirtualController: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateHome: () -> Unit,
    onReloadPage: () -> Unit,
    onToggleClarityBoost: (Boolean) -> Unit,
    clarityBoostActive: Boolean,
    modifier: Modifier = Modifier
) {
    var hudMode by remember { mutableStateOf(HudMode.COMPACT) }
    var offsetX by remember { mutableFloatStateOf(24f) }
    var offsetY by remember { mutableFloatStateOf(80f) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Surface(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .clip(RoundedCornerShape(20.dp))
                .border(
                    1.dp,
                    if (vpnState.mode == NetworkMode.DIRECT_NETWORK) DirectCyan.copy(alpha = 0.6f) else XboxNeonGreen.copy(alpha = 0.6f),
                    RoundedCornerShape(20.dp)
                )
                .testTag("floating_hud_container"),
            color = XboxSurface.copy(alpha = 0.94f),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // MINIMIZED MODE: Tiny Floating Pill
                if (hudMode == HudMode.MINIMIZED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clickable { hudMode = HudMode.COMPACT }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (vpnState.mode == NetworkMode.DIRECT_NETWORK) DirectCyan else JapanRed)
                        )
                        Text(
                            text = if (vpnState.mode == NetworkMode.DIRECT_NETWORK) "⚡ ${vpnState.directLatencyMs ?: 28}ms" else "🇯🇵 ${vpnState.latencyMs ?: 140}ms",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    // COMPACT / EXPANDED HEADER
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        // Latency & Network Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable {
                                hudMode = if (hudMode == HudMode.EXPANDED) HudMode.COMPACT else HudMode.EXPANDED
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (vpnState.mode) {
                                            NetworkMode.DIRECT_NETWORK -> DirectCyan
                                            NetworkMode.VPN_JAPAN -> JapanRed
                                            NetworkMode.DISCONNECTED -> Color.Gray
                                        }
                                    )
                            )

                            Text(
                                text = when (vpnState.mode) {
                                    NetworkMode.DIRECT_NETWORK -> "⚡ Direct (${vpnState.directLatencyMs ?: 28}ms)"
                                    NetworkMode.VPN_JAPAN -> "🇯🇵 VPN JP (${vpnState.latencyMs ?: 135}ms)"
                                    NetworkMode.DISCONNECTED -> "Mạng thường"
                                },
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Controller status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (physicalControllerConnected) XboxGreen.copy(alpha = 0.3f) else Color.Transparent
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = "Gamepad status",
                                tint = if (physicalControllerConnected) XboxNeonGreen else TextSecondary,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(16.dp)
                            )
                        }

                        // Minimize button
                        Text(
                            text = "─",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { hudMode = HudMode.MINIMIZED }
                                .padding(horizontal = 4.dp)
                        )
                    }

                    // COMPACT ACTION BAR
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick 1-tap network switcher
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleNetwork() }
                                .testTag("hud_toggle_network_button"),
                            color = if (vpnState.mode == NetworkMode.VPN_JAPAN) DirectCyan.copy(alpha = 0.2f) else JapanRed.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (vpnState.mode == NetworkMode.VPN_JAPAN) Icons.Default.Bolt else Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = if (vpnState.mode == NetworkMode.VPN_JAPAN) DirectCyan else JapanRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (vpnState.mode == NetworkMode.VPN_JAPAN) "⚡ Sang Direct" else "🇯🇵 Bật lại VPN",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Touch Gamepad toggle
                        IconButton(
                            onClick = onToggleVirtualController,
                            modifier = Modifier.size(32.dp).testTag("hud_toggle_controller_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = "Tay cầm ảo",
                                tint = if (virtualControllerEnabled) XboxNeonGreen else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Reload web
                        IconButton(
                            onClick = onReloadPage,
                            modifier = Modifier.size(32.dp).testTag("hud_reload_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Tải lại trang",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Expand diagnostics
                        IconButton(
                            onClick = {
                                hudMode = if (hudMode == HudMode.EXPANDED) HudMode.COMPACT else HudMode.EXPANDED
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Tùy chỉnh",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Dashboard Home
                        IconButton(
                            onClick = onNavigateHome,
                            modifier = Modifier.size(32.dp).testTag("hud_home_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Về Trang Chủ",
                                tint = XboxNeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // EXPANDED DIAGNOSTICS & TUNING PANEL
                    AnimatedVisibility(
                        visible = hudMode == HudMode.EXPANDED,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                                .width(240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Controller Info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(XboxSurfaceVariant)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "TAY CẦM", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (physicalControllerConnected) controllerName else if (virtualControllerEnabled) "Tay cầm ảo trên màn hình" else "Chưa kết nối",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (physicalControllerConnected || virtualControllerEnabled) XboxNeonGreen else Color.Gray)
                                )
                            }

                            // Clarity Boost Quick Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(XboxSurfaceVariant)
                                    .clickable { onToggleClarityBoost(!clarityBoostActive) }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "CLARITY BOOST (LÀM NÉT)", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (clarityBoostActive) "Đang bật (Tăng độ tương phản)" else "Đang tắt",
                                        color = if (clarityBoostActive) XboxNeonGreen else TextPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = if (clarityBoostActive) "BẬT" else "TẮT",
                                    color = if (clarityBoostActive) XboxNeonGreen else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Full Settings Button
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onOpenSettings() },
                                color = Color.Black.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Cài đặt nâng cao", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
