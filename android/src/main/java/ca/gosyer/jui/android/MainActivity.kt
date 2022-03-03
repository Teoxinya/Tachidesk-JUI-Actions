package ca.gosyer.jui.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import ca.gosyer.jui.android.data.AndroidDownloadService
import ca.gosyer.ui.AppComponent
import ca.gosyer.ui.base.theme.AppTheme
import ca.gosyer.ui.main.MainMenu

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = AppComponent.getInstance(applicationContext)
        if (savedInstanceState == null) {
            appComponent.dataComponent.migrations.runMigrations()
        }

        AndroidDownloadService.start(this, AndroidDownloadService.Actions.START)

        val uiHooks = appComponent.uiComponent.getHooks()
        setContent {
            CompositionLocalProvider(*uiHooks) {
                AppTheme {
                    MainMenu()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AndroidDownloadService.stop(this)
    }
}