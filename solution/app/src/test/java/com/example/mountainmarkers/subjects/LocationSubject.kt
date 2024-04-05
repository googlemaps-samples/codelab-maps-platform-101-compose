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

package com.example.mountainmarkers.subjects

import com.google.common.truth.Subject
import androidx.annotation.Nullable
import com.example.mountainmarkers.data.utils.Meters
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Truth
import com.google.maps.android.ktx.utils.sphericalDistance
import kotlin.math.abs

class LatLngSubject(metadata: FailureMetadata, private val actual: LatLng?) :
    Subject(metadata, actual) {
    inner class TolerantComparison(private val tolerance: Double) {

        fun of(latitude: Double, longitude: Double) {
            of(LatLng(latitude, longitude))
        }

        fun of(other: LatLng) {
            if (actual == null) {
                failWithoutActual(Fact.simpleFact("actual was null"))
            } else {
                val distance = actual.sphericalDistance(other)
                if (distance > tolerance) {
                    failWithActual(Fact.simpleFact("expected LatLng to be within $tolerance meters of $other, but actual distance was $distance"))
                }
            }
        }
    }

    fun hasCoordinates(latitude: Double, longitude: Double, epsilon: Double = EPSILON) {
        if (actual == null) {
            failWithoutActual(Fact.simpleFact("actual was null"))
        } else {
            if (abs(actual.latitude - latitude) > epsilon || abs(actual.longitude - longitude) > epsilon) {
                val latDiff = actual.latitude - latitude
                val lngDiff = actual.longitude - longitude
                val distance = actual.sphericalDistance(LatLng(latitude, longitude))
                failWithActual(Fact.simpleFact("expected coordinates to be $latitude, $longitude (delta $latDiff, $lngDiff; distance = $distance)"))
            }
        }
    }

    fun isWithin(tolerance: Meters): TolerantComparison {
        return TolerantComparison(tolerance.value)
    }

    companion object {
        const val EPSILON: Double = 0.00001
    }
}

fun assertThat(@Nullable latLng: LatLng): LatLngSubject {
    return Truth.assertAbout(latLngSubjectFactory()).that(latLng)
}

fun latLngSubjectFactory(): Subject.Factory<LatLngSubject, LatLng> {
    return Subject.Factory<LatLngSubject, LatLng> { metaData, target ->
        LatLngSubject(
            metaData,
            target
        )
    }
}
