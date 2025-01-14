package com.example.mountainmarkers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.example.mountainmarkers.data.utils.ImperialUnitsConverter
import com.example.mountainmarkers.data.utils.LocalUnitsConverter
import com.example.mountainmarkers.presentation.EmptyLatLngBounds
import com.example.mountainmarkers.presentation.MountainList
import com.example.mountainmarkers.presentation.MountainMapScreen
import com.example.mountainmarkers.ui.theme.MountainMarkersTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState

class BasicMarkersTest {
    @Preview
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
                position = CameraPosition.fromLatLngZoom(mountains.boundingBox.center, 5f)
            }

            MountainMarkersTheme(dynamicColor = false) {
                MountainMapScreen(
                    markerType = markerType,
                    loading = loading,
                    mountains = mountains,
                    showAllMountains = showAllMountains,
                    onEvent = { },
                    cameraPositionState = cameraPositionState,
                    showMarkers = false
                )
            }
        }
    }
}
