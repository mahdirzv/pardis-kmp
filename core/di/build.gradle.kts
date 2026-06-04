plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
}

kotlin {
    android {
        namespace = "app.pardis.core.di"
        compileSdk = 35
        minSdk = 24
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreDI"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(libs.koin.core)
        }
    }
}