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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mountainmarkers.data.utils.DMS
import com.example.mountainmarkers.data.utils.Direction.WEST
import com.example.mountainmarkers.data.utils.toDecimalDegrees
import com.example.mountainmarkers.presentation.AdvancedMarkersMapContent
import com.example.mountainmarkers.presentation.BasicMarkersMapContent
import com.example.mountainmarkers.presentation.ClusteringMarkersMapContent
import com.example.mountainmarkers.presentation.MountainsScreenEvent
import com.example.mountainmarkers.presentation.MountainsScreenViewState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.ScaleBar
import com.google.maps.android.data.kml.KmlLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Shows a [GoogleMap] with collection of markers
 */
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MountainMap(
    paddingValues: PaddingValues,
    viewState: MountainsScreenViewState.MountainList,
    eventFlow: Flow<MountainsScreenEvent>,
    selectedMarkerType: MarkerType,
) {
    var isMapLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapId = stringResource(id = R.string.map_id)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(viewState.boundingBox.center, 5f)
    }

    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json)
            )
        )
    }

    LaunchedEffect(true) {
        eventFlow.collect { event ->
            when (event) {
                MountainsScreenEvent.OnZoomAll -> {
                    zoomAll(scope, cameraPositionState, viewState.boundingBox)
                }
            }
        }
    }

    LaunchedEffect(key1 = viewState.boundingBox) {
        zoomAll(scope, cameraPositionState, viewState.boundingBox)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapLoaded = { isMapLoaded = true },
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId(mapId)
            }
        ) {
            ColoradoPolygon()

            when (selectedMarkerType) {
                MarkerType.Basic -> {
                    BasicMarkersMapContent(
                        mountainsScreenViewState = viewState,
                    )
                }

                MarkerType.Advanced -> {
                    AdvancedMarkersMapContent(
                        mountainsScreenViewState = viewState,
                    )
                }

                MarkerType.Clustered -> {
                    ClusteringMarkersMapContent(
                        mountainsScreenViewState = viewState,
                        onClusterClick = { cluster ->
                            val newZoom = cameraPositionState.position.zoom + 1
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(
                                        cluster.position, newZoom
                                    ),
                                    durationMs = 500,
                                )
                            }
                            false
                        },
                    )
                }
            }

            MapEffect(key1 = true) {map ->
                val layer = KmlLayer(map, R.raw.mountain_ranges, context)
                layer.addLayerToMap()
            }
        }

        ScaleBar(
            modifier = Modifier
                .padding(top = 5.dp, end = 15.dp)
                .align(Alignment.TopEnd),
            cameraPositionState = cameraPositionState
        )

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

@Composable
@GoogleMapComposable
fun ColoradoPolygon() {
    // There are obvious advantages to drawing Colorado in this way...
    val north = 41.0
    val south = 37.0
    val east = DMS(WEST, 102.0, 3.0).toDecimalDegrees()
    val west = DMS(WEST, 109.0, 3.0).toDecimalDegrees()

    val locations = listOf(
        LatLng(north, east),
        LatLng(south, east),
        LatLng(south, west),
        LatLng(north, west),
    )

    Polygon(
        points = locations,
        strokeColor = MaterialTheme.colorScheme.tertiary,
        strokeWidth = 3F,
        fillColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
    )
}

fun zoomAll(
    scope: CoroutineScope,
    cameraPositionState: CameraPositionState,
    boundingBox: LatLngBounds
) {
    scope.launch {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngBounds(boundingBox, 64),
            durationMs = 1000
        )
    }
}
