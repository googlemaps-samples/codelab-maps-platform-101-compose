plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force("androidx.core:core:1.18.0")
            force("androidx.core:core-ktx:1.18.0")
        }
    }
}
