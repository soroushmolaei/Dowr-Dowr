# قوانین پایه پروگارد برای دورینو
-keepattributes *Annotation*
-keepclassmembers class kotlinx.serialization.** { *; }
-keep,includedescriptorclasses class com.dorino.game.**$$serializer { *; }
-keepclassmembers class com.dorino.game.** {
    *** Companion;
}
-keepclasseswithmembers class com.dorino.game.** {
    kotlinx.serialization.KSerializer serializer(...);
}
