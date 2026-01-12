plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "3.2.0"
}

group = "io.github.kullik01"
version = "1.0.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"
val gsonVersion = "2.11.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("io.github.kullik01.focusbean")
    mainClass.set("io.github.kullik01.focusbean.Launcher")
    applicationDefaultJvmArgs = listOf(
        "-Dapp.name=Focus Bean"
    )
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.media")
}

dependencies {
    implementation("com.google.code.gson:gson:${gsonVersion}")
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Add JVM args to allow reflective access for testing and Gson serialization
    jvmArgs(
        "--add-reads", "io.github.kullik01.focusbean=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens", "io.github.kullik01.focusbean/io.github.kullik01.focusbean.model=ALL-UNNAMED",
        "--add-opens", "io.github.kullik01.focusbean/io.github.kullik01.focusbean.controller=ALL-UNNAMED",
        "--add-opens", "io.github.kullik01.focusbean/io.github.kullik01.focusbean.service=ALL-UNNAMED"
    )
}

jlink {
    imageZip.set(layout.buildDirectory.file("distributions/FocusBean-${version}.zip"))
    options.set(listOf(
        "--strip-debug",
        "--compress", "zip-6",
        "--no-header-files",
        "--no-man-pages"
    ))
    launcher {
        name = "FocusBean"
        noConsole = true
    }
    
    // Add jpackage configuration for native Windows installer
    jpackage {
        // Use JAVA_HOME for jpackage tool location
        jpackageHome = System.getenv("JAVA_HOME") ?: ""
        
        installerType = "exe"
        installerName = "FocusBean-Setup"
        appVersion = "1.0.0"
        // Application metadata for the exe
        imageOptions = listOf(
            "--icon", "src/main/resources/io/github/kullik01/focusbean/view/logo.ico",
            "--description", "Focus Bean application",
            "--vendor", "Hannah Kullik",
        )
        
        installerOptions = listOf(
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut"
        )
    }
}
