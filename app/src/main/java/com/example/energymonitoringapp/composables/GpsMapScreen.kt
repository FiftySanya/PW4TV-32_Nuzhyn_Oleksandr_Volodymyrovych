package com.example.energymonitoringapp.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.energymonitoringapp.model.EnergyStation
import com.example.energymonitoringapp.model.sampleStations
import kotlin.math.*

@Composable
fun GpsMapScreen() {

    val userLat = 50.4501
    val userLng = 30.5234

    val stations = remember { sampleStations }
    var selectedStation by remember { mutableStateOf<EnergyStation?>(stations.first()) }
    var showInfoWindow by remember { mutableStateOf(true) }
    var gpsAccuracyMeters by remember { mutableIntStateOf(4) }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return Math.round(r * c * 10.0) / 10.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "GPS та Геомоніторинг об'єктів",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Визначення позиції диспетчера, маркери об'єктів та InfoWindow",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("GPS: $userLat° N, $userLng° E", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("Дозвіл ACCESS_FINE_LOCATION надано (точність: ±$gpsAccuracyMeters м)", fontSize = 11.sp, color = Color(0xFF2E7D32))
                    }
                }
                IconButton(onClick = { gpsAccuracyMeters = if (gpsAccuracyMeters == 4) 2 else 4 }) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF2E7D32))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE1E8EE)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(stations) {
                            detectTapGestures { offset ->
                                val w = size.width
                                val h = size.height
                                val nearest = stations.minByOrNull { st ->
                                    val mx = ((st.longitude - 30.48) / (30.57 - 30.48) * (w - 80) + 40).toFloat()
                                    val my = ((50.48 - st.latitude) / (50.48 - 50.42) * (h - 80) + 40).toFloat()
                                    val dx = offset.x - mx
                                    val dy = offset.y - my
                                    dx * dx + dy * dy
                                }
                                nearest?.let {
                                    selectedStation = it
                                    showInfoWindow = true
                                }
                            }
                        }
                ) {

                    val w = size.width
                    val h = size.height
                    val gridColor = Color(0xFFCFD8DC)
                    for (x in 0..4) drawLine(gridColor, Offset(w * x / 4f, 0f), Offset(w * x / 4f, h), strokeWidth = 1f)
                    for (y in 0..4) drawLine(gridColor, Offset(0f, h * y / 4f), Offset(w, h * y / 4f), strokeWidth = 1f)

                    val userX = ((userLng - 30.48) / (30.57 - 30.48) * (w - 80) + 40).toFloat()
                    val userY = ((50.48 - userLat) / (50.48 - 50.42) * (h - 80) + 40).toFloat()

                    drawCircle(Color(0x332196F3), radius = 32f, center = Offset(userX, userY))
                    drawCircle(Color(0xFF2196F3), radius = 12f, center = Offset(userX, userY))
                    drawCircle(Color.White, radius = 5f, center = Offset(userX, userY))

                    stations.forEach { st ->
                        val mx = ((st.longitude - 30.48) / (30.57 - 30.48) * (w - 80) + 40).toFloat()
                        val my = ((50.48 - st.latitude) / (50.48 - 50.42) * (h - 80) + 40).toFloat()
                        val isSelected = st.id == selectedStation?.id

                        val color = when (st.status) {
                            "Активна" -> Color(0xFF2E7D32)
                            "Пікове навантаження" -> Color(0xFFD32F2F)
                            "Резервна" -> Color(0xFF1976D2)
                            else -> Color(0xFFF57C00)
                        }

                        if (isSelected) {
                            drawCircle(Color(0x44D32F2F), radius = 26f, center = Offset(mx, my))
                        }
                        drawCircle(color, radius = if (isSelected) 14f else 9f, center = Offset(mx, my))
                        drawCircle(Color.White, radius = if (isSelected) 6f else 3.5f, center = Offset(mx, my))
                    }
                }

                if (showInfoWindow && selectedStation != null) {
                    val st = selectedStation!!
                    val distance = calculateDistanceKm(userLat, userLng, st.latitude, st.longitude)

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(12.dp)
                            .fillMaxWidth(0.92f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(st.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                IconButton(onClick = { showInfoWindow = false }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${st.type} • ${st.status} • Потужність: ${st.powerKw} кВт", style = MaterialTheme.typography.bodySmall)
                            Text("Напруга: ${st.voltageV} В | Координати: ${st.latitude}, ${st.longitude}", fontSize = 11.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📍 Відстань від диспетчера: $distance км",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text("Натисніть маркер для деталей", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔵 Диспетчер (Ви)", fontSize = 10.sp)
                    Text("🟢 Активна", fontSize = 10.sp)
                    Text("🔴 Пік", fontSize = 10.sp)
                    Text("🟠 Ремонт", fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Швидкий вибір станції на мапі:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stations) { station ->
                val isSelected = station.id == selectedStation?.id
                Card(
                    modifier = Modifier
                        .clickable {
                            selectedStation = station
                            showInfoWindow = true
                        }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(station.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${station.powerKw} кВт • ${station.type}", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
