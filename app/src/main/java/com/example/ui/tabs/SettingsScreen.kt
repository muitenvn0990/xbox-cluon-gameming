package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppConfig
import com.example.data.SettingsRepository
import com.example.ui.theme.DirectCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.XboxCardBorder
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxNeonGreen
import com.example.ui.theme.XboxSurface
import com.example.ui.theme.XboxSurfaceVariant
import com.example.vpn.VpnConnectionState
import com.example.vpn.VpnEngine
import com.example.vpn.VpnManager

@Composable
fun SettingsScreen(
    config: AppConfig,
    vpnState: VpnConnectionState,
    onClearCookies: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Auto Bypass Logic
        item {
            SettingsCard(title = "CƠ CHẾ TỰ ĐỘNG CHUYỂN MẠNG (ZERO RELOAD)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Tự động ngắt VPN khi vào game", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Chuyển về mạng thường (Direct ISP) ngay khi luồng WebRTC kết nối, không reload trang", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = config.autoBypassEnabled,
                        onCheckedChange = { SettingsRepository.updateAutoBypass(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = XboxNeonGreen),
                        modifier = Modifier.testTag("settings_switch_auto_bypass")
                    )
                }

                if (config.autoBypassEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Thời gian đếm ngược trước khi ngắt VPN", color = TextSecondary, fontSize = 11.sp)
                            Text(text = "${config.autoBypassDelaySeconds} giây", color = DirectCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Slider(
                            value = config.autoBypassDelaySeconds.toFloat(),
                            onValueChange = { SettingsRepository.updateAutoBypassDelay(it.toInt()) },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = SliderDefaults.colors(thumbColor = DirectCyan, activeTrackColor = DirectCyan, inactiveTrackColor = Color.DarkGray)
                        )
                    }
                }
            }
        }

        // Section 2: Engine Selection
        item {
            SettingsCard(title = "CHẾ ĐỘ VƯỢT RÀO (VPN ENGINE)") {
                listOf(
                    Triple(VpnEngine.SMART_BYPASS, "Smart Japan Bypass 🇯🇵 (Khuyên dùng)", "Vượt rào trực tiếp Better-xCloud, 0 độ trễ, không sợ chết proxy"),
                    Triple(VpnEngine.PROXY_TUNNEL, "Proxy Route 🌐 (20+ Máy chủ)", "Định tuyến qua máy chủ Proxy Nhật Bản với tự động đổi máy chủ dự phòng"),
                    Triple(VpnEngine.EXTERNAL_VPN, "VPN Ngoài (1.1.1.1 / WARP / Kiwi)", "Dành cho người chơi dùng app VPN riêng")
                ).forEach { (engine, title, desc) ->
                    val isSelected = vpnState.engine == engine
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, if (isSelected) XboxNeonGreen else XboxCardBorder, RoundedCornerShape(10.dp))
                            .clickable { VpnManager.setEngine(engine) },
                        color = if (isSelected) XboxSurfaceVariant else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(text = desc, color = TextSecondary, fontSize = 10.sp)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = XboxNeonGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Stream Quality & Visuals
        item {
            SettingsCard(title = "CHẤT LƯỢNG HÌNH ẢNH & ĐIỀU KHIỂN") {
                // 1080p Edge UA
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Độ phân giải 1080p Full HD (Edge UA)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Giả lập Edge Desktop để Xbox stream ở 1080p thay vì 720p", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = config.forceDesktopUserAgent,
                        onCheckedChange = { SettingsRepository.updateDesktopUserAgent(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = XboxNeonGreen)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Clarity Boost
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Bộ lọc Clarity Boost (Làm nét)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Tăng độ sắc nét và tương phản video stream", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = config.clarityBoostEnabled,
                        onCheckedChange = { SettingsRepository.updateClarityBoost(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = XboxNeonGreen)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Virtual Touch Gamepad
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Tay cầm ảo trên màn hình", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Hiện D-Pad, cần Analog và nút ABXY khi không có tay cầm Bluetooth", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = config.showVirtualController,
                        onCheckedChange = { SettingsRepository.updateShowVirtualController(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = XboxNeonGreen)
                    )
                }
            }
        }

        // Section 4: Storage & Reset
        item {
            SettingsCard(title = "BỘ NHỚ ĐỆM & TÀI KHOẢN") {
                Button(
                    onClick = onClearCookies,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = XboxSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Text(text = "Xóa Cookie & Đăng Nhập Lại Xbox", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, XboxCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = XboxSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, color = XboxNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            content()
        }
    }
}
