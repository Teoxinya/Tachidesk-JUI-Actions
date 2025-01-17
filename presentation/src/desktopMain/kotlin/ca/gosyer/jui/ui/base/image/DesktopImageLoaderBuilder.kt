/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.image

import ca.gosyer.jui.core.io.userDataDir
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.uicore.vm.ContextWrapper
import com.seiko.imageloader.cache.disk.DiskCacheBuilder
import com.seiko.imageloader.cache.memory.MemoryCacheBuilder
import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.option.Options

actual val imageConfig: Options.ImageConfig = Options.ImageConfig.ARGB_8888

actual fun ComponentRegistryBuilder.register(contextWrapper: ContextWrapper, http: Http) {
    setupDefaultComponents(httpClient = { http })
}

actual fun DiskCacheBuilder.configure(contextWrapper: ContextWrapper, cacheDir: String) {
    directory(userDataDir / cacheDir)
    maxSizeBytes(1024 * 1024 * 150) // 150 MB
}

actual fun MemoryCacheBuilder.configure(contextWrapper: ContextWrapper) {
}
