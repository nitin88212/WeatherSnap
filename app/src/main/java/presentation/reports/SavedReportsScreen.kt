package presentation.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import data.local.WeatherReportEntity
import presentation.common.DecorativeHeader
import presentation.report.FileSizeRows
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedReportsScreen(
    viewModel: SavedReportsViewModel,
    onBack: () -> Unit
) {
    val reports by viewModel.reports.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Reports") },
                navigationIcon = {
                    OutlinedButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (reports.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DecorativeHeader(
                    title = "Weather archive",
                    subtitle = "Your saved reports will collect here after the first capture.",
                    colors = listOf(
                        Color(0xFF264653),
                        Color(0xFF2A9D8F),
                        Color(0xFFE76F51)
                    )
                )
                Card {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No reports saved yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Saved reports will appear here after you capture and save a report.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    DecorativeHeader(
                        title = "Weather archive",
                        subtitle = "Browse every captured snapshot, note, and compressed image detail.",
                        colors = listOf(
                            Color(0xFF264653),
                            Color(0xFF2A9D8F),
                            Color(0xFFE76F51)
                        )
                    )
                }
                item {
                    Text(
                        text = "${reports.size} saved ${if (reports.size == 1) "report" else "reports"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(reports, key = { it.id }) { report ->
                    ReportCard(report = report, modifier = Modifier.animateItem())
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: WeatherReportEntity,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(report.condition.conditionAccent())
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(report.imagePath),
                    contentDescription = "Saved report image for ${report.city}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Column {
                    Text(
                        text = "${report.city}, ${report.country}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = report.condition,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${report.temperature.toInt()}°C", fontWeight = FontWeight.SemiBold)
                    Text("${report.humidity}% humidity")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${report.windSpeed} km/h wind")
                    Text("${report.pressure} hPa")
                }
                AnimatedVisibility(visible = report.notes.isNotBlank()) {
                    Text(report.notes)
                }
                FileSizeRows(report.originalImageSize, report.compressedImageSize)
                Text(
                    text = "Saved ${formatTimestamp(report.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        .format(Date(timestamp))
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
