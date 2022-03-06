/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.ui.library.components.LibraryScreenContent
import ca.gosyer.ui.manga.MangaScreen
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class LibraryScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<LibraryScreenViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        LibraryScreenContent(
            categories = vm.categories.collectAsState().value,
            selectedCategoryIndex = vm.selectedCategoryIndex.collectAsState().value,
            displayMode = vm.displayMode.collectAsState().value,
            gridColumns = vm.gridColumns.collectAsState().value,
            gridSize = vm.gridSize.collectAsState().value,
            isLoading = vm.isLoading.collectAsState().value,
            error = vm.error.collectAsState().value,
            query = vm.query.collectAsState().value,
            updateQuery = vm::updateQuery,
            getLibraryForPage = { vm.getLibraryForCategoryId(it).collectAsState() },
            onPageChanged = vm::setSelectedPage,
            onClickManga = { navigator push MangaScreen(it) },
            onRemoveMangaClicked = vm::removeManga
        )
    }
}
