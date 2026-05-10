package data.repository

import data.local.WeatherReportDao
import data.local.WeatherReportEntity
import data.remote.WeatherApi
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: WeatherApi,
    private val reportDao: WeatherReportDao
) {

    suspend fun searchCity(city: String) =
        api.searchCity(city)

    suspend fun getWeather(
        latitude: Double,
        longitude: Double
    ) = api.getWeather(
        latitude = latitude,
        longitude = longitude
    )

    fun observeReports() = reportDao.observeReports()

    suspend fun saveReport(report: WeatherReportEntity) =
        reportDao.insertReport(report)
}
