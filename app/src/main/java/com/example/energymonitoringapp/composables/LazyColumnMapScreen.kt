package com.example.energymonitoringapp.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlinx.coroutines.launch

@Composable
fun LazyColumnMapScreen() {
    val stations = remember { sampleStations }
    var selectedStation by remember { mutableStateOf<EnergyStation?>(stations.first()) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Списки та Мапа (LazyColumn + Map)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Двостороння взаємодія між списком об'єктів та інтерактивною картою",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EEF5)),
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
                                    val index = stations.indexOf(it)
                                    if (index >= 0) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                    }
                                }
                            }
                        }
                ) {

                    val gridColor = Color(0xFFCFD8DC)
                    for (x in 0..4) {
                        val px = size.width * x / 4f
                        drawLine(gridColor, Offset(px, 0f), Offset(px, size.height), strokeWidth = 1f)
                    }
                    for (y in 0..4) {
                        val py = size.height * y / 4f
                        drawLine(gridColor, Offset(0f, py), Offset(size.width, py), strokeWidth = 1f)
                    }

                    stations.forEach { st ->
                        val mx = ((st.longitude - 30.48) / (30.57 - 30.48) * (size.width - 80) + 40).toFloat()
                        val my = ((50.48 - st.latitude) / (50.48 - 50.42) * (size.height - 80) + 40).toFloat()
                        val isSelected = st.id == selectedStation?.id

                        if (isSelected) {
                            drawCircle(
                                color = Color(0x331976D2),
                                radius = 28f,
                                center = Offset(mx, my)
                            )
                        }

                        val markerColor = when (st.status) {
                            "Активна" -> Color(0xFF2E7D32)
                            "Пікове навантаження" -> Color(0xFFD32F2F)
                            "Резервна" -> Color(0xFF1976D2)
                            else -> Color(0xFFF57C00)
                        }

                        drawCircle(
                            color = markerColor,
                            radius = if (isSelected) 14f else 10f,
                            center = Offset(mx, my)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 6f else 4f,
                            center = Offset(mx, my)
                        )
                    }
                }

                selectedStation?.let { station ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${station.name} (${station.powerKw} кВт)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "Мапа енергетичних вузлів (Київ)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Енергетичні станції (LazyColumn):",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stations) { station ->
                val isSelected = station.id == selectedStation?.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedStation = station
                        }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = station.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Badge(
                                containerColor = when (station.status) {
                                    "Активна" -> Color(0xFF2E7D32)
                                    "Пікове навантаження" -> Color(0xFFD32F2F)
                                    "Резервна" -> Color(0xFF1976D2)
                                    else -> Color(0xFFF57C00)
                                }
                            ) {
                                Text(station.status, color = Color.White, modifier = Modifier.padding(2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Тип: ${station.type}", style = MaterialTheme.typography.bodySmall)
                            Text("Потужність: ${station.powerKw} кВт", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = station.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Координати: ${station.latitude}, ${station.longitude}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
