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

import androidx.compose.runtime.Composable
import com.example.mountainmarkers.presentation.MountainsScreenViewState.MountainList
import com.google.android.gms.maps.model.Marker

/**
 * [GoogleMapComposable] which renders a [MountainList] as a set of [AdvancedMarker]s
 */
@Composable
fun AdvancedMarkersMapContent(
    mountainsScreenViewState: MountainList,
    onMountainClick: (Marker) -> Boolean = { false },
) {
}
