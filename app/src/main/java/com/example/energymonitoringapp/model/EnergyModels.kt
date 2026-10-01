package com.example.energymonitoringapp.model

data class EnergyStation(
    val id: String,
    val name: String,
    val type: String,
    val powerKw: Double,
    val voltageV: Double,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val lastUpdated: String = "18:00"
)

val sampleStations = listOf(
    EnergyStation(
        id = "st-01",
        name = "Підстанція «Північна-110»",
        type = "Промисловий",
        powerKw = 480.0,
        voltageV = 10500.0,
        status = "Активна",
        latitude = 50.4650,
        longitude = 30.5100,
        description = "Магістральний вузол живлення північного промислового хабу",
        lastUpdated = "18:15"
    ),
    EnergyStation(
        id = "st-02",
        name = "ЖК «Затишний» ТП-42",
        type = "Житловий",
        powerKw = 195.5,
        voltageV = 380.0,
        status = "Пікове навантаження",
        latitude = 50.4400,
        longitude = 30.5350,
        description = "Трансформаторна підстанція 16-поверхового житлового масиву",
        lastUpdated = "18:12"
    ),
    EnergyStation(
        id = "st-03",
        name = "ТРЦ «Плаза-Енерго»",
        type = "Комерційний",
        powerKw = 340.0,
        voltageV = 380.0,
        status = "Активна",
        latitude = 50.4550,
        longitude = 30.5200,
        description = "Комерційний центр, система клімат-контролю та освітлення",
        lastUpdated = "18:10"
    ),
    EnergyStation(
        id = "st-04",
        name = "Дахова СЕС «Дніпровська»",
        type = "СЕС/ВЕС",
        powerKw = 120.0,
        voltageV = 400.0,
        status = "Резервна",
        latitude = 50.4700,
        longitude = 30.5400,
        description = "Сонячна генерація з інверторним накопичувачем енергії",
        lastUpdated = "18:05"
    ),
    EnergyStation(
        id = "st-05",
        name = "Промбаза «Енергомаш»",
        type = "Промисловий",
        powerKw = 620.0,
        voltageV = 6000.0,
        status = "Пікове навантаження",
        latitude = 50.4350,
        longitude = 30.4950,
        description = "Важке машинобудування, енергоємне верстатне обладнання",
        lastUpdated = "18:14"
    ),
    EnergyStation(
        id = "st-06",
        name = "Автокластер «Східний»",
        type = "Комерційний",
        powerKw = 150.0,
        voltageV = 380.0,
        status = "На обслуговуванні",
        latitude = 50.4600,
        longitude = 30.5600,
        description = "Швидкісна зарядна станція електромобілів (плановий ТО)",
        lastUpdated = "17:50"
    )
)
