# okhttp (pulled in via ktor-client-okhttp) probes for optional TLS/runtime
# providers that are never on the desktop classpath; these are all
# reflectively-guarded no-ops when absent, not real missing dependencies.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn android.**
-dontwarn dalvik.**

# slf4j's static binder classes are resolved at runtime by whichever logging
# backend is on the classpath; kotlinx.coroutines/ktor only need the API.
-dontwarn org.slf4j.impl.**

# Ktor discovers several plugins via java.util.ServiceLoader
# (META-INF/services/...), so these implementations are only referenced
# reflectively at runtime. Without this, ProGuard shrinks them as unused
# and the app crashes on startup with "Provider ... not found".
-keep class * implements io.ktor.client.HttpClientEngineContainer
-keepnames class * implements io.ktor.client.HttpClientEngineContainer
-keep class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider
-keepnames class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider

# kotlinx-coroutines-swing's SwingDispatcherFactory is likewise only found
# via META-INF/services/kotlinx.coroutines.internal.MainDispatcherFactory,
# not a direct reference; keep it so Dispatchers.Main (used by
# viewModelScope) resolves instead of crashing with "Module with the Main
# dispatcher is missing".
-keep class * implements kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class * implements kotlinx.coroutines.internal.MainDispatcherFactory
