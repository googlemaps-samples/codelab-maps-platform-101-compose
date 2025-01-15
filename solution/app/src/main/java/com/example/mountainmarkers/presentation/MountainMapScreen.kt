package com.example.mountainmarkers.presentation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mountainmarkers.BottomNav
import com.example.mountainmarkers.MarkerType
import com.example.mountainmarkers.MountainMapTopBar
import com.example.mountainmarkers.presentation.common.BigSpinner
import com.google.maps.android.compose.CameraPositionState

@Composable
fun MountainMapScreen(
    markerType: MarkerType,
    loading: Boolean,
    mountains: MountainList,
    showAllMountains: Boolean,
    onEvent: (MountainsViewModelEvent) -> Unit,
    cameraPositionState: CameraPositionState,
    showMarkers: Boolean = true,
    showColorado: Boolean = true,
    showRanges: Boolean = true,
    styleMarkers: Boolean = true,
    showScaleBar: Boolean = true,
    mapBearing: Float = 0f,
    onMapLoaded: () -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
        topBar = {
            MountainMapTopBar(
                topBarTitleStringRes = markerType.title,
                showAllChecked = showAllMountains,
                onZoomAllClick = {
                    onEvent(MountainsViewModelEvent.OnZoomAll)
                },
                onToggleShowAllClick = {
                    onEvent(MountainsViewModelEvent.OnToggleAllPeaks)
                }
            )
        },
        bottomBar = { BottomNav(markerType) { onEvent(MountainsViewModelEvent.OnMarkerTypeChange(it)) } }
    ) { paddingValues ->

        if (loading) {
            BigSpinner(paddingValues)
        } else {
            MountainMap(
                paddingValues = paddingValues,
                mountains = mountains,
                selectedMarkerType = markerType,
                cameraPositionState = cameraPositionState,
                showMarkers = showMarkers,
                showColorado = showColorado,
                showRanges = showRanges,
                styleMarkers = styleMarkers,
                onMapLoaded = onMapLoaded,
                showScaleBar = showScaleBar,
                mapBearing = mapBearing,
                onCompassClicked = { onEvent(MountainsViewModelEvent.OnCompassClick) },
            )
        }
    }
}
