package presentation.weather

import data.remote.City

data class WeatherState(
    val city: String = "",
    val suggestions: List<City> = emptyList(),
    val weather: WeatherUiModel? = null,
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
