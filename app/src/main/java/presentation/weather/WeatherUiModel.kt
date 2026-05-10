package presentation.weather

import data.remote.City
import data.remote.WeatherResponse

data class WeatherUiModel(
    val city: String,
    val country: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double
)

fun WeatherResponse.toUiModel(city: City): WeatherUiModel {
    return WeatherUiModel(
        city = city.name,
        country = city.country,
        temperature = current.temperature_2m,
        condition = current.weather_code.toWeatherCondition(),
        humidity = current.relative_humidity_2m,
        windSpeed = current.wind_speed_10m,
        pressure = current.pressure_msl
    )
}

fun Int.toWeatherCondition(): String {
    return when (this) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75 -> "Snow fall"
        77 -> "Snow grains"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Current weather"
    }
}
