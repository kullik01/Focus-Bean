plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "io.github.kullik01"
version = "1.0.0

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
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Add JVM args to allow reflective access for testing and Gson serialization
    jvmArgs(
        "--add-reads", "io.github.kullik01.focusbean=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens", "io.github.kullik01.focusbean/io.github.kullik01.focusbean.model=ALL-UNNAMED"
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
        installerType = "exe"
        installerName = "FocusBean-Setup"
        appVersion = "1.0.0"
        
        installerOptions.addAll(listOf(
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut"
        ))
    }
}
