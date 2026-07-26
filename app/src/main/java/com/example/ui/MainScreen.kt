package com.example.ui

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppAccentColor
import com.example.data.AppPreferences
import com.example.data.TargetGame
import com.example.service.LocalVpnService
import com.example.shizuku.ShizukuManager
import com.example.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    appPreferences: AppPreferences,
    shizukuManager: ShizukuManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val accentColor by appPreferences.accentColor.collectAsState()
    val targetGame by appPreferences.targetGame.collectAsState()
    val holdDelayMs by appPreferences.holdDelayMs.collectAsState()
    val timerDurationSec by appPreferences.timerDurationSeconds.collectAsState()

    val isShizukuAvail by shizukuManager.isShizukuAvailable.collectAsState()
    val hasShizukuPerm by shizukuManager.hasShizukuPermission.collectAsState()

    var isVpnPrepared by remember { mutableStateOf(VpnService.prepare(context) == null) }
    var isVpnActive by remember { mutableStateOf(LocalVpnService.activeInstance != null) }

    var remainingSeconds by remember { mutableStateOf(timerDurationSec) }
    var showDelayMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var packetsProcessed by remember { mutableStateOf(0L) }
    var bytesProcessed by remember { mutableStateOf(0L) }

    // Synchronize parameters with VPN Service
    LaunchedEffect(holdDelayMs) {
        LocalVpnService.holdDelayMs = holdDelayMs
    }

    // Timer & Stats Ticker when VPN is Active
    LaunchedEffect(isVpnActive) {
        if (isVpnActive) {
            remainingSeconds = timerDurationSec
            while (isVpnActive && remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                packetsProcessed = LocalVpnService.packetsProcessed
                bytesProcessed = LocalVpnService.bytesProcessed
                isVpnActive = (LocalVpnService.activeInstance != null)
            }
            if (remainingSeconds <= 0 && isVpnActive) {
                // Auto exhaust timer stop
                val stopIntent = Intent(context, LocalVpnService::class.java).apply {
                    action = LocalVpnService.ACTION_STOP
                }
                context.startService(stopIntent)
                isVpnActive = false
                Toast.makeText(context, "Timer habis - Local VPN Service stopped.", Toast.LENGTH_SHORT).show()
            }
        } else {
            remainingSeconds = timerDurationSec
            packetsProcessed = 0L
            bytesProcessed = 0L
        }
    }

    val primaryCol = accentColor.primaryColor

    Scaffold(
        containerColor = Color(0xFF050505)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "XRANS FL",
                                color = primaryCol,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "DEV BY XRANS • XRANS.FL",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Permission Status Indicators
                    PermissionStatusGrid(
                        isVpnPrepared = isVpnPrepared,
                        isShizukuAvailable = isShizukuAvail,
                        hasShizukuPermission = hasShizukuPerm,
                        accentColor = accentColor,
                        onRequestShizuku = {
                            if (!isShizukuAvail) {
                                Toast.makeText(context, "Shizuku service belum berjalan. Jalankan Shizuku via ADB.", Toast.LENGTH_LONG).show()
                            } else {
                                shizukuManager.requestShizukuPermission()
                            }
                        },
                        onVpnPrepareResult = { prepared ->
                            isVpnPrepared = prepared
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Game Selector (Free Fire ORI / FF MAX)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TargetGame.values().forEach { game ->
                            val isSelected = targetGame == game
                            val bgCol = if (isSelected) primaryCol else Color.Transparent
                            val txtCol = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(bgCol)
                                    .clickable { appPreferences.setTargetGame(game) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = game.title,
                                    color = txtCol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Middle Network Stats Card
                NetworkStatsCard(
                    isVpnActive = isVpnActive,
                    holdDelayMs = holdDelayMs,
                    bytesProcessed = bytesProcessed,
                    packetsProcessed = packetsProcessed,
                    accentColor = accentColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Shizuku Layer Info Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF111111))
                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Shizuku status",
                            tint = if (hasShizukuPerm) primaryCol else Color.Yellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (hasShizukuPerm)
                                "Shizuku Active: Isolated process priority tuned for ${targetGame.title}"
                            else
                                "Shizuku Optional: ADB system priority service available",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Bottom Skull Toggle Button & Timer Display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                        .background(Color(0xFF111111))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                        )
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.TopEnd,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        val skullColor by animateColorAsState(
                            targetValue = if (isVpnActive) Color(0xFF22C55E) else Color(0xFFEF4444),
                            label = "SkullColor"
                        )

                        // Skull Button with detectTapGestures for long press (0.9s / 900ms)
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = CircleShape,
                                    spotColor = skullColor
                                )
                                .clip(CircleShape)
                                .background(Color(0xFF1C1C1C))
                                .border(4.dp, skullColor, CircleShape)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (!isVpnPrepared) {
                                                val intent = VpnService.prepare(context)
                                                if (intent != null) {
                                                    Toast.makeText(context, "Izinkan permission VPN Service dahulu", Toast.LENGTH_SHORT).show()
                                                    return@detectTapGestures
                                                } else {
                                                    isVpnPrepared = true
                                                }
                                            }

                                            if (isVpnActive) {
                                                val stopIntent = Intent(context, LocalVpnService::class.java).apply {
                                                    action = LocalVpnService.ACTION_STOP
                                                }
                                                context.startService(stopIntent)
                                                isVpnActive = false
                                                Toast.makeText(context, "Local VPN Service Matikan (OFF)", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val startIntent = Intent(context, LocalVpnService::class.java)
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    context.startForegroundService(startIntent)
                                                } else {
                                                    context.startService(startIntent)
                                                }
                                                isVpnActive = true
                                                Toast.makeText(context, "Local VPN Service Aktif (ON)", Toast.LENGTH_SHORT).show()

                                                // Apply Shizuku tuning if active
                                                val statusMsg = shizukuManager.applyNetworkOptimizations(targetGame.packageName)
                                                Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onLongPress = {
                                            // Long press 0.9s (900ms) triggers popup menu with safe spacing
                                            showDelayMenu = !showDelayMenu
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "💀",
                                    fontSize = 38.sp
                                )
                                Text(
                                    text = if (isVpnActive) "ON" else "OFF",
                                    color = skullColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Timer Badge Overlay
                        val hours = remainingSeconds / 3600
                        val mins = (remainingSeconds % 3600) / 60
                        val secs = remainingSeconds % 60
                        val timerFormatted = String.format("%02d:%02d:%02d", hours, mins, secs)

                        Box(
                            modifier = Modifier
                                .offset(x = 16.dp, y = (-8).dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = timerFormatted,
                                color = primaryCol,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "TEKAN TAHAN (0.9s) UNTUK MENU ATUR DURASI TAHAN DATA",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Floating Hold Data Delay Menu (overlay with space gap from bottom button)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 180.dp),
                contentAlignment = Alignment.Center
            ) {
                DelayMenuDialog(
                    visible = showDelayMenu,
                    holdDelayMs = holdDelayMs,
                    accentColor = accentColor,
                    onDelayChange = { newDelay ->
                        appPreferences.setHoldDelayMs(newDelay)
                    }
                )
            }

            // Theme Settings Modal
            SettingsDialog(
                visible = showSettingsDialog,
                currentAccent = accentColor,
                timerSeconds = timerDurationSec,
                onAccentSelected = { newAccent ->
                    appPreferences.setAccentColor(newAccent)
                },
                onTimerSecondsChange = { newTimerSec ->
                    appPreferences.setTimerDurationSeconds(newTimerSec)
                    remainingSeconds = newTimerSec
                },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}
