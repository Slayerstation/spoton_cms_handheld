package com.spoton.cms.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.NSObject
import kotlinx.cinterop.*

class IOSImagePicker : ImagePicker {
    private val _imageFlow = MutableSharedFlow<PickedImage?>(replay = 1)
    override val imageFlow: SharedFlow<PickedImage?> = _imageFlow.asSharedFlow()

    private val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
        override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            image?.let {
                val data = UIImageJPEGRepresentation(it, 0.8)
                data?.let { nsData ->
                    val bytes = nsData.toByteArray()
                    val fileName = "picked_image_${NSDate().timeIntervalSince1970}.jpg"
                    _imageFlow.tryEmit(PickedImage(fileName, bytes))
                }
            }
            picker.dismissViewControllerAnimated(true, null)
        }

        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            picker.dismissViewControllerAnimated(true, null)
        }
    }

    override fun pickImage() {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (rootViewController == null) return

        val picker = UIImagePickerController()
        picker.delegate = delegate
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        rootViewController.presentViewController(picker, true, null)
    }

    override fun takePhoto() {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (rootViewController == null) return

        if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
            pickImage()
            return
        }

        val picker = UIImagePickerController()
        picker.delegate = delegate
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        rootViewController.presentViewController(picker, true, null)
    }
}

// Helper extension for NSData to ByteArray
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val byteArray = ByteArray(size)
    if (size > 0) {
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
    return byteArray
}
