plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.nexosolar.android.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    // 1. Módulos propios
    implementation(project(":domain"))       // Para ver InvoiceRepository (Interfaz) e Invoice (Modelo)
    implementation(project(":data-retrofit")) // Para ver ApiService y descargar datos
    implementation(project(":core"))         // Para utilidades compartidas

    // 2. Room (Copiado de :app)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Retromock
    implementation("co.infinum:retromock:1.1.0")

    // 3. Desugaring (Importante para LocalDate en Room)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
