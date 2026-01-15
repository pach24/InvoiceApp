plugins {

    id("com.android.library")
}

android {
    // 2. Namespace obligatorio
    namespace = "com.nexosolar.android.data.remote"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes{
        release {

            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"https://francisco-pacheco.com/api/\""
            ) //Usé mi propia api ya que la proporcinada no funcionaba
        }
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://francisco-pacheco.com/api/\"")
        }
    }



        compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Retromock
    implementation("co.infinum:retromock:1.1.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
