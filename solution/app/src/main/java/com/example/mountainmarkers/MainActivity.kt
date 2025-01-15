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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mountainmarkers.data.utils.ImperialUnitsConverter
import com.example.mountainmarkers.data.utils.LocalUnitsConverter
import com.example.mountainmarkers.data.utils.MetricUnitsConverter
import com.example.mountainmarkers.presentation.MountainMapScreen
import com.example.mountainmarkers.presentation.MountainsScreenEvent
import com.example.mountainmarkers.presentation.MountainsViewModel
import com.example.mountainmarkers.presentation.MountainsViewModelEvent
import com.example.mountainmarkers.presentation.snapToNorth
import com.example.mountainmarkers.presentation.zoomAll
import com.example.mountainmarkers.ui.theme.MountainMarkersTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MountainsViewModel by viewModels()

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val markerType by viewModel.markerType.collectAsStateWithLifecycle()
            val loading by viewModel.loading.collectAsStateWithLifecycle()
            val mountains by viewModel.mountains.collectAsStateWithLifecycle()
            val showAllMountains by viewModel.showAllMountains.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            val eventFlow = viewModel.getEventChannel()
            val mapBearing by viewModel.mapBearing.collectAsStateWithLifecycle()

            val unitsConverter = if (LocalConfiguration.current.locales.get(0).country == "US") {
                ImperialUnitsConverter
            } else {
                MetricUnitsConverter
            }

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(mountains.boundingBox.center, 5f)
            }

            // Optionally, send changes to the camera position to the ViewModel
            LaunchedEffect(cameraPositionState) {
                scope.launch {
                    snapshotFlow { cameraPositionState.position }
//                        .debounce(300)
                        .collect {
                            viewModel.onEvent(MountainsViewModelEvent.OnCameraBearingChange(it.bearing))
                            cameraPositionState.projection?.let {
                                viewModel.onEvent(MountainsViewModelEvent.OnCameraChange(it))
                            }
                        }
                }
            }

            LaunchedEffect(mountains.boundingBox) {
                zoomAll(
                    scope = scope,
                    cameraPositionState,
                    mountains.boundingBox
                )
            }

            LaunchedEffect(eventFlow) {
                eventFlow.collect { event ->
                    when (event) {
                        MountainsScreenEvent.OnZoomAll -> {
                            zoomAll(scope, cameraPositionState, mountains.boundingBox)
                        }

                        MountainsScreenEvent.OnSnapToNorth -> {
                            snapToNorth(scope, cameraPositionState)
                        }
                    }
                }
            }

            CompositionLocalProvider(
                LocalUnitsConverter provides unitsConverter
            ) {
                MountainMarkersTheme(
                    dynamicColor = false
                ) {
                    MountainMapScreen(
                        markerType = markerType,
                        loading = loading,
                        mountains = mountains,
                        showAllMountains = showAllMountains,
                        onEvent = { viewModel.onEvent(it) },
                        cameraPositionState = cameraPositionState,
                        mapBearing = mapBearing,
                    )
                }
            }
        }
    }
}
