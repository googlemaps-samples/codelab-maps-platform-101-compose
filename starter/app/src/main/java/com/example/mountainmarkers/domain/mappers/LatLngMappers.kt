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

package com.example.mountainmarkers.domain.mappers

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Creates a [LatLngBounds] object from a collection of [LatLng] objects.  The collection must
 * contain at least one LatLng.
 */
fun Collection<LatLng>.toLatLngBounds() : LatLngBounds {
  if (isEmpty()) error("Cannot create a LatLngBounds from an empty list")

  return LatLngBounds.builder().apply {
    for (latLng in this@toLatLngBounds) {
      include(latLng)
    }
  }.build()
}
