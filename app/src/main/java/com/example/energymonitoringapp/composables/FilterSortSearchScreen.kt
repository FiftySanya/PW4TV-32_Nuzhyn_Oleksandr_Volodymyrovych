package com.example.energymonitoringapp.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.energymonitoringapp.model.EnergyStation
import com.example.energymonitoringapp.model.sampleStations

enum class SortCriteria {
    POWER_DESC, POWER_ASC, NAME_ASC, NAME_DESC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortSearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Всі") }
    var selectedStatus by remember { mutableStateOf("Всі") }
    var currentSort by remember { mutableStateOf(SortCriteria.POWER_DESC) }
    var expandedStationId by remember { mutableStateOf<String?>(null) }

    val allStations = remember { sampleStations }

    val filteredStations = remember(searchQuery, selectedType, selectedStatus, currentSort) {
        allStations.filter { station ->
            val matchesSearch = searchQuery.isBlank() ||
                    station.name.contains(searchQuery, ignoreCase = true) ||
                    station.description.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedType == "Всі" || station.type == selectedType
            val matchesStatus = selectedStatus == "Всі" || station.status == selectedStatus
            matchesSearch && matchesType && matchesStatus
        }.let { list ->
            when (currentSort) {
                SortCriteria.POWER_DESC -> list.sortedByDescending { it.powerKw }
                SortCriteria.POWER_ASC -> list.sortedBy { it.powerKw }
                SortCriteria.NAME_ASC -> list.sortedBy { it.name }
                SortCriteria.NAME_DESC -> list.sortedByDescending { it.name }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Пошук, фільтрація та сортування",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Багаторівнева фільтрація об'єктів енергоспоживання у реальному часі",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пошук станції за назвою або описом...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Тип об'єкта:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Всі", "Промисловий", "Житловий", "Комерційний", "СЕС/ВЕС").forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text("Статус роботи:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Всі", "Активна", "Пікове навантаження", "Резервна", "На обслуговуванні").forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { selectedStatus = status },
                    label = { Text(status) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Знайдено: ${filteredStations.size} з ${allStations.size}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AssistChip(
                    onClick = {
                        currentSort = if (currentSort == SortCriteria.POWER_DESC) SortCriteria.POWER_ASC else SortCriteria.POWER_DESC
                    },
                    label = {
                        Text(if (currentSort == SortCriteria.POWER_DESC) "Потужність ↓" else "Потужність ↑")
                    },
                    leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = {
                        currentSort = if (currentSort == SortCriteria.NAME_ASC) SortCriteria.NAME_DESC else SortCriteria.NAME_ASC
                    },
                    label = {
                        Text(if (currentSort == SortCriteria.NAME_ASC) "Назва А-Я" else "Назва Я-А")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredStations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Об'єктів за заданими критеріями не знайдено.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStations) { station ->
                    val isExpanded = expandedStationId == station.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedStationId = if (isExpanded) null else station.id
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                                Text(
                                    text = "${station.powerKw} кВт",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${station.type} • ${station.status}", style = MaterialTheme.typography.bodySmall)
                                Text("Оновлено: ${station.lastUpdated}", fontSize = 11.sp, color = Color.Gray)
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Опис: ${station.description}", style = MaterialTheme.typography.bodySmall)
                                    Text("Номінальна напруга: ${station.voltageV} В", style = MaterialTheme.typography.bodySmall)
                                    Text("Геолокація: ${station.latitude}, ${station.longitude}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
