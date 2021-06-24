plugins {
    `kotlin-dsl`

    id("io.gitlab.arturbosch.detekt") version "1.17.1"
}

repositories {
    mavenCentral()
    /*maven {
        url = uri("https://plugins.gradle.org/m2/")
    }*/
}

dependencies {
    //implementation("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.0")
    implementation("de.undercouch:gradle-download-task:4.1.1")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.1")
}

detekt {
    config = files("$rootDir/detekt-config.yml")
    buildUponDefaultConfig = true

    reports {
        html.enabled = false
        xml.enabled = false
        txt.enabled = false
    }
}

gradlePlugin {
    plugins {
        create("android-studio-deps-plugin") {
            id = "io.github.petretiandrea.android-studio-deps"
            implementationClass = "io.github.petretiandrea.AndroidStudioSourcePlugin"
        }
    }
}