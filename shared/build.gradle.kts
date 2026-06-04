import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.skie)
}

val sharedFrameworkBundleId = "app.pardis.shared"

kotlin {
    android {
        namespace = "app.pardis.shared"
        compileSdk = 35
        minSdk = 24
    }

    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework(buildTypes = setOf(NativeBuildType.DEBUG)) {
            baseName = "Shared"
            isStatic = false
            binaryOption("bundleId", sharedFrameworkBundleId)
        }
        iosTarget.binaries.framework(buildTypes = setOf(NativeBuildType.RELEASE)) {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", sharedFrameworkBundleId)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:di"))
            implementation(project(":core:domain"))
            implementation(project(":core:network"))
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
            api(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            // turbine for flow testing if added
        }
    }
}

skie {
    features {
        enableSwiftUIObservingPreview = true
    }
}