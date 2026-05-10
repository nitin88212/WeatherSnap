package data.remote

data class CityResponse(
    val results: List<City> = emptyList()
)

data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val admin1: String? = null
)
