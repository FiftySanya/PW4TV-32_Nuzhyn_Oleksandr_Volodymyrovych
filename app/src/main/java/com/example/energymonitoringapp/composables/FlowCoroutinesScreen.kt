package com.example.energymonitoringapp.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

data class CombinedTelemetry(
    val activePowerKw: Double = 0.0,
    val voltageV: Double = 0.0,
    val forecastKw: Double = 160.0,
    val deltaKw: Double = 0.0,
    val status: String = "Норма",
    val timestamp: String = "--:--"
)

class FlowCoroutinesViewModel : ViewModel() {
    private var dataCollectionJob: Job? = null

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _telemetryState = MutableStateFlow(CombinedTelemetry())
    val telemetryState: StateFlow<CombinedTelemetry> = _telemetryState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _simulateError = MutableStateFlow(false)

    private val _eventSharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val eventSharedFlow: SharedFlow<String> = _eventSharedFlow.asSharedFlow()

    private fun activePowerFlow(): Flow<Double> = flow {
        while (true) {
            val power = 150.0 + Random.nextDouble(5.0, 45.0)
            emit(Math.round(power * 10.0) / 10.0)
            delay(1200)
        }
    }

    private fun voltageFlow(): Flow<Double> = flow {
        var attempts = 0
        while (true) {
            if (_simulateError.value) {
                attempts++
                addLog("⚠️ Помилка зчитування датчика напруги! Спроба повтору #$attempts...")
                if (attempts >= 3) {
                    _simulateError.value = false
                    attempts = 0
                    addLog("✅ Автоматичне відновлення зв'язку з датчиком напруги.")
                }
                throw IllegalStateException("Sensor timeout")
            }
            val volt = 220.0 + Random.nextDouble(-6.0, 8.0)
            emit(Math.round(volt * 10.0) / 10.0)
            delay(1200)
        }
    }.retry(retries = 3) { cause ->
        delay(600)
        true
    }.catch {
        emit(220.0)
        addLog("ℹ️ Застосовано аварійне значення напруги 220В.")
    }

    private fun forecastFlow(): Flow<Double> = flow {
        while (true) {
            val forecast = 170.0 + Random.nextDouble(-10.0, 15.0)
            emit(Math.round(forecast * 10.0) / 10.0)
            delay(2400)
        }
    }

    fun startMonitoring() {
        if (_isMonitoring.value) return
        _isMonitoring.value = true
        addLog("▶️ Запуск асинхронного комбінування потоків Flow...")

        dataCollectionJob = viewModelScope.launch {

            combine(
                activePowerFlow(),
                voltageFlow(),
                forecastFlow()
            ) { power, volt, forecast ->
                val delta = Math.round((power - forecast) * 10.0) / 10.0
                val status = when {
                    power > 185.0 -> "Пікове навантаження"
                    delta > 15.0 -> "Перевищення прогнозу"
                    else -> "Оптимальне"
                }
                val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                CombinedTelemetry(power, volt, forecast, delta, status, time)
            }.collect { combined ->
                _telemetryState.value = combined
                _eventSharedFlow.emit("Дані [${combined.timestamp}]: ${combined.activePowerKw} кВт / ${combined.voltageV} В")
                addLog("📊 Оновлено: Потужність ${combined.activePowerKw} кВт, Напруга ${combined.voltageV} В (${combined.status})")
            }
        }
    }

    fun stopMonitoring() {
        dataCollectionJob?.cancel()
        _isMonitoring.value = false
        addLog("⏹️ Моніторинг зупинено.")
    }

    fun triggerSensorError() {
        _simulateError.value = true
        addLog("⚡ Імітовано збій сенсора напруги! Запуск Retry Policy...")
    }

    private fun addLog(message: String) {
        _logs.value = listOf(message) + _logs.value.take(20)
    }

    override fun onCleared() {
        super.onCleared()
        dataCollectionJob?.cancel()
    }
}

@Composable
fun FlowCoroutinesScreen(viewModel: FlowCoroutinesViewModel = viewModel()) {
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val telemetry by viewModel.telemetryState.collectAsState()
    val logs by viewModel.logs.collectAsState()

    LaunchedEffect(Unit) {
        if (!isMonitoring) {
            viewModel.startMonitoring()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Асинхронний моніторинг (Flow & Coroutines)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Комбінування трьох потоків даних (Power + Voltage + AI Forecast)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (telemetry.status) {
                    "Пікове навантаження" -> Color(0xFFFFEBEE)
                    "Перевищення прогнозу" -> Color(0xFFFFF8E1)
                    else -> Color(0xFFE8F5E9)
                }
            ),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Статус: ${telemetry.status}",
                        fontWeight = FontWeight.Bold,
                        color = when (telemetry.status) {
                            "Пікове навантаження" -> Color(0xFFC62828)
                            "Перевищення прогнозу" -> Color(0xFFF57F17)
                            else -> Color(0xFF2E7D32)
                        }
                    )
                    Text(
                        text = "Оновлено: ${telemetry.timestamp}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Активна потужність", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${telemetry.activePowerKw} кВт",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text("Напруга мережі", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${telemetry.voltageV} В",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text("Прогноз AI", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${telemetry.forecastKw} кВт",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Відхилення від прогнозу: ${if (telemetry.deltaKw >= 0) "+" else ""}${telemetry.deltaKw} кВт",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (isMonitoring) viewModel.stopMonitoring() else viewModel.startMonitoring()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMonitoring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isMonitoring) "Зупинити" else "Запустити")
            }

            OutlinedButton(
                onClick = { viewModel.triggerSensorError() },
                modifier = Modifier.weight(1.2f),
                enabled = isMonitoring
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Імітувати збій")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Журнал асинхронних подій (Flow Log):",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            items(logs) { log ->
                Text(
                    text = log,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}
