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

    android.buildFeatures.viewBinding = true
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