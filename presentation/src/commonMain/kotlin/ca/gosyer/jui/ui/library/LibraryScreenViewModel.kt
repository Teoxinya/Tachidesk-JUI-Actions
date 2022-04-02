/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library

import ca.gosyer.jui.core.lang.getDefault
import ca.gosyer.jui.core.lang.lowercase
import ca.gosyer.jui.core.lang.withDefaultContext
import ca.gosyer.jui.data.library.LibraryPreferences
import ca.gosyer.jui.data.library.model.Sort
import ca.gosyer.jui.data.models.Category
import ca.gosyer.jui.data.models.Manga
import ca.gosyer.jui.data.server.interactions.CategoryInteractionHandler
import ca.gosyer.jui.data.server.interactions.LibraryInteractionHandler
import ca.gosyer.jui.data.server.interactions.UpdatesInteractionHandler
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.util.lang.Collator
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import io.fluidsonic.locale.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

private typealias CategoryItems = Pair<StateFlow<List<Manga>>, MutableStateFlow<List<Manga>>>
private typealias LibraryMap = MutableMap<Long, CategoryItems>
private data class Library(val categories: MutableStateFlow<List<Category>>, val mangaMap: LibraryMap)

private fun LibraryMap.getManga(id: Long, getItemsFlow: (StateFlow<List<Manga>>) -> StateFlow<List<Manga>>) =
    getOrPut(id) {
        val unfilteredItems = MutableStateFlow<List<Manga>>(emptyList())
        getItemsFlow(unfilteredItems) to unfilteredItems
    }
private fun LibraryMap.setManga(id: Long, manga: List<Manga>, getItemsFlow: (StateFlow<List<Manga>>) -> StateFlow<List<Manga>>) {
    getManga(id, getItemsFlow).second.value = manga
}

class LibraryScreenViewModel @Inject constructor(
    private val categoryHandler: CategoryInteractionHandler,
    private val libraryHandler: LibraryInteractionHandler,
    private val updatesHandler: UpdatesInteractionHandler,
    libraryPreferences: LibraryPreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
    private val library = Library(MutableStateFlow(emptyList()), mutableMapOf())
    val categories = library.categories.asStateFlow()

    private val _selectedCategoryIndex = MutableStateFlow(0)
    val selectedCategoryIndex = _selectedCategoryIndex.asStateFlow()

    val displayMode = libraryPreferences.displayMode().stateIn(scope)
    val gridColumns = libraryPreferences.gridColumns().stateIn(scope)
    val gridSize = libraryPreferences.gridSize().stateIn(scope)

    private val sortMode = libraryPreferences.sortMode().stateIn(scope)
    private val sortAscending = libraryPreferences.sortAscending().stateIn(scope)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val comparator = combine(sortMode, sortAscending) { sortMode, sortAscending ->
        getComparator(sortMode, sortAscending)
    }.stateIn(scope, SharingStarted.Eagerly, compareBy { it.title })

    init {
        getLibrary()
    }

    private fun getLibrary() {
        _isLoading.value = true
        categoryHandler.getCategories()
            .onEach { categories ->
                if (categories.isEmpty()) {
                    throw Exception(MR.strings.library_empty.toPlatformString())
                }
                library.categories.value = categories.sortedBy { it.order }
                updateCategories(categories)
                _isLoading.value = false
            }
            .catch {
                _error.value = it.message
                log.warn(it) { "Error getting categories" }
                _isLoading.value = false
            }
            .launchIn(scope)
    }

    fun setSelectedPage(page: Int) {
        _selectedCategoryIndex.value = page
    }

    private fun getComparator(sortMode: Sort, ascending: Boolean): Comparator<Manga> {
        val sortFn = when (sortMode) {
            Sort.ALPHABETICAL -> {
                val locale = Locale.getDefault()
                val collator = Collator(locale);

                { a: Manga, b: Manga ->
                    collator.compare(a.title.lowercase(locale), b.title.lowercase(locale))
                }
            }
            Sort.UNREAD -> {
                { a: Manga, b: Manga ->
                    (a.unreadCount ?: 0).compareTo(b.unreadCount ?: 0)
                }
            }
            Sort.DATE_ADDED -> {
                { a: Manga, b: Manga ->
                    a.inLibraryAt.compareTo(b.inLibraryAt)
                }
            }
        }
        return if (ascending) {
            Comparator(sortFn)
        } else {
            Comparator(sortFn).reversed()
        }
    }

    private suspend fun filterManga(query: String, mangaList: List<Manga>): List<Manga> {
        if (query.isBlank()) return mangaList
        val queries = query.split(" ")
        return mangaList.asFlow()
            .filter { manga ->
                queries.all { query ->
                    manga.title.contains(query, true) ||
                        manga.author.orEmpty().contains(query, true) ||
                        manga.artist.orEmpty().contains(query, true) ||
                        manga.genre.any { it.contains(query, true) } ||
                        manga.description.orEmpty().contains(query, true) ||
                        manga.status.name.contains(query, true)
                }
            }
            .cancellable()
            .buffer()
            .toList()
    }

    private fun getMangaItemsFlow(unfilteredItemsFlow: StateFlow<List<Manga>>): StateFlow<List<Manga>> {
        return combine(unfilteredItemsFlow, query) { unfilteredItems, query ->
            filterManga(query, unfilteredItems)
        }.combine(comparator) { filteredManga, comparator ->
            filteredManga.sortedWith(comparator)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    fun getLibraryForCategoryId(id: Long): StateFlow<List<Manga>> {
        return library.mangaMap.getManga(id, ::getMangaItemsFlow).first
    }

    private suspend fun updateCategories(categories: List<Category>) {
        withDefaultContext {
            categories.map { category ->
                async {
                    library.mangaMap.setManga(
                        id = category.id,
                        manga = categoryHandler.getMangaFromCategory(category)
                            .catch {
                                log.warn(it) { "Error getting manga for category $category" }
                                emit(emptyList())
                            }
                            .single(),
                        getItemsFlow = ::getMangaItemsFlow
                    )
                }
            }.awaitAll()
        }
    }

    private fun getCategoriesToUpdate(mangaId: Long): List<Category> {
        return library.mangaMap
            .filter { mangaMapEntry ->
                mangaMapEntry.value.first.value.firstOrNull { it.id == mangaId } != null
            }
            .map { (id) -> library.categories.value.first { it.id == id } }
    }

    fun removeManga(mangaId: Long) {
        libraryHandler.removeMangaFromLibrary(mangaId)
            .onEach {
                updateCategories(getCategoriesToUpdate(mangaId))
            }
            .catch {
                log.warn(it) { "Error removing manga from library" }
            }
            .launchIn(scope)
    }

    fun updateQuery(query: String) {
        _query.value = query
    }

    fun updateLibrary() {
        updatesHandler.updateLibrary()
            .catch {
                log.warn(it) { "Error updating library" }
            }
            .launchIn(scope)
    }

    fun updateCategory(category: Category) {
        updatesHandler.updateCategory(category)
            .catch {
                log.warn(it) { "Error updating category" }
            }
            .launchIn(scope)
    }

    private companion object {
        private val log = logging()
    }
}