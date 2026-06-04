plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.sqldelight)
}

kotlin {
    android {
        namespace = "app.pardis.core.database"
        compileSdk = 35
        minSdk = 24
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreDatabase"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
    }
}

sqldelight {
    databases {
        create("PardisDatabase") {
            packageName.set("app.pardis.core.database")
        }
    }
}