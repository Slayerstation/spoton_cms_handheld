plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget()
    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)

            // Decompose
            implementation(libs.decompose)
            implementation(libs.decompose.compose)
            
            // Rich Text Editor
            implementation(libs.richeditor)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.json)

            // SQLDelight
            implementation(libs.sqldelight.coroutines)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // KotlinX
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)

            // Settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)

            // Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.core)
        }

        androidMain.dependencies {
            // Ktor Engine
            implementation(libs.ktor.client.okhttp)
            
            // Coil Network
            implementation(libs.coil.network)

            // SQLDelight Driver
            implementation(libs.sqldelight.android.driver)

            // Koin
            implementation(libs.koin.android)

            // AndroidX
            api(libs.androidx.activity.compose)
            api(libs.androidx.core.ktx)

            // CameraX for barcode scanning
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)

            // ML Kit Barcode
            implementation(libs.mlkit.barcode)
        }

        iosMain.dependencies {
            // Ktor Engine
            implementation(libs.ktor.client.darwin)

            // SQLDelight Driver
            implementation(libs.sqldelight.native.driver)
        }

        jvmMain.dependencies {
            // Ktor Engine
            implementation(libs.ktor.client.okhttp)

            // Coil Network
            implementation(libs.coil.network)

            // SQLDelight Driver
            implementation(libs.sqldelight.jvm.driver)

            // Coroutines Main Dispatcher for Swing
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.spoton.cms.shared"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("SpotOnDatabase") {
            packageName.set("com.spoton.cms.db")
        }
    }
}
