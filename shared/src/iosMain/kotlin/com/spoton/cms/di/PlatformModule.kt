package com.spoton.cms.di

import com.spoton.cms.util.ImagePicker
import com.spoton.cms.util.IOSImagePicker
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ImagePicker> { IOSImagePicker() }
}
