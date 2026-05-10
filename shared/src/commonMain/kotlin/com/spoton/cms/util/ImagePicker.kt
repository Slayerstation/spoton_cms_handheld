package com.spoton.cms.util

import kotlinx.coroutines.flow.SharedFlow

data class PickedImage(
    val fileName: String,
    val byteArray: ByteArray
)

interface ImagePicker {
    fun pickImage()
    fun takePhoto()
    val imageFlow: SharedFlow<PickedImage?>
}
