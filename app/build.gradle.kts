import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.tatumgames.tatumtech.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tatumgames.tatumtech.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" //1.5.14 - for kotlin (1.9.24) // Required for Compose 1.6.6
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
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.6")
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.constraintlayout.compose)
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.6")
    implementation(libs.androidx.navigation.compose)

    // Core Android dependencies
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.storage)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Framework reference - contains all Firebase and Google SSO logic
    implementation(project(":tatumtech-framework-android"))
}
