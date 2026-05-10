package presentation.weather



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.remote.City
import data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {
    private var searchJob: Job? = null

    private val cityCache = mutableMapOf<String, List<City>>()

    private val _state = MutableStateFlow(WeatherState())
    val state = _state.asStateFlow()

    fun searchCity(query: String) {
        val normalizedQuery = query.trim()

        if (normalizedQuery.length < 3) {
            _state.value = _state.value.copy(
                city = normalizedQuery,
                suggestions = emptyList(),
                hasSearched = false,
                error = null
            )
            return
        }

        _state.value = _state.value.copy(city = normalizedQuery, hasSearched = true)
        val cacheKey = normalizedQuery.lowercase()
        val cachedSuggestions = cityCache[cacheKey]
        if (cachedSuggestions != null) {
            _state.value = _state.value.copy(
                suggestions = cachedSuggestions,
                isLoading = false,
                error = null
            )
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)

            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null
                )

                val response = repository.searchCity(normalizedQuery)
                cityCache[cacheKey] = response.results

                _state.value = _state.value.copy(
                    suggestions = response.results,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.localizedMessage ?: "Unable to load city suggestions",
                    isLoading = false
                )
            }
        }
    }

    fun fetchWeather(city: City) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null
                )

                val weatherResponse = repository.getWeather(
                    latitude = city.latitude,
                    longitude = city.longitude
                )

                _state.value = _state.value.copy(
                    weather = weatherResponse.toUiModel(city),
                    city = city.name,
                    suggestions = emptyList(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.localizedMessage ?: "Unable to load weather",
                    isLoading = false
                )
            }
        }
    }
}
