package com.example.energymonitoringapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.energymonitoringapp.composables.FilterSortSearchScreen
import com.example.energymonitoringapp.composables.FlowCoroutinesScreen
import com.example.energymonitoringapp.composables.GpsMapScreen
import com.example.energymonitoringapp.composables.LazyColumnMapScreen
import com.example.energymonitoringapp.ui.theme.EnergyMonitoringAppTheme

sealed class Screen(val title: String, val icon: ImageVector) {
    object FlowCoroutines : Screen("Flow & Async", Icons.Default.Timeline)
    object LazyMap : Screen("Списки й Мапа", Icons.Default.Map)
    object FilterSort : Screen("Пошук/Фільтри", Icons.Default.FilterList)
    object GpsMap : Screen("GPS Моніторинг", Icons.Default.LocationOn)
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnergyMonitoringAppTheme {
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val screens = listOf(
                    Screen.FlowCoroutines,
                    Screen.LazyMap,
                    Screen.FilterSort,
                    Screen.GpsMap
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("ПР4: Моніторинг енергії (Вар. 15)")
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            screens.forEachIndexed { index, screen ->
                                NavigationBarItem(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTabIndex) {
                            0 -> FlowCoroutinesScreen()
                            1 -> LazyColumnMapScreen()
                            2 -> FilterSortSearchScreen()
                            3 -> GpsMapScreen()
                        }
                    }
                }
            }
        }
    }
}
