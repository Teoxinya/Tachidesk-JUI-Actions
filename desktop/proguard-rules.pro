-dontobfuscate
-keepattributes Signature,LineNumberTable

-keep,includedescriptorclasses,allowoptimization class ca.gosyer.jui.** { public protected *; }
-keep class ca.gosyer.jui.desktop.MainKt {
    public static void main(java.lang.String[]);
}
-keep class 'module-info'

# Kotlin
# todo optimize more
-keep,includedescriptorclasses class kotlin.reflect.** { *; }

# Log4J
-dontwarn org.apache.logging.log4j.**
-keep,includedescriptorclasses class org.apache.logging.log4j.** { *; }

# SLF4J
-dontwarn org.apache.logging.slf4j.**
-keep,includedescriptorclasses class org.apache.logging.slf4j.** { *; }

# OKHTTP
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.**

# DarkLaf
# todo optimize more
-keep,includedescriptorclasses class com.github.weisj.darklaf.** { *; }
-dontwarn com.github.weisj.darklaf.**
-keep class com.github.weisj.jsvg.** { *; }
-dontwarn com.github.weisj.jsvg.**

# Ktor
-keep,includedescriptorclasses class * extends io.ktor.client.HttpClientEngineContainer
-dontwarn io.ktor.network.sockets.DatagramSendChannel

# Coroutines
-keep,includedescriptorclasses class kotlinx.coroutines.swing.** { *; }

# Other
-dontwarn org.pbjar.jxlayer.plaf.ext.TransformUI
-keep class com.sun.jna.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class ca.gosyer.jui.**$$serializer { *; }
-keepclassmembers class ca.gosyer.jui.** {
    *** Companion;
}
-keepclasseswithmembers class ca.gosyer.jui.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Skiko
-dontwarn org.jetbrains.skiko.**