plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.ktor.client.okhttp)
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

android {
    namespace = "com.ollamaconnect"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.ollamaconnect"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "2.0.0"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "com.ollamaconnect.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageVersion = "2.0.0"
            packageName = "Ollama Connect"

            windows {
                menuGroup = "Ollama Connect"
                upgradeUuid = "E3E94DD0-0C05-48FB-9384-4ED6DAA96608"
            }

            macOS {
                bundleID = "com.ollamaconnect"
                dockName = "Ollama Connect"
            }
        }
    }
}

// A single JAR that runs on any desktop OS/arch, unlike packageUberJarForCurrentOS
// (which only bundles the Skiko native library of the machine that built it).
// Compose's desktop-jvm-<platform> artifacts are metadata-only modules that all
// route to the same common desktop-jvm.jar plus a platform-specific Skiko native
// library (unique filename per platform, e.g. libskiko-windows-x64.dll), so
// merging every platform's runtime jar together carries no class-name conflicts.
val uberJarAllPlatforms: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    // Match the variant-selection attributes of the desktop JVM runtime classpath
    // so Gradle doesn't hit ambiguity between Skiko's android/awt variants.
    val desktopRuntimeAttributes = configurations.getByName("desktopRuntimeClasspath").attributes
    attributes {
        desktopRuntimeAttributes.keySet().forEach { key ->
            @Suppress("UNCHECKED_CAST")
            attribute(key as Attribute<Any>, desktopRuntimeAttributes.getAttribute(key) as Any)
        }
    }
}
dependencies {
    listOf("windows-x64", "macos-x64", "macos-arm64", "linux-x64", "linux-arm64").forEach { platform ->
        uberJarAllPlatforms("org.jetbrains.compose.desktop:desktop-jvm-$platform:${libs.versions.compose.multiplatform.get()}")
    }
}

tasks.register<Jar>("packageUberJarForAllPlatforms") {
    group = "compose desktop"
    description = "Assembles a fat jar bundling Skiko native libraries for every supported desktop platform."
    archiveBaseName.set("ollama-connect-all-platforms")
    archiveVersion.set("2.0.0")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.ollamaconnect.MainKt"
    }

    val desktopMainCompilation = kotlin.targets.getByName("desktop").compilations.getByName("main")
    from(desktopMainCompilation.output.allOutputs)

    val desktopRuntimeClasspath = configurations.getByName("desktopRuntimeClasspath")
    from({ desktopRuntimeClasspath.filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) } })
    from({ uberJarAllPlatforms.filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) } })
}
