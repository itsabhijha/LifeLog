plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.lifelogai.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lifelogai.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }

    val keystorePath = System.getenv("KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
    val keystorePassword = System.getenv("KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() }
    val keyAliasValue = System.getenv("KEY_ALIAS")?.takeIf { it.isNotBlank() }
    val keyPasswordValue = System.getenv("KEY_PASSWORD")?.takeIf { it.isNotBlank() }

    signingConfigs {
        if (keystorePath != null &&
            keystorePassword != null &&
            keyAliasValue != null &&
            keyPasswordValue != null
        ) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystorePath != null &&
                keystorePassword != null &&
                keyAliasValue != null &&
                keyPasswordValue != null
            ) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    // Room compiler can be added with KSP when expanding the project.
}
