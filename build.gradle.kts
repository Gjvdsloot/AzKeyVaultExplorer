import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("org.springframework.boot") version "3.1.3"  // Use the latest stable version
    id("io.spring.dependency-management") version "1.1.3"  // For managing dependency versions
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

application {
    mainClass = "com.gjvandersloot.Main"
    applicationName = "SimpleKeyVault"
}

version = "1.0.0"


repositories {
    mavenCentral()
}

javafx {
    version = "21.0.3"
    modules = listOf("javafx.controls", "javafx.fxml")
}
tasks {
    // disable the plain jar task
    named<Jar>("jar") {
        enabled = true
    }

//    named<ShadowJar>("shadowJar") {
//        exclude("module-info.class")
//    }

    // configure the ShadowJar to include everything on your runtimeClasspath
    named<ShadowJar>("shadowJar") {
        // tells Shadow to merge in *all* runtime dependencies (including JavaFX)
//        configurations = listOf(project.configurations.getByName("runtimeClasspath"))

        // merge META-INF/services, spring.factories, etc.
        mergeServiceFiles()

        // strip out broken signature files
//        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

        // drop duplicates rather than failing
//        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        // point at your launcher
//        manifest {
//            attributes("Main-Class" to "com.gjvandersloot.Main")
//        }
    }

    register<Copy>("copyRuntime") {
        print(layout.buildDirectory.dir("runtime"))
        from(file(System.getenv("JAVA_HOME")))  // or hard‑code a path to your JRE
        into(layout.buildDirectory.dir("runtime"))
    }

    register<Exec>("packageExe") {
        dependsOn("shadowJar", "copyRuntime")

        // the fat‑jar we just built:
        val jar = layout.buildDirectory.file("libs/${project.name}-all.jar").get().asFile
//        val jar = layout.buildDirectory.file("libs/${project.name}-${project.version}-all.jar").get().asFile
        // the copied JRE:
        val runtimeImage = layout.buildDirectory.dir("runtime").get().asFile

        commandLine = listOf(
            "jpackage",
            "--type",           "exe",                         // produce a Windows .exe installer
            "--name",           project.name,                  // e.g. "SimpleKeyVault"
            "--app-version",    project.version.toString(),
            "--input",          jar.parent,                    // folder containing the jar
            "--main-jar",       jar.name,                      // the shadow jar file name
            "--main-class",     "com.gjvandersloot.Main",
//            "--icon",           "src/main/resources/hellofx.ico",
            "--vendor",         "Vanderlotos",
            "--resource-dir",   "src/main/resources",
            "--runtime-image",  runtimeImage.absolutePath      // point at your full JRE
        )
    }
}



dependencies {
//    implementation("")
    implementation("com.fasterxml.jackson.core:jackson-core:2.19.1")

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

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.test {
    useJUnitPlatform()
}