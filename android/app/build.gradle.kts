plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hyperion.marinareviewer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hyperion.marinareviewer"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            // Play Store 64-bit requirement
            abiFilters.addAll(setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    signingConfigs {
        create("release") {
            // Configured via gradle.properties or environment variables for CI/CD Play Store releases
            val keystoreFile = System.getenv("PLAYSTORE_KEYSTORE_PATH") ?: "release.keystore"
            if (file(keystoreFile).exists()) {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("PLAYSTORE_KEYSTORE_PASSWORD") ?: "hyperionpass"
                keyAlias = System.getenv("PLAYSTORE_KEY_ALIAS") ?: "hyperionkey"
                keyPassword = System.getenv("PLAYSTORE_KEY_PASSWORD") ?: "hyperionpass"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing if configured, otherwise fallback to debug for local release testing
            signingConfig = if (signingConfigs.getByName("release").storeFile?.exists() == true) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
}
