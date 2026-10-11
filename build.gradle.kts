plugins {
    java
    id("io.qameta.allure") version "2.12.0"
}

group = "ru.test.weather"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

repositories {
    mavenCentral()
}

val cucumberVersion = "7.20.1"
val allureVersion = "2.29.0"
val junitPlatformVersion = "1.11.3"
val wiremockVersion = "3.9.2"
val restAssuredVersion = "5.5.0"

dependencies {
    // BDD
    testImplementation(platform("io.cucumber:cucumber-bom:$cucumberVersion"))
    testImplementation("io.cucumber:cucumber-java")
    testImplementation("io.cucumber:cucumber-junit-platform-engine")
    testImplementation("org.junit.platform:junit-platform-suite:$junitPlatformVersion")

    // Заглушки
    testImplementation("org.wiremock:wiremock-standalone:$wiremockVersion")

    // HTTP-клиент и парсинг
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("io.rest-assured:json-schema-validator:$restAssuredVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")

    // Проверки
    testImplementation("org.assertj:assertj-core:3.26.3")

    // Отчётность
    testImplementation(platform("io.qameta.allure:allure-bom:$allureVersion"))
    testImplementation("io.qameta.allure:allure-cucumber7-jvm")
    testImplementation("io.qameta.allure:allure-rest-assured")

    // Логирование
    testImplementation("org.slf4j:slf4j-api:2.0.16")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.12")
}

allure {
    adapter {
        allureJavaVersion.set(allureVersion)
        // @Step-аннотации не используем, шаги создаются через Allure.step(...)
        aspectjWeaver.set(false)
    }
}

// Обход бага allure-gradle 2.12.0: у задачи copyCategories выход markerFile объявлен как @OutputFile,
// а создаётся каталогом (mkdirs). При повторном запуске Gradle падает с ошибкой
// "property 'markerFile' is not writable because ... is not a file". Исправлено только в плагине 4.x
// (там уже Allure 3 и Java 17+), поэтому просто удаляем этот каталог перед задачей.
val cleanCopyCategoriesMarker by tasks.registering(Delete::class) {
    delete(layout.buildDirectory.dir("copy-categories"))
}
tasks.named("copyCategories") {
    dependsOn(cleanCopyCategoriesMarker)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    systemProperty("file.encoding", "UTF-8")

    // Версии для environment.properties в отчёте Allure (пишется в Hooks)
    systemProperty("versions.cucumber", cucumberVersion)
    systemProperty("versions.wiremock", wiremockVersion)
    systemProperty("versions.restassured", restAssuredVersion)
    systemProperty("versions.allure", allureVersion)

    // Пробрасываем параметры Cucumber из командной строки:
    // ./gradlew test -Dcucumber.filter.tags="@negative"
    System.getProperties()
        .filterKeys { it.toString().startsWith("cucumber.") }
        .forEach { (key, value) -> systemProperty(key.toString(), value.toString()) }

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
