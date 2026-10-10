package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

enum class GuideSection(val title: String, val icon: ImageVector) {
    QUICK_START("Bắt Đầu Nhanh", Icons.Default.RocketLaunch),
    ZERO_LAG("Giảm Ping 0ms Lag", Icons.Default.Speed),
    CONTROLLER("Tay Cầm & Cảm Ứng", Icons.Default.Gamepad),
    FAQS("Khắc Phục Lỗi", Icons.Default.Lightbulb)
}

@Composable
fun HowToUseGuideDialog(
    onDismiss: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(GuideSection.QUICK_START) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    1.5.dp,
                    Brush.verticalGradient(listOf(XboxNeonGreen, DirectCyan, XboxCardBorder)),
                    RoundedCornerShape(24.dp)
                )
                .testTag("how_to_use_guide_dialog"),
            color = XboxDark.copy(alpha = 0.98f),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DirectCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = DirectCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "HƯỚNG DẪN CÁCH DÙNG",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Cẩm nang chơi Xbox Cloud 0ms lag tại Việt Nam",
                                color = XboxNeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Section Tabs Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(XboxSurfaceVariant.copy(alpha = 0.8f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GuideSection.entries.forEach { section ->
                        val isSelected = selectedSection == section
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedSection = section }
                                .padding(1.dp),
                            color = if (isSelected) XboxGreen else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = section.title,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Scrollable Content per Section
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedSection) {
                        GuideSection.QUICK_START -> {
                            item {
                                GuideStepCard(
                                    stepNum = "1",
                                    badge = "Tài khoản",
                                    title = "Chuẩn bị tài khoản chơi game",
                                    desc = "• Nếu đã có tài khoản Xbox Game Pass Ultimate: Bạn có thể chơi hàng trăm game như Forza Horizon 5, Starfield, GTA V...\n• Nếu CHƯA có Game Pass: Game FORTNITE hoàn toàn MIỄN PHÍ 100%! Chỉ cần có tài khoản Microsoft thường là chơi được ngay mà không tốn tiền."
                                )
                            }
                            item {
                                GuideStepCard(
                                    stepNum = "2",
                                    badge = "Vượt rào",
                                    title = "Bấm 'CHƠI XBOX CLOUD' hoặc chọn game",
                                    desc = "• Bạn không cần tự bật VPN thủ công!\n• Hệ thống Smart VPN của ứng dụng sẽ tự động mượn IP Nhật Bản để Microsoft cấp quyền truy cập phòng chờ game (bypass kiểm tra vùng)."
                                )
                            }
                            item {
                                GuideStepCard(
                                    stepNum = "3",
                                    badge = "0ms Lag",
                                    title = "Tự động tắt VPN khi vào trận",
                                    desc = "• Ngay khi màn hình game bắt đầu chạy luồng video WebRTC, hệ thống sẽ TỰ ĐỘNG TẮT VPN và chuyển về mạng nhà (Direct ISP Viettel/VNPT/FPT).\n• Trang web KHÔNG reload, luồng game giữ nguyên 100%, Ping tụt từ ~140ms xuống chỉ còn 25-35ms cực mượt mà!"
                                )
                            }
                            item {
                                GuideStepCard(
                                    stepNum = "4",
                                    badge = "Thanh HUD",
                                    title = "Dùng thanh điều khiển nổi (Floating HUD)",
                                    desc = "• Trong lúc chơi, thanh HUD tròn nổi trên góc màn hình cho phép bạn: Đổi mạng tức thì, bật/tắt tay cầm ảo, làm nét Clarity Boost và quay về Trang Chủ bất kỳ lúc nào."
                                )
                            }
                        }

                        GuideSection.ZERO_LAG -> {
                            item {
                                GuideDetailCard(
                                    icon = Icons.Default.Bolt,
                                    iconColor = DirectCyan,
                                    title = "Cơ chế Zero-Reload Direct Network",
                                    content = "Bình thường nếu chơi qua VPN, toàn bộ dữ liệu game nặng phải chạy vòng qua máy chủ Tokyo khiến ping cao (~140ms) và giật lag. Ứng dụng này chỉ dùng VPN lúc bấm nút Play để vượt qua bước duyệt IP của Microsoft, sau đó ngắt kết nối VPN để luồng video truyền trực tiếp về điện thoại bạn."
                                )
                            }
                            item {
                                GuideDetailCard(
                                    icon = Icons.Default.Wifi,
                                    iconColor = XboxNeonGreen,
                                    title = "Mẹo chọn mạng Internet tối ưu",
                                    content = "• Khuyên dùng Wifi băng tần 5GHz hoặc mạng 4G/5G tốc độ cao.\n• Tránh dùng Wifi 2.4GHz vì dễ bị nhiễu sóng với Bluetooth của tay cầm, gây tụt khung hình hoặc nhảy ping.\n• Nên ngồi gần Router Wifi để đường truyền ổn định."
                                )
                            }
                            item {
                                GuideDetailCard(
                                    icon = Icons.Default.Speed,
                                    iconColor = WarningAmber,
                                    title = "Bật bộ lọc Clarity Boost (Làm Nét)",
                                    content = "Vào tab Cài Đặt hoặc mở thanh HUD trong game -> Bật 'Clarity Boost'. Bộ lọc thông minh sẽ tăng độ tương phản và độ sắc nét giúp hình ảnh 1080p sắc sảo như đang chơi trên máy console thật."
                                )
                            }
                        }

                        GuideSection.CONTROLLER -> {
                            item {
                                GuideDetailCard(
                                    icon = Icons.Default.Gamepad,
                                    iconColor = XboxNeonGreen,
                                    title = "Kết nối tay cầm Bluetooth / Type-C",
                                    content = "• Hỗ trợ 100%: Tay cầm Xbox One/Series, PS4 DualShock 4, PS5 DualSense, Gamesir, Flydigi, EasySMX...\n• Bật Bluetooth trên điện thoại -> Ghép đôi tay cầm -> Đèn báo tay cầm trên ứng dụng sẽ chuyển màu Xanh lá cây báo hiệu sẵn sàng."
                                )
                            }
                            item {
                                GuideDetailCard(
                                    icon = Icons.Default.CheckCircle,
                                    iconColor = DirectCyan,
                                    title = "Chơi bằng Tay Cầm Ảo trên màn hình",
                                    content = "• Nếu không có tay cầm vật lý, vào Cài Đặt -> Bật công tắc 'Tay cầm ảo trên màn hình'.\n• Hệ thống sẽ hiển thị cần xoay Analog 360°, cụm nút ABXY, D-Pad và các nút cò LT/RT/LB/RB với độ phản hồi rung haptic chân thực."
                                )
                            }
                        }

                        GuideSection.FAQS -> {
                            item {
                                GuideDetailCard(
                                    icon = Icons.Default.Language,
                                    iconColor = JapanRed,
                                    title = "Bị ép hiển thị tiếng Nhật thì làm sao?",
                                    content = "Mở tab 'Cài Đặt' -> Tại mục Ngôn Ngữ, chọn 'Tiếng Anh (en-US)' hoặc ngôn ngữ máy -> Bấm 'Áp dụng'. Sau đó kéo xuống dưới cùng bấm 'Xóa Cookie & Đăng Nhập Lại'. Giao diện sẽ vĩnh viễn chuyển sang tiếng Anh dễ đọc!"
                                )
                            }
                            item {
                                GuideDetailCard(
                                    icon = Icons.Default.Speed,
                                    iconColor = WarningAmber,
                                    title = "Máy chủ phản hồi chậm hoặc báo lỗi vùng?",
                                    content = "Chuyển sang tab 'Máy Chủ' -> Bấm nút 'Kiểm Tra Ping' hoặc 'Tải Proxy Sống' để cập nhật danh sách máy chủ mới nhất. Bạn cũng có thể bấm 'Đổi Server Dự Phòng' ngay trên Trang Chủ."
                                )
                            }
                            item {
                                GuideDetailCard(
                                    icon = Icons.Default.Bolt,
                                    iconColor = DirectCyan,
                                    title = "Game bị đứng hình hoặc màn hình đen?",
                                    content = "Chạm vào thanh HUD nổi trên màn hình -> Nhấn biểu tượng 🔄 'Tải Lại Trang'. Hệ thống sẽ tự động bắt tay lại với máy chủ Xbox mà không cần thoát ứng dụng."
                                )
                            }
                        }
                    }
                }

                // Bottom Action
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = XboxGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Đã Hiểu • Bắt Đầu Chơi Ngay 🎮",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideStepCard(
    stepNum: String,
    badge: String,
    title: String,
    desc: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, XboxCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = XboxSurface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(XboxNeonGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNum,
                            color = XboxNeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DirectCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge,
                        color = DirectCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = desc,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GuideDetailCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, XboxCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = XboxSurface.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = content,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
