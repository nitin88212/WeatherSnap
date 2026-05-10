package presentation.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import presentation.common.DecorativeHeader
import presentation.weather.WeatherCard
import presentation.weather.WeatherUiModel
import util.formatFileSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    weather: WeatherUiModel?,
    imagePath: String,
    originalImageSize: Long,
    compressedImageSize: Long,
    viewModel: CreateReportViewModel,
    onCapturePhoto: () -> Unit,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var notes by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.resetSavedState()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Report") },
                navigationIcon = {
                    OutlinedButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (weather == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Search and select weather before creating a report.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                DecorativeHeader(
                    title = "Build your report",
                    subtitle = "Capture the sky, add notes, and save the full weather snapshot.",
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }

            item {
                WeatherCard(weather = weather)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Photo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (imagePath.isBlank()) {
                                "Capture a fresh image for this weather snapshot."
                            } else {
                                "Image compressed and ready for saving."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (imagePath.isBlank()) {
                            EmptyPreview()
                        } else {
                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(animationSpec = spring())
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(imagePath),
                                    contentDescription = "Captured weather report photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            FileSizeRows(originalImageSize, compressedImageSize)
                        }
                        Button(onClick = onCapturePhoto, modifier = Modifier.fillMaxWidth()) {
                            Text(if (imagePath.isBlank()) "Capture Photo" else "Retake Photo")
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    label = { Text("Notes") },
                    placeholder = { Text("Add context for this weather report") },
                    supportingText = { Text("${notes.length} characters") }
                )
            }

            item {
                val readyToSave = imagePath.isNotBlank()
                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AnimatedVisibility(visible = !readyToSave) {
                    Text(
                        text = "Capture a photo to enable saving.",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = {
                        viewModel.saveReport(
                            weather = weather,
                            notes = notes,
                            imagePath = imagePath,
                            originalImageSize = originalImageSize,
                            compressedImageSize = compressedImageSize
                        )
                    },
                    enabled = readyToSave && !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (notes.isBlank()) "Save Report" else "Save Report with Notes")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun EmptyPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Image preview area",
            modifier = Modifier.padding(24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun FileSizeRows(originalImageSize: Long, compressedImageSize: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Original size")
            Text(formatFileSize(originalImageSize), fontWeight = FontWeight.SemiBold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Compressed size")
            Text(formatFileSize(compressedImageSize), fontWeight = FontWeight.SemiBold)
        }
        if (originalImageSize > 0 && compressedImageSize > 0) {
            val savedRatio = (1f - compressedImageSize.toFloat() / originalImageSize.toFloat())
                .coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { savedRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
            )
            Text(
                text = "${(savedRatio * 100).toInt()}% smaller after compression",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
