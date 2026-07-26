package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppAccentColor

@Composable
fun NetworkStatsCard(
    isVpnActive: Boolean,
    holdDelayMs: Int,
    bytesProcessed: Long,
    packetsProcessed: Long,
    accentColor: AppAccentColor,
    modifier: Modifier = Modifier
) {
    val primaryCol = accentColor.primaryColor

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF111111), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NETWORK BUFFER",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "80-100 Byte Capped",
                color = primaryCol,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val points = 15
                val step = width / points

                for (i in 0 until points) {
                    val x = i * step
                    val barHeightFactor = if (isVpnActive) {
                        ((i * 37 + holdDelayMs) % 70 + 20) / 100f
                    } else {
                        0.05f
                    }
                    val barHeight = height * barHeightFactor
                    drawRect(
                        color = primaryCol.copy(alpha = 0.3f + (i % 5) * 0.1f),
                        topLeft = Offset(x, height - barHeight),
                        size = androidx.compose.ui.geometry.Size(step * 0.7f, barHeight)
                    )
                }
            }

            Text(
                text = if (isVpnActive) "TUN0 INTERFACE ACTIVE (${packetsProcessed} PKTS)" else "TUN0 INTERFACE IDLE",
                color = if (isVpnActive) primaryCol else Color.White.copy(alpha = 0.3f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "JITTER",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isVpnActive) "${(holdDelayMs * 0.5f).toInt()}ms" else "0.0ms",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LOSS",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "0.0%",
                    color = Color(0xFF22C55E),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DATA TUNNELED",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${bytesProcessed / 1024} KB",
                    color = primaryCol,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
