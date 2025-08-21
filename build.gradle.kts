import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.git.versioning)
}

val productName: String = "polybool-kotlin"

description = productName
group = "de.stefan-oltmann"
version = "0.0.0"

gitVersioning.apply {

    refs {
        /* The main branch contains the current dev version */
        branch("main") {
            version = "\${commit.short}"
        }
        /* Releases have real version numbers */
        tag("v(?<version>.*)") {
            version = "\${ref.version}"
        }
    }

    /* Fallback if the branch was not found (for feature branches) */
    rev {
        version = "\${commit.short}"
    }
}

repositories {
    mavenCentral()
    google()
}

kotlin {

    explicitApi()

    jvmToolchain(17)

    jvm {

        java {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    mingwX64()
    linuxX64()
    linuxArm64()

    androidTarget {

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }

        publishLibraryVariants("release")
    }

    val xcf = XCFramework()

    listOf(
        /* App Store */
        iosArm64(),
        /* Apple Silicon iOS Simulator */
        iosSimulatorArm64()
    ).forEach {

        it.binaries {

            framework(
                buildTypes = setOf(NativeBuildType.RELEASE)
            ) {
                baseName = "polybool-kotlin"
                /* Part of the XCFramework */
                xcf.add(this)
            }
        }
    }

    js()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs()

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi()

    sourceSets {

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

// region Writing version.txt for GitHub Actions
val writeVersion: TaskProvider<Task> = tasks.register("writeVersion") {
    doLast {
        File("build/version.txt").writeText(project.version.toString())
    }
}

tasks.getByPath("build").finalizedBy(writeVersion)
// endregion

// region Android setup
android {

    namespace = "de.stefan_oltmann.polybool"

    compileSdk = libs.versions.android.compile.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}
// endregion

// region Maven publish

val signingEnabled: Boolean = System.getenv("SIGNING_ENABLED")?.toBoolean() ?: false

mavenPublishing {

    publishToMavenCentral()

    if (signingEnabled)
        signAllPublications()

    coordinates(
        groupId = "de.stefan-oltmann",
        artifactId = "polybool-kotlin",
        version = version.toString()
    )

    pom {

        name = productName
        description = "Kotlin Multiplatform port of polybool-java"
        url = "https://github.com/StefanOltmann/polybool-kotlin"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit"
            }
        }

        developers {
            developer {
                name = "Stefan Oltmann"
                url = "https://stefan-oltmann.de/"
                roles = listOf("maintainer", "developer")
                properties = mapOf("github" to "StefanOltmann")
            }
        }

        scm {
            url = "https://github.com/StefanOltmann/polybool-kotlin"
            connection = "scm:git:git://github.com/StefanOltmann/polybool-kotlin.git"
        }
    }
}
// endregion

