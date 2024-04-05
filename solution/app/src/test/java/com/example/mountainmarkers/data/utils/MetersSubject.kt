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

package com.example.mountainmarkers.data.utils

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth

fun assertThat(meters: Meters): MetersSubject {
    return Truth.assertAbout(metersSubjectFactory()).that(meters)
}

fun metersSubjectFactory(): Subject.Factory<MetersSubject, Meters> {
    return Subject.Factory<MetersSubject, Meters> { metaData, target ->
        MetersSubject(
            metaData,
            target
        )
    }
}

class MetersSubject(
    metadata: FailureMetadata,
    private val actual: Meters?
) : Subject(metadata, actual) {
    inner class TolerantComparison(private val tolerance: Double) {
        fun of(other: Meters) {
            if (actual == null) {
                failWithoutActual(Fact.simpleFact("actual was null"))
            } else {
                if ((actual - other).value > tolerance) {
                    failWithActual(Fact.simpleFact("expected $actual to be within $tolerance meters of $other, but was ${actual - other}"))
                }
            }
        }
    }

    fun isWithin(tolerance: Double): TolerantComparison {
        return TolerantComparison(tolerance)
    }

    fun isWithin(tolerance: Meters): TolerantComparison {
        return TolerantComparison(tolerance.value)
    }

    fun isAboutEqualTo(other: Meters) {
        this.isWithin(EPSILON).of(other)
    }

    companion object {
        const val EPSILON: Double = 0.00001
    }
}
