import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

// Tatum Tech API settings, read from `-P<key>=<value>` or the git-ignored local.properties.
// Never put credentials in this file.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.isFile }?.inputStream()?.use(::load)
}

fun tatumTechProperty(name: String): String? =
    (findProperty("tatumTech.$name") as String?) ?: localProperties.getProperty("tatumTech.$name")

fun tatumTechChoice(name: String, allowed: List<String>, default: String): String {
    val value = tatumTechProperty(name)?.trim() ?: default
    require(value in allowed) { "tatumTech.$name must be one of $allowed but was '$value'" }
    return value
}

fun buildConfigString(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val tatumTechDataSource = tatumTechChoice("dataSource", listOf("NETWORK", "LOCAL_JSON"), "NETWORK")
val tatumTechEnvironment = tatumTechChoice("environment", listOf("PRODUCTION", "STAGE"), "PRODUCTION")
val tatumTechApiKey = tatumTechProperty("apiKey")?.trim().orEmpty()
val tatumTechConnectTimeoutMs = tatumTechProperty("connectTimeoutMs")?.trim()
    ?.let { requireNotNull(it.toLongOrNull()?.takeIf { ms -> ms > 0 }) { "tatumTech.connectTimeoutMs must be a positive number" } }
    ?: 0L

android {
    namespace = "com.tatumgames.tatumtech.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.tatumgames.tatumtech.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Store listings use the release id; debug builds append applicationIdSuffix.
        buildConfigField("String", "STORE_PACKAGE_NAME", "\"$applicationId\"")

        // Tatum Tech API; the data source is independent of the build type.
        buildConfigField("String", "TATUM_TECH_DATA_SOURCE", buildConfigString(tatumTechDataSource))
        buildConfigField("String", "TATUM_TECH_ENVIRONMENT", buildConfigString(tatumTechEnvironment))
        buildConfigField("String", "TATUM_TECH_API_KEY", buildConfigString(tatumTechApiKey))
        // 0 = framework default.
        buildConfigField("long", "TATUM_TECH_CONNECT_TIMEOUT_MS", "${tatumTechConnectTimeoutMs}L")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            // Store builds always use the live production API, never bundled fixtures or stage.
            buildConfigField("String", "TATUM_TECH_DATA_SOURCE", buildConfigString("NETWORK"))
            buildConfigField("String", "TATUM_TECH_ENVIRONMENT", buildConfigString("PRODUCTION"))
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // java.time (meeting times and time zones) on API 24-25.
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        disable += "CredentialProviderPlayServicesAuthMissing"
    }
    testOptions {
        // Lets JVM tests exercise code that logs through android.util.Log.
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Jetpack Compose (Material 3)
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui:1.6.6")
    implementation("androidx.compose.foundation:foundation:1.6.6")
    implementation("com.google.accompanist:accompanist-pager:0.34.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.6")
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.constraintlayout.compose)
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.6")
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.foundation:foundation-layout:1.6.6")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Core Android dependencies
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.storage)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Virtual speaker meeting reminders
    implementation(libs.androidx.work.runtime.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Framework reference - contains Firebase Auth and Google SSO logic
    implementation(project(":tatumgames-framework-android"))

    // Firebase Analytics + Crashlytics (BoM; Auth remains in framework module)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")

    // MPAndroidChart dependency for charting in the Stats screen implementation
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // CameraX dependencies
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("androidx.camera:camera-extensions:1.3.0")
    implementation("io.coil-kt:coil-compose:2.4.0")

    // QR encode/decode for digital contact cards
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Room dependencies (2.8.x required for Kotlin 2.2+ metadata)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    kapt("org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.versions.kotlin.get()}")

    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // OkHttp (Discord community API client)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        force("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
        force("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.3")
    }
}
