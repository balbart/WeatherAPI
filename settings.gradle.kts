plugins {
    // Позволяет Gradle автоматически скачать JDK 19, если он не установлен локально
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "weather-api"
