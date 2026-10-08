package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SettingsRepository
import com.example.ui.theme.DirectCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.XboxCardBorder
import com.example.ui.theme.XboxNeonGreen
import com.example.ui.theme.XboxSurface
import com.example.ui.theme.XboxSurfaceVariant
import com.example.vpn.DEFAULT_SERVERS
import com.example.vpn.ServerLocation
import com.example.vpn.VpnManager

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onClearCookies: () -> Unit
) {
    val config by SettingsRepository.config.collectAsState()
    val vpnState by VpnManager.state.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, XboxCardBorder, RoundedCornerShape(24.dp))
                .testTag("settings_dialog"),
            color = XboxSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = XboxNeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Cài đặt & Tối ưu hóa",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Auto-Switch Rules
                Text(
                    text = "CƠ CHẾ TỰ ĐỘNG CHUYỂN MẠNG",
                    color = XboxNeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Auto Bypass Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tự động ngắt VPN khi vào game",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Chuyển về mạng thường không cần reload trang khi WebRTC kết nối",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = config.autoBypassEnabled,
                        onCheckedChange = { SettingsRepository.updateAutoBypass(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = XboxNeonGreen
                        ),
                        modifier = Modifier.testTag("switch_auto_bypass")
                    )
                }

                // Delay Slider
                if (config.autoBypassEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(XboxSurfaceVariant)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Thời gian chờ trước khi ngắt VPN",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${config.autoBypassDelaySeconds} giây",
                                color = DirectCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Slider(
                            value = config.autoBypassDelaySeconds.toFloat(),
                            onValueChange = { SettingsRepository.updateAutoBypassDelay(it.toInt()) },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = DirectCyan,
                                activeTrackColor = DirectCyan,
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("slider_bypass_delay")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: VPN Engine
                Text(
                    text = "CHẾ ĐỘ VƯỢT RÀO (VPN ENGINE)",
                    color = XboxNeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    Triple(com.example.vpn.VpnEngine.SMART_BYPASS, "Smart Japan Bypass 🇯🇵 (Khuyên dùng)", "Vượt rào trực tiếp Better-xCloud, 0 độ trễ, không sợ chết proxy"),
                    Triple(com.example.vpn.VpnEngine.PROXY_TUNNEL, "Proxy Route 🌐 (20+ Máy chủ)", "Định tuyến qua máy chủ Proxy Nhật Bản với tự động đổi máy chủ dự phòng"),
                    Triple(com.example.vpn.VpnEngine.EXTERNAL_VPN, "VPN Ngoài (1.1.1.1 / WARP / Kiwi)", "Dành cho người chơi dùng app VPN riêng, app sẽ hỗ trợ nhắc ngắt khi vào game")
                ).forEach { (engine, title, desc) ->
                    val isSelected = vpnState.engine == engine
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) XboxNeonGreen else XboxCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { VpnManager.setEngine(engine) },
                        color = if (isSelected) XboxSurfaceVariant else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = desc,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = XboxNeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Preferred Region / VPN Server
                Text(
                    text = "MÁY CHỦ KHU VỰC VƯỢT RÀO",
                    color = XboxNeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                DEFAULT_SERVERS.forEach { server ->
                    val isSelected = vpnState.currentServer.id == server.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) XboxNeonGreen else XboxCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { VpnManager.selectServer(server) },
                        color = if (isSelected) XboxSurfaceVariant else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = server.flag, fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = server.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = server.description,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = XboxNeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Visual & Controls
                Text(
                    text = "HÌNH ẢNH & ĐIỀU KHIỂN",
                    color = XboxNeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Desktop 1080p UA Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kích hoạt luồng 1080p (Edge UA)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Giả lập trình duyệt Edge Desktop để Xbox stream ở chất lượng Full HD",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = config.forceDesktopUserAgent,
                        onCheckedChange = { SettingsRepository.updateDesktopUserAgent(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = XboxNeonGreen
                        )
                    )
                }

                // Clarity Boost
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bộ lọc Clarity Boost (Tăng độ nét)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Khử nhòe và tăng độ sắc nét cho hình ảnh stream",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = config.clarityBoostEnabled,
                        onCheckedChange = { SettingsRepository.updateClarityBoost(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = XboxNeonGreen
                        )
                    )
                }

                // Virtual Controller Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tay cầm ảo trên màn hình (Touch)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Hiển thị các nút điều khiển ảo khi không có tay cầm Bluetooth",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = config.showVirtualController,
                        onCheckedChange = { SettingsRepository.updateShowVirtualController(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = XboxNeonGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Clear Cache Button
                Button(
                    onClick = onClearCookies,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("clear_cookies_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = XboxSurfaceVariant)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = TextSecondary)
                        Text(text = "Xóa Cookie & Đăng nhập lại Xbox", color = TextSecondary)
                    }
                }
            }
        }
    }
}
