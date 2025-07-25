plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Google Services Gradle Plugin
}

android {
    namespace = "com.example.covid_19"
    compileSdk = 35 // Keep at 35

    defaultConfig {
        applicationId = "com.example.covid_19"
        minSdk = 24
        targetSdk = 35 // Keep at 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    // AndroidX Core and UI
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0") // Keep only the latest Material version
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Navigation Component (using latest stable for 2.9.0, which is fine)
    implementation("androidx.navigation:navigation-fragment:2.9.0")
    implementation("androidx.navigation:navigation-ui:2.9.0")

    // Firebase BOM (Bill of Materials) to manage Firebase library versions
    // This ensures all Firebase libraries use compatible versions.
    // As of my last update, 33.1.0 is a very recent stable BOM.
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth") // Version managed by BOM

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database") // Version managed by BOM

    // Firebase Firestore (You have this, but your current code doesn't use it. Keep if planned.)
    implementation("com.google.firebase:firebase-firestore") // Version managed by BOM

    // Firebase Storage (You have this, but your current code doesn't use it. Keep if planned for profile images.)
    implementation("com.google.firebase:firebase-storage") // Version managed by BOM

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.2.0") // Updated to latest stable

    // Retrofit for HTTP requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Remove these if you are ONLY using Email/Password auth and not Google Sign-In / Credential Manager
    // If you plan to add Google Sign-In, keep them and ensure compatibility.
    // implementation("androidx.credentials:credentials:1.5.0")
    // implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    // implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
}