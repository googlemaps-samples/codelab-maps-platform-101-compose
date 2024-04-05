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

package com.example.mountainmarkers.data.local

import com.example.mountainmarkers.data.utils.Meters
import com.example.mountainmarkers.data.utils.feet
import com.google.android.gms.maps.model.LatLng

/**
 * Data class holding information about a mountain.
 */
data class Mountain(
    val id: Int,
    val name: String,
    val location: LatLng,
    val elevation: Meters,
)

/**
 * Extension function to determine whether a mountain is a "14er", i.e., has an elevation greater
 * than 14,000 feet (~4267 meters).
 */
fun Mountain.is14er() = elevation >= 14_000.feet
