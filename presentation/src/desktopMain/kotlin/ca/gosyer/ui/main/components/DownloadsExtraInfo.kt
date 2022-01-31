/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.gosyer.data.base.WebsocketService
import ca.gosyer.i18n.MR
import ca.gosyer.ui.downloads.DownloadsScreenViewModel
import ca.gosyer.uicore.resources.stringResource
import ca.gosyer.uicore.vm.LocalViewModelFactory

@Composable
fun DownloadsExtraInfo() {
    val vmFactory = LocalViewModelFactory.current
    val vm = remember { vmFactory.instantiate<DownloadsScreenViewModel>(true) }
    val status by vm.serviceStatus.collectAsState()
    val list by vm.downloadQueue.collectAsState()
    val text = when (status) {
        WebsocketService.Status.STARTING -> stringResource(MR.strings.downloads_loading)
        WebsocketService.Status.RUNNING -> {
            if (list.isNotEmpty()) {
                stringResource(MR.strings.downloads_remaining, list.size)
            } else null
        }
        WebsocketService.Status.STOPPED -> null
    }
    if (text != null) {
        Text(
            text,
            style = MaterialTheme.typography.body2,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
        )
    } else if (status == WebsocketService.Status.STOPPED) {
        Surface(onClick = vm::restartDownloader, shape = RoundedCornerShape(4.dp)) {
            Text(
                stringResource(MR.strings.downloads_stopped),
                style = MaterialTheme.typography.body2,
                color = Color.Red.copy(alpha = ContentAlpha.disabled)
            )
        }
    }
}