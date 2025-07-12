plugins {
    application
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.springframework.boot") version "3.1.3"  // Use the latest stable version
    id("io.spring.dependency-management") version "1.1.3"  // For managing dependency versions
}

group = "com.gjvandersloot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.springframework.boot:spring-boot-starter")          // Core Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.azure:azure-identity:1.16.2")
    implementation("com.azure:azure-security-keyvault-secrets:4.10.0")
//    implementation("")
    implementation("com.azure.resourcemanager:azure-resourcemanager:2.52.0")
    implementation("com.azure.resourcemanager:azure-resourcemanager-resources:2.32.0")
    implementation("com.azure.resourcemanager:azure-resourcemanager-keyvault:2.32.0")

}

javafx {
    modules("javafx.controls", "javafx.fxml")
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
        "--add-modules", "javafx.controls,javafx.fxml"
    )
}
