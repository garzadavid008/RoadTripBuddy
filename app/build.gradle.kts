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
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Core Android & Kotlin
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.locationdelegation)
    implementation("androidx.datastore:datastore:1.1.0")
    implementation("com.google.protobuf:protobuf-kotlin-lite:3.25.3")

    // Protobuf Tools
    kapt("com.squareup:javapoet:1.13.0")

    // UI toolkits
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Lifecycle & LiveData
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.runtime.livedata)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Layouts
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation("sh.calvin.reorderable:reorderable:2.4.3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    androidTestImplementation("androidx.navigation:navigation-testing:2.8.8")

    // TomTom SDK
    implementation("com.tomtom.sdk.navigation:navigation-online:$version")
    implementation("com.tomtom.sdk.location:provider-default:$version")
    implementation("com.tomtom.sdk.location:provider-map-matched:$version")
    implementation("com.tomtom.sdk.location:provider-simulation:$version")
    implementation("com.tomtom.sdk.maps:map-display:$version")
    implementation("com.tomtom.sdk.datamanagement:navigation-tile-store:$version")
    implementation("com.tomtom.sdk.routing:route-planner-online:$version")
    implementation("com.tomtom.sdk.search:search-online:$version")
    implementation("com.tomtom.sdk.search:reverse-geocoder:1.23.2")
    implementation("com.tomtom.sdk.search:reverse-geocoder-online:1.23.2")
    implementation("com.tomtom.sdk.maps.visualization:navigation:$version")
    implementation("com.tomtom.sdk.navigation:ui:1.23.2")

    // Google APIs
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Android Instrumentation Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.4")

    // Firebase (use one BOM)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth")
}

