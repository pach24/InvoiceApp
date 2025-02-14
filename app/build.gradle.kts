plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pruebas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pruebas"
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
    // Annotation processor
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:$lifecycleversion")
    // Alternativamente - si usas Java 8, usa lo siguiente en lugar de lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleversion")

    // Retrofit para las peticiones de red
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Mockito para pruebas
    testImplementation("org.mockito:mockito-core:4.0.0")

    // Retromock para pruebas

    implementation ("co.infinum:retromock:1.1.0")

    implementation("androidx.recyclerview:recyclerview:1.4.0")
    // For control over item selection of both touch and mouse driven selection
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")


}
