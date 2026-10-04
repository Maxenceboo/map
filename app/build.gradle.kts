import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Secrets locaux (local.properties n'est pas versionné) : la clé TomTom n'est jamais écrite dans le code.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val tomTomApiKey: String = localProperties.getProperty("tomtom.apiKey", "").trim()
require(tomTomApiKey.all { it.isLetterOrDigit() }) { "tomtom.apiKey : lettres et chiffres uniquement" }

android {
    namespace = "com.gamemaps.irl"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gamemaps.irl"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-native"
        // Vide = pas de trafic : l'app calcule ses itinéraires avec OSRM.
        buildConfigField("String", "TOMTOM_API_KEY", "\"$tomTomApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // AndroidX de base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // UI téléphone (Jetpack Compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    // Android Auto (Car App Library)
    implementation(libs.car.app)
    implementation(libs.car.app.projected)

    // Carte native
    implementation(libs.maplibre.android)

    // Réseau & asynchrone
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)

    // Tests unitaires JVM (org.json réel car celui d'Android est un stub en test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.org.json)
}
