import com.google.protobuf.gradle.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    //firebase
    alias(libs.plugins.google.gms.google.services)
}


configurations.configureEach {
    resolutionStrategy {
        force("com.squareup:javapoet:1.13.0")   // or 1.15.0 – anything ≥1.13.0
    }
}

configurations.all {
    exclude(group = "com.google.protobuf", module = "protobuf-kotlin")

    // 2) Keep every protobuf artefact on ONE version (matches your protoc)
    resolutionStrategy.eachDependency {
        if (requested.group == "com.google.protobuf"
            && requested.name.startsWith("protobuf-")      // runtime / compiler
        ) {
            useVersion("3.25.3")
        }
        // leave protoc-gen-kotlin (and any other tools) at their own versions
    }
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.25.3" }

    generateProtoTasks {
        all().configureEach {
            builtins { id("java")   { option("lite") } }   // Java-Lite classes
            builtins { id("kotlin") { option("lite") } }   // Kotlin wrappers
        }
    }
}

val tomtomApiKey: String by project

android {
    namespace = "com.example.roadtripbuddy"
    compileSdk = 35

    packaging {
        resources.excludes.addAll(listOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt"
        ))
    }

    defaultConfig {
        applicationId = "com.example.roadtripbuddy"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.example.roadtripbuddy.CustomTestRunner" // ui.test.junit4.createAndroidComposeRule  // was removed for customTest
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
    // Core Android and Kotlin Libraries
    implementation(libs.androidx.core.ktx) // Kotlin extensions for Android
    implementation(libs.androidx.appcompat) // AppCompat for backward compatibility
    implementation(libs.material) // Material Design components
    implementation(libs.androidx.constraintlayout) // ConstraintLayout for UI
    implementation(libs.androidx.fragment) // Fragment support
    implementation(libs.androidx.drawerlayout) // DrawerLayout for navigation
    implementation(libs.androidx.runtime.android) // Android runtime
    implementation(libs.androidx.runtime.livedata) // LiveData for reactive data
    implementation(libs.androidx.foundation.layout.android) // Foundation layout utilities
    implementation(libs.locationdelegation) // Location delegation utilities
    implementation(libs.androidx.foundation.android) // Android foundation

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.10.00")) // Compose BOM for version management
    implementation("androidx.activity:activity-compose:1.10.1") // Compose Activity integration
    implementation("androidx.compose.ui:ui") // Compose UI core
    implementation("androidx.compose.ui:ui-tooling-preview") // Compose preview tools
    implementation("androidx.compose.material3:material3") // Material3 components for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7") // adds lifecycle support for Compose

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.8") // Compose navigation
    implementation(libs.androidx.navigation.fragment.ktx) // Fragment navigation
    implementation(libs.androidx.navigation.ui.ktx) // Navigation UI utilities

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1")) // Firebase BOM for version management
    implementation("com.google.firebase:firebase-firestore-ktx") // Firestore for Kotlin
    implementation("com.google.firebase:firebase-auth") // Firebase Authentication

    // TomTom SDK
    implementation("com.tomtom.sdk.navigation:navigation-online:$version") // TomTom navigation
    implementation("com.tomtom.sdk.location:provider-default:$version") // Default location provider
    implementation("com.tomtom.sdk.location:provider-map-matched:$version") // Map-matched location
    implementation("com.tomtom.sdk.location:provider-simulation:$version") // Simulated location
    implementation("com.tomtom.sdk.maps:map-display:$version") // Map display
    implementation("com.tomtom.sdk.datamanagement:navigation-tile-store:$version") // Navigation tile store
    implementation("com.tomtom.sdk.routing:route-planner-online:$version") // Route planner
    implementation("com.tomtom.sdk.search:search-online:$version") // Search API
    implementation("com.tomtom.sdk.search:reverse-geocoder:1.23.2") // Reverse geocoding
    implementation("com.tomtom.sdk.search:reverse-geocoder-online:1.23.2") // Online reverse geocoding
    implementation("com.tomtom.sdk.maps.visualization:navigation:$version") // Navigation visualization
    implementation("com.tomtom.sdk.navigation:ui:1.23.2") // Navigation UI

    // Google APIs
    implementation("com.google.android.libraries.places:places:3.5.0") // Google Places API
    implementation("com.google.android.gms:play-services-maps:19.0.0") // Google Maps

    // HTTP Client
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14") // OkHttp for network requests

    // Other Libraries
    implementation("sh.calvin.reorderable:reorderable:2.4.3") // Reorderable list library

    // Hilt & Compiler
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt("com.squareup:javapoet:1.13.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Android Instrumentation Testing
    androidTestImplementation(libs.androidx.junit) // Android JUnit extensions
    androidTestImplementation(libs.androidx.espresso.core) // Espresso for UI testing
    androidTestImplementation("androidx.test.ext:junit:1.2.1") // Test extensions for JUnit
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") // Latest Espresso
    androidTestImplementation("androidx.navigation:navigation-testing:2.8.8") // Navigation testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8") // Compose UI testing

    // Debug Tools
    debugImplementation("androidx.compose.ui:ui-tooling") // Compose UI tooling
    debugImplementation("androidx.compose.ui:ui-test-manifest") // Compose test manifest

    // Unit Testing
    testImplementation(libs.junit) // JUnit for unit tests
    testImplementation("io.mockk:mockk:1.13.13") // MockK for mocking
    testImplementation("androidx.arch.core:core-testing:2.2.0") // Core testing utilities

    // Additional Integrations
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.datastore:datastore:1.1.0")
    implementation("com.google.protobuf:protobuf-kotlin-lite:3.25.3")
    implementation("com.composables:core:1.20.1")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("com.tomtom.sdk.navigation:navigation:$version")



}


hilt {
    enableAggregatingTask = false
}



