import JSZip from 'jszip';

export async function generateAndroidProjectZip(): Promise<Blob> {
  const zip = new JSZip();

  // Root project files
  zip.file(
    'settings.gradle.kts',
    `pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HyperionMARINAReviewer"
include(":app")
`
  );

  zip.file(
    'build.gradle.kts',
    `plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
`
  );

  zip.file(
    'gradle.properties',
    `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=false
android.nonTransitiveRClass=true
kotlin.code.style=official
`
  );

  // Gradle Wrapper
  const gradleWrapper = zip.folder('gradle/wrapper');
  gradleWrapper?.file(
    'gradle-wrapper.properties',
    `distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
`
  );

  // App module
  const app = zip.folder('app');
  app?.file(
    'build.gradle.kts',
    `plugins {
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
            abiFilters.addAll(setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    signingConfigs {
        create("release") {
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
`
  );

  app?.file(
    'proguard-rules.pro',
    `-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
-keepclassmembers class * extends android.webkit.WebViewClient {
   public void *(android.webkit.WebView, java.lang.String);
   public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
   public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
   public void *(android.webkit.WebView, java.lang.String);
}
`
  );

  // App Manifest
  app?.file(
    'src/main/AndroidManifest.xml',
    `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Hyperion"
        android:hardwareAccelerated="true"
        android:networkSecurityConfig="@xml/network_security_config"
        tools:targetApi="35">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Hyperion"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
`
  );

  // MainActivity.kt
  const mainJava = app?.folder('src/main/java/com/hyperion/marinareviewer');
  mainJava?.file(
    'MainActivity.kt',
    `package com.hyperion.marinareviewer

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowInsetsController
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSystemBars()

        webView = WebView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(ContextCompat.getColor(context, R.color.primary_variant))
        }
        setContentView(webView)

        configureWebSettings()
        setupClients()
        setupBackNavigation()
        setupDownloadListener()

        // Replace with your production domain or load bundled offline assets
        val appUrl = "https://ais-dev-jbxucaox755rmvqdf6jooy-56793989426.asia-southeast1.run.app"
        webView.loadUrl(appUrl)
    }

    private fun setupSystemBars() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.nav_bar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebSettings() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            userAgentString = "$userAgentString HyperionMARINA/1.0"
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
    }

    private fun setupClients() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
                    return false
                }
                return true
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupDownloadListener() {
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            try {
                if (url.startsWith("blob:") || url.startsWith("data:")) {
                    Toast.makeText(this, "Export downloaded", Toast.LENGTH_SHORT).show()
                    return@setDownloadListener
                }

                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    addRequestHeader("User-Agent", userAgent)
                    setDescription("Downloading Hyperion MARINA Reviewer File")
                    setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        URLUtil.guessFileName(url, contentDisposition, mimetype)
                    )
                }

                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this, "Downloading file to Downloads...", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Download error: \${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
`
  );

  // Resources
  const res = app?.folder('src/main/res');
  res?.folder('values')?.file(
    'strings.xml',
    `<resources>
    <string name="app_name">Hyperion MARINA Reviewer</string>
</resources>`
  );

  res?.folder('values')?.file(
    'colors.xml',
    `<resources>
    <color name="primary">#0B192C</color>
    <color name="primary_variant">#070F1B</color>
    <color name="accent">#D4AF37</color>
    <color name="status_bar">#070F1B</color>
    <color name="nav_bar">#070F1B</color>
</resources>`
  );

  res?.folder('values')?.file(
    'styles.xml',
    `<resources>
    <style name="Theme.Hyperion" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryVariant">@color/primary_variant</item>
        <item name="colorSecondary">@color/accent</item>
        <item name="android:statusBarColor">@color/status_bar</item>
        <item name="android:navigationBarColor">@color/nav_bar</item>
    </style>
</resources>`
  );

  res?.folder('xml')?.file(
    'network_security_config.xml',
    `<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </base-config>
</network-security-config>`
  );

  res?.folder('xml')?.file(
    'data_extraction_rules.xml',
    `<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup><include domain="root" path="." /></cloud-backup>
    <device-transfer><include domain="root" path="." /></device-transfer>
</data-extraction-rules>`
  );

  res?.folder('xml')?.file(
    'backup_rules.xml',
    `<?xml version="1.0" encoding="utf-8"?>
<full-backup-content><include domain="root" path="." /></full-backup-content>`
  );

  // Play Store docs
  const docs = zip.folder('play-store');
  docs?.file(
    'README.md',
    `# Google Play Store Release Kit
This project is configured to build:
1. Android App Bundle (.aab): \`./gradlew bundleRelease\`
2. Standalone APK (.apk): \`./gradlew assembleRelease\`

Open this directory in Android Studio (Ladybug/Meerkat) and select 'Build > Generate Signed Bundle / APK'.
`
  );

  return await zip.generateAsync({ type: 'blob' });
}

export function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
