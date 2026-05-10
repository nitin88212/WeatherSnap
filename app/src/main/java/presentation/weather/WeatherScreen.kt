package presentation.weather

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weathersnap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onCreateReport: () -> Unit,
    onOpenReports: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var city by remember { mutableStateOf(state.city) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WeatherSnap") },
                actions = {
                    TextButton(onClick = onOpenReports) {
                        Text("Reports")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                WeatherHero(
                    onQuickCity = {
                        city = it
                        viewModel.searchCity(it)
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = city,
                    onValueChange = {
                        city = it
                        viewModel.searchCity(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("City") },
                    trailingIcon = {
                        AnimatedVisibility(visible = city.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    city = ""
                                    viewModel.searchCity("")
                                }
                            ) {
                                Text("Clear")
                            }
                        }
                    },
                    supportingText = { Text("Enter more than 2 letters to start city suggestions.") }
                )
            }

            item {
                AnimatedVisibility(visible = state.suggestions.isNotEmpty()) {
                    Text(
                        text = "${state.suggestions.size} matches found. Tap a city to load live weather.",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                AnimatedContent(
                    targetState = ScreenStatus(
                        loading = state.isLoading,
                        error = state.error,
                        hasSearched = state.hasSearched,
                        suggestionCount = state.suggestions.size,
                        hasWeather = state.weather != null
                    ),
                    label = "weather status"
                ) {
                    when {
                        it.loading -> LoadingCard("Loading latest data...")
                        it.error != null -> ErrorCard(it.error)
                        it.hasSearched && it.suggestionCount == 0 && !it.hasWeather -> EmptyCard("No city suggestions found.")
                    }
                }
            }

            items(
                items = state.suggestions,
                key = { "${it.name}-${it.latitude}-${it.longitude}" }
            ) { suggestion ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .clickable { viewModel.fetchWeather(suggestion) }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = suggestion.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = listOfNotNull(suggestion.admin1, suggestion.country).joinToString(", "),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap to load weather",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(visible = state.weather != null) {
                    state.weather?.let { weather ->
                        WeatherCard(
                            weather = weather,
                            primaryAction = {
                                Button(onClick = onCreateReport) {
                                    Text("Create Report")
                                }
                            },
                            secondaryAction = {
                                OutlinedButton(onClick = onOpenReports) {
                                    Text("Reports")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherHero(
    onQuickCity: (String) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "hero motion")
    val imageOffset by transition.animateFloat(
        initialValue = -4f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero image offset"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF2374AB),
                            Color(0xFF2A9D8F),
                            Color(0xFFE9C46A)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Search live weather",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Pick a city, capture a weather photo, and save a polished report.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Lucknow", "Delhi", "Mumbai").forEach { city ->
                        ElevatedAssistChip(
                            onClick = { onQuickCity(city) },
                            label = { Text(city) }
                        )
                    }
                }
            }
            Image(
                painter = painterResource(R.drawable.ic_weather_hero),
                contentDescription = "Weather illustration",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(top = (70 + imageOffset.toInt()).dp)
                    .size(128.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private data class ScreenStatus(
    val loading: Boolean,
    val error: String?,
    val hasSearched: Boolean,
    val suggestionCount: Int,
    val hasWeather: Boolean
)

@Composable
fun WeatherCard(
    weather: WeatherUiModel,
    primaryAction: @Composable () -> Unit = {},
    secondaryAction: @Composable () -> Unit = {}
) {
    val accent = weather.condition.conditionAccent()
    val cardBrush = Brush.linearGradient(
        colors = listOf(
            accent.copy(alpha = 0.86f),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(cardBrush)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${weather.city}, ${weather.country}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weather.condition,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                WeatherPulse(accent = accent)
            }
            Text(
                text = "${weather.temperature.toInt()}°C",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                WeatherMetric("Humidity", "${weather.humidity}%", weather.humidity / 100f, accent)
                WeatherMetric("Wind", "${weather.windSpeed} km/h", (weather.windSpeed / 80f).toFloat(), accent)
                WeatherMetric("Pressure", "${weather.pressure} hPa", ((weather.pressure - 950) / 100).toFloat(), accent)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
            ) {
                secondaryAction()
                primaryAction()
            }
        }
    }
}

@Composable
fun WeatherMetric(label: String, value: String, progress: Float, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0.05f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = accent,
            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun WeatherPulse(accent: Color) {
    val transition = rememberInfiniteTransition(label = "weather pulse")
    val radius by transition.animateFloat(
        initialValue = 18f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse radius"
    )

    Canvas(
        modifier = Modifier
            .size(78.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(accent.copy(alpha = 0.22f), radius = radius, center = center)
        drawCircle(accent, radius = 18f, center = center)
        drawCircle(Color.White.copy(alpha = 0.65f), radius = 7f, center = center)
    }
}

@Composable
private fun LoadingCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()
            Text(message)
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun String.conditionAccent(): Color {
    val lower = lowercase()
    return when {
        "thunder" in lower -> Color(0xFF8E6BD8)
        "rain" in lower || "drizzle" in lower -> Color(0xFF2A9D8F)
        "snow" in lower || "fog" in lower -> Color(0xFF7DA7D9)
        "cloud" in lower -> Color(0xFFB7791F)
        "clear" in lower -> Color(0xFFE9A23B)
        else -> Color(0xFF4F9D69)
    }
}
