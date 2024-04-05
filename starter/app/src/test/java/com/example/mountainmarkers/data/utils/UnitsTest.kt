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

import com.google.common.truth.Truth
import org.junit.Test

class UnitsTest {
    @Test
    fun metersConversions() {
        assertThat(10.meters).isEqualTo(10.m)
        assertThat(1.km).isEqualTo(1_000.m)
        assertThat(10.feet).isAboutEqualTo(3.048.m)
        assertThat(1.miles).isAboutEqualTo(1609.3445.m)

        Truth.assertThat(300.meters.toFeet).isWithin(0.0001).of(984.252)
        Truth.assertThat(10.km.toMiles).isWithin(0.0001).of(6.21371)
        Truth.assertThat(1234.m.toKilometers).isWithin(0.0001).of(1.234)
    }
}
