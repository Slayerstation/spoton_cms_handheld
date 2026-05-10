package com.spoton.cms

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.arkivanov.decompose.defaultComponentContext
import com.spoton.cms.di.sharedModule
import com.spoton.cms.navigation.RootComponent
import com.spoton.cms.ui.MainView
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        // Initialize Koin (only once for shared/platform modules)
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(applicationContext)
                modules(sharedModule, com.spoton.cms.di.platformModule)
            }
        }

        // Create Image Picker immediately in onCreate (must be done before STARTED state)
        val imagePicker = com.spoton.cms.util.AndroidImagePicker(this)

        // Always reload the ImagePicker module to bind it to the current instance
        val imagePickerModule = org.koin.dsl.module {
            single<com.spoton.cms.util.ImagePicker> { imagePicker }
        }
        org.koin.core.context.loadKoinModules(imagePickerModule)

        // Create root component with the Activity's lifecycle
        val rootComponent = RootComponent(defaultComponentContext())

        setContent {
            MainView(rootComponent)
        }
    }
}
