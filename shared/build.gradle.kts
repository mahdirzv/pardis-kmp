import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.skie)
    alias(libs.plugins.kotlinSerialization)
}

val sharedFrameworkBundleId = "app.pardis.shared"

kotlin {
    android {
        namespace = "app.pardis.shared"
        compileSdk = 37
        minSdk = 24
        // Opt-in to JVM host-side unit tests so commonTest runs locally without Xcode/a device
        // (the iOS test targets need full Xcode to link/run; host tests run on the JVM).
        withHostTest { }
    }

    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework(buildTypes = setOf(NativeBuildType.DEBUG)) {
            baseName = "Shared"
            isStatic = false
            linkerOpts("-lsqlite3")
            // Expose core:model types (Story, StoryPage, VocabItem) that appear in the public API
            // (e.g. ReaderUiState/LibraryUiState) so Swift can name them. api() alone compiles but
            // does not put them in the framework's Swift API.
            export(project(":core:model"))
            export(project(":core:data"))
            binaryOption("bundleId", sharedFrameworkBundleId)
        }
        iosTarget.binaries.framework(buildTypes = setOf(NativeBuildType.RELEASE)) {
            baseName = "Shared"
            isStatic = true
            linkerOpts("-lsqlite3")
            export(project(":core:model"))
            export(project(":core:data"))
            binaryOption("bundleId", sharedFrameworkBundleId)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:di"))
            // api (not implementation) so the framework can export it: the iOS app references
            // IosPlatformModuleKt.iosOfflineAssetCacheModule (core:data iosMain) to wire the real cache.
            api(project(":core:data"))
            implementation(project(":core:domain"))
            implementation(project(":core:network"))
            api(libs.androidx.lifecycle.viewmodel)
            // savedstate is Android/JVM only; do not put in common, or it breaks iOS native metadata resolution
            // (pulls coroutines-android which has no native variant)
            api(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
        }
        androidMain.dependencies {
            // Only for Android; provides SavedStateHandle support if VMs ever use it.
            // Current VMs (Library/Reader) do not use it, but kept for compatibility with Android KMP patterns.
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            // ProfileRepositoryImpl's public ctor takes PardisDatabase? (from core:database), which
            // core:data only pulls via implementation — so the test needs it on its classpath.
            implementation(project(":core:database"))
            // turbine for flow testing if added
        }
    }
}

skie {
    isEnabled = true  // Updated to 0.10.12 which supports Kotlin 2.1.20
    features {
        enableSwiftUIObservingPreview = true
    }
}

// Workaround for "The archives configuration has been deprecated for artifact declaration."
// See https://youtrack.jetbrains.com/issue/KT-61096 and Gradle 9/10 deprecation.
// This will be needed until the Kotlin Multiplatform plugin updates its artifact publication logic.
afterEvaluate {
    configurations.findByName("archives")?.artifacts?.clear()
}