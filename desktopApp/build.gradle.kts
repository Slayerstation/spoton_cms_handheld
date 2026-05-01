plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(projects.shared)
            implementation(compose.desktop.currentOs)
            implementation(libs.decompose)
            implementation(libs.decompose.compose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.datetime)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.spoton.cms.MainKt"

        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "SpotOnCMS"
            packageVersion = "1.0.0"
            
            windows {
                shortcut = true
                menu = true
                // iconFile.set(project.file("icon.ico"))
            }
        }
    }
}
