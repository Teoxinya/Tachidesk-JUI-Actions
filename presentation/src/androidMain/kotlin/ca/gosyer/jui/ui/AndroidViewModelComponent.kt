/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.SavedStateHandle
import ca.gosyer.jui.ui.base.LocalViewModels
import ca.gosyer.jui.ui.base.state.addScreenModelKey
import ca.gosyer.jui.ui.base.state.createSavedStateHandle
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.androidx.AndroidScreenLifecycleOwner
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

actual interface ViewModelComponent : SharedViewModelComponent

@Composable
actual inline fun <reified VM : ViewModel> Screen.stateViewModel(
    tag: String?,
    crossinline factory: @DisallowComposableCalls ViewModelComponent.(SavedStateHandle) -> VM
): VM {
    val viewModelFactory = LocalViewModels.current
    val lifecycle = LocalLifecycleOwner.current as AndroidScreenLifecycleOwner
    val handle = remember {
        lifecycle.defaultViewModelCreationExtras
            .addScreenModelKey<VM>(this, tag)
            .createSavedStateHandle()
    }
    return rememberScreenModel(tag) { viewModelFactory.factory(handle) }
}
