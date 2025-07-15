import java.util.Locale

plugins {
    application
    id("java")
    id("org.springframework.boot") version "3.1.3"  // Use the latest stable version
    id("io.spring.dependency-management") version "1.1.3"  // For managing dependency versions
}

group = "com.gjvandersloot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val javafxVersion = "21.0.3"

val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
val platform = when {
    osName.contains("win") -> "win"
    osName.contains("mac") -> "mac"
    osName.contains("linux") -> "linux"
    else -> throw GradleException("Unknown OS: $osName")
}

dependencies {
    //    implementation("")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter")          // Core Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Azure SDK
    implementation("com.azure:azure-identity:1.16.2")
    implementation("com.azure:azure-security-keyvault-secrets:4.10.0")
    implementation("com.azure.resourcemanager:azure-resourcemanager:2.52.0")
    implementation("com.azure.resourcemanager:azure-resourcemanager-resources:2.32.0")
    implementation("com.azure.resourcemanager:azure-resourcemanager-keyvault:2.32.0")
    implementation("com.microsoft.azure:msal4j:1.21.0")

    // JavaFX
    implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-fxml:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
}

application {
    mainClass.set("com.gjvandersloot.Main")
}

tasks.test {
    useJUnitPlatform()
}

java {
    modularity.inferModulePath.set(true)
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--module-path", configurations.runtimeClasspath.get().asPath,
        "--add-modules", "javafx.controls,javafx.fxml",
        "-Djava.awt.headless=false"
    )
}
