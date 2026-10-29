import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

val mapsApiKey = localProperties.getProperty("MAPS_API_KEY", "")

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")

if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use {
        keystoreProperties.load(it)
    }
}

/** Path file daftar penguji/grup App Distribution (email/alias dipisah koma atau baris baru), atau null kalau kosong. */
fun distributionFile(path: String): String? =
    rootProject.file(path).takeIf { it.exists() && it.readText().isNotBlank() }?.path

// APK yang tidak ditandatangani tidak bisa dipasang penguji, jadi jangan diunggah
val hasReleaseSigning = keystorePropertiesFile.exists()
tasks.matching { it.name == "appDistributionUploadRelease" }.configureEach {
    // Disalin ke variabel lokal supaya doFirst tidak merujuk objek skrip (syarat configuration cache)
    val signed = hasReleaseSigning
    doFirst {
        check(signed) { "keystore.properties tidak ada: APK release tidak ditandatangani dan tidak bisa dipasang penguji." }
    }
}

android {
    namespace = "com.test.cobacompose"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.test.cobacompose"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            // Tanpa keystore.properties, release tetap di-build tapi unsigned
            signingConfig = signingConfigs.findByName("release")

            // Firebase App Distribution: ./gradlew assembleRelease appDistributionUploadRelease
            // Penguji dan catatan rilis diatur di folder distribution/ (lihat distribution/README.md)
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotesFile = rootProject.file("distribution/release-notes.txt").path
                distributionFile("distribution/testers.txt")?.let { testersFile = it }
                distributionFile("distribution/groups.txt")?.let { groupsFile = it }
            }
        }
    }
    // Hanya bahasa yang didukung aplikasi (ID, EN, Mandarin, Arab); terjemahan library lain dibuang
    androidResources {
        localeFilters += listOf("en", "in", "zh", "zh-rCN", "ar")
    }
    // Semua bahasa harus tetap ada di instalasi karena pengguna menggantinya dari dalam aplikasi.
    bundle {
        language {
            enableSplit = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.maps.compose)
    implementation(libs.places)
    // Kamera di dalam aplikasi untuk Kera Campaign (Tantangan 3 Hari 3)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
