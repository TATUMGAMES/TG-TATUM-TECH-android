plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.tatumgames.tatumtech.framework.android"
    compileSdk = 37
    // Versioning
    val versionMajor = 1
    val versionMinor = 0
    val versionPatch = 0
    val versionCode = 1
    val versionName = "$versionMajor.$versionMinor.$versionPatch"

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        // there are two buildTypes {debug, release}. The only difference between debug
        // and release buildTypes is that code debug logging is enabled in debug environment,
        // otherwise logging is disabled.
        getByName("debug") {
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
            buildConfigField("String", "VERSION_CODE", "\"$versionCode\"")
            manifestPlaceholders["build_version"] = versionName
        }
        getByName("release") {
            buildConfigField("boolean", "DEBUG_MODE", "false")
            buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
            buildConfigField("String", "VERSION_CODE", "\"$versionCode\"")
            manifestPlaceholders["build_version"] = versionName

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
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.googleid)
    testImplementation(libs.junit)

    // Firebase & Google SSO
    implementation("androidx.credentials:credentials:1.2.1")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.1")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("com.google.firebase:firebase-auth:22.3.1") // or latest

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // HTTP stack (implementation scope keeps OkHttp/Gson out of consumers' compile classpath)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        force("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
        force("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.3")
    }
}
