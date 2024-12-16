// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.secrets.gradle.plugin) apply false

//    id("com.android.application") version "8.7.3" apply false
//    id("org.jetbrains.kotlin.android") version "2.0.21" apply false

    id("com.google.dagger.hilt.android") version "2.50" apply false
}

//buildscript {
//    dependencies {
//        classpath(libs.secrets.gradle.plugin)
//    }
//}
