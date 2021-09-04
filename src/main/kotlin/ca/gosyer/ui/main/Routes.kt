package ca.gosyer.ui.main

import ca.gosyer.data.ui.model.StartScreen

sealed class Routes {
    object Library : Routes()
    object Sources : Routes()
    object Extensions : Routes()
    data class Manga(val mangaId: Long) : Routes()
    object Downloads : Routes()

    data class SourceSettings(val sourceId: Long) : Routes()

    object Settings : Routes()
    object SettingsGeneral : Routes()
    object SettingsAppearance : Routes()
    object SettingsLibrary : Routes()
    object SettingsReader : Routes()

    /*object SettingsDownloads : Route()
    object SettingsTracking : Route()*/
    object SettingsBrowse : Routes()
    object SettingsBackup : Routes()
    object SettingsServer : Routes()

    /*object SettingsSecurity : Route()
    object SettingsParentalControls : Route()*/
    object SettingsAdvanced : Routes()
}

fun StartScreen.toRoute() = when (this) {
    StartScreen.Library -> Routes.Library
    StartScreen.Sources -> Routes.Sources
    StartScreen.Extensions -> Routes.Extensions
}
