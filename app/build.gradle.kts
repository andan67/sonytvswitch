import java.util.Properties
import java.io.FileInputStream
import java.io.FileNotFoundException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.android)
}

kotlin {
    jvmToolchain(17)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
localProperties.load(FileInputStream(localPropertiesFile))


val keystorePropertiesFile = localProperties["keystore.properties"]?.let { file(it) }

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()
try {
    keystorePropertiesFile?.let {keystoreProperties.load(FileInputStream(keystorePropertiesFile))}
}
catch (e:FileNotFoundException) {
}

android {

    compileSdk = 34

    namespace = "org.andan.android.tvbrowser.sonycontrolplugin"

    defaultConfig {
        applicationId = "org.andan.android.tvbrowser.sonycontrolplugin"
        minSdk = 23
        targetSdk = 34
        versionCode = 31
        versionName = "@string/app_version"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }



    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.txt"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }
    buildToolsVersion = "34.0.0"

}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.test.ext)
    implementation(libs.androidx.test.runner)

    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Material3
    implementation(libs.androidx.compose.material3)
    //implementation 'androidx.compose.ui:ui-tooling-preview'
    //debugImplementation 'androidx.compose.ui:ui-tooling'

    // UI Tests
    //androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
    //debugImplementation 'androidx.compose.ui:ui-test-manifest'


    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)


    implementation(libs.accompanist.theme)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.drawablepainter)

    implementation(libs.google.oss.licenses)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.lifecycle.viewModelKtx)
    implementation(libs.androidx.lifecycle.livedataKtx)
    implementation(libs.androidx.lifecycle.livedataKtx)
    implementation(libs.androidx.lifecycle.runtimeKtx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelSavedstate)

    // Annotation processor
    ksp(libs.androidx.lifecycle.common)


    // Datastore
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.room.runtime)

    implementation(libs.room.runtime)
    testImplementation(libs.room.testing)

    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.coroutines.android)

    androidTestImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.test.runner)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    testImplementation(libs.okhttp.mockserver)


    implementation(libs.timber)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}