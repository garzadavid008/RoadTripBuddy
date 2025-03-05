plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
        //firebase
    alias(libs.plugins.google.gms.google.services)
// fire base
   // id("com.google.gms.google-services")
}

val tomtomApiKey: String by project

android {
    namespace = "com.example.roadtripbuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.roadtripbuddy"
        minSdk = 26
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
    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    buildTypes.configureEach {
        buildConfigField("String", "TOMTOM_API_KEY", "\"$tomtomApiKey\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

val version = "1.23.1"

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("com.tomtom.sdk.navigation:navigation-online:$version")
    implementation("com.tomtom.sdk.location:provider-default:$version")
    implementation("com.tomtom.sdk.location:provider-map-matched:$version")
    implementation("com.tomtom.sdk.location:provider-simulation:$version")
    implementation("com.tomtom.sdk.maps:map-display:$version")
    implementation("com.tomtom.sdk.datamanagement:navigation-tile-store:$version")
    implementation("com.tomtom.sdk.navigation:ui:$version")
    implementation("com.tomtom.sdk.routing:route-planner-online:$version")
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    // fire base depen
//    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
////    implementation("com.google.firebase:firebase-analytics")
////    // you would add other dependencies you will use here from fire base
////    // for auth
////    implementation("com.google.firebase:firebase-auth")
//// Use an older compatible Firebase version
//    implementation("com.google.firebase:firebase-auth:22.2.0") // Instead of 23.2.0
//
//    // If you're using other Firebase services, ensure they match the BOM
//    implementation("com.google.firebase:firebase-analytics")
//
//    // Downgrade Google Play Services dependencies if necessary
//    implementation("com.google.android.gms:play-services-measurement-api:21.0.0")
    debugImplementation(libs.androidx.ui.tooling)
//
//    // firestore depend
//    implementation("com.google.firebase:firebase-firestore-ktx")


    //implementation(platform("com.google.firebase:firebase-bom:33.10.0")) //Get the latest version from Firebase release notes
    implementation(platform("com.google.firebase:firebase-bom:32.2.3"))

    //implementation('com.google.firebase:firebase-firestore-ktx') // Or firebase-firestore if using Java


    implementation("com.google.firebase:firebase-auth") //Let the BOM manage the version

    implementation("com.google.firebase:firebase-firestore-ktx") //Let the BOM manage
 //   implementation("com.google.protobuf:protobuf-javalite:3.25.5")
}
// Apply resolution strategy outside dependencies


