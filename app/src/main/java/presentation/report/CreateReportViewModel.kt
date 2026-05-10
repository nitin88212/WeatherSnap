package presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.local.WeatherReportEntity
import data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import presentation.weather.WeatherUiModel
import javax.inject.Inject

data class CreateReportState(
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CreateReportState())
    val state = _state.asStateFlow()

    fun saveReport(
        weather: WeatherUiModel,
        notes: String,
        imagePath: String,
        originalImageSize: Long,
        compressedImageSize: Long
    ) {
        viewModelScope.launch {
            try {
                _state.value = CreateReportState(isSaving = true)
                repository.saveReport(
                    WeatherReportEntity(
                        city = weather.city,
                        country = weather.country,
                        temperature = weather.temperature,
                        condition = weather.condition,
                        humidity = weather.humidity,
                        windSpeed = weather.windSpeed,
                        pressure = weather.pressure,
                        notes = notes.trim(),
                        imagePath = imagePath,
                        originalImageSize = originalImageSize,
                        compressedImageSize = compressedImageSize,
                        createdAt = System.currentTimeMillis()
                    )
                )
                _state.value = CreateReportState(saved = true)
            } catch (e: Exception) {
                _state.value = CreateReportState(
                    error = e.localizedMessage ?: "Unable to save report"
                )
            }
        }
    }

    fun resetSavedState() {
        _state.value = CreateReportState()
    }
}
