package com.example.ui

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cloudplay.GamepadDetector
import com.example.data.GameLibraryRepository
import com.example.data.SettingsRepository
import com.example.ui.tabs.GameLibraryScreen
import com.example.ui.tabs.HomeScreen
import com.example.ui.tabs.ServersScreen
import com.example.ui.tabs.SettingsScreen
import com.example.ui.theme.DirectCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.XboxDark
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxNeonGreen
import com.example.ui.theme.XboxSurface
import com.example.ui.theme.XboxSurfaceVariant
import com.example.vpn.VpnEngine
import com.example.vpn.VpnManager

enum class DashboardTab {
    HOME,
    LIBRARY,
    SERVERS,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLaunchUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vpnState by VpnManager.state.collectAsState()
    val config by SettingsRepository.config.collectAsState()
    val allGames by GameLibraryRepository.games.collectAsState()

    val gamepadDetector = remember { GamepadDetector(context) }
    val gamepadStatus by gamepadDetector.status.collectAsState()

    DisposableEffect(Unit) {
        onDispose { gamepadDetector.release() }
    }

    var currentTab by remember { mutableStateOf(DashboardTab.HOME) }

    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            VpnManager.connect(context)
        } else {
            VpnManager.setEngine(VpnEngine.SMART_BYPASS)
            VpnManager.connect(context)
        }
    }

    fun handleConnect() {
        VpnManager.connect(context)
    }

    Scaffold(
        modifier = modifier.testTag("dashboard_screen"),
        containerColor = XboxDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(XboxGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "CloudPlay Switcher",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Xbox Cloud Auto-Bypass",
                                color = XboxNeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = XboxSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = XboxSurface,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == DashboardTab.HOME,
                    onClick = { currentTab = DashboardTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang Chủ") },
                    label = { Text("Trang Chủ", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = XboxNeonGreen,
                        indicatorColor = XboxNeonGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == DashboardTab.LIBRARY,
                    onClick = { currentTab = DashboardTab.LIBRARY },
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Thư Viện") },
                    label = { Text("Thư Viện", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = XboxNeonGreen,
                        indicatorColor = XboxNeonGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == DashboardTab.SERVERS,
                    onClick = { currentTab = DashboardTab.SERVERS },
                    icon = { Icon(Icons.Default.Speed, contentDescription = "Máy Chủ") },
                    label = { Text("Máy Chủ", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = XboxNeonGreen,
                        indicatorColor = XboxNeonGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == DashboardTab.SETTINGS,
                    onClick = { currentTab = DashboardTab.SETTINGS },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Cài Đặt") },
                    label = { Text("Cài Đặt", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = XboxNeonGreen,
                        indicatorColor = XboxNeonGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_animation"
            ) { tab ->
                when (tab) {
                    DashboardTab.HOME -> {
                        HomeScreen(
                            vpnState = vpnState,
                            gamepadStatus = gamepadStatus,
                            featuredGames = allGames,
                            onConnectVpn = { handleConnect() },
                            onDisconnectVpn = { VpnManager.disconnect(context) },
                            onSwitchEngine = { VpnManager.setEngine(it) },
                            onLaunchGame = { onLaunchUrl(it) },
                            onOpenXboxCloud = { onLaunchUrl(config.targetRegionUrl) },
                            onNavigateToLibrary = { currentTab = DashboardTab.LIBRARY }
                        )
                    }
                    DashboardTab.LIBRARY -> {
                        GameLibraryScreen(
                            games = allGames,
                            onLaunchGame = { onLaunchUrl(it) }
                        )
                    }
                    DashboardTab.SERVERS -> {
                        ServersScreen(
                            currentServer = vpnState.currentServer,
                            onSelectServer = { VpnManager.selectServer(it, context) }
                        )
                    }
                    DashboardTab.SETTINGS -> {
                        SettingsScreen(
                            config = config,
                            vpnState = vpnState,
                            onClearCookies = {}
                        )
                    }
                }
            }
        }
    }
}
