plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.nexosolar.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nexosolar.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true // VITAL
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- MÓDULOS PROPIOS ---
    implementation(project(":domain"))
    implementation(project(":data-retrofit"))
    implementation(project(":core"))

    // --- ANDROID UI ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // --- LIFECYCLE & VIEWMODEL ---
    val lifecycleVersion = "2.8.7"
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime:$lifecycleVersion")

    // --- NETWORKING ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("co.infinum:retromock:1.1.0")

    // --- DATABASE (ROOM) ---
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // --- DESUGARING ---
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // --- TESTING ---
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
