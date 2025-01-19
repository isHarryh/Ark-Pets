sourceSets {
    main {
        java {
            srcDirs("src/")
        }
    }
}

detekt {
    source.setFrom("src/")
}

dependencies {
    val gdxVersion: String by rootProject
    val jnaVersion: String by rootProject
    val javaFXVersion: String by rootProject
    val dbusVersion: String by rootProject
    val javaFXPlatforms: String by rootProject
    val lwjglList: String by rootProject
    val lwjglVersion: String by rootProject

    // Spine Runtime
    api("com.esotericsoftware.spine:spine-libgdx:3.8.99.1")
    // libGDX
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    // JNA
    api("net.java.dev.jna:jna:$jnaVersion")
    api("net.java.dev.jna:jna-platform:$jnaVersion")
    // JavaFX
    javaFXPlatforms.split(',').forEach {
        api("org.openjfx:javafx-base:$javaFXVersion:$it")
        api("org.openjfx:javafx-controls:$javaFXVersion:$it")
        api("org.openjfx:javafx-graphics:$javaFXVersion:$it")
        api("org.openjfx:javafx-fxml:$javaFXVersion:$it")
    }
    // JFoenix
    api("com.jfoenix:jfoenix:9.0.1")
    // FastJson
    api("com.alibaba:fastjson:2.0.39")
    // Log4j
    api("apache-log4j:log4j:1.2.15")
    // dbus-java
    api("com.github.hypfvieh:dbus-java-core:$dbusVersion")
    api("com.github.hypfvieh:dbus-java-transport-native-unixsocket:$dbusVersion")
    // LWJGL
    lwjglList.split(',').forEach {
        api("org.lwjgl:$it") {
            version {
                strictly(lwjglVersion)
            }
        }
    }
}
