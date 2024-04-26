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

package com.example.mountainmarkers.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.example.mountainmarkers.R
import com.example.mountainmarkers.data.local.Mountain
import com.example.mountainmarkers.data.local.is14er
import com.example.mountainmarkers.data.utils.toElevationString
import com.example.mountainmarkers.presentation.MountainsScreenViewState.MountainList
import com.example.mountainmarkers.presentation.utils.BitmapParameters
import com.example.mountainmarkers.presentation.utils.vectorToBitmap
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PinConfig
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.rememberMarkerState

/**
 * [GoogleMapComposable] which renders a [MountainList] as a set of [AdvancedMarker]s
 */
@Composable
@GoogleMapComposable
fun AdvancedMarkersMapContent(
    mountains: List<Mountain>,
    onMountainClick: (Marker) -> Boolean = { false },
) {
    val mountainIcon = vectorToBitmap(
        LocalContext.current,
        BitmapParameters(
            id = R.drawable.baseline_filter_hdr_24,
            iconColor = MaterialTheme.colorScheme.onSecondary.toArgb(),
        )
    )

    val mountainPin = with(PinConfig.builder()) {
        setGlyph(PinConfig.Glyph(mountainIcon))
        setBackgroundColor(MaterialTheme.colorScheme.secondary.toArgb())
        setBorderColor(MaterialTheme.colorScheme.onSecondary.toArgb())
        build()
    }

    val fourteenerIcon = vectorToBitmap(
        LocalContext.current,
        BitmapParameters(
            id = R.drawable.baseline_filter_hdr_24,
            iconColor = MaterialTheme.colorScheme.onPrimary.toArgb(),
        )
    )

    val fourteenerPin = with(PinConfig.builder()) {
        setGlyph(PinConfig.Glyph(fourteenerIcon))
        setBackgroundColor(MaterialTheme.colorScheme.primary.toArgb())
        setBorderColor(MaterialTheme.colorScheme.onPrimary.toArgb())
        build()
    }

    mountains.forEach { mountain ->
        val pin = if (mountain.is14er()) fourteenerPin else mountainPin
        AdvancedMarker(
            state = rememberMarkerState(position = mountain.location),
            title = mountain.name,
            snippet = mountain.elevation.toElevationString(),
            collisionBehavior = AdvancedMarkerOptions.CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL,
            pinConfig = pin,
            onClick = { marker ->
                onMountainClick(marker)
                false
            }
        )
    }
}
