plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id("kotlin-kapt") // <--- AÑADE ESTA LÍNEA para el procesador de anotaciones de Kotlin
}

android {
    namespace = "com.example.petcareapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.petcareapp"
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

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.credential)
    implementation(libs.lifecycle.runtime)

    implementation(libs.androidx.appcompat.v161)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx.v262)
    implementation(libs.androidx.savedstate)
    // Ya tienes estas, solo para confirmar:
    // implementation(libs.google.firebase.firestore)
    // implementation(libs.google.firebase.auth)
    implementation(libs.androidx.material3.android)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.storage.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.circleimageview)
    implementation(libs.google.maps)
    implementation(libs.google.places)
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Dependencias de Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0") // <--- AÑADE ESTA LÍNEA (procesador de anotaciones de Glide)
}