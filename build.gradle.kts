plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.arona.meow"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    
    shadowJar {
        archiveClassifier.set("")
        minimize()
    }
    
    build {
        dependsOn(shadowJar)
    }
}
