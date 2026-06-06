/**
 * iOS App Gradle Module
 *
 * This module orchestrates iOS app builds and integrates with the Gradle build system.
 * The actual iOS app is a native SwiftUI project managed by Xcode, but we coordinate
 * the Shared.framework build and provide convenient Gradle tasks.
 */

plugins {
    // No plugins needed for a native iOS app; this is orchestration only
}

val sharedFrameworkPath = "${rootProject.projectDir}/shared/build/frameworks"
val projectName = project.name

// Task to print iOS build configuration
tasks.register("printConfig") {
    doLast {
        println("""
            ================================
            Pardis iOS App Configuration
            ================================
            Project: $projectName
            
            Shared Framework: $sharedFrameworkPath
            
            Build steps:
            1. Run: gradle :shared:assembleSharedReleaseXCFramework
            2. Open Xcode project (when created)
            3. Run: gradle :PardisiOSApp:buildDebug
            
            For development:
            - Use Xcode to build and run on simulator/device
            - Shared framework auto-builds on changes
            ================================
        """.trimIndent())
    }
}

// Task to build the shared framework for iOS debug
tasks.register("buildFrameworkDebug") {
    dependsOn(":shared:assembleSharedDebugXCFramework")
    doLast {
        println("✓ Shared.framework built for iOS Debug")
    }
}

// Task to build the shared framework for iOS release
tasks.register("buildFrameworkRelease") {
    dependsOn(":shared:assembleSharedReleaseXCFramework")
    doLast {
        println("✓ Shared.framework built for iOS Release")
    }
}

// Task to build all iOS debug variants
tasks.register("buildDebug") {
    group = "build"
    description = "Build Shared framework for iOS debug"
    dependsOn("buildFrameworkDebug")
    doLast {
        println("iOS debug build complete")
    }
}

// Task to build all iOS release variants
tasks.register("buildRelease") {
    group = "build"
    description = "Build Shared framework for iOS release"
    dependsOn("buildFrameworkRelease")
    doLast {
        println("iOS release build complete")
    }
}

// Task to list available architectures
tasks.register("listArchitectures") {
    doLast {
        println("""
            iOS Architectures Configured:
            - arm64 (physical device)
            - x86_64 (simulator Intel)
            - arm64 (simulator Apple Silicon)
            
            All variants will be bundled in XCFramework
        """.trimIndent())
    }
}

// Helper task to open Xcode (when project exists)
tasks.register("openXcode") {
    val xcodeProjectPath = "${projectDir}/iosApp.xcodeproj"
    doLast {
        val xcodeProj = file(xcodeProjectPath)
        if (xcodeProj.exists()) {
            Runtime.getRuntime().exec(arrayOf("open", xcodeProj.absolutePath))
            println("✓ Opened Xcode project")
        } else {
            println("⚠ Xcode project not found at: $xcodeProjectPath")
            println("  Create it first in Xcode or run xcodegen if you have a project.yml")
        }
    }
}

// Task to clean iOS artifacts
tasks.register("cleanFrameworks") {
    doLast {
        delete("${rootProject.projectDir}/shared/build/frameworks")
        println("✓ Cleaned iOS frameworks")
    }
}


