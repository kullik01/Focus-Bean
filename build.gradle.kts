plugins {
    java
    application
    id("org.beryx.jlink") version "3.2.0"
}

group = "io.github.kullik01"
version = "1.2.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"
val gsonVersion = "2.11.0"
val javafxVersion = "25"

// Detect current OS for platform-specific JavaFX dependencies
val osName = System.getProperty("os.name").lowercase()
val platform = when {
    osName.contains("win") -> "win"
    osName.contains("mac") -> "mac"
    osName.contains("linux") -> "linux"
    else -> "linux"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    modularity.inferModulePath.set(true)
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

dependencies {
    // JavaFX dependencies (platform-specific)
    implementation("org.openjfx:javafx-controls:${javafxVersion}:${platform}")
    implementation("org.openjfx:javafx-graphics:${javafxVersion}:${platform}")
    implementation("org.openjfx:javafx-media:${javafxVersion}:${platform}")
    implementation("org.openjfx:javafx-base:${javafxVersion}:${platform}")
    
    implementation("com.google.code.gson:gson:${gsonVersion}")
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
    imageDir.set(layout.buildDirectory.dir("FocusBean-${version}"))
    val zipOsSuffix = if (osName.contains("win")) "Windows" else "Linux"
    imageZip.set(layout.buildDirectory.file("distributions/FocusBean-${version}-${zipOsSuffix}.zip"))
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
    
    // Add jpackage configuration for native installers (Windows and Linux)
    jpackage {
        // Use JAVA_HOME for jpackage tool location
        jpackageHome = System.getenv("JAVA_HOME") ?: ""
        
        // Detect OS and set installer type accordingly
        val isWindows = osName.contains("win")
        
        // Allow overriding installer type via property (e.g. -PinstallerType=app-image)
        val typeProp = project.findProperty("installerType") as? String
        installerType = typeProp ?: if (isWindows) "exe" else "rpm"
        
        installerName = "FocusBean"
        appVersion = "1.2.0"
        
        // Application metadata with platform-appropriate icon
        imageOptions = listOf(
            "--icon", if (isWindows) 
                "src/main/resources/io/github/kullik01/focusbean/view/logo.ico"
            else 
                "src/main/resources/io/github/kullik01/focusbean/view/logo_linux.png",
            "--description", "Focus Bean - A modern timer application for deep work and productivity",
            "--vendor", "Hannah Kullik",
        )
        
        // Platform-specific installer options
        installerOptions = if (isWindows) {
            listOf(
                "--win-dir-chooser",
                "--win-menu",
                "--win-shortcut"
            )
        } else {
            val baseOptions = mutableListOf(
                "--linux-menu-group", "Utility",
                "--linux-shortcut"
            )
            // Allow overriding install dir via property (e.g. -PinstallDir=/home/user/.focusbean)
            val installDir = project.findProperty("installDir") as? String
            if (installDir != null) {
                baseOptions.add("--install-dir")
                baseOptions.add(installDir)
            }
            baseOptions
        }
    }
}

// Copy the install script and icon to the image directory after jlink task
tasks.named("jlink") {
    doLast {
        val imageDirPath = layout.buildDirectory.dir("FocusBean-${version}").get().asFile
        if (!osName.contains("win")) {
            copy {
                from("src/main/scripts/linux/install_shortcut.sh")
                into(imageDirPath)
            }
            // Set permissions using Ant since fileMode in copy spec can be finicky in this context
            ant.withGroovyBuilder {
                "chmod"("file" to File(imageDirPath, "install_shortcut.sh"), "perm" to "755")
            }
            
            copy {
                from("src/main/resources/io/github/kullik01/focusbean/view/logo_linux.png")
                into(imageDirPath)
                rename { "icon.png" }
            }
        }
    }
}

