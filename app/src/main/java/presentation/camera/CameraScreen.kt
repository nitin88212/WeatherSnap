package presentation.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import presentation.common.DecorativeHeader
import util.CompressedImage
import util.compressImageFile
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onCaptured: (CompressedImage) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var isCapturing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Camera") },
                actions = {
                    OutlinedButton(onClick = onClose, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DecorativeHeader(
                title = "Frame the moment",
                subtitle = "Use the custom camera to capture the weather scene for this report.",
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.secondaryContainer
                )
            )

            if (hasPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { viewContext ->
                                PreviewView(viewContext).also { previewView ->
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
                                    cameraProviderFuture.addListener({
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageCapture
                                        )
                                    }, ContextCompat.getMainExecutor(viewContext))
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isCapturing) {
                            Card(modifier = Modifier.align(Alignment.Center)) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(strokeWidth = 2.dp)
                                    Text("Capturing and compressing...")
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission is required to capture a report photo.")
                }
            }

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close")
                }
                Button(
                    onClick = {
                        isCapturing = true
                        error = null
                        captureAndCompress(
                            imageCapture = imageCapture,
                            outputDirectory = context.filesDir,
                            executor = cameraExecutor,
                            mainExecutor = ContextCompat.getMainExecutor(context),
                            onSuccess = {
                                isCapturing = false
                                onCaptured(it)
                            },
                            onError = {
                                isCapturing = false
                                error = it
                            }
                        )
                    },
                    enabled = hasPermission && !isCapturing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    } else {
                        Text("Capture")
                    }
                }
            }
        }
    }
}

private fun captureAndCompress(
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: java.util.concurrent.Executor,
    mainExecutor: java.util.concurrent.Executor,
    onSuccess: (CompressedImage) -> Unit,
    onError: (String) -> Unit
) {
    val original = File(outputDirectory, "weather_original_${System.currentTimeMillis()}.jpg")
    val compressed = File(outputDirectory, "weather_compressed_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(original).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                try {
                    val image = compressImageFile(original, compressed)
                    mainExecutor.execute { onSuccess(image) }
                } catch (e: Exception) {
                    mainExecutor.execute {
                        onError(e.localizedMessage ?: "Unable to compress image")
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                mainExecutor.execute {
                    onError(exception.localizedMessage ?: "Unable to capture image")
                }
            }
        }
    )
}
