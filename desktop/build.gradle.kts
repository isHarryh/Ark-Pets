sourceSets {
    main {
        java {
            srcDirs("src/")
        }
        resources {
            srcDirs("../assets")
        }
    }
}

detekt {
    source.setFrom("src/")
}

dependencies {
    val gdxVersion: String by rootProject

    implementation(project(":core"))
    // libGDX Desktop
    api("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    api("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    // sqlite jdbc driver
    implementation("org.xerial:sqlite-jdbc:3.47.2.0")
    // kotlin orm framework
    implementation("org.ktorm:ktorm-core:4.1.1")
    // kotlin orm sqlite extension
    implementation("org.ktorm:ktorm-support-sqlite:4.1.1")
}

tasks {
    val mainClassName = "cn.harryh.arkpets.DesktopLauncher"
    val assetsDir = File("../assets")

    processResources {
        includeEmptyDirs = false
        exclude(
            "**/models_enemies/**",
            "**/models/**",
            "**/logs/**",
            "**/data/**",
            "models_data.json"
        )
    }

    // Runs the app without debug.
    register<JavaExec>("run") {
        group = "execute"
        dependsOn(classes)

        mainClass.set(mainClassName)
        classpath = sourceSets.main.get().runtimeClasspath
        standardInput = System.`in`
        workingDir = assetsDir
        isIgnoreExitValue = true

        if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
            jvmArguments.add("-XstartOnFirstThread") // Required to run on macOS
        }
    }

    // Runs the app within debug.
    register<JavaExec>("debug") {
        group = "execute"
        dependsOn(classes)

        mainClass.set(mainClassName)
        classpath = sourceSets.main.get().runtimeClasspath
        standardInput = System.`in`
        workingDir = assetsDir
        isIgnoreExitValue = true
        debug = true
    }

    /* DISTRIBUTION TASKS */
    // RootProject vars
    val appName: String by rootProject
    val appAuthor: String by rootProject
    val appVersion: String by rootProject
    // Environment vars
    val javaHome: String = System.getProperty("java.home")
    val osName = System.getProperty("os.name").lowercase().split(' ')[0]
    // Distribution related vars
    val jarLibDir = "$buildDir/libs"
    val jarLibName = "$name-$version"
    val jlinkRuntimeDir = "$buildDir/jlink"
    val jlinkRuntimeImg = "$jlinkRuntimeDir/runtime"
    val jlinkModuleList =
        "java.base,java.desktop,java.logging,java.management,java.scripting,java.sql,jdk.crypto.ec,jdk.localedata,jdk.unsupported"
    val jlinkLocalesList = "en-US,zh-CN"
    val jpackageDir = "$buildDir/jpackage"
    val issFileRel = "$rootDir/docs/scripts/ExePacking.iss"
    val distDir = "$buildDir/dist"
    val distName = "$appName-v$version"

    // Generates a distributable JAR file for the app.
    register<Jar>("distJar") {
        group = "dist"
        dependsOn(classes)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        dependsOn(configurations.runtimeClasspath)

        from(
            configurations.runtimeClasspath.get().map {
                if (it.isDirectory) it else zipTree(it)
            }
        )

        manifest {
            attributes("Main-Class" to mainClassName)
            attributes(jar.get().manifest.attributes)
        }

        with(jar.get())

        doLast {
            copy {
                from("$jarLibDir/$jarLibName.jar")
                into(distDir)
                rename("$jarLibName.jar", "$distName.jar")
            }
        }
    }

    // Creates a customized Java Runtime Environment for the app.
    register<Exec>("jlink") {
        group = "dist"
        dependsOn("distJar")

        workingDir = projectDir
        inputs.property("runtime", jlinkRuntimeImg)

        commandLine(
            "$javaHome/bin/jlink",
            "--module-path", "$javaHome/jmods",
            "--add-modules", jlinkModuleList,
            "--output", jlinkRuntimeImg,
            "--strip-debug",
            "--no-header-files",
            "--no-man-pages",
            "--vm=server",
            "--compress=1",
            "--include-locales", jlinkLocalesList
        )

        outputs.dir(jlinkRuntimeDir)
    }

    // Packs the app into an EXE.
    register<Exec>("jpackage") {
        group = "dist"
        dependsOn("jlink")

        doFirst {
            if (fileTree(jarLibDir).files.size > 1) {
                throw RuntimeException("There may be legacy jars in the libs dir, please run 'clean' first.")
            }
            delete(jpackageDir)
        }

        doLast {
            println("Copying $rootDir/LICENSE to $jpackageDir")
            copy {
                from("$rootDir/LICENSE")
                into(jpackageDir)
            }
            println("LICENSE file copied successfully")
            delete(jlinkRuntimeDir)
        }

        workingDir = projectDir

        val commands = mutableListOf(
            "$javaHome/bin/jpackage",
            "--input", jarLibDir,
            "--dest", jpackageDir,
            "--type", "app-image",
            "--name", appName,
            "--vendor", appAuthor,
            "--app-version", appVersion,
            "--main-class", mainClassName,
            "--main-jar", jar.get().archiveFile.get().asFile.absolutePath,
            "--runtime-image", jlinkRuntimeImg
        )

        when {
            osName.contains("windows") -> {
                commands.add("--icon")
                commands.add("$assetsDir/icons/icon.ico")
            }

            osName.contains("linux") -> {
                commands.add("--icon")
                commands.add("$assetsDir/icons/icon.png")
            }

            osName.contains("mac") -> {
                commands.add("--java-options")
                commands.add("-XstartOnFirstThread")
            }
        }

        commandLine = commands
    }

    // Generates a distributable ZIP file for the app.
    register<Zip>("distZip") {
        group = "dist"
        dependsOn("jpackage")

        from(jpackageDir) {
            include("**")
        }

        from(rootDir) {
            include("README.md")
        }

        archiveFileName.set("$distName.zip")
        destinationDirectory.set(File(distDir))
    }

    // Generates a distributable EXE file for the app, using Inno Setup.
    // Note that you must install Inno Setup in your environment and add it to PATH before running this task.
    register<Exec>("distExe") {
        group = "dist"
        dependsOn("jpackage")

        environment("APP_VERSION", version)
        environment("APP_COPYRIGHT", ext.get("appCopyright") as String)

        commandLine("iscc", "/Q", issFileRel)
    }

    // Generates ALL kinds of distributing files.
    register("distAll") {
        group = "dist"
        dependsOn("distJar", "distZip", "distExe")

        doLast {
            println("All files were successfully generated, see: ${File(distDir).absolutePath}")
            try {
                delete("$buildDir/tmp")
                delete(jpackageDir)
                delete(jarLibDir)
            } catch (ignored: Exception) {
                println("Unable to delete temp files.")
            }
        }
    }
}
