package com.example.ui.components

import android.net.VpnService
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppAccentColor

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

    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val prepared = VpnService.prepare(context.applicationContext) == null
        onVpnPrepareResult(prepared)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // VPN Service Permission Box
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
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isVpnPrepared) Color(0xFF22C55E) else Color(0xFFEF4444))
                    )
                    Text(
                        text = "VPN SERVICE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (isVpnPrepared) "READY" else "TAP PERMISSION",
                    color = if (isVpnPrepared) Color(0xFF22C55E) else Color(0xFFEF4444),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Shizuku Service Permission Box
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
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasShizukuPermission) accentColor.primaryColor
                                else if (isShizukuAvailable) Color(0xFFEAB308)
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                    Text(
                        text = "SHIZUKU ADB",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (hasShizukuPermission) "ACTIVE & GRANTED"
                           else if (isShizukuAvailable) "TAP TO ALLOW"
                           else "SHIZUKU OFF",
                    color = if (hasShizukuPermission) accentColor.primaryColor
                           else if (isShizukuAvailable) Color(0xFFEAB308)
                           else Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
