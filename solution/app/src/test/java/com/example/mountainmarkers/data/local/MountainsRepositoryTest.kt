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

import androidx.test.core.app.ApplicationProvider
import com.example.mountainmarkers.data.utils.m
import com.example.mountainmarkers.subjects.assertThat
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MountainsRepositoryTest {
    @Test
    fun canLoadMountainData() = runBlocking {
        val repo = MountainsRepository(ApplicationProvider.getApplicationContext())
        val mountains = repo.loadMountains().value

        assertThat(mountains).hasSize(143)

        val sneffels = mountains.first { it.name == "Mount Sneffels" }

        assertThat(sneffels.name).isEqualTo("Mount Sneffels")
        assertThat(sneffels.elevation.value).isWithin(1.0e-6).of(4315.4)
        assertThat(sneffels.location).isWithin(3.m).of(38.0038, -107.7923)
        assertThat(sneffels.is14er()).isTrue()
    }
}
