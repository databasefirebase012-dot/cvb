package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppAccentColor
import com.example.service.FloatingControlService

@Composable
fun PermissionStatusGrid(
    isVpnPrepared: Boolean,
    isShizukuAvailable: Boolean,
    hasShizukuPermission: Boolean,
    accentColor: AppAccentColor,
    onRequestShizuku: () -> Unit,
    onVpnPrepareResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    var isFloatingRunning by remember {
        mutableStateOf(FloatingControlService.isRunning)
    }

    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val prepared = VpnService.prepare(context.applicationContext) == null
        onVpnPrepareResult(prepared)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. VPN Service Permission Box
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = if (isVpnPrepared) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    val intent = VpnService.prepare(context.applicationContext)
                    if (intent != null) {
                        vpnLauncher.launch(intent)
                    } else {
                        onVpnPrepareResult(true)
                    }
                },
            color = Color(0xFF141414)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isVpnPrepared) Color(0xFF22C55E) else Color(0xFFEF4444))
                    )
                    Text(
                        text = "VPN",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (isVpnPrepared) "READY" else "ALLOW",
                    color = if (isVpnPrepared) Color(0xFF22C55E) else Color(0xFFEF4444),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 2. Shizuku Service Permission Box
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = if (hasShizukuPermission) accentColor.primaryColor.copy(alpha = 0.5f)
                           else if (isShizukuAvailable) Color(0xFFEAB308).copy(alpha = 0.4f)
                           else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onRequestShizuku() },
            color = Color(0xFF141414)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasShizukuPermission) accentColor.primaryColor
                                else if (isShizukuAvailable) Color(0xFFEAB308)
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                    Text(
                        text = "SHIZUKU",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (hasShizukuPermission) "ACTIVE"
                           else if (isShizukuAvailable) "TAP ALLOW"
                           else "INACTIVE",
                    color = if (hasShizukuPermission) accentColor.primaryColor
                           else if (isShizukuAvailable) Color(0xFFEAB308)
                           else Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 3. Floating Overlay Permission & Widget Box
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = if (isFloatingRunning) Color(0xFF22C55E).copy(alpha = 0.5f)
                           else if (hasOverlayPermission) accentColor.primaryColor.copy(alpha = 0.4f)
                           else Color(0xFFEF4444).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    hasOverlayPermission = Settings.canDrawOverlays(context)
                    if (!hasOverlayPermission) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        if (FloatingControlService.isRunning) {
                            val stopIntent = Intent(context, FloatingControlService::class.java).apply {
                                action = FloatingControlService.ACTION_STOP
                            }
                            context.startService(stopIntent)
                            isFloatingRunning = false
                        } else {
                            val startIntent = Intent(context, FloatingControlService::class.java).apply {
                                action = FloatingControlService.ACTION_START
                            }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(startIntent)
                            } else {
                                context.startService(startIntent)
                            }
                            isFloatingRunning = true
                        }
                    }
                },
            color = Color(0xFF141414)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFloatingRunning) Color(0xFF22C55E)
                                else if (hasOverlayPermission) accentColor.primaryColor
                                else Color(0xFFEF4444)
                            )
                    )
                    Text(
                        text = "FLOATING",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (isFloatingRunning) "WIDGET ON"
                           else if (hasOverlayPermission) "TAP TO START"
                           else "ALLOW OVERLAY",
                    color = if (isFloatingRunning) Color(0xFF22C55E)
                           else if (hasOverlayPermission) accentColor.primaryColor
                           else Color(0xFFEF4444),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
