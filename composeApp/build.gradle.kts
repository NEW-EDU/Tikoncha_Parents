import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.cocoapods)
    alias(libs.plugins.google.services)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    val iosTargets = listOf(iosX64(), iosArm64(), iosSimulatorArm64())

    iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }



    cocoapods {
        // Podspec -> shared.podspec avto-generate bo‘ladi
        version = "1.0.0"
        summary = "KMP + Compose + Google Maps namunasi"
        homepage = "https://example.com"
        ios.deploymentTarget = "15.0"
        podfile = project.file("../iosApp/Podfile")

        framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        pod("YandexMapsMobile") {
            version = "4.24.0-lite"
        }
    }
    
    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.ktor.client.okhttp)
            implementation("com.google.android.gms:play-services-location:21.2.0")

            //firebase
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.5.1"))
            implementation("com.google.firebase:firebase-messaging-ktx")

            implementation(libs.androidx.appcompat)

            //update
            implementation("com.google.android.play:app-update:2.1.0")
            implementation("com.google.android.play:app-update-ktx:2.1.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

            //map
            implementation("com.yandex.android:maps.mobile:4.24.0-lite")

            //player
            implementation("androidx.media3:media3-exoplayer:1.10.0")
            implementation("androidx.media3:media3-ui:1.10.0")
            implementation("androidx.media3:media3-exoplayer-hls:1.10.0")

            //telegram login
            implementation(libs.telegram.login.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.preview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)
            implementation(libs.voyager.screenmodel)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            api(libs.koin.annotations)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.websocket)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.remember.settings)



            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            implementation("network.chaintech:qr-kit:3.1.2")



            //location
            api("dev.icerock.moko:permissions:0.20.1")
            api("dev.icerock.moko:permissions-compose:0.20.1")
            api("dev.icerock.moko:permissions-location:0.20.1")

            // Lokatsiya trakeri
            api("dev.icerock.moko:geo:0.8.0")
            // Compose yordamchi adapterlari
            api("dev.icerock.moko:geo-compose:0.8.0")

            api("dev.icerock.moko:resources:0.25.0")
            api("dev.icerock.moko:resources-compose:0.25.0")


            implementation("com.russhwolf:multiplatform-settings:1.3.0")
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")

            // Coil
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")

            implementation("io.github.aditya-gupta99:inAppPurchase-kmp:1.0.12")





        }

        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }

    }
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}

ksp {
    arg("KOIN_USE_COMPOSE_VIEWMODEL","true")
    arg("KOIN_CONFIG_CHECK","true")
}
dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
}

project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if(name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

android {
    namespace = "uz.tikoncha_parent"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "uz.tikoncha.parent"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 23
        versionName = "1.2.3"
    }
    setProperty("archivesBaseName", "Tikoncha_Parent_v${defaultConfig.versionName}")


    val props = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) load(f.inputStream())
    }



    signingConfigs {
        create("release") {
            storeFile = rootProject.file(props.getProperty("RELEASE_STORE_FILE"))
            storePassword = props.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = props.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = props.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")   // debug ham release kalit bilan
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }


    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
//            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    debugImplementation(compose.uiTooling)
}

