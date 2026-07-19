plugins {
    id("com.android.application")
}

android {
    namespace = "org.seniorconnect.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.seniorconnect.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("com.google.mediapipe:tasks-genai:0.10.24")
    implementation("org.maplibre.gl:android-sdk:11.11.0")
}
