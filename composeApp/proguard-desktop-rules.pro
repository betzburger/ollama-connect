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
