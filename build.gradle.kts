import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.squareup.sqldelight")
}

group = "jatx"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        val commonMain by getting {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            dependencies {
                implementation(compose.components.resources)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.compose.material3:material3-desktop:1.5.12")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.1")
                implementation("uk.co.caprica:vlcj:4.8.2")
                implementation("uk.co.caprica:vlcj-info:2.0.3")
                implementation("com.squareup.sqldelight:sqlite-driver:1.5.5")
                implementation("com.google.api-client:google-api-client:2.7.2")
                implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
                implementation("com.google.apis:google-api-services-youtube:v3-rev20250224-2.0.0")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "jatx.video.manager.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "VideoManager"
            packageVersion = "1.0.0"
        }
    }
}

sqldelight {
    database("AppDatabase") {
        packageName = "jatx.video.manager.db"
        dialect = "sqlite:3.18"
        schemaOutputDirectory = file("schema")
        migrationOutputDirectory = file("migrations")
        deriveSchemaFromMigrations = true
        verifyMigrations = true
    }
}

