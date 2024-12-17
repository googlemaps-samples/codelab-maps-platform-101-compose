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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mountainmarkers.MountainMarkersApplication
import com.example.mountainmarkers.data.utils.m
import com.example.mountainmarkers.subjects.assertThat
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MountainsRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mountainRepository: MountainsRepository
    private lateinit var context: Context
    private lateinit var testScope: CoroutineScope

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext<MountainMarkersApplication>()
        testScope = CoroutineScope(testDispatcher)
        mountainRepository = MountainsRepository(context.assets, testScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel() // Cancel the test scope to clean up
    }

    @Test
    fun canLoadMountainData() = runTest {
        launch {
            val loadingValues = mutableListOf<Boolean>()
            val mountainsValues = mutableListOf<List<Mountain>>()

            // Collect the loading flow
            val loadingJob = launch {
                mountainRepository.loading.collect { isLoading ->
                    loadingValues.add(isLoading)
                }
            }

            val mountainsJob = launch {
                mountainRepository.mountains.collect { mountains ->
                    mountainsValues.add(mountains)
                }
            }

            mountainRepository.loadMountains()

            // Fire off multiple loadMountains calls to ensure the data is loaded only once!
            mountainRepository.loadMountains()
            mountainRepository.loadMountains()

            testScheduler.advanceUntilIdle()

            loadingJob.cancel()
            mountainsJob.cancel()

            assertThat(loadingValues).containsExactly(true, false).inOrder()

            assertThat(mountainsValues).hasSize(2)
            assertThat(mountainsValues[0]).isEmpty()

            val mountains = mountainsValues[1]
            assertThat(mountains).hasSize(143)

            with(mountains.first { it.name == "Mount Sneffels" }) {
                assertThat(name).isEqualTo("Mount Sneffels")
                assertThat(elevation.value).isWithin(1.0e-6).of(4315.4)
                assertThat(location).isWithin(3.m).of(38.0038, -107.7923)
                assertThat(is14er()).isTrue()
            }

            with(mountains.first { it.name.contains("\uD83D\uDC3B") }) {
                assertThat(name).isEqualTo("Grizzly Peak \uD83D\uDC3B")
                assertThat(elevation.value).isWithin(1.0e-6).of(4265.6)
                assertThat(location).isWithin(3.m).of(39.0425, -106.5976)
                assertThat(is14er()).isFalse()
            }
        }
    }
}
