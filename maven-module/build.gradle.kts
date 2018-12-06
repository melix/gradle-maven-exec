import org.gradle.maven.MavenExec

plugins {
   `maven-embedder`
}

maven {
   version = "3.5.4"
}

tasks.register<MavenExec>("clean") {
   taskName = "clean"
}

tasks.register<MavenExec>("install") {
   taskName = "install"
}