// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.mountainmarkers

import android.os.Bundle
import android.widget.Toast
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

enum class Scenario {
    INTRO,

    NULL_ISLAND,
    CAMERA1,
    CAMERA2,
    BASIC_MARKERS,
    BASIC_MARKERS_CUSTOMIZED,

    ADVANCED_MARKERS,
    ADVANCED_MARKERS_CUSTOMIZED,

    CLUSTERED_MARKERS_ZOOMED_IN,
    CLUSTERED_MARKERS_EXTENTS,
    CLUSTERED_MARKERS_ZOOMED_OUT,
    CLUSTERED_MARKERS_CUSTOMIZED,

    BASIC_MARKERS_WITH_COLORADO,

    BASIC_FINAL,
    ADVANCED_FINAL,
    CLUSTERED_FINAL
}

sealed interface Zoom {
    data object ORIGIN: Zoom
    data object EXTENTS: Zoom
    data class CUSTOM(val zoom: Float): Zoom
}

/**
 * This is a helper activity to generate all the screenshots for the codelab.
 *
 * To use it, run this script:
 *
 * #!/bin/bash
 *
 * # List of scenarios
 * scenarios=(
 *     "NULL_ISLAND"
 *     "CAMERA1"
 *     "CAMERA2"
 *     "BASIC_MARKERS"
 *     "BASIC_MARKERS_CUSTOMIZED"
 *     "ADVANCED_MARKERS"
 *     "ADVANCED_MARKERS_CUSTOMIZED"
 *     "CLUSTERED_MARKERS_ZOOMED_IN"
 *     "CLUSTERED_MARKERS_EXTENTS"
 *     "CLUSTERED_MARKERS_ZOOMED_OUT"
 *     "CLUSTERED_MARKERS_CUSTOMIZED"
 *     "BASIC_MARKERS_WITH_COLORADO"
 *     "BASIC_FINAL"
 *     "ADVANCED_FINAL"
 *     "CLUSTERED_FINAL"
 * )
 *
 * # Loop through the scenarios
 * for scenario in "${scenarios[@]}"; do
 *     echo "Sending scenario: $scenario"
 *     adb shell am start -n com.example.mountainmarkers/.CreateScreenshotsActivity -e scenario "$scenario"
 *
 *     sleep 5  # Wait for the scenario to settle
 *
 *     filename_lower=$(echo "$scenario" | tr '[:upper:]' '[:lower:]')  # Convert to lowercase
 *     adb exec-out screencap -p | convert - -resize 50% "${filename_lower}.png"
 *     echo "Screenshot saved as ${filename_lower}.png"
 * done
 *
 * echo "All scenarios processed."
 */
@AndroidEntryPoint
class CreateScreenshotsActivity : ComponentActivity() {
    private val viewModel: MountainsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val scenarioIntent = try {
                Scenario.valueOf(intent.getStringExtra("scenario")?.uppercase() ?: "NULL_ISLAND")
            } catch (e: IllegalArgumentException) {
                Scenario.NULL_ISLAND
            }

        val autoAdvance = intent.getIntExtra("autoadvance", -1)

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
                        mutableStateOf<Zoom>(Zoom.ORIGIN)
                    }

                    var scenario by remember {
                        mutableStateOf(scenarioIntent)
                    }

                    if (autoAdvance > 0) {
                        LaunchedEffect(Unit) {
                            while (true) {
                                Scenario.entries.forEach {
                                    scenario = it
                                    delay(autoAdvance.seconds)
                                }
                            }
                        }
                    }

                    val context = LocalView.current.context
                    LaunchedEffect(scenario) {
                        Toast.makeText(context, "Switching to ${scenario.prettyPrint()}", Toast.LENGTH_SHORT).show()
                    }

                    LaunchedEffect(mountainList, mapLoaded, zoom) {
                        if (mapLoaded) {
                            scope.launch {
                                when (val z = zoom) {
                                    Zoom.ORIGIN -> {
                                        cameraPositionState.animate(
                                            update = CameraUpdateFactory.newLatLngZoom(
                                                LatLng(0.0, 0.0),
                                                1f
                                            ),
                                            durationMs = 1000
                                        )
                                    }
                                    Zoom.EXTENTS -> {
                                        zoomAll(scope, cameraPositionState, mountainList.boundingBox)
                                    }
                                    is Zoom.CUSTOM -> {
                                        cameraPositionState.animate(
                                            update = CameraUpdateFactory.newLatLngZoom(
                                                mountainList.boundingBox.center,
                                                z.zoom
                                            ),
                                            durationMs = 1000
                                        )
                                    }
                                }
                            }
                        }
                    }

                    when (scenario) {
                        Scenario.NULL_ISLAND -> {
                            zoom = Zoom.ORIGIN

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
                            zoom = Zoom.CUSTOM(5f)

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
                            zoom = Zoom.EXTENTS

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
                            zoom = Zoom.EXTENTS

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

                        Scenario.BASIC_MARKERS_CUSTOMIZED -> {
                            zoom = Zoom.EXTENTS

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
                            zoom = Zoom.EXTENTS

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

                        Scenario.ADVANCED_MARKERS_CUSTOMIZED -> {
                            zoom = Zoom.EXTENTS

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
                            zoom = Zoom.CUSTOM(8f)

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
                            zoom = Zoom.EXTENTS

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
                            zoom = Zoom.CUSTOM(5f)

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

                        Scenario.CLUSTERED_MARKERS_CUSTOMIZED -> {
                            zoom = Zoom.CUSTOM(8f)

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

                        Scenario.BASIC_MARKERS_WITH_COLORADO -> {
                            zoom = Zoom.CUSTOM(5f)

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
                            zoom = Zoom.CUSTOM(9f)

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
                            zoom = Zoom.CUSTOM(9f)

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
                            zoom = Zoom.EXTENTS

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

                        Scenario.INTRO ->  {
                            zoom = Zoom.EXTENTS

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
                                    showRanges = false,
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

private fun Scenario.prettyPrint(): String = this.name.split("_")
    .joinToString(" ") {
        it.lowercase().replaceFirstChar { char -> char.uppercase() }
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
        onEvent = { },
        cameraPositionState = parameters.cameraPositionState,
        showMarkers = parameters.showMarkers,
        showColorado = parameters.showColorado,
        showRanges = parameters.showRanges,
        styleMarkers = parameters.styleMarkers,
        showScaleBar = parameters.showScaleBar,
        onMapLoaded = onMapLoaded,
    )
}
