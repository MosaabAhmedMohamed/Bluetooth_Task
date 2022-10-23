plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0" // compose_version
    }
    android.buildFeatures.viewBinding = true
    android.buildFeatures.compose = true
}

dependencies {

    implementation(project(":core"))
    implementation(project(":domain"))

    implementation(kotlin("stdlib-jdk7"))
    implementation(SupportLibs.ANDROIDX_APPCOMPAT)
    implementation(SupportLibs.ANDROIDX_CORE_KTX)
    implementation(SupportLibs.ANDROIDX_CONSTRAINT_LAYOUT)
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.6.1")

    testImplementation(TestingLib.RoboElectric)
    testImplementation(TestingLib.mockito)
    testImplementation(TestingLib.mockito_inline)
    testImplementation(TestingLib.testing_core_testing)
    testImplementation(TestingLib.android_test_room)
    testImplementation(AndroidTestingLib.ANDROIDX_TEST_CORE)

    //Compose
    implementation("androidx.compose.ui:ui:1.2.1")
    implementation("androidx.compose.material:material:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha04")
    implementation("androidx.compose.material:material-icons-extended:1.2.1")
    implementation("com.google.accompanist:accompanist-permissions:0.26.5-rc")

    // compose insets
    implementation("com.google.accompanist:accompanist-insets:0.20.2")

    //Hilt
    implementation("com.google.dagger:hilt-android:2.42")
    kapt("com.google.dagger:hilt-android-compiler:2.42")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    //Navigation
    implementation(NAVIGATION.NAVIGATION_FRAGMENT)
    implementation(NAVIGATION.NAVIGATION_UI)

}