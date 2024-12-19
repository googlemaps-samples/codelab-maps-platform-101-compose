package com.example.mountainmarkers

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mountainmarkers.data.utils.ImperialUnitsConverter
import com.example.mountainmarkers.data.utils.LocalUnitsConverter
import com.example.mountainmarkers.presentation.EmptyLatLngBounds
import com.example.mountainmarkers.presentation.MountainList
import com.example.mountainmarkers.presentation.MountainMapScreen
import com.example.mountainmarkers.presentation.MountainsViewModel
import com.example.mountainmarkers.presentation.MountainsViewModelEvent
import com.example.mountainmarkers.presentation.zoomAll
import com.example.mountainmarkers.ui.theme.MountainMarkersTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

enum class Scenario {
//    NONE,
    NULL_ISLAND,
    CAMERA1,
    CAMERA2,
    BASIC_MARKERS,
    BASIC_MARKERS_STYLED,

    ADVANCED_MARKERS,
    ADVANCED_MARKERS_STYLED,

    CLUSTERED_MARKERS_ZOOMED_IN,
    CLUSTERED_MARKERS_EXTENTS,
    CLUSTERED_MARKERS_ZOOMED_OUT,
    CLUSTERED_MARKERS_STYLED,

    CLUSTERED_MARKERS_BASIC_WITH_COLORADO,

    BASIC_FINAL,
    ADVANCED_FINAL,
    CLUSTERED_FINAL
}

@AndroidEntryPoint
class CreateScreenshotsActivity : ComponentActivity() {
    private val viewModel: MountainsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

//        var scenario = intent.getStringExtra("scenario") ?: "null_island"

        setContent {
            CompositionLocalProvider(
                LocalUnitsConverter provides ImperialUnitsConverter
            ) {
                MountainMarkersTheme(dynamicColor = false) {
                    val mountainList by viewModel.mountains.collectAsStateWithLifecycle()
                    val loading by viewModel.loading.collectAsStateWithLifecycle()
                    var mapLoaded by remember { mutableStateOf(false) }
                    val showAllMountains by viewModel.showAllMountains.collectAsStateWithLifecycle()

                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
                    }

                    val scope = rememberCoroutineScope()

                    var zoom by remember {
                        mutableStateOf(1f)
                    }

                    var scenario by remember {
                        mutableStateOf(Scenario.NULL_ISLAND)
                    }

                    LaunchedEffect(Unit) {
                        while (true) {
                            Scenario.entries.forEach {
                                scenario = it
                                delay(20.seconds)
                            }
                        }
                    }

                    LaunchedEffect(mountainList, mapLoaded, zoom) {
                        if (mapLoaded) {
                            scope.launch {
                                if (zoom < 0) {
                                    zoomAll(scope, cameraPositionState, mountainList.boundingBox)
                                } else {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(
                                            mountainList.boundingBox.center,
                                            zoom
                                        ),
                                        durationMs = 1000
                                    )
                                }
                            }
                        }
                    }

                    when (scenario) {
                        Scenario.NULL_ISLAND -> {
                            zoom = 1f

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = MountainList(),
                                    markerType = MarkerType.Basic,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.CAMERA1 -> {
                            zoom = 5f

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = MountainList(),
                                    markerType = MarkerType.Basic,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.CAMERA2 -> {
                            zoom = -1f

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = MountainList(),
                                    markerType = MarkerType.Basic,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.BASIC_MARKERS ->  {
                            zoom = -1f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = false))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Basic,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = false,
                                    showMarkers = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.BASIC_MARKERS_STYLED -> {
                            zoom = -1f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = true))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Basic,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = true,
                                    showMarkers = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.ADVANCED_MARKERS -> {
                            zoom = -1f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = false))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Advanced,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = false,
                                    showMarkers = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.ADVANCED_MARKERS_STYLED -> {
                            zoom = -1f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = true))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Advanced,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = true,
                                    showMarkers = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.CLUSTERED_MARKERS_ZOOMED_IN -> {
                            zoom = 8f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = true))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Clustered,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = false,
                                    showMarkers = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.CLUSTERED_MARKERS_EXTENTS -> {
                            zoom = -1f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = true))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Clustered,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = false,
                                    showMarkers = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.CLUSTERED_MARKERS_ZOOMED_OUT ->  {
                            zoom = 5f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = true))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Clustered,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = false,
                                    showMarkers = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.CLUSTERED_MARKERS_STYLED -> {
                            zoom = 8f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = true))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Basic,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = true,
                                    showMarkers = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.CLUSTERED_MARKERS_BASIC_WITH_COLORADO -> {
                            zoom = 5f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = false))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Basic,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = true,
                                    showMarkers = true,
                                    showColorado = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.BASIC_FINAL -> {
                            zoom = 9f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = true))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Basic,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = true,
                                    showMarkers = true,
                                    showColorado = true,
                                    showRanges = true,
                                    showScaleBar = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.ADVANCED_FINAL -> {
                            zoom = 9f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = true))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Advanced,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = true,
                                    showMarkers = true,
                                    showColorado = true,
                                    showRanges = true,
                                    showScaleBar = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }

                        Scenario.CLUSTERED_FINAL -> {
                            zoom = -1f

                            viewModel.onEvent(event = MountainsViewModelEvent.OnShowAllMountainsChange(showAllMountains = true))

                            MountainMapScreenPreview(
                                MountainMapScreenParameters(
                                    loading = loading,
                                    mountains = mountainList,
                                    markerType = MarkerType.Clustered,
                                    showAllMountains = showAllMountains,
                                    cameraPositionState = cameraPositionState,
                                    styleMarkers = true,
                                    showMarkers = true,
                                    showColorado = true,
                                    showRanges = true,
                                    showScaleBar = true,
                                ),
                                onMapLoaded = { mapLoaded = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MountainMapScreenParameters(
    val loading: Boolean,
    val mountains: MountainList,
    val markerType: MarkerType,
    val showAllMountains: Boolean,
    val cameraPositionState: CameraPositionState,
    val showMarkers: Boolean = false,
    val showColorado: Boolean = false,
    val showRanges: Boolean = false,
    val styleMarkers: Boolean = false,
    val showScaleBar: Boolean = false,
)

@Composable
private fun MountainMapScreenPreview(
    parameters: MountainMapScreenParameters,
    onMapLoaded: () -> Unit
) {
    MountainMapScreen(
        markerType = parameters.markerType,
        loading = parameters.loading,
        mountains = parameters.mountains,
        showAllMountains = parameters.showAllMountains,
        cameraPositionState = parameters.cameraPositionState,
        showMarkers = parameters.showMarkers,
        showColorado = parameters.showColorado,
        showRanges = parameters.showRanges,
        styleMarkers = parameters.styleMarkers,
        onEvent = { },
        onMapLoaded = onMapLoaded,
        showScaleBar = parameters.showScaleBar,
    )
}

@Composable
private fun ClusteredMarkersPreview_intro_markers(mountainList: MountainList, loading: Boolean) {
    CompositionLocalProvider(
        LocalUnitsConverter provides ImperialUnitsConverter
    ) {
        // Wait for the mountains to load before displaying the map
        val markerType = MarkerType.Basic
        val showAllMountains = false
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(mountainList.boundingBox.center, 1f)
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(mountainList) {
            scope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(mountainList.boundingBox.center, 5f),
                    durationMs = 1000
                )
            }
        }

        MountainMarkersTheme(dynamicColor = false) {
            MountainMapScreen(
                markerType = markerType,
                loading = loading,
                mountains = mountainList,
                showAllMountains = showAllMountains,
                onEvent = { },
                cameraPositionState = cameraPositionState,
                showMarkers = false,
                showColorado = false,
                showRanges = false
            )
        }
    }
}

@Composable
fun BasicMarkersPreview_null_island() {
    CompositionLocalProvider(
        LocalUnitsConverter provides ImperialUnitsConverter
    ) {
        val markerType = MarkerType.Basic
        val loading = false
        val mountains = MountainList(
            mountains = emptyList(),
            boundingBox = EmptyLatLngBounds
        )
        val showAllMountains = false
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(mountains.boundingBox.center, 1f)
        }

        MountainMarkersTheme(dynamicColor = false) {
            MountainMapScreen(
                markerType = markerType,
                loading = loading,
                mountains = mountains,
                showAllMountains = showAllMountains,
                onEvent = { },
                cameraPositionState = cameraPositionState,
                showMarkers = false,
                showColorado = false,
                showRanges = false
            )
        }
    }
}

@Composable
fun BasicMarkersPreview_camera1(mountainList: MountainList, loading: Boolean) {
    CompositionLocalProvider(
        LocalUnitsConverter provides ImperialUnitsConverter
    ) {
        // Wait for the mountains to load before displaying the map
        val markerType = MarkerType.Basic
        val showAllMountains = false
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(mountainList.boundingBox.center, 1f)
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(mountainList) {
            scope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(mountainList.boundingBox.center, 5f),
                    durationMs = 1000
                )
            }
        }

        MountainMarkersTheme(dynamicColor = false) {
            MountainMapScreen(
                markerType = markerType,
                loading = loading,
                mountains = mountainList,
                showAllMountains = showAllMountains,
                onEvent = { },
                cameraPositionState = cameraPositionState,
                showMarkers = false,
                showColorado = false,
                showRanges = false
            )
        }
    }
}

@Composable
fun BasicMarkersPreview_camera2(mountainList: MountainList, loading: Boolean) {
    CompositionLocalProvider(
        LocalUnitsConverter provides ImperialUnitsConverter
    ) {
        // Wait for the mountains to load before displaying the map
        val markerType = MarkerType.Basic
        val showAllMountains = false
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(mountainList.boundingBox.center, 1f)
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(mountainList) {
            zoomAll(scope, cameraPositionState, mountainList.boundingBox)
        }

        MountainMarkersTheme(dynamicColor = false) {
            MountainMapScreen(
                markerType = markerType,
                loading = loading,
                mountains = mountainList,
                showAllMountains = showAllMountains,
                onEvent = { },
                cameraPositionState = cameraPositionState,
                showMarkers = false,
                showColorado = false,
                showRanges = false
            )
        }
    }
}

@Composable
fun BasicMarkersPreview_basic_markers(mountainList: MountainList, loading: Boolean) {
    CompositionLocalProvider(
        LocalUnitsConverter provides ImperialUnitsConverter
    ) {
        // Wait for the mountains to load before displaying the map
        val markerType = MarkerType.Basic
        val showAllMountains = false
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(mountainList.boundingBox.center, 1f)
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(mountainList) {
            zoomAll(scope, cameraPositionState, mountainList.boundingBox)
        }

        MountainMarkersTheme(dynamicColor = false) {
            MountainMapScreen(
                markerType = markerType,
                loading = loading,
                mountains = mountainList,
                showAllMountains = showAllMountains,
                onEvent = { },
                cameraPositionState = cameraPositionState,
                showMarkers = true,
                styleMarkers = false,
                showColorado = false,
                showRanges = false
            )
        }
    }
}


@Composable
fun CaptureScreenshot(filename: String = "screenshot") {
    val context = LocalView.current.context
    val view = LocalView.current
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val activity = LocalView.current.context as? Activity
        val window = activity?.window

        if (window != null) {
            LaunchedEffect(Unit) {
                val handler = Handler(Looper.getMainLooper())
                try {
                    PixelCopy.request(
                        window,
                        android.graphics.Rect(
                            location[0],
                            location[1],
                            location[0] + view.width,
                            location[1] + view.height
                        ),
                        bitmap,
                        { copyResult: Int ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                saveBitmap(context, bitmap, filename)
                            } else {
                                Log.e("Screenshot", "PixelCopy failed: $copyResult")
                            }
                        },
                        handler
                    )
                } catch (e: IllegalArgumentException) {
                    Log.e("Screenshot", "PixelCopy IllegalArgumentException: ${e.message}")
                }
            }
        }
    } else {
        // Older versions are not supported with this method.
        Log.w("Screenshot", "PixelCopy is only available on API 26 and above.")
    }
}

private fun saveBitmap(context: Context, bitmap: Bitmap, filename: String) {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "${filename}_$timeStamp.png"

    val imagesFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshots")
    imagesFolder.mkdirs()
    val imageFile = File(imagesFolder, fileName)

    try {
        val fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        Log.d("Screenshot", "Screenshot saved to: ${imageFile.absolutePath}")
        // Optionally, you can use MediaStore to make the image appear in the Gallery
        // ... (See example below)
    } catch (e: Exception) {
        Log.e("Screenshot", "Error saving screenshot: ${e.message}")
    }
}
