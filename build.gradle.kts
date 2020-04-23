plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.3.71")
    application
}

group
version

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testCompile("org.jetbrains.kotlin:kotlin-test")

    testCompile("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "MainKt"
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }

    from(
        configurations.compile.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}

val run by tasks.getting(JavaExec::class) {
    if (project.hasProperty("args")) {
        args = (project.property("args") as String).split("\\s+")
    }
}