package com.example.mountainmarkers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mountainmarkers.presentation.MountainsScreenEvent
import com.example.mountainmarkers.presentation.MountainsScreenViewState
import kotlinx.coroutines.flow.Flow

/**
 * Shows a [GoogleMap] with collection of markers
 */
@Composable
fun MountainMap(
    paddingValues: PaddingValues,
    viewState: MountainsScreenViewState.MountainList,
    eventFlow: Flow<MountainsScreenEvent>,
    selectedMarkerType: MarkerType,
) {
    var isMapLoaded by remember { mutableStateOf(false) }

    // TODO: Create cameraPositionState

    // TODO: Create scope from rememberCoroutineScope
    //   Add LaunchedEffect to zoom when the bounding box changes
    //   Add LaunchedEffect to react to events from the ViewModel

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // TODO: Add GoogleMap here

        // TODO: Add cameraPositionState to GoogleMap

        // TODO: Add GoogleMap content

        // TODO: Add call to ColoradoPolygon.  Inside the GoogleMap content, but outside of the when
        // statement

        // TODO: Add code to add KmlLayer.  Inside the GoogleMap content, but outside of the when
        // statement

        // TODO: Add ScaleBar outside of of the GoogleMap content

        if (!isMapLoaded) {
            AnimatedVisibility(
                modifier = Modifier.matchParentSize(),
                visible = !isMapLoaded,
                enter = EnterTransition.None,
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .wrapContentSize()
                )
            }
        }
    }
}

// TODO: Create zoomAll function

// TODO: Create ColoradoPolygon function