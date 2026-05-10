package com.spoton.cms.util

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AndroidImagePicker(private val activity: ComponentActivity) : ImagePicker {
    
    private val _imageFlow = MutableSharedFlow<PickedImage?>(replay = 1)
    override val imageFlow: SharedFlow<PickedImage?> = _imageFlow.asSharedFlow()

    private val getContent: ActivityResultLauncher<String> = activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bytes = activity.contentResolver.openInputStream(it)?.readBytes()
            if (bytes != null) {
                val fileName = "picked_image_${System.currentTimeMillis()}.jpg"
                _imageFlow.tryEmit(PickedImage(fileName, bytes))
            }
        }
    }

    override fun pickImage() {
        getContent.launch("image/*")
    }

    override fun takePhoto() {
        // For simplicity in Phase 3, we focus on Gallery picking. 
        // Camera requires FileProvider setup which is more complex.
        pickImage()
    }
}
