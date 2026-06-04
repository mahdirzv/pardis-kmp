plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
}

kotlin {
    android {
        namespace = "app.pardis.core.data"
        compileSdk = 35
        minSdk = 24
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreData"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(project(":core:network"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}