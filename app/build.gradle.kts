plugins {
    id("com.android.application")
}

android {
    namespace = "org.seniorconnect.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.seniorconnect.app"
        minSdk = 23
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    // HTTP client for Gemini API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // JSON parsing
    implementation("org.json:json:20240303")
}
