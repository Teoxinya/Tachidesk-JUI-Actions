pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    
}
rootProject.name = "Tachidesk-JUI"

include("desktop")

enableFeaturePreview("VERSION_CATALOGS")
