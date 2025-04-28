import com.google.protobuf.gradle.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    //firebase
    alias(libs.plugins.google.gms.google.services)
// fire base
    // id("com.google.gms.google-services")
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

    packagingOptions {
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
    implementation(libs.hilt.android)
    implementation(libs.material)
    kapt(libs.hilt.compiler)
    kapt("com.squareup:javapoet:1.13.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.datastore:datastore:1.1.0")
    implementation("com.google.protobuf:protobuf-kotlin-lite:3.25.3")
    implementation("com.composables:core:1.20.1")
    implementation("sh.calvin.reorderable:reorderable:2.4.3")
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
    implementation("com.tomtom.sdk.location:provider-default:$version")
    implementation("com.tomtom.sdk.routing:route-planner-online:$version")
    implementation("com.tomtom.sdk.search:search-online:$version")
    implementation("com.tomtom.sdk.search:reverse-geocoder:1.23.2")
    implementation("com.tomtom.sdk.search:reverse-geocoder-online:1.23.2")
    implementation("com.tomtom.sdk.maps.visualization:navigation:$version")
    implementation("com.tomtom.sdk.navigation:ui:1.23.2")
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.locationdelegation)
    implementation(libs.androidx.foundation.android)
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

    implementation("com.google.firebase:firebase-firestore-ktx") // Or firebase-firestore if using Java


    implementation("com.google.firebase:firebase-auth") //Let the BOM manage the version

    // for http request
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")

    // google places api sdk
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Testing dependencies
//Unit testing
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.1.0")


// Jetpack ui testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.8.8")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")



// Espresso ui testing
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


//    implementation("com.google.firebase:firebase-firestore-ktx") {
//        exclude(group = "com.google.protobuf", module = "protobuf-java")
//    }
    // implementation("com.google.protobuf:protobuf-javalite:3.25.5")

    //implementation("com.google.firebase:firebase-firestore-ktx") //Let the BOM manage
    //   implementation("com.google.protobuf:protobuf-javalite:3.25.5")
}

hilt {
    enableAggregatingTask = false
}