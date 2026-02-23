plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

val prodApiBaseUrl = providers.gradleProperty("prodApiBaseUrl")
    .orElse(providers.environmentVariable("PROD_API_BASE_URL"))
    .orElse("https://example.com/")

fun buildConfigString(value: String): String = "\"$value\""

android {
    namespace = "com.club.poc.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.club.poc"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "env"
    productFlavors {
        create("demo") {
            dimension = "env"
            buildConfigField("String", "API_BASE_URL", buildConfigString("https://example.com/"))
            buildConfigField("String", "BOOTSTRAP_CLUB_ID", buildConfigString("demo-club"))
            buildConfigField("String", "BOOTSTRAP_MEMBER_ID", buildConfigString("demo-member"))
            resValue("string", "app_name", "Club POC Demo")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "API_BASE_URL", buildConfigString(prodApiBaseUrl.get()))
            buildConfigField("String", "BOOTSTRAP_CLUB_ID", buildConfigString("prod-club"))
            buildConfigField("String", "BOOTSTRAP_MEMBER_ID", buildConfigString("prod-member"))
            resValue("string", "app_name", "Club POC")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:auth"))
    implementation(project(":core:payments"))
    implementation(project(":core:work"))

    implementation(project(":feature:invite-auth"))
    implementation(project(":feature:membership"))
    implementation(project(":feature:lodge-catalog"))
    implementation(project(":feature:booking"))
    implementation(project(":feature:documents"))
    implementation(project(":feature:committee-admin"))
    implementation(project(":feature:profile"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.retrofit)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit4)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}
