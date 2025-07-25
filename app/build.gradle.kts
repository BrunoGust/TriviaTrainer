import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-parcelize")
    alias(libs.plugins.googleService)

}

android {
    namespace = "com.example.triviatrainerapp"
    compileSdk = 35
    // Leer apiKey desde apikey.properties
    val apikeyPropsFile = rootProject.file("apikey.properties")
    val apikeyProps = Properties()
    apikeyProps.load(apikeyPropsFile.inputStream())
    defaultConfig {
        applicationId = "com.example.triviatrainerapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Inserta la apiKey como BuildConfig.API_KEY
        buildConfigField("String", "API_KEY", "\"${apikeyProps["API_KEY"]}\"")
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
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src/main/assets")
            }
        }
    }
    buildFeatures{
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebaseDatabase)
    implementation("com.google.code.gson:gson:2.10.1")
    // SDK de Gemini
    implementation(libs.google.generativeai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}