plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)

    kotlin("kapt")
    //id("org.jetbrains.kotlin.kapt")


    //id ("com.android.application")
    id ("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.mobile_kotlin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mobile_kotlin"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner += "androidx.test.runner.AndroidJUnitRunner"
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
}

configurations.all {
    exclude(group = "xpp3", module = "xpp3")
    //exclude(group = "xmlpull", module = "xmlpull")
}

kapt {
    correctErrorTypes = true
}

dependencies {

    // Подключаем BO
    implementation(libs.compose.bom)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    debugImplementation     (libs.ui.test.manifest)

    implementation(libs.androidx.hilt.navigation.compose.v110alpha01)

    implementation(libs.hilt.android)
    //mplementation(libs.hilt.android.compiler)
    //implementation (libs.hilt.android.compiler)

    /*implementation("com.google.firebase:firebase-bom:33.0.0") {
        exclude group: 'xpp3', module: 'xpp3'
    }*/
    implementation (libs.androidx.hilt.navigation.compose)

    //implementation(libs.okhttp.coroutines)

    //implementation(libs.okhttp)

    // Для работы с корутинами (если нужно)
    //implementation(libs.kotlinx.coroutines.android)

    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.material3)
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.coil.compose)

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies

    implementation (libs.hilt.android)
    //implementation (libs.androidx.hilt.lifecycle.viewmodel)
    implementation (libs.androidx.material3.v121)
    implementation(libs.google.firebase.auth)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.coil)
    implementation(libs.volley)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.benchmark.common)
    implementation(libs.androidx.navigation.safe.args.generator)
    implementation(libs.litert.support.api)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
}