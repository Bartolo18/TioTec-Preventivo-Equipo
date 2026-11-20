import java.util.Properties
import java.io.File
import org.gradle.testing.jacoco.tasks.JacocoReport
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
    id("jacoco")
}

android {
    namespace = "com.example.tiotec"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tiotec.entrega"
        minSdk = 33
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
        buildConfigField("String", "BACKEND_URL", "\"http://127.0.0.1:8080/\"")
    }

    signingConfigs {
        create("academica") {
            val archivo = rootProject.file("firma.properties")
            if (archivo.exists()) {
                val firma = Properties().apply { archivo.inputStream().use { load(it) } }
                storeFile = rootProject.file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("academica")
        }
    }

    testOptions { unitTests.isReturnDefaultValues = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.compose.animation:animation")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}


dependencies {
    val local = rootProject.file("tools/cobertura/jacoco.jar")
    if (local.exists()) {
        jacocoAgent(files(local))
        jacocoAnt(files(local))
    }
}
jacoco { toolVersion = "0.8.15" }
tasks.register<JacocoReport>("coberturaLogica") {
    dependsOn("testDebugUnitTest")
    executionData(fileTree(layout.buildDirectory) { include("**/*.exec") })
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        include("com/example/tiotec/model/**", "com/example/tiotec/validation/**",
            "com/example/tiotec/repository/**", "com/example/tiotec/viewmodel/**")
    })
    reports { html.required.set(true); xml.required.set(true) }
}

tasks.register("exportarClasspathPruebas") {
    dependsOn("compileDebugUnitTestKotlin", "compileDebugJavaWithJavac")
    doLast {
        val pruebas = tasks.getByName("testDebugUnitTest") as Test
        layout.buildDirectory.file("classpath-pruebas.txt").get().asFile.writeText(
            pruebas.testClassesDirs.asPath + File.pathSeparator + pruebas.classpath.asPath)
    }
}
