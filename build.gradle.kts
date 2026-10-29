import org.gradle.kotlin.dsl.dependencies

plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.tahacheji"
version = "1.3.2"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    intellijPlatform {
        intellijIdea("2026.2.0.1")
    }
}

// IntelliJ Platform 2026.2 runs on Java 25.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "UMD CS Submit Client"
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "262"
            untilBuild = "262.*"
        }
    }

    // JetBrains requires Plugin Verifier checks for Marketplace uploads.
    pluginVerification {
        ides {
            recommended()
        }
    }

    // These values are read only from environment variables. Never commit secrets.
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}
