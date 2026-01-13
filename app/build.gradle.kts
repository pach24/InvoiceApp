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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    val lifecycleversion = "2.8.7"

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleversion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycleversion")
    // Lifecycles
    implementation("androidx.lifecycle:lifecycle-runtime:$lifecycleversion")

    // Saved state ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleversion")
    //Conversor de datos a nuestro modelo
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    annotationProcessor("androidx.lifecycle:lifecycle-compiler:$lifecycleversion")

    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleversion")

    // Retrofit para las peticiones de red
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    // Retromock para pruebas

    implementation ("co.infinum:retromock:1.1.0")

    implementation("androidx.recyclerview:recyclerview:1.4.0")

    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")

    implementation("com.google.android.material:material:1.6.0")

    //ROOM
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Testing Unitario
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    // Importante para mockear clases finales/estáticas
    // Para testear LiveData (InstantTaskExecutorRule)
    testImplementation("androidx.arch.core:core-testing:2.2.0")


}
