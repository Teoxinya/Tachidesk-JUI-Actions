/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ca.gosyer.i18n.MR
import ca.gosyer.presentation.build.BuildKonfig
import com.vanpra.composematerialdialogs.DesktopWindowPosition
import com.vanpra.composematerialdialogs.MaterialDialogProperties
import com.vanpra.composematerialdialogs.SecurePolicy

@Composable
fun getMaterialDialogProperties(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    securePolicy: SecurePolicy = SecurePolicy.Inherit,
    usePlatformDefaultWidth : Boolean = false,
    position: DesktopWindowPosition = DesktopWindowPosition(Alignment.Center),
    size: DpSize = DpSize(400.dp, 300.dp),
    title: String = BuildKonfig.NAME,
    icon: Painter = remember { MR.images.icon.image.toPainter() },
    resizable: Boolean = true
): MaterialDialogProperties {
    return MaterialDialogProperties(
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        securePolicy = securePolicy,
        usePlatformDefaultWidth = usePlatformDefaultWidth,
        position = position,
        size = size,
        title = title,
        icon = icon,
        resizable = resizable
    )
}