plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
}

kotlin {
    android {
        namespace = "app.pardis.core.domain"
        compileSdk = 35
        minSdk = 24
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreDomain"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}